/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package com.Skyflow

import Skyflow.*
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import junit.framework.Assert.assertEquals
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test

class GetTest {
    lateinit var skyflow : Client
    lateinit var context: Context

    @Before
    fun setup() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098d2c",
            "https://vaulturl.com",
            DemoTokenProvider()
        )
        skyflow = Client(configuration)
        context = ApplicationProvider.getApplicationContext<Context>()
    }



    @Test
    fun testEmptyVaultID()
    {
        val skyflowConfiguration = Skyflow.Configuration(
           "",
            "https://vaulturl.com",
            DemoTokenProvider()
        )
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "895630c8-cb87-4876-8df5-0a785ebfcdda")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        skyflowClient.detokenize(records = revealRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                assertEquals(skyflowError.getErrorMessage(),getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testEmptyVaultURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "",
            DemoTokenProvider()
        )
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "895630c8-cb87-4876-8df5-0a785ebfcdda")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        skyflowClient.detokenize(records = revealRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                assertEquals(skyflowError.getErrorMessage(),getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidVaultURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "http://vault.url.com",
            DemoTokenProvider()
        )
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "895630c8-cb87-4876-8df5-0a785ebfcdda")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)
         revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        skyflowClient.detokenize(records = revealRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL,params = arrayOf(skyflowConfiguration.vaultURL))
                assertEquals(skyflowError.getErrorMessage(),getErrorMessage(exception as JSONObject))
            }

        })
    }



    @Test
    fun testMissingToken()
    {

        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        //recordObj.put("token", "")
        recordObj.put("redaction", " RedactionType.PLAIN_TEXT")
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        skyflow.detokenize(records = revealRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TOKEN)
                assertEquals(skyflowError.getErrorMessage(),getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyToken()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "",
            DemoTokenProvider()
        )
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "")
        recordObj.put("redaction", " RedactionType.PLAIN_TEXT")
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        skyflowClient.detokenize(records = revealRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                assertEquals(skyflowError.getErrorMessage(),getErrorMessage(exception as JSONObject))
            }

        })
    }



    @Test
    fun testMissingRecords()
    {

        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "1234")
        recordObj.put("redaction", " RedactionType.PLAIN_TEXT")
        revealRecordsArray.put(recordObj)
       // revealRecords.put("records", revealRecordsArray)
        skyflow.detokenize(records = revealRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND)
                assertEquals(skyflowError.getErrorMessage(),getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidRecordsType()
    {

        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "1234")
        recordObj.put("redaction", " RedactionType.PLAIN_TEXT")
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", JSONObject())
        skyflow.detokenize(records = revealRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORDS)
                assertEquals(skyflowError.getErrorMessage(),getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyRecords()
    {

        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "123")
        recordObj.put("redaction", " RedactionType.PLAIN_TEXT")
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", JSONArray())
        skyflow.detokenize(records = revealRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_RECORDS)
                assertEquals(skyflowError.getErrorMessage(),getErrorMessage(exception as JSONObject))
            }

        })
    }

    fun getErrorMessage(error:JSONObject) : String
    {
        val errors = error.getJSONArray("errors")
        val skyflowError = errors.getJSONObject(0).get("error") as SkyflowError
        return skyflowError.getErrorMessage()
    }
}