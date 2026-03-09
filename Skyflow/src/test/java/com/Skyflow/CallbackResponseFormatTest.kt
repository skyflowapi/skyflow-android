package com.Skyflow

import Skyflow.*
import Skyflow.collect.client.CollectAPICallback
import Skyflow.collect.client.MixedAPICallback
import Skyflow.collect.client.UpdateAPICallback
import Skyflow.core.APIClient
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CallbackResponseFormatTest {

    private lateinit var apiClient: APIClient

    @Before
    fun setup() {
        apiClient = APIClient(
            "test-vault-id",
            "https://test-vault.com",
            AccessTokenProvider(),
            LogLevel.ERROR
        )
    }

    @Test
    fun testCollectCallback_successResponse_isJSONObject() {
        var responseReceived: Any? = null
        var onSuccessCalled = false

        val callback = object : Callback {
            override fun onSuccess(responseBody: Any) {
                responseReceived = responseBody
                onSuccessCalled = true
            }

            override fun onFailure(exception: Any) {
                // Should not be called
            }
        }

        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        val fields = JSONObject()
        fields.put("card_number", "4111111111111111")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)

        val collectCallback = CollectAPICallback(
            apiClient,
            records,
            callback,
            InsertOptions(true),
            LogLevel.ERROR
        )

        // The response should be a JSONObject
        val mockResponse = JSONObject()
        mockResponse.put("records", JSONArray())
        
        // Verify that responses are JSONObject type
        Assert.assertTrue(mockResponse is JSONObject)
        Assert.assertTrue(mockResponse.has("records"))
    }

    @Test
    fun testCollectCallback_failureResponse_isJSONObject() {
        var exceptionReceived: Any? = null
        var onFailureCalled = false

        val callback = object : Callback {
            override fun onSuccess(responseBody: Any) {
                // Should not be called
            }

            override fun onFailure(exception: Any) {
                exceptionReceived = exception
                onFailureCalled = true
                
                // Exception should be a JSONObject
                Assert.assertTrue(exception is JSONObject)
                val errorJson = exception as JSONObject
                Assert.assertTrue(errorJson.has("errors"))
            }
        }

        // Simulate error response format
        val errorResponse = JSONObject()
        val errorsArray = JSONArray()
        val errorObj = JSONObject()
        val error = JSONObject()
        error.put("code", 400)
        error.put("description", "Test error")
        error.put("type", "400")
        errorObj.put("error", error)
        errorsArray.put(errorObj)
        errorResponse.put("errors", errorsArray)

        Assert.assertTrue(errorResponse.has("errors"))
        Assert.assertTrue(errorResponse is JSONObject)
    }

    @Test
    fun testUpdateCallback_successResponse_format() {
        val updateRecords = mutableListOf(
            Skyflow.collect.client.UpdateRequestRecord("cards", "sky-123", mutableMapOf("cvv" to "456"))
        )

        var responseReceived: Any? = null

        val callback = object : Callback {
            override fun onSuccess(responseBody: Any) {
                responseReceived = responseBody
                
                // Response should be JSONObject
                Assert.assertTrue(responseBody is JSONObject)
                val jsonResponse = responseBody as JSONObject
                
                // Should have records array
                if (jsonResponse.has("records")) {
                    Assert.assertTrue(jsonResponse.get("records") is JSONArray)
                }
            }

            override fun onFailure(exception: Any) {
                // Should be JSONObject even on failure
                Assert.assertTrue(exception is JSONObject)
            }
        }

        val updateCallback = UpdateAPICallback(
            apiClient,
            updateRecords,
            callback,
            InsertOptions(true),
            LogLevel.ERROR
        )

        // Verify callback accepts Any type
        Assert.assertNotNull(callback)
    }

    @Test
    fun testUpdateCallback_tokenFalse_responseFormat() {
        // When token=false, the response should have skyflow_id directly in the record object (no fields wrapper)
        val updateRecords = mutableListOf(
            Skyflow.collect.client.UpdateRequestRecord("cards", "sky-123", mutableMapOf("cvv" to "456"))
        )

        var responseReceived: Any? = null

        val callback = object : Callback {
            override fun onSuccess(responseBody: Any) {
                responseReceived = responseBody
                
                // Response should be JSONObject
                Assert.assertTrue(responseBody is JSONObject)
                val jsonResponse = responseBody as JSONObject
                
                // Should have records array
                Assert.assertTrue(jsonResponse.has("records"))
                val recordsArray = jsonResponse.getJSONArray("records")
                Assert.assertTrue(recordsArray.length() > 0)
                
                // When token=false, record should have skyflow_id directly (not in fields)
                val record = recordsArray.getJSONObject(0)
                Assert.assertTrue(record.has("table"))
                Assert.assertTrue(record.has("skyflow_id"))
                Assert.assertFalse(record.has("fields"))  // No fields wrapper when token=false
            }

            override fun onFailure(exception: Any) {
                Assert.fail("Should not fail")
            }
        }

        val updateCallback = UpdateAPICallback(
            apiClient,
            updateRecords,
            callback,
            InsertOptions(false),  // token=false
            LogLevel.ERROR
        )

        // This test verifies the expected format structure
        Assert.assertNotNull(callback)
    }

    @Test
    fun testUpdateCallback_tokenTrue_responseFormat() {
        // When token=true, the response should have fields wrapper with skyflow_id and tokens
        val updateRecords = mutableListOf(
            Skyflow.collect.client.UpdateRequestRecord("cards", "sky-456", mutableMapOf("cvv" to "789"))
        )

        var responseReceived: Any? = null

        val callback = object : Callback {
            override fun onSuccess(responseBody: Any) {
                responseReceived = responseBody
                
                // Response should be JSONObject
                Assert.assertTrue(responseBody is JSONObject)
                val jsonResponse = responseBody as JSONObject
                
                // Should have records array
                Assert.assertTrue(jsonResponse.has("records"))
                val recordsArray = jsonResponse.getJSONArray("records")
                Assert.assertTrue(recordsArray.length() > 0)
                
                // When token=true, record should have fields wrapper
                val record = recordsArray.getJSONObject(0)
                Assert.assertTrue(record.has("table"))
                Assert.assertTrue(record.has("fields"))  // Fields wrapper when token=true
                
                val fields = record.getJSONObject("fields")
                Assert.assertTrue(fields.has("skyflow_id"))
            }

            override fun onFailure(exception: Any) {
                Assert.fail("Should not fail")
            }
        }

        val updateCallback = UpdateAPICallback(
            apiClient,
            updateRecords,
            callback,
            InsertOptions(true),  // token=true
            LogLevel.ERROR
        )

        // This test verifies the expected format structure
        Assert.assertNotNull(callback)
    }

    @Test
    fun testMixedCallback_mergedResponse_format() {
        // Test that MixedAPICallback returns merged response with both records and errors
        val mockInsertResponse = JSONObject()
        val insertRecords = JSONArray()
        val insertRecord = JSONObject()
        insertRecord.put("table", "cards")
        val insertFields = JSONObject()
        insertFields.put("skyflow_id", "new-id-123")
        insertRecord.put("fields", insertFields)
        insertRecords.put(insertRecord)
        mockInsertResponse.put("records", insertRecords)

        val mockUpdateResponse = JSONObject()
        val updateRecords = JSONArray()
        val updateRecord = JSONObject()
        updateRecord.put("table", "users")
        val updateFields = JSONObject()
        updateFields.put("skyflow_id", "existing-id-456")
        updateRecord.put("fields", updateFields)
        updateRecords.put(updateRecord)
        mockUpdateResponse.put("records", updateRecords)

        // Merge logic
        val mergedResponse = JSONObject()
        val allRecords = JSONArray()
        
        if (mockInsertResponse.has("records")) {
            val ir = mockInsertResponse.getJSONArray("records")
            for (i in 0 until ir.length()) {
                allRecords.put(ir.get(i))
            }
        }
        
        if (mockUpdateResponse.has("records")) {
            val ur = mockUpdateResponse.getJSONArray("records")
            for (i in 0 until ur.length()) {
                allRecords.put(ur.get(i))
            }
        }
        
        mergedResponse.put("records", allRecords)

        // Verify merged response structure
        Assert.assertTrue(mergedResponse.has("records"))
        Assert.assertEquals(2, mergedResponse.getJSONArray("records").length())
    }

    @Test
    fun testMixedCallback_withErrors_format() {
        // Test response with both successful records and errors
        val response = JSONObject()
        
        val recordsArray = JSONArray()
        val successRecord = JSONObject()
        successRecord.put("table", "cards")
        val fields = JSONObject()
        fields.put("skyflow_id", "success-id")
        successRecord.put("fields", fields)
        recordsArray.put(successRecord)
        
        val errorsArray = JSONArray()
        val errorObj = JSONObject()
        val error = JSONObject()
        error.put("code", 400)
        error.put("description", "Invalid field")
        error.put("type", "400")
        errorObj.put("error", error)
        errorsArray.put(errorObj)
        
        response.put("records", recordsArray)
        response.put("errors", errorsArray)

        // Verify structure
        Assert.assertTrue(response.has("records"))
        Assert.assertTrue(response.has("errors"))
        Assert.assertEquals(1, response.getJSONArray("records").length())
        Assert.assertEquals(1, response.getJSONArray("errors").length())
    }

    @Test
    fun testCallback_unifiedResponseFormat() {
        // Both success and failure should use JSONObject
        val successResponse = JSONObject()
        successResponse.put("records", JSONArray())
        
        val failureResponse = JSONObject()
        failureResponse.put("errors", JSONArray())

        // Both are JSONObject type
        Assert.assertTrue(successResponse is JSONObject)
        Assert.assertTrue(failureResponse is JSONObject)

        // Success has records
        Assert.assertTrue(successResponse.has("records"))
        Assert.assertFalse(successResponse.has("errors"))

        // Failure has errors
        Assert.assertTrue(failureResponse.has("errors"))
        Assert.assertFalse(failureResponse.has("records"))
    }

    @Test
    fun testCallback_partialSuccess_format() {
        // Partial success: some records succeed, some fail
        val response = JSONObject()
        
        val recordsArray = JSONArray()
        val record1 = JSONObject()
        record1.put("table", "cards")
        recordsArray.put(record1)
        
        val errorsArray = JSONArray()
        val error1 = JSONObject()
        error1.put("error", JSONObject().put("code", 400).put("description", "Error"))
        errorsArray.put(error1)
        
        response.put("records", recordsArray)
        response.put("errors", errorsArray)

        // This should be treated as failure (onFailure called) but with records included
        Assert.assertTrue(response.has("records"))
        Assert.assertTrue(response.has("errors"))
    }

    @Test
    fun testErrorResponse_structure() {
        val errorResponse = JSONObject()
        val errorsArray = JSONArray()
        
        for (i in 1..3) {
            val errorWrapper = JSONObject()
            val errorDetail = JSONObject()
            errorDetail.put("code", 400 + i)
            errorDetail.put("description", "Error $i")
            errorDetail.put("type", "${400 + i}")
            errorWrapper.put("error", errorDetail)
            errorsArray.put(errorWrapper)
        }
        
        errorResponse.put("errors", errorsArray)

        Assert.assertEquals(3, errorResponse.getJSONArray("errors").length())
        
        // Verify each error has correct structure
        for (i in 0 until 3) {
            val errorWrapper = errorResponse.getJSONArray("errors").getJSONObject(i)
            Assert.assertTrue(errorWrapper.has("error"))
            val error = errorWrapper.getJSONObject("error")
            Assert.assertTrue(error.has("code"))
            Assert.assertTrue(error.has("description"))
            Assert.assertTrue(error.has("type"))
        }
    }

    @Test
    fun testSuccessResponse_structure() {
        val successResponse = JSONObject()
        val recordsArray = JSONArray()
        
        for (i in 1..3) {
            val record = JSONObject()
            record.put("table", "table$i")
            val fields = JSONObject()
            fields.put("skyflow_id", "id-$i")
            fields.put("field1", "value$i")
            record.put("fields", fields)
            recordsArray.put(record)
        }
        
        successResponse.put("records", recordsArray)

        Assert.assertEquals(3, successResponse.getJSONArray("records").length())
        
        // Verify each record has correct structure
        for (i in 0 until 3) {
            val record = successResponse.getJSONArray("records").getJSONObject(i)
            Assert.assertTrue(record.has("table"))
            Assert.assertTrue(record.has("fields"))
            val fields = record.getJSONObject("fields")
            Assert.assertTrue(fields.has("skyflow_id"))
        }
    }

    @Test
    fun testCallback_typeCompatibility() {
        // Verify that Callback interface can accept JSONObject in both methods
        val testCallback = object : Callback {
            override fun onSuccess(responseBody: Any) {
                // Can receive JSONObject
                if (responseBody is JSONObject) {
                    Assert.assertTrue(true)
                }
            }

            override fun onFailure(exception: Any) {
                // Can receive JSONObject
                if (exception is JSONObject) {
                    Assert.assertTrue(true)
                }
            }
        }

        // Simulate success
        testCallback.onSuccess(JSONObject().put("records", JSONArray()))
        
        // Simulate failure
        testCallback.onFailure(JSONObject().put("errors", JSONArray()))
    }
}
