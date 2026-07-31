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
}
