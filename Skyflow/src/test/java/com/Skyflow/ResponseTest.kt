/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package com.Skyflow

import Skyflow.*
import Skyflow.Callback
import Skyflow.collect.client.CollectAPICallback
import Skyflow.core.APIClient
import Skyflow.core.ConnectionApiCallback
import Skyflow.reveal.GetByIdRecord
import Skyflow.reveal.RevealApiCallback
import Skyflow.reveal.RevealByIdCallback
import Skyflow.reveal.RevealRequestRecord
import junit.framework.Assert.assertEquals

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ResponseTest {


    lateinit var skyflow : Client
    lateinit var logLevel: LogLevel
    lateinit var request: Request
    internal lateinit var apiClient: APIClient
    @Before
    fun setup()
    {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098d2c09",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        skyflow = Client(configuration)
        logLevel = LogLevel.ERROR
        request = Request
                .Builder()
                .url("https://www.url.com")
                .build()
        apiClient = APIClient("b359c43f1b84f098d2c09193","https://vaulturl.com/v1/vaults",AccessTokenProvider(),
            LogLevel.ERROR)

    }
    @Test
    fun testVerifyResponseNotSuccessForCollectCallback() //fail
    {

        val records = JSONObject()
        val collectAPICallback = CollectAPICallback(apiClient,records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                val expectedError = SkyflowError(SkyflowErrorCode.SERVER_ERROR, params =  arrayOf("jwt expired")).getInternalErrorMessage()
                assertEquals(expectedError,(exception as SkyflowError).getInternalErrorMessage())
            }
        },InsertOptions(),logLevel)
        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(),"jwt expired")
        val res = Response.Builder().code(401).message("not found").protocol(Protocol.HTTP_2).request(request).body(responseBody).build()
        collectAPICallback.verifyResponse(res)
    }
    @Test
    fun testVerifyResponseSuccess() //failed because response is not json object
    {
        val apiClient = APIClient("b359c43f1b84f098d2c09193","https://vaulturl.com/v1/vaults",AccessTokenProvider(),
            LogLevel.ERROR)
        val records = JSONObject()
        val collectAPICallback = CollectAPICallback(apiClient,records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val expectedError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, params =  arrayOf("Value not of type java.lang.String cannot be converted to JSONObject")).getInternalErrorMessage()
                assertEquals(expectedError,(exception as SkyflowError).getInternalErrorMessage())
            }
        }, InsertOptions(), LogLevel.ERROR)
        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(),"not json object")
        val res = Response.Builder().code(200).message("not found").protocol(Protocol.HTTP_2).request(request).body(responseBody).build()
        collectAPICallback.verifyResponse(res)
    }

    @Test
    fun testOnSuccessFailedForCollectCallback() //failed because fields is not json object
    {
        val apiClient = APIClient("b359c43f1b84f098d2c09193","https://vaulturl.com/v1/vaults",AccessTokenProvider(),
            LogLevel.ERROR)
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        record.put("fields", "fields")
        recordsArray.put(record)
        records.put("records", recordsArray)
        val collectAPICallback = CollectAPICallback(apiClient,records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val expectedError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, params =  arrayOf("Value fields at fields of type java.lang.String cannot be converted to JSONObject")).getInternalErrorMessage()
                assertEquals(expectedError,(exception as SkyflowError).getInternalErrorMessage())
            }
        }, InsertOptions(), LogLevel.ERROR)
        collectAPICallback.onSuccess("token")
    }
    @Test
    fun testOnSuccessFailedForCollectCallback1() //records missing
    {
        val apiClient = APIClient("b359c43f1b84f098d2c09193","https://vaulturl.com/v1/vaults",AccessTokenProvider(),
            LogLevel.ERROR)
        val records = JSONObject()
        val collectAPICallback = CollectAPICallback(apiClient,records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val expectedError = SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND).getInternalErrorMessage()
                assertEquals(expectedError,(exception as SkyflowError).getInternalErrorMessage())
            }
        }, InsertOptions(), LogLevel.ERROR)
        collectAPICallback.onSuccess("token")
    }

    @Test
    fun testVerifyResponseNotSuccessForRevealCallback()
    {
        val apiClient = APIClient("b359c43f1b84f098d2c09193","https://vaulturl.com/v1/vaults",AccessTokenProvider(),
            LogLevel.ERROR)
        val records = mutableListOf<RevealRequestRecord>()
        records.add(RevealRequestRecord("token"))
        val revealAPICallback = RevealApiCallback(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                val expectedError = SkyflowError(SkyflowErrorCode.SERVER_ERROR, params =  arrayOf("jwt expired")).getInternalErrorMessage()
                assertEquals(expectedError,(exception as SkyflowError).getInternalErrorMessage())
            }
        }, apiClient,records)
        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(),"jwt expired")
        val res = Response.Builder().code(401).message("not found").protocol(Protocol.HTTP_2).request(request).body(responseBody).build()
        revealAPICallback.verifyResponse(res,records[0])

    }

    @Test
    fun testVerifyResponseNotSuccessForRevealCallback1()
    {
        val apiClient = APIClient("b359c43f1b84f098d2c09193","https://vaulturl.com/v1/vaults",AccessTokenProvider(),
            LogLevel.ERROR)
        val records = mutableListOf<RevealRequestRecord>()
        records.add(RevealRequestRecord("token"))
        val revealApiCallback = RevealApiCallback(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                val expectedError = SkyflowError(SkyflowErrorCode.SERVER_ERROR, params =  arrayOf("not found")).getInternalErrorMessage()
                assertEquals(expectedError,(exception as SkyflowError).getInternalErrorMessage())
            }
        }, apiClient,records)
        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(),"{ error : {message:'not found' }}")
        val res = Response.Builder().code(401).message("not found").protocol(Protocol.HTTP_2).request(request).body(responseBody).build()
        revealApiCallback.verifyResponse(res,records[0])

    }
    @Test
    fun testVerifyResponseSuccessForRevealCallback()
    {
        val apiClient = APIClient("b359c43f1b84f098d2c09193","https://vaulturl.com/v1/vaults",AccessTokenProvider(),
            LogLevel.ERROR)
        val records = mutableListOf<RevealRequestRecord>()
        records.add(RevealRequestRecord("token"))
        val revealApiCallback = RevealApiCallback(ApiCallback(), apiClient,records)
        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(),"{'records': [ ] }")
        val res = Response.Builder().code(200).message("not found").protocol(Protocol.HTTP_2).request(request).body(responseBody).build()
        revealApiCallback.verifyResponse(res,records[0])
    }

    @Test
    fun testVerifyResponseNotSuccessForRevealByIdCallback()
    {
        val apiClient = APIClient("b359c43f1b84f098d2c09193","https://vaulturl.com/v1/vaults",AccessTokenProvider(),
            LogLevel.ERROR)
        val records = mutableListOf<GetByIdRecord>()
        records.add(GetByIdRecord(ArrayList(),"table","redaction"))
        val revealByIdCallback = RevealByIdCallback(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                val expectedError = SkyflowError(SkyflowErrorCode.SERVER_ERROR, params =  arrayOf("jwt expired")).getInternalErrorMessage()
                assertEquals(expectedError,(exception as SkyflowError).getInternalErrorMessage())
            }
        }, apiClient,records)
        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(),"jwt expired")
        val res = Response.Builder().code(401).message("not found").protocol(Protocol.HTTP_2).request(request).body(responseBody).build()
        revealByIdCallback.verifyResponse(res,records[0])
    }

    @Test
    fun testVerifyResponseNotSuccessForRevealByIdCallback1()
    {
        val apiClient = APIClient("b359c43f1b84f098d2c09193","https://vaulturl.com/v1/vaults",AccessTokenProvider(),
            LogLevel.ERROR)
        val records = mutableListOf<GetByIdRecord>()
        records.add(GetByIdRecord(ArrayList(),"table","redaction"))
        val revealByIdCallback = RevealByIdCallback(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                val expectedError = SkyflowError(SkyflowErrorCode.SERVER_ERROR, params =  arrayOf("not found")).getInternalErrorMessage()
                assertEquals(expectedError,(exception as SkyflowError).getInternalErrorMessage())
            }
        }, apiClient,records)
        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(),"{ error : {message:'not found' }}")
        val res = Response.Builder().code(401).message("not found").protocol(Protocol.HTTP_2).request(request).body(responseBody).build()
        revealByIdCallback.verifyResponse(res,records[0])
    }

    @Test
    fun testBuildRequestNotSuccessForRevealByIdCallback() //invalid vault url
    {
        val apiClient = APIClient("b359c43f1b84f098d2c09193","vaulturl.com/v1/vaults",AccessTokenProvider(),
            LogLevel.ERROR)
        val records = mutableListOf<GetByIdRecord>()
        records.add(GetByIdRecord(ArrayList(),"table","redaction"))
        val revealByIdAPICallback = RevealByIdCallback(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                val expectedError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL, params =  arrayOf("vaulturl.com/v1/vaults")).getInternalErrorMessage()
                assertEquals(expectedError,UnitTests.getErrorMessage(exception as JSONObject))
            }
        }, apiClient,records)
        revealByIdAPICallback.onSuccess("token")
    }
    @Test
    fun testVerifyResponseSuccessForRevealByIdCallback() //success but fields is not json object
    {
        val apiClient = APIClient("b359c43f1b84f098d2c09193","https://vaulturl.com/v1/vaults",AccessTokenProvider(),
            LogLevel.ERROR)
        val records = mutableListOf<GetByIdRecord>()
        records.add(GetByIdRecord(ArrayList(),"table","redaction"))
        val revealByIdCallback = RevealByIdCallback(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                val expectedError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, params =  arrayOf("Value not json at fields of type java.lang.String cannot be converted to JSONObject")).getInternalErrorMessage()
                assertEquals(expectedError,UnitTests.getErrorMessage(exception as JSONObject))
            }
        }, apiClient,records)
        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(),"{'records': [ { 'fields':{ 'name':'xyz'} } , { 'fields': 'not json' } ] }")
        val res = Response.Builder().code(200).message("not found").protocol(Protocol.HTTP_2).request(request).body(responseBody).build()
        revealByIdCallback.verifyResponse(res,records[0])
    }


    @Test
    fun testVerifyResponseNotSuccessForConnectionCallback()
    {
        val connectionConfiguration = ConnectionConfig("https://www.google.com",
            RequestMethod.POST)
        val callback = ConnectionApiCallback(connectionConfiguration,
                object : Callback {
                override fun onSuccess(responseBody: Any) {
                }
                override fun onFailure(exception: Any) {
                    val expectedError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, params =  arrayOf("{error:{message:'not found'}}")).getInternalErrorMessage().trim()
                    assertEquals(expectedError,UnitTests.getErrorMessage(exception as JSONObject).trim())
                }
            }, logLevel = logLevel,skyflow)
        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(),"{error:{message:'not found'}}")
        val res = Response.Builder().code(401).message("not found").protocol(Protocol.HTTP_2).request(request).body(responseBody).build()
        callback.verifyResponse(res)

    }

    @Test
    fun testVerifyResponseTextSuccessForConnectionCallback() // response from api cannot converted to json
    {
        val connectionConfiguration = ConnectionConfig("https://www.google.com",
            RequestMethod.POST)
        val callback = ConnectionApiCallback(connectionConfiguration,
            object : Callback {
                override fun onSuccess(responseBody: Any) {
                }
                override fun onFailure(exception: Any) {
                    val expectedError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, params =  arrayOf("Value text of type java.lang.String cannot be converted to JSONObject")).getInternalErrorMessage().trim()
                    assertEquals(expectedError,UnitTests.getErrorMessage(exception as JSONObject).trim())
                }
            }, logLevel = logLevel,skyflow)
        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(),"text response")
        val res = Response.Builder().code(200).message("not found").protocol(Protocol.HTTP_2).request(request).body(responseBody).build()
        callback.verifyResponse(res)

    }

    @Test
    fun testVerifyResponseSuccessForConnectionCallback()
    {
        val connectionConfiguration = ConnectionConfig("https://www.google.com",
            RequestMethod.POST)
        val callback = ConnectionApiCallback(connectionConfiguration,ApiCallback(), logLevel = logLevel,skyflow)
        val responseBody = ResponseBody.create("application/json".toMediaTypeOrNull(),"{ records : {name:'xyz' }}")
        val res = Response.Builder().code(200).message("not found").protocol(Protocol.HTTP_2).request(request).body(responseBody).build()
        callback.verifyResponse(res)

    }
}