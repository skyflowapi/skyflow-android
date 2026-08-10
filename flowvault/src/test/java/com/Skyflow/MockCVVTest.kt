package com.Skyflow

import Skyflow.collect.client.CVVMap
import Skyflow.collect.client.generateMockCVV
import Skyflow.collect.client.replaceCVVTokensInRecord
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MockCVVTest {

    // ---- generateMockCVV ----

    @Test
    fun `generateMockCVV returns empty string for length 0`() {
        assertEquals("", generateMockCVV(0, ""))
        assertEquals("", generateMockCVV(-1, ""))
    }

    @Test
    fun `generateMockCVV produces numeric string of requested length`() {
        for (length in intArrayOf(3, 4)) {
            repeat(200) {
                val mock = generateMockCVV(length, "999999")
                assertEquals(length, mock.length)
                assertTrue("expected all digits, got $mock", mock.all { it.isDigit() })
            }
        }
    }

    @Test
    fun `generateMockCVV never equals the entered value`() {
        // Repeated draws must always differ from the entered value for the same element.
        repeat(2000) {
            assertNotEquals("123", generateMockCVV(3, "123"))
        }
        repeat(2000) {
            assertNotEquals("4321", generateMockCVV(4, "4321"))
        }
    }

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

    // ---- replaceCVVTokensInRecord ----

    @Test
    fun `insert flow swaps CVV column matched by table name and leaves other columns intact`() {
        val tokens = JSONObject()
            .put("cvv", tokenList("realCvvToken"))
            .put("card_number", tokenList("realCardToken"))

        val cvvMap = CVVMap(byTable = mapOf("cards" to mapOf("cvv" to "321")), byRecordId = emptyMap())
        replaceCVVTokensInRecord(tokens, tableName = "cards", skyflowId = "newlyGeneratedId", cvvMap = cvvMap)

        val swapped = tokens.getJSONArray("cvv").getJSONObject(0)
        assertEquals(3, swapped.getString("token").length)
        assertTrue(swapped.getString("token").all { it.isDigit() })
        assertNotEquals("321", swapped.getString("token"))
        assertNotEquals("realCvvToken", swapped.getString("token"))
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

        val swapped = tokens.getJSONArray("cvv").getJSONObject(0).getString("token")
        assertEquals(4, swapped.length)
        assertNotEquals("4321", swapped)
    }

    @Test
    fun `record id match takes precedence over table match`() {
        val tokens = JSONObject().put("cvv", tokenList("realCvvToken"))
        val cvvMap = CVVMap(
            byTable = mapOf("cards" to mapOf("cvv" to "111")),      // 3-digit
            byRecordId = mapOf("rec-1" to mapOf("cvv" to "2222"))   // 4-digit
        )

        replaceCVVTokensInRecord(tokens, tableName = "cards", skyflowId = "rec-1", cvvMap = cvvMap)

        // Length must follow the record-id entry (4), proving record id won.
        assertEquals(4, tokens.getJSONArray("cvv").getJSONObject(0).getString("token").length)
    }

    @Test
    fun `same mock is applied across all token entries of a CVV column`() {
        val tokens = JSONObject().put("cvv", tokenList("tokA", "tokB", "tokC"))
        val cvvMap = CVVMap(byTable = mapOf("cards" to mapOf("cvv" to "321")), byRecordId = emptyMap())

        replaceCVVTokensInRecord(tokens, tableName = "cards", skyflowId = "id", cvvMap = cvvMap)

        val entries = tokens.getJSONArray("cvv")
        val first = entries.getJSONObject(0).getString("token")
        for (i in 0 until entries.length()) {
            assertEquals(first, entries.getJSONObject(i).getString("token"))
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
        // pincode entry: replaced with 3-digit mock ≠ "500"
        val pincodeToken = addr.getJSONObject(1).getString("token")
        assertEquals(3, pincodeToken.length)
        assertTrue(pincodeToken.all { it.isDigit() })
        assertNotEquals("500", pincodeToken)
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
        // city.street: replaced with 3-digit mock ≠ "123"
        val streetToken = addr.getJSONObject(3).getString("token")
        assertEquals(3, streetToken.length)
        assertTrue(streetToken.all { it.isDigit() })
        assertNotEquals("123", streetToken)
        assertNotEquals("street-tok", streetToken)
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
        // details.cvv: replaced with 4-digit mock ≠ "4321"
        val cvvToken = addr.getJSONObject(6).getString("token")
        assertEquals(4, cvvToken.length)
        assertTrue(cvvToken.all { it.isDigit() })
        assertNotEquals("4321", cvvToken)
        assertNotEquals("real-cvv-tok", cvvToken)
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
        // path-less entries replaced with same mock
        val mock = entries.getJSONObject(0).getString("token")
        assertEquals(3, mock.length)
        assertTrue(mock.all { it.isDigit() })
        assertNotEquals("321", mock)
        assertEquals(mock, entries.getJSONObject(1).getString("token"))
        // path-carrying entry untouched
        assertEquals("sub-tok", entries.getJSONObject(2).getString("token"))
    }
}
