package com.Skyflow

import Skyflow.*
import Skyflow.collect.client.FlowDBCollectRequestBody
import Skyflow.collect.client.FlowDBCollectAPICallback
import Skyflow.core.FlowDBAPIClient
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

    // H2 regression: reveal() must fail fast on missing vault config, BEFORE any bearer-token /
    // network call. With an empty vaultURL the failure is delivered synchronously (checkVaultDetails);
    // without the fix, reveal() would pass empty validation and fail async at the network layer,
    // leaving capturedError null right after the call returns.
    @Test
    fun testRevealFailsFastOnMissingVaultUrl() {
        val client = Client(Configuration("vault123", "", AccessTokenProvider()))
        val container = client.container(ContainerType.REVEAL)
        var capturedError: SkyflowError? = null
        container.reveal(object : RevealCallback {
            override fun onSuccess(response: RevealResponse) {}
            override fun onFailure(error: SkyflowError) { capturedError = error }
        })
        // Fail-fast (synchronous) delivery is the core H2 guarantee; and the message now survives the
        // error round-trip (constructErrorResponse -> SkyflowError.fromJson) — carrying the real
        // EMPTY_VAULT_URL text rather than a generic "Unknown error".
        assertNotNull("reveal should fail fast (synchronously) when vaultURL is empty", capturedError)
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
}