package com.Skyflow

import Skyflow.*
import Skyflow.utils.Utils
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UtilsTest {

    @Test
    fun testConstructErrorResponse_withCodeAndDescription() {
        val code = 400
        val description = "Invalid input"
        
        val result = Utils.constructErrorResponse(code, description)
        
        Assert.assertTrue(result.has("errors"))
        val errors = result.getJSONArray("errors")
        Assert.assertEquals(1, errors.length())
        
        val errorObj = errors.getJSONObject(0)
        Assert.assertTrue(errorObj.has("error"))
        
        val error = errorObj.getJSONObject("error")
        Assert.assertEquals(code, error.getInt("code"))
        Assert.assertEquals(description, error.getString("description"))
        Assert.assertEquals("$code", error.getString("type"))
    }

    @Test
    fun testConstructErrorResponse_withException() {
        val exception = Exception("Test exception message")
        
        val result = Utils.constructErrorResponse(exception)
        
        Assert.assertTrue(result.has("errors"))
        val errors = result.getJSONArray("errors")
        Assert.assertEquals(1, errors.length())
        
        val errorObj = errors.getJSONObject(0)
        val error = errorObj.getJSONObject("error")
        Assert.assertEquals(400, error.getInt("code"))
        Assert.assertEquals("Test exception message", error.getString("description"))
    }

    @Test
    fun testConstructErrorResponse_withSkyflowError() {
        val skyflowError = SkyflowError(
            SkyflowErrorCode.INVALID_INPUT,
            params = arrayOf("test field")
        )
        skyflowError.setErrorCode(422)
        
        val result = Utils.constructErrorResponse(skyflowError)
        
        Assert.assertTrue(result.has("errors"))
        val errors = result.getJSONArray("errors")
        Assert.assertEquals(1, errors.length())
        
        val errorObj = errors.getJSONObject(0)
        val error = errorObj.getJSONObject("error")
        Assert.assertEquals(422, error.getInt("code"))
    }

    @Test
    fun testConstructErrorResponse_withCustomCode() {
        val exception = Exception("Server error")
        
        val result = Utils.constructErrorResponse(exception, 500)
        
        val errors = result.getJSONArray("errors")
        val errorObj = errors.getJSONObject(0)
        val error = errorObj.getJSONObject("error")
        Assert.assertEquals(500, error.getInt("code"))
        Assert.assertEquals("Server error", error.getString("description"))
    }

    @Test
    fun testConstructErrorObject() {
        val code = 404
        val description = "Record not found"
        
        val result = Utils.constructErrorObject(code, description)
        
        Assert.assertTrue(result.has("error"))
        val error = result.getJSONObject("error")
        Assert.assertEquals(code, error.getInt("code"))
        Assert.assertEquals(description, error.getString("description"))
        Assert.assertEquals("$code", error.getString("type"))
    }

    @Test
    fun testConstructErrorObject_multipleErrors() {
        val errorsArray = JSONArray()
        
        errorsArray.put(Utils.constructErrorObject(400, "Invalid field 1"))
        errorsArray.put(Utils.constructErrorObject(400, "Invalid field 2"))
        errorsArray.put(Utils.constructErrorObject(500, "Server error"))
        
        val response = JSONObject()
        response.put("errors", errorsArray)
        
        Assert.assertEquals(3, errorsArray.length())
        
        val error1 = errorsArray.getJSONObject(0).getJSONObject("error")
        Assert.assertEquals(400, error1.getInt("code"))
        Assert.assertEquals("Invalid field 1", error1.getString("description"))
        
        val error2 = errorsArray.getJSONObject(1).getJSONObject("error")
        Assert.assertEquals(400, error2.getInt("code"))
        Assert.assertEquals("Invalid field 2", error2.getString("description"))
        
        val error3 = errorsArray.getJSONObject(2).getJSONObject("error")
        Assert.assertEquals(500, error3.getInt("code"))
        Assert.assertEquals("Server error", error3.getString("description"))
    }

    @Test
    fun testConstructError_legacyMethod() {
        val exception = Exception("Legacy error")
        
        val result = Utils.constructError(exception)
        
        Assert.assertTrue(result.has("errors"))
        val errors = result.getJSONArray("errors")
        Assert.assertTrue(errors.length() > 0)
    }

    @Test
    fun testConstructError_withCustomCode() {
        val exception = Exception("Custom code error")
        
        val result = Utils.constructError(exception, 503)
        
        val errors = result.getJSONArray("errors")
        val errorObj = errors.getJSONObject(0)
        // constructError returns OLD format with SkyflowError object, not JSONObject
        val error = errorObj.get("error") as SkyflowError
        
        // The SkyflowError inside has setErrorCode called, check the code
        Assert.assertEquals(503, error.getErrorcode())
        Assert.assertEquals("Custom code error", error.getInternalErrorMessage())
    }

    @Test
    fun testErrorResponseFormat_consistency() {
        // Test that all error construction methods produce consistent format
        val response1 = Utils.constructErrorResponse(400, "Error 1")
        val response2 = Utils.constructErrorResponse(Exception("Error 2"), 400)
        
        // Both should have "errors" array
        Assert.assertTrue(response1.has("errors"))
        Assert.assertTrue(response2.has("errors"))
        
        // Both errors arrays should have the same structure
        val error1 = response1.getJSONArray("errors").getJSONObject(0).getJSONObject("error")
        val error2 = response2.getJSONArray("errors").getJSONObject(0).getJSONObject("error")
        
        Assert.assertTrue(error1.has("code"))
        Assert.assertTrue(error1.has("description"))
        Assert.assertTrue(error1.has("type"))
        
        Assert.assertTrue(error2.has("code"))
        Assert.assertTrue(error2.has("description"))
        Assert.assertTrue(error2.has("type"))
    }
}
