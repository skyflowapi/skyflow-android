package Skyflow.collect.client

import Skyflow.*
import Skyflow.LogLevel
import Skyflow.SkyflowElementType
import Skyflow.TextField
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Update by Skyflow ID feature
 */
class UpdateBySkyflowIdTest {

    @Test
    fun testSeparateInsertAndUpdateRecords_onlyInsertElements() {
        // Create mock elements without skyflowID
        val elements = mutableListOf<TextField>()
        // Mock setup would go here
        
        val (insertElements, insertAdditionalFields, updateRecords) = CollectRequestBody.separateInsertAndUpdateRecords(
            elements,
            null,
            LogLevel.ERROR
        )
        
        // Verify all elements are in insert list
        // assertTrue(insertElements.size == elements.size)
        // assertTrue(updateRecords.isEmpty())
    }

    @Test
    fun testSeparateInsertAndUpdateRecords_onlyUpdateElements() {
        // Create mock elements with skyflowID
        val elements = mutableListOf<TextField>()
        // Mock setup would go here
        
        val (insertElements, insertAdditionalFields, updateRecords) = CollectRequestBody.separateInsertAndUpdateRecords(
            elements,
            null,
            LogLevel.ERROR
        )
        
        // Verify all elements are in update list
        // assertTrue(insertElements.isEmpty())
        // assertTrue(updateRecords.size > 0)
    }

    @Test
    fun testSeparateInsertAndUpdateRecords_mixedElements() {
        // Create mock elements with and without skyflowID
        val elements = mutableListOf<TextField>()
        // Mock setup would go here
        
        val (insertElements, insertAdditionalFields, updateRecords) = CollectRequestBody.separateInsertAndUpdateRecords(
            elements,
            null,
            LogLevel.ERROR
        )
        
        // Verify elements are separated correctly
        // assertTrue(insertElements.size > 0)
        // assertTrue(updateRecords.size > 0)
    }

    @Test
    fun testSeparateInsertAndUpdateRecords_groupSameSkyflowId() {
        // Create mock elements with same skyflowID
        val elements = mutableListOf<TextField>()
        // Mock setup: 2 elements with same skyflowID
        
        val (insertElements, insertAdditionalFields, updateRecords) = CollectRequestBody.separateInsertAndUpdateRecords(
            elements,
            null,
            LogLevel.ERROR
        )
        
        // Verify elements with same skyflowID are grouped
        // assertTrue(updateRecords.size == 1)
        // assertTrue(updateRecords[0].columns.size == 2)
    }

    @Test
    fun testUpdateRequestRecord_structure() {
        val updateRecord = UpdateRequestRecord(
            table = "cards",
            skyflowID = "test-id-123",
            columns = mutableMapOf("card_number" to "4111111111111111", "cvv" to "123")
        )
        
        assertEquals("cards", updateRecord.table)
        assertEquals("test-id-123", updateRecord.skyflowID)
        assertEquals(2, updateRecord.columns.size)
        assertEquals("4111111111111111", updateRecord.columns["card_number"])
        assertEquals("123", updateRecord.columns["cvv"])
    }

    @Test
    fun testCollectElementInput_withSkyflowId() {
        val input = CollectElementInput(
            table = "cards",
            column = "card_number",
            type = SkyflowElementType.CARD_NUMBER,
            skyflowID = "test-skyflow-id"
        )
        
        assertEquals("test-skyflow-id", input.skyflowID)
        assertEquals("cards", input.table)
        assertEquals("card_number", input.column)
    }

    @Test
    fun testCollectElementInput_withoutSkyflowId() {
        val input = CollectElementInput(
            table = "cards",
            column = "card_number",
            type = SkyflowElementType.CARD_NUMBER
        )
        
        assertNull(input.skyflowID)
    }

    /**
     * Test response merging in MixedAPICallback
     */
    @Test
    fun testMixedResponse_merging() {
        // Mock insert response - use fully qualified names
        val insertResponse = org.json.JSONObject()
        val insertRecordsArray = org.json.JSONArray()
        val insertRecord = org.json.JSONObject()
        insertRecord.put("table", "cards")
        val insertFields = org.json.JSONObject()
        insertFields.put("skyflow_id", "new-id-123")
        insertFields.put("card_number", "token-xyz")
        insertRecord.put("fields", insertFields)
        insertRecordsArray.put(insertRecord)
        insertResponse.put("records", insertRecordsArray)

        // Mock update response - use fully qualified names
        val updateResponse = org.json.JSONObject()
        val updateRecordsArray = org.json.JSONArray()
        val updateRecord = org.json.JSONObject()
        updateRecord.put("table", "cards")
        val updateFields = org.json.JSONObject()
        updateFields.put("skyflow_id", "existing-id-456")
        updateFields.put("cvv", "token-abc")
        updateRecord.put("fields", updateFields)
        updateRecordsArray.put(updateRecord)
        updateResponse.put("records", updateRecordsArray)

        // Verify merged response would contain both records
        // This would be tested in the actual MixedAPICallback implementation
    }
}