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

class GetByIdsTest {
    lateinit var skyflowClient : Client
    lateinit var context: Context

    @Before
    fun setup() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098d2c09",
            "https://sb1.area51.vault.skyflowapis.tech",
            DemoTokenProvider()
        )
        skyflowClient = Client(configuration)
        context = ApplicationProvider.getApplicationContext<Context>()
    }


    @Test
    fun testEmptyVaultID()
    {
        val skyflowConfiguration =  Configuration(
            "",
            "http://sb1.area51.vault.skyflowapis.tech",
            DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        // records.put("records",recordsArray)
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyVaultURL()
    {
        val skyflowConfiguration =  Configuration(
            "b359c43f1b844ff4bea0f098d2c0",
            "",
            DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        // records.put("records",recordsArray)
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidVaultURL()
    {
        val skyflowConfiguration =  Configuration(
            "b359c43f1b844ff4bea0f098d2",
            "http://sb1.area51.vault.skyflowapis.tech",
            DemoTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        // records.put("records",recordsArray)
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL)
                skyflowError.setErrorResponse(skyflowConfiguration.vaultURL)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyTable()
    {
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","")
        record.put("redaction",RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                Log.d("exp",exception.toString())
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TABLE_NAME)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyRedaction()
    {
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction","")

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_REDACTION_VALUE)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidRedaction()
    {
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction","something")

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
         records.put("records",recordsArray)
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_REDACTION_TYPE)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidTableType()
    {
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table",JSONObject())
        record.put("redaction",RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun testMissingTable()
    {
        val recordsArray = JSONArray()
        val record = JSONObject()
       // record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.TABLE_KEY_ERROR)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testMissingRedaction()
    {
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        //record.put("redaction",RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
         records.put("records",recordsArray)
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.REDACTION_KEY_ERROR)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testMissingRecords()
    {

        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
       // records.put("records",recordsArray)
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidRecordsType()
    {
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",JSONObject())
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORDS)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyRecords()
    {
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",JSONArray())
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_RECORDS)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testMissingIds()
    {
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
       // record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_IDS)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyIds()
    {
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
         records.put("records",recordsArray)
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_RECORD_IDS)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidIds()
    {
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

        record.put("ids",JSONObject())
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        skyflowClient.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORD_IDS)
                assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    fun getErrorMessage(error: JSONObject) : String
    {
        val errors = error.getJSONArray("errors")
        val skyflowError = errors.getJSONObject(0).get("error") as SkyflowError
        return skyflowError.getErrorMessage()
    }
}