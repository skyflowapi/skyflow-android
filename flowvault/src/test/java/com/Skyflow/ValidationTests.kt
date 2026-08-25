package com.Skyflow

import Skyflow.*
import Skyflow.collect.client.FlowDBCollectRequestBody
import Skyflow.collect.client.FlowDBCollectAPICallback
import Skyflow.collect.client.FlowDBMixedAPICallback
import Skyflow.core.FlowDBAPIClient
import Skyflow.utils.Utils
import Skyflow.collect.elements.validations.ElementValueMatchRule
import android.app.Activity
import com.Skyflow.collect.elements.validations.*
import io.mockk.every
import io.mockk.mockk
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.Shadows.shadowOf
import android.os.Looper

@RunWith(RobolectricTestRunner::class)
class ValidationTests{

    lateinit var skyflow : Client
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity
    @Before
    fun setup() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        skyflow = Client(configuration)
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()
    }
    @Test
    fun testValidateFunction(){
        val skyflowValidationSet = ValidationSet()
        val regexMatch = RegexMatchRule("[0-9][A-Za-z]", "Regex validation Failed")
        skyflowValidationSet.add(regexMatch)
        val sampleSuccessInput = "1e"
        val sampleErrorInput = "e1"
        val successResponse = SkyflowValidator.validate(sampleSuccessInput, skyflowValidationSet)
        val error = SkyflowValidator.validate(sampleErrorInput, skyflowValidationSet)
        assertEquals(successResponse, "")
        assertEquals("Regex validation Failed", error)
    }

    // H2 regression: reveal() must fail fast on missing vault config — the failure is determined
    // synchronously by checkVaultDetails, BEFORE any bearer-token / network call. Callbacks are now
    // delivered on the main thread (issue #5), so idle the main looper to run the posted onFailure;
    // the point is that NO network round-trip was needed to produce it (idling the looper does not
    // run OkHttp I/O). Without the H2 fix, reveal would go async at the network layer and capturedError
    // would remain null after idling.
    @Test
    fun testRevealFailsFastOnMissingVaultUrl() {
        val client = Client(Configuration("vault123", "", AccessTokenProvider()))
        val container = client.container(ContainerType.REVEAL)
        var capturedError: SkyflowError? = null
        container.reveal(object : RevealCallback {
            override fun onSuccess(response: RevealResponse) {}
            override fun onFailure(error: SkyflowError) { capturedError = error }
        })
        shadowOf(Looper.getMainLooper()).idle()   // run the main-thread-posted callback (issue #5)
        // The message survives the error round-trip (constructErrorResponse -> SkyflowError.fromJson),
        // carrying the real EMPTY_VAULT_URL text rather than a generic "Unknown error".
        assertNotNull("reveal should fail fast (no network) when vaultURL is empty", capturedError)
        assertTrue("SkyflowError should carry the real vaultURL message", capturedError!!.message?.contains("vaultURL") == true)
    }

    // M2 regression: an additionalFields column colliding with an already-present column (element or
    // another additionalFields record) must throw DUPLICATE_COLUMN_FOUND, not silently drop the value.
    @Test
    fun testAdditionalFieldsDuplicateColumnThrows() {
        val options = CollectOptions(additionalFields = AdditionalFields(listOf(
            AdditionalFieldsRecord("cards", mapOf("cvv" to "111")),
            AdditionalFieldsRecord("cards", mapOf("cvv" to "222"))   // same table + column -> collision
        )))
        try {
            FlowDBCollectRequestBody.buildRequestBody("vault123", mutableListOf(), options, LogLevel.ERROR)
            fail("expected DUPLICATE_COLUMN_FOUND for the colliding 'cvv' column")
        } catch (e: SkyflowInternalError) {
            assertEquals(SkyflowErrorCode.DUPLICATE_COLUMN_FOUND, e.skyflowErrorCode)
        }
    }

    // M3 regression: additionalFields must be validated (empty records / table / fields / column)
    // with the same v1 error codes, instead of being sent unvalidated.
    @Test
    fun testAdditionalFieldsValidation() {
        fun expect(af: AdditionalFields, code: SkyflowErrorCode) {
            try {
                FlowDBCollectRequestBody.validateAdditionalFields(af, LogLevel.ERROR)
                fail("expected $code")
            } catch (e: SkyflowInternalError) {
                assertEquals(code, e.skyflowErrorCode)
            }
        }
        expect(AdditionalFields(emptyList()), SkyflowErrorCode.ADDITIONAL_FIELDS_EMPTY_RECORDS)
        expect(AdditionalFields(listOf(AdditionalFieldsRecord("", mapOf("cvv" to "1")))), SkyflowErrorCode.ADDITIONAL_FIELDS_EMPTY_TABLE_KEY)
        expect(AdditionalFields(listOf(AdditionalFieldsRecord("cards", emptyMap()))), SkyflowErrorCode.ADDITIONAL_FIELDS_EMPTY_FIELDS)
        expect(AdditionalFields(listOf(AdditionalFieldsRecord("cards", mapOf("" to "1")))), SkyflowErrorCode.EMPTY_COLUMN_NAME)
    }

    // M4 regression: if the app's onSuccess handler throws, the SDK must NOT also fire onFailure
    // (exactly one callback). onSuccess is now delivered outside the try that maps to onFailure.
    @Test
    fun testAppOnSuccessThrowingDoesNotTriggerOnFailure() {
        val apiClient = mockk<FlowDBAPIClient>(relaxed = true)
        val requestBody = JSONObject("""{"records":[{"tableName":"cards"}]}""")
        var onFailureCalled = false
        val appCallback = object : Callback {
            override fun onSuccess(responseBody: Any) { throw RuntimeException("app handler bug") }
            override fun onFailure(exception: Any) { onFailureCalled = true }
        }
        val apiCallback = FlowDBCollectAPICallback(apiClient, requestBody, appCallback, CollectOptions(), LogLevel.ERROR)

        val bodyStr = """{"records":[{"tableName":"cards","skyflowID":"id1","tokens":{},"httpCode":200}]}"""
        val response = mockk<Response>(relaxed = true)
        every { response.isSuccessful } returns true
        every { response.body } returns bodyStr.toResponseBody("application/json".toMediaTypeOrNull())

        val verify = FlowDBCollectAPICallback::class.java.getDeclaredMethod("verifyResponse", Response::class.java)
        verify.isAccessible = true
        try { verify.invoke(apiCallback, response) } catch (e: Exception) { /* app's onSuccess exception propagates — expected */ }

        assertFalse("onFailure must not fire when the app's onSuccess throws", onFailureCalled)
    }

    @Test
    fun testLengthMatch()
    {
        val match = LengthMatchRule(2,10,"failed")
        val successInput = 209
        val failedInput = 2
        val skyflowValidationSet = ValidationSet()
        skyflowValidationSet.add(match)
        assertEquals("",SkyflowValidator.validate(successInput.toString(),skyflowValidationSet))
        assertEquals("failed",SkyflowValidator.validate(failedInput.toString(),skyflowValidationSet))
        assertEquals("",SkyflowValidator.validate("",skyflowValidationSet)) //empty string
    }

    @Test
    fun testLengthMatchForArray()
    {
        val match = SkyflowValidateLengthMatch(intArrayOf(2,3,4),"failed")
        val successInput = 209
        val failedInput = 2
        val skyflowValidationSet = ValidationSet()
        skyflowValidationSet.add(match)
        assertEquals("failed",SkyflowValidator.validate(failedInput.toString(),skyflowValidationSet))
        assertEquals("",SkyflowValidator.validate("",skyflowValidationSet)) //empty string

    }

    @Test
    fun testValidateCardNumber()
    {
        val match = SkyflowValidateCardNumber("failed")
        val successInput = "4111111111111111"
        val failedInput = "1111"
        val failedInputWithAlphabets = "xyz"
        val skyflowValidationSet = ValidationSet()
        skyflowValidationSet.add(match)
        assertEquals("failed",SkyflowValidator.validate(failedInput,skyflowValidationSet))
        assertEquals("failed",SkyflowValidator.validate(failedInputWithAlphabets,skyflowValidationSet))
        assertEquals("",SkyflowValidator.validate("",skyflowValidationSet)) // empty string
    }

    @Test
    fun testValidateExpiryDate()
    {
        val match = SkyflowValidateExpireDate("MM/YY","failed")
        val failedInput = "1111" //not in expire date format
        val failedInputWithAlphabets = "xy/xz" //no digits
        val failedInput2 = "11/20"
        val skyflowValidationSet = ValidationSet()
        skyflowValidationSet.add(match)
        assertEquals("failed",SkyflowValidator.validate(failedInput,skyflowValidationSet))
        assertEquals("failed",SkyflowValidator.validate(failedInputWithAlphabets,skyflowValidationSet))
    }

    @Test
    fun testElementMatchRule() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards", "PIN",
            SkyflowElementType.PIN, placeholder = "enter pin"
        )
        val pin = container.create(activity, collectInput) as? TextField
        pin!!.inputField.setText("4111111")
        pin.actualValue = "4111111"
        val validationSet = ValidationSet()
        validationSet.add(ElementValueMatchRule(pin, "not matched"))
        val collectInput1 = CollectElementInput("cards", "PIN",
            SkyflowElementType.PIN, placeholder = "confirm pin", validations = validationSet
        )
        val confirmPin = container.create(activity, collectInput1) as? TextField
        confirmPin!!.inputField.setText("11111")
        assertEquals("not matched", confirmPin.validate())
    }

    // Issue #1 regression: a CONSUMER-authored ValidationRule — implementing ONLY the public
    // ValidationRule interface (all an app in another module can implement) — must validate normally.
    // Before the fix, SkyflowValidator force-cast every rule to the internal
    // SkyflowInternalValidationProtocol, so a consumer rule threw ClassCastException inside
    // SkyflowValidator.validate — reached synchronously from container.create() (StateforText) / the
    // first keystroke, on the UI thread. (This test could not even have compiled before the fix,
    // since ValidationRule had no validate() to override.)
    @Test
    fun testConsumerAuthoredValidationRule() {
        val onlyAcme = object : ValidationRule {
            override var error: SkyflowValidationError = "must be ACME"
            override fun validate(text: String?): Boolean = text.isNullOrEmpty() || text == "ACME"
        }

        // 1) Direct validator path — the exact call site that used to throw ClassCastException.
        val set = ValidationSet()
        set.add(onlyAcme)
        assertEquals("", SkyflowValidator.validate("ACME", set))
        assertEquals("must be ACME", SkyflowValidator.validate("ZEBRA", set))

        // 2) Real-world entry point: container.create() runs validation via StateforText and must NOT
        // crash. INPUT_FIELD carries no built-in rules, so only the consumer rule applies.
        val container = skyflow.container(ContainerType.COLLECT)
        val field = container.create(activity, CollectElementInput("cards", "name",
            SkyflowElementType.INPUT_FIELD, placeholder = "name", validations = set)) as? TextField
        assertNotNull("container.create() must not crash for a consumer-authored rule", field)
        field!!.inputField.setText("ZEBRA"); field.actualValue = "ZEBRA"
        assertEquals("must be ACME", field.validate())
        field.inputField.setText("ACME"); field.actualValue = "ACME"
        assertEquals("", field.validate())
    }

    // Issue #2 regression: a mixed insert+update collect() fans out into two HTTP calls; the SDK must
    // deliver EXACTLY ONE terminal callback and never drop a committed half. These drive the
    // reconciliation (subCallbackFor) directly with canned sub-call results — no network needed.
    @Test
    fun testMixedCallbackPartialFailureConsolidatesIntoOneOnSuccess() {
        val apiClient = mockk<FlowDBAPIClient>(relaxed = true)
        val updateBody = JSONObject("""{"records":[{"skyflowID":"id1","tableName":"cards"}]}""")
        val insertBody = JSONObject("""{"records":[{"tableName":"cards"}]}""")
        var successResponse: Any? = null
        var failureCount = 0
        val finalCallback = object : Callback {
            override fun onSuccess(responseBody: Any) { successResponse = responseBody }
            override fun onFailure(exception: Any) { failureCount++ }
        }
        val mixed = FlowDBMixedAPICallback(apiClient, updateBody, insertBody, finalCallback, CollectOptions(), LogLevel.ERROR)

        // update commits (already written server-side); insert fails.
        mixed.subCallbackFor(updateBody).onSuccess(
            """{"records":[{"tableName":"cards","skyflowId":"id1","tokens":{"cvv":[{"token":"tok1"}]},"httpCode":200}]}"""
        )
        mixed.subCallbackFor(insertBody).onFailure(Utils.constructErrorResponse(400, "insert failed"))

        assertEquals("onFailure must NOT fire when one half committed", 0, failureCount)
        assertNotNull("a single consolidated onSuccess must fire", successResponse)
        val resp = CollectResponse.fromJson(successResponse.toString())
        assertEquals("both the committed update and the failed insert must be represented", 2, resp.records.size)
        assertTrue("committed update token must survive",
            resp.records.any { it.tokens?.get("cvv")?.firstOrNull()?.token == "tok1" })
        assertTrue("failed insert must appear as an error record (not be dropped)",
            resp.records.any { it.error == "insert failed" && it.httpCode == 400 })
    }

    @Test
    fun testMixedCallbackBothFailuresFireOnFailureExactlyOnce() {
        val apiClient = mockk<FlowDBAPIClient>(relaxed = true)
        val updateBody = JSONObject("""{"records":[{"skyflowID":"id1","tableName":"cards"}]}""")
        val insertBody = JSONObject("""{"records":[{"tableName":"cards"}]}""")
        var successCount = 0
        var failureCount = 0
        val finalCallback = object : Callback {
            override fun onSuccess(responseBody: Any) { successCount++ }
            override fun onFailure(exception: Any) { failureCount++ }
        }
        val mixed = FlowDBMixedAPICallback(apiClient, updateBody, insertBody, finalCallback, CollectOptions(), LogLevel.ERROR)

        mixed.subCallbackFor(updateBody).onFailure(Utils.constructErrorResponse(500, "update failed"))
        mixed.subCallbackFor(insertBody).onFailure(Utils.constructErrorResponse(400, "insert failed"))

        assertEquals("both-fail must fire onFailure exactly once (previously fired twice)", 1, failureCount)
        assertEquals("no onSuccess when nothing committed", 0, successCount)
    }

    // Issue #3 regression: the SDK must not report an undecodable response as an empty success.
    // fromJsonOrThrow (used by the success-path adapters) throws on a malformed body so the adapter
    // routes to onFailure; the public fromJson stays lenient (returns empty) for back-compat.
    @Test
    fun testFromJsonOrThrowThrowsOnMalformedResponse() {
        try {
            CollectResponse.fromJsonOrThrow("not-json", LogLevel.ERROR)
            fail("expected a parse exception for a malformed collect response")
        } catch (e: Exception) { /* expected */ }
        try {
            RevealResponse.fromJsonOrThrow("<<<", LogLevel.ERROR)
            fail("expected a parse exception for a malformed reveal response")
        } catch (e: Exception) { /* expected */ }
    }

    @Test
    fun testPublicFromJsonStaysLenientForBackCompat() {
        // public fromJson must never throw (back-compat) — it returns an empty response.
        assertEquals(0, CollectResponse.fromJson("not-json").records.size)
        assertEquals(0, RevealResponse.fromJson("<<<").records.size)
    }

    @Test
    fun testFromJsonOrThrowParsesValidAndSkipsNonObjectEntries() {
        val json = """{"records":["oops",{"tableName":"cards","skyflowId":"id1","tokens":{"cvv":[{"token":"tok1"}]},"httpCode":200}]}"""
        val resp = CollectResponse.fromJsonOrThrow(json, LogLevel.ERROR)
        // the non-object "oops" entry is skipped (logged, not silently dropped); the valid record parses.
        assertEquals(1, resp.records.size)
        assertEquals("tok1", resp.records[0].tokens?.get("cvv")?.firstOrNull()?.token)
    }
}