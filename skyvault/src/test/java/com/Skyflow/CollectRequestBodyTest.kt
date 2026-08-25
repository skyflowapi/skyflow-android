package com.Skyflow

import Skyflow.*
import Skyflow.collect.client.CollectRequestBody
import android.app.Activity
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class CollectRequestBodyTest {
    
    private lateinit var skyflow: Client
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity
    private lateinit var layoutParams: ViewGroup.LayoutParams

    @Before
    fun setup() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        skyflow = Client(configuration)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()
    }

    @Test
    fun testSeparateInsertAndUpdate_pureInsert() {
        val container = skyflow.container(ContainerType.COLLECT)
        
        // Create elements without skyflowID (INSERT operations)
        val cardNumberInput = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER
        )
        val cvvInput = CollectElementInput(
            "cards",
            "cvv",
            SkyflowElementType.CVV
        )
        
        val cardNumber = container.create(activity, cardNumberInput) as TextField
        val cvv = container.create(activity, cvvInput) as TextField
        
        val elements = mutableListOf(cardNumber, cvv)
        
        val (insertElements, insertAdditionalFields, updateRecords) = 
            CollectRequestBody.separateInsertAndUpdateRecords(elements, null, LogLevel.ERROR)
        
        // Should have 2 insert elements
        Assert.assertEquals(2, insertElements.size)
        Assert.assertTrue(insertElements.contains(cardNumber))
        Assert.assertTrue(insertElements.contains(cvv))
        
        // Should have no additional fields
        Assert.assertNull(insertAdditionalFields)
        
        // Should have no update records
        Assert.assertEquals(0, updateRecords.size)
    }

    @Test
    fun testSeparateInsertAndUpdate_pureUpdate() {
        val container = skyflow.container(ContainerType.COLLECT)
        
        // Create elements with skyflowID (UPDATE operations)
        val cardNumberInput = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER,
            skyflowID = "sky-123"
        )
        val cvvInput = CollectElementInput(
            "cards",
            "cvv",
            SkyflowElementType.CVV,
            skyflowID = "sky-123"
        )
        
        val cardNumber = container.create(activity, cardNumberInput) as TextField
        val cvv = container.create(activity, cvvInput) as TextField
        
        cardNumber.actualValue = "4111111111111111"
        cvv.actualValue = "123"
        
        val elements = mutableListOf(cardNumber, cvv)
        
        val (insertElements, insertAdditionalFields, updateRecords) = 
            CollectRequestBody.separateInsertAndUpdateRecords(elements, null, LogLevel.ERROR)
        
        // Should have no insert elements
        Assert.assertEquals(0, insertElements.size)
        
        // Should have no additional fields
        Assert.assertNull(insertAdditionalFields)
        
        // Should have 1 update record (merged from both elements)
        Assert.assertEquals(1, updateRecords.size)
        Assert.assertEquals("cards", updateRecords[0].table)
        Assert.assertEquals("sky-123", updateRecords[0].skyflowID)
        Assert.assertEquals(2, updateRecords[0].columns.size)
        Assert.assertTrue(updateRecords[0].columns.containsKey("card_number"))
        Assert.assertTrue(updateRecords[0].columns.containsKey("cvv"))
    }

    @Test
    fun testSeparateInsertAndUpdate_mixedInsertAndUpdate() {
        val container = skyflow.container(ContainerType.COLLECT)
        
        // Create mix of insert and update elements
        val insertElement = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER
        )
        val updateElement = CollectElementInput(
            "cards",
            "cvv",
            SkyflowElementType.CVV,
            skyflowID = "sky-456"
        )
        
        val insert = container.create(activity, insertElement) as TextField
        val update = container.create(activity, updateElement) as TextField
        
        update.actualValue = "456"
        
        val elements = mutableListOf(insert, update)
        
        val (insertElements, insertAdditionalFields, updateRecords) = 
            CollectRequestBody.separateInsertAndUpdateRecords(elements, null, LogLevel.ERROR)
        
        // Should have 1 insert element
        Assert.assertEquals(1, insertElements.size)
        Assert.assertTrue(insertElements.contains(insert))
        
        // Should have 1 update record
        Assert.assertEquals(1, updateRecords.size)
        Assert.assertEquals("sky-456", updateRecords[0].skyflowID)
    }

    @Test
    fun testSeparateInsertAndUpdate_additionalFieldsInsert() {
        val elements = mutableListOf<TextField>()
        
        val additionalFields = JSONObject()
        val recordsArray = JSONArray()
        
        val record = JSONObject()
        record.put("table", "users")
        val fields = JSONObject()
        fields.put("email", "test@example.com")
        fields.put("name", "John Doe")
        record.put("fields", fields)
        recordsArray.put(record)
        
        additionalFields.put("records", recordsArray)
        
        val (insertElements, insertAdditionalFields, updateRecords) = 
            CollectRequestBody.separateInsertAndUpdateRecords(elements, additionalFields, LogLevel.ERROR)
        
        // Should have no insert elements
        Assert.assertEquals(0, insertElements.size)
        
        // Should have insert additional fields
        Assert.assertNotNull(insertAdditionalFields)
        Assert.assertTrue(insertAdditionalFields!!.has("records"))
        val records = insertAdditionalFields.getJSONArray("records")
        Assert.assertEquals(1, records.length())
        
        // Should have no update records
        Assert.assertEquals(0, updateRecords.size)
    }

    @Test
    fun testSeparateInsertAndUpdate_additionalFieldsUpdate() {
        val elements = mutableListOf<TextField>()
        
        val additionalFields = JSONObject()
        val recordsArray = JSONArray()
        
        val record = JSONObject()
        record.put("table", "users")
        val fields = JSONObject()
        fields.put("skyflowID", "sky-789")
        fields.put("email", "updated@example.com")
        record.put("fields", fields)
        recordsArray.put(record)
        
        additionalFields.put("records", recordsArray)
        
        val (insertElements, insertAdditionalFields, updateRecords) = 
            CollectRequestBody.separateInsertAndUpdateRecords(elements, additionalFields, LogLevel.ERROR)
        
        // Should have no insert elements
        Assert.assertEquals(0, insertElements.size)
        
        // Should have no insert additional fields
        Assert.assertNull(insertAdditionalFields)
        
        // Should have 1 update record from additionalFields
        Assert.assertEquals(1, updateRecords.size)
        Assert.assertEquals("users", updateRecords[0].table)
        Assert.assertEquals("sky-789", updateRecords[0].skyflowID)
        Assert.assertTrue(updateRecords[0].columns.containsKey("email"))
        Assert.assertFalse(updateRecords[0].columns.containsKey("skyflowID"))
    }

    @Test
    fun testSeparateInsertAndUpdate_mergeElementAndAdditionalFields() {
        val container = skyflow.container(ContainerType.COLLECT)
        
        // Create element with skyflowID
        val cardNumberInput = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER,
            skyflowID = "sky-merge-123"
        )
        
        val cardNumber = container.create(activity, cardNumberInput) as TextField
        cardNumber.actualValue = "4111111111111111"
        
        val elements = mutableListOf(cardNumber)
        
        // Create additionalFields with same skyflowID
        val additionalFields = JSONObject()
        val recordsArray = JSONArray()
        
        val record = JSONObject()
        record.put("table", "cards")
        val fields = JSONObject()
        fields.put("skyflowID", "sky-merge-123")
        fields.put("cvv", "456")
        record.put("fields", fields)
        recordsArray.put(record)
        
        additionalFields.put("records", recordsArray)
        
        val (insertElements, insertAdditionalFields, updateRecords) = 
            CollectRequestBody.separateInsertAndUpdateRecords(elements, additionalFields, LogLevel.ERROR)
        
        // Should have no insert elements
        Assert.assertEquals(0, insertElements.size)
        
        // Should have no insert additional fields
        Assert.assertNull(insertAdditionalFields)
        
        // Should have 1 merged update record
        Assert.assertEquals(1, updateRecords.size)
        Assert.assertEquals("cards", updateRecords[0].table)
        Assert.assertEquals("sky-merge-123", updateRecords[0].skyflowID)
        Assert.assertEquals(2, updateRecords[0].columns.size)
        Assert.assertTrue(updateRecords[0].columns.containsKey("card_number"))
        Assert.assertTrue(updateRecords[0].columns.containsKey("cvv"))
    }

    @Test
    fun testSeparateInsertAndUpdate_multipleTables() {
        val container = skyflow.container(ContainerType.COLLECT)
        
        // Create elements for different tables
        val card1Input = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER,
            skyflowID = "sky-card-1"
        )
        val user1Input = CollectElementInput(
            "users",
            "name",
            SkyflowElementType.INPUT_FIELD,
            skyflowID = "sky-user-1"
        )
        
        val card1 = container.create(activity, card1Input) as TextField
        val user1 = container.create(activity, user1Input) as TextField
        
        card1.actualValue = "4111111111111111"
        user1.actualValue = "John"
        
        val elements = mutableListOf(card1, user1)
        
        val (insertElements, insertAdditionalFields, updateRecords) = 
            CollectRequestBody.separateInsertAndUpdateRecords(elements, null, LogLevel.ERROR)
        
        // Should have 2 update records for different tables
        Assert.assertEquals(2, updateRecords.size)
        
        val cardUpdate = updateRecords.find { it.table == "cards" }
        val userUpdate = updateRecords.find { it.table == "users" }
        
        Assert.assertNotNull(cardUpdate)
        Assert.assertNotNull(userUpdate)
        Assert.assertEquals("sky-card-1", cardUpdate!!.skyflowID)
        Assert.assertEquals("sky-user-1", userUpdate!!.skyflowID)
    }

    @Test
    fun testSeparateInsertAndUpdate_multipleRecordsSameTable() {
        val container = skyflow.container(ContainerType.COLLECT)
        
        // Create elements for same table but different skyflowIDs
        val card1Input = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER,
            skyflowID = "sky-card-1"
        )
        val card2Input = CollectElementInput(
            "cards",
            "cvv",
            SkyflowElementType.CVV,
            skyflowID = "sky-card-2"
        )
        
        val card1 = container.create(activity, card1Input) as TextField
        val card2 = container.create(activity, card2Input) as TextField
        
        card1.actualValue = "4111111111111111"
        card2.actualValue = "123"
        
        val elements = mutableListOf(card1, card2)
        
        val (insertElements, insertAdditionalFields, updateRecords) = 
            CollectRequestBody.separateInsertAndUpdateRecords(elements, null, LogLevel.ERROR)
        
        // Should have 2 separate update records
        Assert.assertEquals(2, updateRecords.size)
        Assert.assertEquals("cards", updateRecords[0].table)
        Assert.assertEquals("cards", updateRecords[1].table)
        Assert.assertNotEquals(updateRecords[0].skyflowID, updateRecords[1].skyflowID)
    }

    @Test
    fun testSeparateInsertAndUpdate_complexMixed() {
        val container = skyflow.container(ContainerType.COLLECT)
        
        // Mix of inserts and updates across elements and additionalFields
        val insertElement = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER
        )
        val updateElement = CollectElementInput(
            "users",
            "name",
            SkyflowElementType.INPUT_FIELD,
            skyflowID = "sky-user-100"
        )
        
        val insert = container.create(activity, insertElement) as TextField
        val update = container.create(activity, updateElement) as TextField
        
        insert.actualValue = "4111111111111111"
        update.actualValue = "Jane"
        
        val elements = mutableListOf(insert, update)
        
        // AdditionalFields with both insert and update
        val additionalFields = JSONObject()
        val recordsArray = JSONArray()
        
        // Insert record
        val insertRecord = JSONObject()
        insertRecord.put("table", "products")
        val insertFields = JSONObject()
        insertFields.put("name", "Product A")
        insertRecord.put("fields", insertFields)
        recordsArray.put(insertRecord)
        
        // Update record
        val updateRecord = JSONObject()
        updateRecord.put("table", "users")
        val updateFields = JSONObject()
        updateFields.put("skyflowID", "sky-user-100")
        updateFields.put("email", "jane@example.com")
        updateRecord.put("fields", updateFields)
        recordsArray.put(updateRecord)
        
        additionalFields.put("records", recordsArray)
        
        val (insertElements, insertAdditionalFields, updateRecords) = 
            CollectRequestBody.separateInsertAndUpdateRecords(elements, additionalFields, LogLevel.ERROR)
        
        // Should have 1 insert element
        Assert.assertEquals(1, insertElements.size)
        
        // Should have 1 insert additional field record
        Assert.assertNotNull(insertAdditionalFields)
        val insertRecords = insertAdditionalFields!!.getJSONArray("records")
        Assert.assertEquals(1, insertRecords.length())
        
        // Should have 1 merged update record
        Assert.assertEquals(1, updateRecords.size)
        Assert.assertEquals("users", updateRecords[0].table)
        Assert.assertEquals("sky-user-100", updateRecords[0].skyflowID)
        Assert.assertEquals(2, updateRecords[0].columns.size)
        Assert.assertTrue(updateRecords[0].columns.containsKey("name"))
        Assert.assertTrue(updateRecords[0].columns.containsKey("email"))
    }

    @Test
    fun testSeparateInsertAndUpdate_emptySkyflowIDTreatedAsInsert() {
        val container = skyflow.container(ContainerType.COLLECT)
        
        // Element with empty skyflowID should be treated as INSERT
        val cardNumberInput = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER,
            skyflowID = ""
        )
        
        val cardNumber = container.create(activity, cardNumberInput) as TextField
        val elements = mutableListOf(cardNumber)
        
        val (insertElements, insertAdditionalFields, updateRecords) = 
            CollectRequestBody.separateInsertAndUpdateRecords(elements, null, LogLevel.ERROR)
        
        // Should be treated as insert
        Assert.assertEquals(1, insertElements.size)
        Assert.assertEquals(0, updateRecords.size)
    }

    @Test
    fun testSeparateInsertAndUpdate_nullAdditionalFields() {
        val container = skyflow.container(ContainerType.COLLECT)
        
        val cardNumberInput = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER
        )
        
        val cardNumber = container.create(activity, cardNumberInput) as TextField
        val elements = mutableListOf(cardNumber)
        
        // Null additionalFields should be handled gracefully
        val (insertElements, insertAdditionalFields, updateRecords) = 
            CollectRequestBody.separateInsertAndUpdateRecords(elements, null, LogLevel.ERROR)
        
        Assert.assertEquals(1, insertElements.size)
        Assert.assertNull(insertAdditionalFields)
        Assert.assertEquals(0, updateRecords.size)
    }

    @Test
    fun testSeparateInsertAndUpdate_emptyRecordsArray() {
        val elements = mutableListOf<TextField>()
        
        val additionalFields = JSONObject()
        additionalFields.put("records", JSONArray())
        
        val (insertElements, insertAdditionalFields, updateRecords) = 
            CollectRequestBody.separateInsertAndUpdateRecords(elements, additionalFields, LogLevel.ERROR)
        
        Assert.assertEquals(0, insertElements.size)
        Assert.assertNull(insertAdditionalFields)
        Assert.assertEquals(0, updateRecords.size)
    }
}
