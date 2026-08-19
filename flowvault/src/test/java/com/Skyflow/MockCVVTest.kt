package com.Skyflow

import Skyflow.*
import Skyflow.collect.client.CVVMap
import Skyflow.collect.client.MOCK_CVV_3
import Skyflow.collect.client.MOCK_CVV_4
import Skyflow.collect.client.mockCVV
import Skyflow.collect.client.replaceCVVTokensInRecord
import android.app.Activity
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MockCVVTest {

    private lateinit var client: Client
    private lateinit var activity: Activity

    @Before
    fun setup() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098",
            "https://vaulturl.com",
            AccessTokenProvider(),
            options = Options(logLevel = LogLevel.DEBUG, env = Env.DEV)
        )
        client = Client(configuration)
        activity = Robolectric.buildActivity(Activity::class.java).setup().get()
    }

    /** Creates a real CVV collect [TextField] with the given opt-in flag and entered value. */
    private fun cvvElement(
        returnMockValue: Boolean,
        value: String,
        column: String = "cvv",
        tableName: String = "cards",
        skyflowId: String? = null
    ): TextField {
        val input = CollectElementInput(
            tableName = tableName,
            column = column,
            type = SkyflowElementType.CVV,
            placeholder = "cvv",
            skyflowId = skyflowId
        )
        val element = client.container(ContainerType.COLLECT)
            .create(activity, input, CollectElementOptions(returnMockValue = returnMockValue)) as TextField
        element.onAttachedToWindow()
        element.setText(value)
        return element
    }

    // ---- mockCVV: fixed placeholder values ----

    @Test
    fun `mockCVV returns empty string for non-positive length`() {
        assertEquals("", mockCVV(0))
        assertEquals("", mockCVV(-1))
    }

    @Test
    fun `mockCVV returns the 3-digit constant for length 3 and shorter`() {
        assertEquals("817", mockCVV(3))
        assertEquals("817", mockCVV(2))
        assertEquals("817", mockCVV(1))
        assertEquals(MOCK_CVV_3, mockCVV(3))
    }

    @Test
    fun `mockCVV returns the 4-digit constant for length 4 and longer`() {
        assertEquals("8173", mockCVV(4))
        assertEquals("8173", mockCVV(5))
        assertEquals(MOCK_CVV_4, mockCVV(4))
    }

    @Test
    fun `mock constants hold the agreed fixed values`() {
        assertEquals("817", MOCK_CVV_3)
        assertEquals("8173", MOCK_CVV_4)
    }

    // ---- token-list builders (mirror the vault's response shape) ----

    private fun tokenList(vararg tokens: String): JSONArray {
        val array = JSONArray()
        for (t in tokens) {
            array.put(JSONObject().put("token", t).put("tokenGroupName", "grp"))
        }
        return array
    }

    /** Builds a path-carrying token entry as the vault returns for nested JSON columns. */
    private fun pathEntry(path: String, token: String): JSONObject =
        JSONObject().put("path", path).put("token", token).put("tokenGroupName", "grp")

    /**
     * Builds the address column token list from the background doc:
     *   [whole-col, pincode, city, city.street, city.ward]
     */
    private fun addressTokenList(): JSONArray = JSONArray().apply {
        put(JSONObject().put("token", "whole-col-tok").put("tokenGroupName", "grp")) // no path
        put(pathEntry("pincode",     "pincode-tok"))
        put(pathEntry("city",        "city-tok"))
        put(pathEntry("city.street", "street-tok"))
        put(pathEntry("city.ward",   "ward-tok"))
    }

    /**
     * Builds the address column token list extended with a nested CVV sub-field:
     *   address.details.cvv — sits alongside city.street / city.ward to verify exact isolation.
     */
    private fun addressWithCvvTokenList(): JSONArray = JSONArray().apply {
        put(JSONObject().put("token", "whole-col-tok").put("tokenGroupName", "grp")) // no path
        put(pathEntry("pincode",         "pincode-tok"))
        put(pathEntry("city",            "city-tok"))
        put(pathEntry("city.street",     "street-tok"))
        put(pathEntry("city.ward",       "ward-tok"))
        put(pathEntry("details",         "details-tok"))
        put(pathEntry("details.cvv",     "real-cvv-tok"))
    }

    // ---- replaceCVVTokensInRecord: swaps in the FIXED mock ----

    @Test
    fun `insert flow swaps CVV column matched by table name and leaves other columns intact`() {
        val tokens = JSONObject()
            .put("cvv", tokenList("realCvvToken"))
            .put("card_number", tokenList("realCardToken"))

        val cvvMap = CVVMap(byTable = mapOf("cards" to mapOf("cvv" to "321")), byRecordId = emptyMap())
        replaceCVVTokensInRecord(tokens, tableName = "cards", skyflowId = "newlyGeneratedId", cvvMap = cvvMap)

        val swapped = tokens.getJSONArray("cvv").getJSONObject(0)
        // 3-digit entered value -> fixed 817.
        assertEquals("817", swapped.getString("token"))
        // tokenGroupName preserved.
        assertEquals("grp", swapped.getString("tokenGroupName"))
        // Non-CVV column untouched.
        assertEquals("realCardToken", tokens.getJSONArray("card_number").getJSONObject(0).getString("token"))
    }

    @Test
    fun `update flow swaps CVV column matched by record id`() {
        val tokens = JSONObject().put("cvv", tokenList("realCvvToken"))
        val cvvMap = CVVMap(byTable = emptyMap(), byRecordId = mapOf("rec-1" to mapOf("cvv" to "4321")))

        replaceCVVTokensInRecord(tokens, tableName = "cards", skyflowId = "rec-1", cvvMap = cvvMap)

        // 4-digit entered value -> fixed 8173.
        assertEquals("8173", tokens.getJSONArray("cvv").getJSONObject(0).getString("token"))
    }

    @Test
    fun `record id match takes precedence over table match`() {
        val tokens = JSONObject().put("cvv", tokenList("realCvvToken"))
        val cvvMap = CVVMap(
            byTable = mapOf("cards" to mapOf("cvv" to "111")),      // 3-digit -> would be 817
            byRecordId = mapOf("rec-1" to mapOf("cvv" to "2222"))   // 4-digit -> 8173
        )

        replaceCVVTokensInRecord(tokens, tableName = "cards", skyflowId = "rec-1", cvvMap = cvvMap)

        // Value must follow the record-id entry (8173), proving record id won.
        assertEquals("8173", tokens.getJSONArray("cvv").getJSONObject(0).getString("token"))
    }

    @Test
    fun `same mock is applied across all token entries of a CVV column`() {
        val tokens = JSONObject().put("cvv", tokenList("tokA", "tokB", "tokC"))
        val cvvMap = CVVMap(byTable = mapOf("cards" to mapOf("cvv" to "321")), byRecordId = emptyMap())

        replaceCVVTokensInRecord(tokens, tableName = "cards", skyflowId = "id", cvvMap = cvvMap)

        val entries = tokens.getJSONArray("cvv")
        for (i in 0 until entries.length()) {
            assertEquals("817", entries.getJSONObject(i).getString("token"))
        }
    }

    @Test
    fun `no CVV columns captured leaves tokens unchanged`() {
        val tokens = JSONObject().put("cvv", tokenList("realCvvToken"))
        replaceCVVTokensInRecord(tokens, tableName = "cards", skyflowId = "id", cvvMap = CVVMap.EMPTY)
        assertEquals("realCvvToken", tokens.getJSONArray("cvv").getJSONObject(0).getString("token"))
    }

    // ---- empty entered-value tests ----

    @Test
    fun `flat CVV with empty entered value sets token to empty string, sibling columns untouched`() {
        val tokens = JSONObject()
            .put("cvv", tokenList("realCvvToken"))
            .put("card_number", tokenList("realCardToken"))
        val cvvMap = CVVMap(byTable = mapOf("cards" to mapOf("cvv" to "")), byRecordId = emptyMap())

        replaceCVVTokensInRecord(tokens, tableName = "cards", skyflowId = "id", cvvMap = cvvMap)

        assertEquals("", tokens.getJSONArray("cvv").getJSONObject(0).getString("token"))
        assertEquals("realCardToken", tokens.getJSONArray("card_number").getJSONObject(0).getString("token"))
    }

    @Test
    fun `nested CVV with empty entered value sets only that path entry to empty string, siblings untouched`() {
        val tokens = JSONObject().put("address", addressTokenList())
        val cvvMap = CVVMap(byTable = mapOf("nested" to mapOf("address.pincode" to "")), byRecordId = emptyMap())

        replaceCVVTokensInRecord(tokens, tableName = "nested", skyflowId = "id", cvvMap = cvvMap)

        val addr = tokens.getJSONArray("address")
        assertEquals("", addr.getJSONObject(1).getString("token"))   // pincode entry: empty
        assertEquals("whole-col-tok", addr.getJSONObject(0).getString("token"))
        assertEquals("city-tok",      addr.getJSONObject(2).getString("token"))
        assertEquals("street-tok",    addr.getJSONObject(3).getString("token"))
        assertEquals("ward-tok",      addr.getJSONObject(4).getString("token"))
    }

    // ---- nested JSON column tests ----

    @Test
    fun `one-level nested column replaces only its path entry, leaves parent and siblings intact`() {
        val tokens = JSONObject()
            .put("address", addressTokenList())
            .put("card_number", tokenList("card-tok"))
        val cvvMap = CVVMap(byTable = mapOf("nested" to mapOf("address.pincode" to "500")), byRecordId = emptyMap())

        replaceCVVTokensInRecord(tokens, tableName = "nested", skyflowId = "id", cvvMap = cvvMap)

        val addr = tokens.getJSONArray("address")
        // whole-column entry (no path): untouched
        assertEquals("whole-col-tok", addr.getJSONObject(0).getString("token"))
        // pincode entry: replaced with the fixed 3-digit mock
        assertEquals("817", addr.getJSONObject(1).getString("token"))
        // city, city.street, city.ward: untouched
        assertEquals("city-tok",    addr.getJSONObject(2).getString("token"))
        assertEquals("street-tok",  addr.getJSONObject(3).getString("token"))
        assertEquals("ward-tok",    addr.getJSONObject(4).getString("token"))
        // other column untouched
        assertEquals("card-tok", tokens.getJSONArray("card_number").getJSONObject(0).getString("token"))
    }

    @Test
    fun `two-level nested column replaces only exact path entry, parent and sibling paths intact`() {
        val tokens = JSONObject().put("address", addressTokenList())
        val cvvMap = CVVMap(byTable = mapOf("nested" to mapOf("address.city.street" to "123")), byRecordId = emptyMap())

        replaceCVVTokensInRecord(tokens, tableName = "nested", skyflowId = "id", cvvMap = cvvMap)

        val addr = tokens.getJSONArray("address")
        // whole-column, pincode, city, city.ward: all untouched
        assertEquals("whole-col-tok", addr.getJSONObject(0).getString("token"))
        assertEquals("pincode-tok",   addr.getJSONObject(1).getString("token"))
        assertEquals("city-tok",      addr.getJSONObject(2).getString("token"))
        assertEquals("ward-tok",      addr.getJSONObject(4).getString("token"))
        // city.street: replaced with the fixed 3-digit mock
        assertEquals("817", addr.getJSONObject(3).getString("token"))
    }

    @Test
    fun `nested CVV column replaces only its exact path entry, all sibling paths intact`() {
        // address.details.cvv — two-level nested CVV sub-field
        val tokens = JSONObject()
            .put("address", addressWithCvvTokenList())
            .put("card_number", tokenList("card-tok"))
        val cvvMap = CVVMap(byTable = mapOf("nested" to mapOf("address.details.cvv" to "4321")), byRecordId = emptyMap())

        replaceCVVTokensInRecord(tokens, tableName = "nested", skyflowId = "id", cvvMap = cvvMap)

        val addr = tokens.getJSONArray("address")
        // whole-column, pincode, city, city.street, city.ward, details: all untouched
        assertEquals("whole-col-tok", addr.getJSONObject(0).getString("token"))
        assertEquals("pincode-tok",   addr.getJSONObject(1).getString("token"))
        assertEquals("city-tok",      addr.getJSONObject(2).getString("token"))
        assertEquals("street-tok",    addr.getJSONObject(3).getString("token"))
        assertEquals("ward-tok",      addr.getJSONObject(4).getString("token"))
        assertEquals("details-tok",   addr.getJSONObject(5).getString("token"))
        // details.cvv: replaced with the fixed 4-digit mock
        assertEquals("8173", addr.getJSONObject(6).getString("token"))
        // other column untouched
        assertEquals("card-tok", tokens.getJSONArray("card_number").getJSONObject(0).getString("token"))
    }

    @Test
    fun `flat column with mixed list replaces only path-less entries, leaves path-carrying entries intact`() {
        val mixed = JSONArray().apply {
            put(JSONObject().put("token", "flat-1").put("tokenGroupName", "grp"))          // no path
            put(JSONObject().put("token", "flat-2").put("tokenGroupName", "grp"))          // no path
            put(pathEntry("some.sub", "sub-tok"))                                           // has path
        }
        val tokens = JSONObject().put("cvv", mixed)
        val cvvMap = CVVMap(byTable = mapOf("cards" to mapOf("cvv" to "321")), byRecordId = emptyMap())

        replaceCVVTokensInRecord(tokens, tableName = "cards", skyflowId = "id", cvvMap = cvvMap)

        val entries = tokens.getJSONArray("cvv")
        // path-less entries replaced with the same fixed mock
        assertEquals("817", entries.getJSONObject(0).getString("token"))
        assertEquals("817", entries.getJSONObject(1).getString("token"))
        // path-carrying entry untouched
        assertEquals("sub-tok", entries.getJSONObject(2).getString("token"))
    }

    // ---- CVVMap.capture: gating on returnMockValue (real elements) ----

    @Test
    fun `capture includes an opted-in CVV element keyed by table`() {
        val cvv = cvvElement(returnMockValue = true, value = "123")
        val map = CVVMap.capture(listOf(cvv))
        assertFalse(map.isEmpty())
        assertEquals("123", map.byTable["cards"]?.get("cvv"))
        assertTrue(map.byRecordId.isEmpty())
    }

    @Test
    fun `capture skips a CVV element that did not opt in`() {
        val cvv = cvvElement(returnMockValue = false, value = "123")
        val map = CVVMap.capture(listOf(cvv))
        assertTrue("opted-out CVV must not be captured", map.isEmpty())
    }

    @Test
    fun `capture skips a CVV element created with default options`() {
        // Default CollectElementOptions() -> returnMockValue defaults to false.
        val input = CollectElementInput(
            tableName = "cards", column = "cvv", type = SkyflowElementType.CVV, placeholder = "cvv"
        )
        val cvv = client.container(ContainerType.COLLECT).create(activity, input) as TextField
        cvv.onAttachedToWindow()
        cvv.setText("123")
        assertTrue(CVVMap.capture(listOf(cvv)).isEmpty())
    }

    @Test
    fun `capture is a no-op for a non-CVV element even when opted in`() {
        val input = CollectElementInput(
            tableName = "cards", column = "card_number", type = SkyflowElementType.CARD_NUMBER, placeholder = "card"
        )
        val card = client.container(ContainerType.COLLECT)
            .create(activity, input, CollectElementOptions(returnMockValue = true)) as TextField
        card.onAttachedToWindow()
        card.setText("4111111111111111")
        assertTrue("returnMockValue must be a no-op for non-CVV types", CVVMap.capture(listOf(card)).isEmpty())
    }

    @Test
    fun `capture keys an opted-in update CVV by its record id`() {
        val cvv = cvvElement(returnMockValue = true, value = "4321", skyflowId = "rec-9")
        val map = CVVMap.capture(listOf(cvv))
        assertEquals("4321", map.byRecordId["rec-9"]?.get("cvv"))
        assertTrue(map.byTable.isEmpty())
    }

    @Test
    fun `capture includes only the opted-in element among mixed CVV elements`() {
        val optIn = cvvElement(returnMockValue = true, value = "111", column = "cvv_a")
        val optOut = cvvElement(returnMockValue = false, value = "222", column = "cvv_b")
        val cols = CVVMap.capture(listOf(optIn, optOut)).byTable["cards"]
        assertEquals("111", cols?.get("cvv_a"))
        assertFalse("opted-out column must be absent", cols?.containsKey("cvv_b") ?: false)
    }

    // ---- capture -> replace integration ----

    @Test
    fun `opted-in CVV element yields the fixed mock in the response tokens`() {
        val cvv = cvvElement(returnMockValue = true, value = "321") // 3-digit
        val map = CVVMap.capture(listOf(cvv))
        val tokens = JSONObject().put("cvv", tokenList("realCvvToken"))
        replaceCVVTokensInRecord(tokens, tableName = "cards", skyflowId = "id", cvvMap = map)
        assertEquals("817", tokens.getJSONArray("cvv").getJSONObject(0).getString("token"))
    }

    @Test
    fun `opted-out CVV element leaves the real token in the response`() {
        val cvv = cvvElement(returnMockValue = false, value = "321")
        val map = CVVMap.capture(listOf(cvv))
        val tokens = JSONObject().put("cvv", tokenList("realCvvToken"))
        replaceCVVTokensInRecord(tokens, tableName = "cards", skyflowId = "id", cvvMap = map)
        assertEquals("realCvvToken", tokens.getJSONArray("cvv").getJSONObject(0).getString("token"))
    }
}
