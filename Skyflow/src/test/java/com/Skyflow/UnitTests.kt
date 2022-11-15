package com.Skyflow

import Skyflow.*
import Skyflow.collect.elements.utils.*
import Skyflow.core.APIClient
import Skyflow.core.JWTUtils
import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.elements.state.StateforText
import Skyflow.utils.EventName
import Skyflow.utils.Utils
import android.app.Activity
import android.view.ViewGroup
import android.widget.CheckBox
import com.skyflow_android.R
import io.mockk.MockKAnnotations
import junit.framework.Assert
import junit.framework.TestCase.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class UnitTests {

    lateinit var skyflow : Client
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity
    lateinit var layoutParams : ViewGroup.LayoutParams
    @Before
    fun setup()
    {
        MockKAnnotations.init(this)
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        skyflow = Client(configuration)
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
    }




    @Test
    fun testValidUrl()
    {
        val url = "https://www.google.com"
        assertEquals(Utils.checkUrl(url),true)
    }

    @Test
    fun testInvalidUrl()
    {
        val url = "http://www.google.com"
        assertEquals(Utils.checkUrl(url),false)
    }


    @Test
    fun testCopyJSON()
    {
        val old = JSONObject()
        old.put("cardNumber","1234")
        val new = JSONObject()
        Utils.copyJSON(old,new)
        assertEquals(old.getString("cardNumber"),new.getString("cardNumber"))
    }



    //detokenize

    @Test
    fun testEmptyTokenForDetokenize()
    {
        val apiClient = APIClient("1234","https://vaulturl.com",AccessTokenProvider(),LogLevel.ERROR)
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "")
        recordObj.put("redaction", RedactionType.MASKED)
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        apiClient.get(revealRecords, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

        revealRecords.put("records","")
        apiClient.get(revealRecords, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_RECORDS)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
        revealRecords.put("records","{}")
        apiClient.get(revealRecords, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORDS)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testMissingTokenForDetokenize()
    {
        val apiClient = APIClient("1234","https://vaulturl.com",AccessTokenProvider(),LogLevel.ERROR)
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
       // recordObj.put("token", "")
        recordObj.put("redaction", RedactionType.DEFAULT)
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        apiClient.get(revealRecords, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TOKEN)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testRecordsForDetokenize()
    {
        val apiClient = APIClient("1234","https://vaulturl.com",AccessTokenProvider(),LogLevel.ERROR)
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "1234-134")
         recordObj.put("redaction", RedactionType.REDACTED)
        revealRecordsArray.put(recordObj)
       // revealRecords.put("records", revealRecordsArray)
        apiClient.get(revealRecords, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testInvalidRecordsForDetokenize()
    {
        val apiClient = APIClient("1234","https://vaulturl.com",AccessTokenProvider(),LogLevel.ERROR)
        val revealRecords = JSONObject()
        revealRecords.put("records", JSONObject())
        apiClient.get(revealRecords, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORDS)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testEmptyRecordsForDetokenize()
    {
        val apiClient = APIClient("1234","https://vaulturl.com",AccessTokenProvider(),LogLevel.ERROR)
        val revealRecords = JSONObject()
        revealRecords.put("records", JSONArray())
        apiClient.get(revealRecords, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_RECORDS)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }



    @Test
    fun testEmptyVaultID()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "895630c8-cb87-4876-8df5-0a785ebfcdda")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        val skyflowClient = Client(skyflowConfiguration)
        skyflowClient.detokenize(records = revealRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testEmptyVaultURL2()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "",
            AccessTokenProvider()
        )
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "895630c8-cb87-4876-8df5-0a785ebfcdda")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        val skyflowClient = Client(skyflowConfiguration)
        skyflowClient.detokenize(records = revealRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidVaultURL2()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "http://www.goog.com",
            AccessTokenProvider()
        )
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "895630c8-cb87-4876-8df5-0a785ebfcdda")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        val skyflowClient = Client(skyflowConfiguration)
        skyflowClient.detokenize(records = revealRecords, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL,params = arrayOf(skyflowConfiguration.vaultURL))
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testValidRequestForDetokenize()
    {
        val apiClient = APIClient("1234","https://vaulturl.com",AccessTokenProvider(),LogLevel.ERROR)
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
         recordObj.put("token", "")
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        apiClient.get(revealRecords, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                assertEquals(SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID).getInternalErrorMessage(),getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testValidRequestForDetokenizeInClient()
    {
        val configuration = Configuration(
            "12344",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val revealRecords = JSONObject()
        client.detokenize(revealRecords, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                assertEquals(SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND).getInternalErrorMessage(),getErrorMessage(exception as JSONObject))
            }

        })

    }

    //end detokenize




    //insert  function is testConstructBatchRequestBody

    @Test
    fun testMissingRecords()
    {
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        val fields = JSONObject()
        fields.put("fullname", "san")
        fields.put("card_number", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
       // records.put("records", recordsArray)

        try {
            Utils.constructBatchRequestBody(records, InsertOptions(),LogLevel.ERROR)
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND)
            assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }
    }

    @Test
    fun testInvalidRecordsType()
    {
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        val fields = JSONObject()
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", JSONObject())
        try {
            Utils.constructBatchRequestBody(records, InsertOptions(),LogLevel.ERROR)
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORDS)
            assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }
    }

    @Test
    fun testMissingTableForInsert()
    {
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
       // record.put("table", "")
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)

        try {
           val x = Utils.constructBatchRequestBody(records, InsertOptions(),LogLevel.ERROR)
            assertEquals(x.toString().trim(),JSONObject().toString().trim())
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError( SkyflowErrorCode.MISSING_TABLE_KEY, params = arrayOf())
            assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }

    }

    @Test
    fun testInvalidTableTypeForInsert()
    {
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        record.put("table", JSONObject())
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        try {
            val x = Utils.constructBatchRequestBody(records, InsertOptions(),LogLevel.ERROR)
            assertEquals(x.toString().trim(),JSONObject().toString().trim())
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError( SkyflowErrorCode.INVALID_TABLE_NAME, params = arrayOf())
            assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }

    }

    @Test
    fun testEmptyTableForInsert()
    {
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        record.put("table", "")
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        try {
            val x = Utils.constructBatchRequestBody(records, InsertOptions(),LogLevel.ERROR)
            assertEquals(x.toString().trim(),JSONObject().toString().trim())
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError( SkyflowErrorCode.EMPTY_TABLE_KEY, params = arrayOf())
            assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }

    }

    @Test
    fun testEmptyColumnName()
    {
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        record.put("table", "cards")
        fields.put("", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        try {
            val x = Utils.constructBatchRequestBody(records, InsertOptions(),LogLevel.ERROR)
            assertEquals(x.toString().trim(),JSONObject().toString().trim())
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError( SkyflowErrorCode.EMPTY_COLUMN_KEY, params = arrayOf())
            assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }

    }
    //end insert

    @Test
    fun testCheckElement() //valid
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111")
        activity.addContentView(card_number,layoutParams)
        card_number.inputField.setText("4111 1111 1111 1111")
        val isValid = Utils.checkElement(card_number,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
            }

        },LogLevel.ERROR)

        assertTrue(isValid)
    }

    @Test
    fun testCheckElementForInvalid() //invalid
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        activity.addContentView(card_number,layoutParams)
        card_number!!.inputField.setText("4111")
        val isValid = Utils.checkElement(card_number,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
            }

        },LogLevel.ERROR)

        assertTrue(!isValid)
    }

    @Test
    fun testCheckElementRequiredForInvalid() //invalid
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        activity.addContentView(card_number,layoutParams)
        card_number!!.inputField.setText("")
        val isValid = Utils.checkElement(card_number,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
            }

        },LogLevel.ERROR)

        assertTrue(!isValid)
    }


    @Test
    fun testBearerTokenFunction() //success
    {
        val client = APIClient("1234","https://vaulturl.com",APITokenProviderForSuccess(),LogLevel.ERROR,"")
        client.getAccessToken(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
                    assertEquals("Bearer token",responseBody.toString())
            }

            override fun onFailure(exception: Any) {

            }

        })
    }
    //client

    @Test
    fun testInsertEmptyVaultID()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val records = JSONObject()
        val skyflowClient = Client(skyflowConfiguration)
        skyflowClient.insert(records = records, InsertOptions(), object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    (exception as SkyflowError).getInternalErrorMessage())
            }

        })

    }

    @Test
    fun testInsertEmptyVaultURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "",
            AccessTokenProvider()
        )
        val records = JSONObject()
        val skyflowClient = Client(skyflowConfiguration)
        skyflowClient.insert(records = records, InsertOptions(), object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    (exception as SkyflowError).getInternalErrorMessage())
            }

        })
    }

    @Test
    fun testInsertInvalidVaultURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "http://www.goog.com",
            AccessTokenProvider()
        )
        val records = JSONObject()
        val skyflowClient = Client(skyflowConfiguration)
        skyflowClient.insert(records = records, InsertOptions(), object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL,params = arrayOf(skyflowConfiguration.vaultURL))
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    (exception as SkyflowError).getInternalErrorMessage())
            }

        })
    }

    @Test
    fun testValidRequestForInsert()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "vault_id",
            "https://www.google.com",
            AccessTokenProvider()
        )
        val records = JSONObject()
        val skyflowClient = Client(skyflowConfiguration)
        skyflowClient.insert(records = records, InsertOptions(), object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                //valid request
            }

        })
    }


    //end client


    //api client

    @Test
    fun testPostMethod()
    {
        val apiClient = APIClient("1234","https://vaulturl.com",AccessTokenProvider(),LogLevel.ERROR)
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        record.put("table","")
        fields.put("card_number","123")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        apiClient.post(records, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TABLE_KEY)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    (exception as SkyflowError).getInternalErrorMessage())
            }

        }, InsertOptions())
    }

    @Test
    fun testValidRequestForPostMethod()
    {
        val apiClient = APIClient("1234","https://vaulturl.com",AccessTokenProvider(),LogLevel.ERROR)
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        record.put("table","cards")
        fields.put("card_number","123")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        apiClient.post(records, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_EMPTY_TABLE_NAME)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    (exception as SkyflowError).getInternalErrorMessage())
            }

        }, InsertOptions())
    }


    @Test
    fun testInvokeConnection()
    {
        val apiClient = APIClient("1234","https://vaulturl.com",AccessTokenProvider(),LogLevel.ERROR)
        val requestRecord = JSONObject()
        requestRecord.put("xxx",CheckBox(activity))
        val url = "BuildConfig.GATEWAY_CVV_GEN_URL " // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,methodName = RequestMethod.POST,requestBody = requestRecord)

        apiClient.invokeConnection(connectionRequestBody, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    (exception as SkyflowError).getInternalErrorMessage())
            }

        }, skyflow)

    }

    @Test
    fun testValidForInvokeConnection()
    {
        val apiClient = APIClient("1234","https://vaulturl.com",AccessTokenProvider(),LogLevel.ERROR)
        val requestRecord = JSONObject()
        requestRecord.put("card_number","41111")
        val url = "BuildConfig.GATEWAY_CVV_GEN_URL " // eg:  url.../{cardNumber}/...
        val connectionRequestBody = ConnectionConfig(connectionURL = url,methodName = RequestMethod.POST,requestBody = requestRecord)

        apiClient.invokeConnection(connectionRequestBody, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                    // valid
            }

        }, skyflow)

    }
    //end api client


    @Test
    fun testValidateFunction()
    {
        val element = Element(activity)
        assertEquals(element.getValue(),"")
        assertEquals(element.validate(), "")
    }
    @Test
    fun testStateClass()
    {
        val state= State("first_name",true)
        assertNotNull(state.show())
        assertEquals(state.getInternalState().get("columnName"),"first_name")

        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.INPUT_FIELD,placeholder = "card number"
        )
        val first_name = container.create(activity,collectInput) as TextField
        activity.addContentView(first_name,layoutParams)
        val stateforText = StateforText(first_name)
        assertTrue(stateforText.isEmpty)
        assertTrue(stateforText.isValid)
        assertFalse(stateforText.isFocused)
        assertEquals(0,stateforText.inputLength)
        assertEquals(SkyflowElementType.INPUT_FIELD,stateforText.fieldType)
    }
    @Test
    fun testLoggerClass()
    {
        val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
        val logger = Logger()
        Logger.debug("debug",skyflowError.getInternalErrorMessage(),LogLevel.DEBUG)
        Logger.error("error",skyflowError.getInternalErrorMessage(),LogLevel.DEBUG)
        Logger.info("info",skyflowError.getInternalErrorMessage(),LogLevel.DEBUG)
        Logger.warn("warn",skyflowError.getInternalErrorMessage(),LogLevel.DEBUG)
    }

    @Test
    fun testCardType()
    {
        val newCard = Card("new card","[123]", intArrayOf(12,13,14,15), intArrayOf(4,8,12,16),3,"cvv", R.drawable.ic_emptycard)
        val card = CardType.AMEX
        assertTrue(newCard.cardLength.contains(12))
        assertEquals(CardType.forCardNumber("4111111111111111"),CardType.VISA)
        assertEquals(CardType.forCardNumber("4111111111111111"),CardType.VISA)
    }

    @Test
    fun testSecurityCode()
    {
        val securityCode = SecurityCode.cid
        assertEquals(securityCode.rawValue,"cid")
    }

    @Test
    fun testDateValidator()
    {
        val date = DateValidator()
        assertTrue(date.isValid("10","28"))
        assertFalse(date.isValid("xx","12"))
        assertFalse(date.isValid("","22"))
        assertFalse(date.isValid("10",""))
        assertFalse(date.isValid("22","22"))
        assertFalse(date.isValid("10","12"))
        assertTrue(date.isValid("10","2032"))
        assertFalse(date.isValid("10","2011"))
        assertFalse(date.isValid("02","2021"))
        assertFalse(date.isValid("02","202222"))

    }

    @Test
    fun testVibrateHelper()
    {
       VibrationHelper.vibrate(activity,10)
    }
    //JWTUtils

    @Test
    fun testJwtUtils()
    {
        assertNotNull(JWTUtils.isExpired("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwczovL21hbmFnZS5za3lmbG93YXBpcy50ZWNoIiwiY2xpIjoiZWM3NzBlODQyOWNjNDNiM2JhMGI2NDk2MzA3M2EyZDMiLCJleHAiOjE2MzQ4MDkxOTcsImlhdCI6MTYzNDgwNTU5NywiaXNzIjoic2EtYXV0aEBtYW5hZ2Uuc2t5Zmxvd2FwaXMudGVjaCIsImp0aSI6InA0ZjA0YzMwYTczODQyMzM4OGQ2ZGNjYjE4Y2E4Nzg5Iiwic2NwIjpbImFjY291bnRzLnJlYWQiLCJ3b3Jrc3BhY2VzLnJlYWQiLCJ2YXVsdHMucmVhZCIsInZhdWx0VGVtcGxhdGVzLnZhbGlkYXRlIiwic3FsLnJlYWQiLCJyZWNvcmRzLnJlYWQiLCJyZWNvcmRzLmNyZWF0ZSIsInJlY29yZHMudXBkYXRlIiwicmVjb3Jkcy5kZWxldGUiLCJ3b3JrZmxvd3MucmVhZCIsIndvcmtmbG93UnVucy5jcmVhdGUiLCJ3b3JrZmxvd1J1bnMucmVhZCIsIndvcmtmbG93UnVucy51cGRhdGUiLCJhY2NvdW50cy5yZWFkIiwid29ya3NwYWNlcy5yZWFkIiwidmF1bHRzLnJlYWQiLCJ2YXVsdFRlbXBsYXRlcy52YWxpZGF0ZSIsInNxbC5yZWFkIiwicmVjb3Jkcy5yZWFkIiwid29ya2Zsb3dzLnJlYWQiLCJhY2NvdW50cy5yZWFkIiwid29ya3NwYWNlcy5yZWFkIiwidXNlcnMucmVhZCIsInNlcnZpY2VBY2NvdW50LnZhdWx0LmNyZWF0ZSIsInNlcnZpY2VBY2NvdW50LnJlYWQiLCJzZXJ2aWNlQWNjb3VudC51cGRhdGUiLCJzZXJ2aWNlQWNjb3VudC5kZWxldGUiLCJzZXJ2aWNlQWNjb3VudC5zdGF0dXMudXBkYXRlIiwidmF1bHRGdW5jdGlvbkNvbmZpZy52YXVsdC5jcmVhdGUiLCJ2YXVsdEZ1bmN0aW9uQ29uZmlnLnJlYWQiLCJ2YXVsdEZ1bmN0aW9uQ29uZmlnLnVwZGF0ZSIsInZhdWx0RnVuY3Rpb25Db25maWcuZGVsZXRlIiwidmF1bHRGdW5jdGlvbkNvbmZpZy5zdGF0dXMudXBkYXRlIiwic3FsU2VydmljZUFjY291bnQuY3JlYXRlIiwidmF1bHRzLnJlYWQiLCJ2YXVsdHMudXBkYXRlIiwidmF1bHRzLmRlbGV0ZSIsInZhdWx0cy5zdGF0dXMudXBkYXRlIiwia2V5LmNyZWF0ZSIsImtleS51cGRhdGUiLCJrZXkudmF1bHQudXBkYXRlIiwia2V5LnZhdWx0LnJlYWQiLCJ2YXVsdEludGVncmF0aW9ucy5jcmVhdGUiLCJ2YXVsdFRlbXBsYXRlcy52YWxpZGF0ZSIsInNxbC5yZWFkIiwicmVjb3Jkcy5yZWFkIiwicmVjb3Jkcy5jcmVhdGUiLCJyZWNvcmRzLnVwZGF0ZSIsInJlY29yZHMuZGVsZXRlIiwicm9sZXMudmF1bHQuY3JlYXRlIiwicm9sZXMudmF1bHQucmVhZCIsInJvbGVzLnZhdWx0LnVwZGF0ZSIsInJvbGVzLnZhdWx0LmRlbGV0ZSIsInJvbGVzLnZhdWx0Lm1lbWJlcnMucmVhZCIsInJvbGVzLnBvbGljeS5yZWFkIiwicm9sZXMudmF1bHRGdW5jdGlvbkNvbmZpZy5jcmVhdGUiLCJyb2xlcy52YXVsdEZ1bmN0aW9uQ29uZmlnLnJlYWQiLCJyb2xlcy52YXVsdEZ1bmN0aW9uQ29uZmlnLnVwZGF0ZSIsInJvbGVzLnZhdWx0RnVuY3Rpb25Db25maWcuZGVsZXRlIiwicm9sZXMudmF1bHRGdW5jdGlvbkNvbmZpZy5tZW1iZXJzLnJlYWQiLCJyb2xlcy5kZWZpbml0aW9ucy5yZWFkIiwicm9sZXMubWVtYmVyUm9sZXMucmVhZCIsInJvbGVzLm1lbWJlclBlcm1pc3Npb25zLnJlYWQiLCJ3b3JrZmxvd3MucmVhZCIsIndvcmtmbG93UnVucy5jcmVhdGUiLCJ3b3JrZmxvd1J1bnMucmVhZCIsIndvcmtmbG93UnVucy51cGRhdGUiLCJwb2xpY2llcy52YXVsdC5jcmVhdGUiLCJwb2xpY2llcy52YXVsdC5yZWFkIiwicG9saWNpZXMudmF1bHQudXBkYXRlIiwicG9saWNpZXMudmF1bHQuZGVsZXRlIiwicG9saWNpZXMudmF1bHQuc3RhdHVzLnVwZGF0ZSIsInBvbGljaWVzLnJvbGUudmF1bHQucmVhZCJdLCJzdWIiOiJnby1zZXJ2ZXItZm9yLWFuZHJvaWQtdGVzdGluZyJ9.Qqx4_4NUFt3ah86Pl09leSMi7g8r3JH680r6CW9jzoJh4c6DBuupuqFVJ-R0-5tWh3Qk8KpIqwZ0xDN-XQiGwwGd_Ho6lszmAU4yyx23wcAocZfBY4TQ1QIfzVDBEwVXkYdAxkcfpfiKHIRZTjkSa5lkYYveIwNBYzpG96ZNFNXWvixCVlwXcNhxryG93GPWPQVFbhAiu3tctbncf7TxnlqW8RJW7jqJo9dWL6prw03-4bl_71TdSJm2BuXryaIIpxxkgrsp_xinOZ2kLOmJVga3pyMgwrDe92yrYdCOPMKvb8IbCmvNAxEJBlHuPx_rrREmpU-1lFkjvI9-el4IZA"))

    }

    //end JwtUtils



    //getbyid

    @Test
    fun testNoRecords()
    {
        val configuration = Configuration(
            "1234",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val records = JSONObject()
        //records.put("records",JSONObject())
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND)
                Assert.assertEquals(UnitTests.getErrorMessage(exception as JSONObject),
                    skyflowError.getInternalErrorMessage())
            }

        })
    }

    @Test
    fun testInvalidRecords()
    {
        val configuration = Configuration(
            "1234",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val records = JSONObject()
        records.put("records", JSONObject())
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORDS)
                Assert.assertEquals(UnitTests.getErrorMessage(exception as JSONObject),
                    skyflowError.getInternalErrorMessage())
            }

        })
    }

    @Test
    fun testEmptyRecords()
    {
        val configuration = Configuration(
            "1234",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val records = JSONObject()
        records.put("records", JSONArray())
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_RECORDS)
                Assert.assertEquals(UnitTests.getErrorMessage(exception as JSONObject),
                    skyflowError.getInternalErrorMessage())
            }

        })
    }

    @Test
    fun testMissingIds()
    {
        val configuration = Configuration(
            "1234",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction", RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        // record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_IDS)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyIds()
    {
        val configuration = Configuration(
            "1234",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction", RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_RECORD_IDS)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testEmptySkyflowId()
    {
        val configuration = Configuration(
            "1234",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction", RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun testInvalidIds()
    {
        val configuration = Configuration(
            "1234",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction", RedactionType.PLAIN_TEXT)

        record.put("ids", JSONObject())
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORD_IDS)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testMissingRedaction()
    {
        val configuration = Configuration(
            "1234",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        //record.put("redaction",RedactionType.PLAIN_TEXT)

        record.put("ids", JSONObject())
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.REDACTION_KEY_ERROR)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testMissingTable()
    {
        val configuration = Configuration(
            "1234",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        //record.put("table","cards")
        record.put("redaction", RedactionType.PLAIN_TEXT)

        record.put("ids", JSONObject())
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TABLE_KEY)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidTableType()
    {
        val configuration = Configuration(
            "1234",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", JSONObject())
        record.put("redaction", RedactionType.PLAIN_TEXT)

        record.put("ids", JSONObject())
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testInvalidRedaction()
    {
        val configuration = Configuration(
            "1234",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
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
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_REDACTION_TYPE)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testEmptyRedaction()
    {
        val configuration = Configuration(
            "1234",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
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
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_REDACTION_VALUE)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyTableName()
    {
        val configuration = Configuration(
            "1234",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","")
        record.put("redaction","")

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TABLE_KEY)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidVaultURL()
    {
        val configuration = Configuration(
            "1234",
            "http://sb1.aa51.vault.skyflowapis.tech>",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","")
        record.put("redaction", RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL,params = arrayOf(configuration.vaultURL))
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyVaultURL()
    {
        val configuration = Configuration(
            "1234",
            "",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","")
        record.put("redaction","")

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyVaultId()
    {
        val configuration = Configuration(
            "",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","")
        record.put("redaction","")

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                Assert.assertEquals(skyflowError.getInternalErrorMessage(),
                    UnitTests.getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testValidRequestForGetById()
    {
        val configuration = Configuration(
            "23456",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction", RedactionType.REDACTED)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add("f8d8a622-b557-4c6b-a12c-c5ebe0b0bfd9")
        skyflowIds.add("da26de53-95d5-4bdb-99db-8d8c66a35ff9")
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                //valid request
            }

        })
    }
    //end getbyid

//    @Test
//    fun testOnChangeListener(){
//        val container = skyflow.container(ContainerType.COLLECT)
//        val options = CollectElementOptions(false)
//        val collectInput = CollectElementInput("cards","card_number",
//            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
//        )
//        val card_number = container.create(activity,collectInput, options) as? TextField
//        card_number!!.inputField.setText("4111")
//        activity.addContentView(card_number,layoutParams)
//
//        card_number.inputField.setText("4111 1111 1111 1111")
//    }

    @Test
    fun testOnFocucChangeListener() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val element = (container.create(activity, collectInput, options) as? TextField)
        element?.on(EventName.FOCUS){
            state ->
            assertTrue(state.get("elementType").equals(SkyflowElementType.CARD_NUMBER))
            assertTrue(state.get("isEmpty").equals(false))
            assertTrue(state.get("isValid").equals(false))
        }
        activity.addContentView(element,layoutParams)
        element!!.inputField.setText("4111")
        element.state = StateforText(element)
        element.requestFocus()
        element.clearFocus()
    }

    @Test
    fun testOnBlurChangeListner() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val element = (container.create(activity, collectInput, options) as? TextField)
        element?.on(EventName.BLUR){
                state ->
            assertTrue(state.get("elementType").equals(SkyflowElementType.CARD_NUMBER))
            assertTrue(state.get("isEmpty").equals(false))
            assertTrue(state.get("isValid").equals(true))
        }
        element!!.inputField.setText("4111 1111 1111 1111")
        element.state = StateforText(element)
        activity.addContentView(element,layoutParams)
        element.requestFocus()
        element.clearFocus()
    }

    @Test
    fun testOnElementReadyListener() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val element = (container.create(activity, collectInput, options) as? TextField)
        element?.on(EventName.READY){
                state ->
            assertTrue(state.get("elementType").equals(SkyflowElementType.CARD_NUMBER))
            assertTrue(state.get("isEmpty").equals(true))
            assertTrue(state.get("isValid").equals(true))
        }
        activity.addContentView(element,layoutParams)
        element!!.requestFocus()
        element.clearFocus()
    }



    @Test
    fun testConstructError()
    {
        val error = Exception("Not found")
        val constructedError = Utils.constructError(error,404)
        assertEquals(error.message,getErrorMessage(constructedError))
    }
    @Test
    fun testCardTypeClass()
    {
        val cardNumber1 = "4929939187355598"
        var cardtype  = CardType.forCardNumber(cardNumber1)
        assertTrue(cardtype.equals(CardType.VISA))
        assertTrue(cardtype.defaultName.equals("Visa"))
        assertTrue(cardtype.image.equals(R.drawable.ic_visa))

        val cardNumber2 = "5454422955385717"
        cardtype  = CardType.forCardNumber(cardNumber2)
        assertTrue(cardtype.equals(CardType.MASTERCARD))
        assertTrue(cardtype.defaultName.equals("MasterCard"))
        assertTrue(cardtype.image.equals(R.drawable.ic_mastercard))

        val cardNumber3 = "11111"
        cardtype  = CardType.forCardNumber(cardNumber3)
        assertTrue(cardtype.equals(CardType.UNKNOWN))
        assertTrue(cardtype.defaultName.equals("Unknown"))
        assertTrue(cardtype.image.equals(R.drawable.ic_emptycard))

    }
    @Test
    fun testBearerTokenFunctionFailed() // invalid token
    {
        val client = APIClient("1234","https://vaulturl.com",APITokenProviderForFail(),LogLevel.ERROR)
        client.getAccessToken(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val errorMessage = "bearer token is invalid or expired"
                assertEquals(errorMessage,(exception as SkyflowError).getInternalErrorMessage())
            }

        })
    }

    @Test
    fun testXMLfunctionInSkyflowError()
    {
        val skyflowError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, params = arrayOf("cvv"))
        skyflowError.setXml("xml")
        assertEquals("xml", skyflowError.getXml())
    }
    companion object
    {
        fun getErrorMessage(error: JSONObject): String {
            val errors = error.getJSONArray("errors")
            val skyflowError = errors.getJSONObject(0).get("error") as SkyflowError
            val message = skyflowError.getInternalErrorMessage()
            if(message.indexOf("-") !=-1)
                return message.substring(message.indexOf("-")+2)
            else
                return message
        }
    }

    @Test
    fun testGetValueForLabel()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv",token = "1234"),
            RevealElementOptions(formatRegex = "..$"))
        activity.addContentView(cvv,layoutParams)
        Utils.setValueForLabel(cvv,"1234")
        assertEquals(cvv.placeholder.text.toString(),"34")
    }



    @Test
    fun testSetValueForLabel()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv",token = "1234"),
            RevealElementOptions(formatRegex = "^([0-9]{1})$", replaceText = "0$1"))
        activity.addContentView(cvv,layoutParams)
        Utils.setValueForLabel(cvv,"1")
        assertEquals("01",cvv.actualValue)
    }

    @Test
    fun testSetValueForLabel1()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv",token = "1234"),
            RevealElementOptions(formatRegex = "^([0-9]{1})$", replaceText = "0$1"))
        activity.addContentView(cvv,layoutParams)
        Utils.setValueForLabel(cvv,"11")
        assertEquals("11",cvv.actualValue)
    }
    @Test
    fun testSetValueForLabelFailed()
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv",token = "1234"),
            RevealElementOptions(formatRegex = "^([0-9]{1})$", replaceText = "0$1"))
        activity.addContentView(cvv,layoutParams)
        Utils.setValueForLabel(cvv,"2211")
        assertEquals("2211",cvv.actualValue)
    }

    @Test
    fun testSetValueForLabelFailed1() // no format regex
    {
        val revealContainer = skyflow.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput(label = "cvv",token = "1234"),
            RevealElementOptions( replaceText = "0$1"))
        activity.addContentView(cvv,layoutParams)
        Utils.setValueForLabel(cvv,"2211")
        assertEquals("2211",cvv.actualValue)
    }

    @Test
    fun testRenderKey()
    {
        val list = mutableListOf<Any>()
        list.add("card")
        list.add("key")
        list.add("number")
        assertEquals(Utils.renderKey(list),"card[key][number]")
    }
    @Test
    fun testConverQueryString()
    {
        val json = JSONObject()
        json.put("type","card")
        val card= HashMap<String,String>()
        val card1 = JSONObject()
        card.put("number","4242")
        card.put("exp_month","1")
        card1.put("exp_year","2024")
        card1.put("cvc","314")
        json.put("card",card)
        json.put("card1",card1)
        assertEquals(Utils.convertJSONToQueryString(json),"card%5Bexp_month%5D=1&card1%5Bexp_year%5D=2024&type=card&card%5Bnumber%5D=4242&card1%5Bcvc%5D=314")
    }
    @Test
    fun testConverQueryStringWithArray()
    {
        val json = JSONObject()
        val card= JSONObject()
        card.put("number",JSONArray().put("123"))
        json.put("card",card)
        assertEquals(Utils.convertJSONToQueryString(json),"card%5Bnumber%5D%5B0%5D=123")
    }

    @Test
    fun testGetBody()
    {
        val json = JSONObject()
        val card= JSONObject()
        card.put("number",JSONArray().put("123"))
        json.put("card",card)
        assertTrue(Utils.getRequestbodyForConnection(json,ContentType.FORMURLENCODED.type).contentType().toString().contains(ContentType.FORMURLENCODED.type))
        assertTrue(Utils.getRequestbodyForConnection(json,ContentType.FORMDATA.type).contentType().toString().contains(ContentType.FORMDATA.type))
        assertTrue(Utils.getRequestbodyForConnection(json,ContentType.APPLICATIONORJSON.type).contentType().toString().contains(ContentType.APPLICATIONORJSON.type))
    }

    @Test
    fun testGetBIN() {
        assertEquals("41111111XXXXXXXX", CardType.getBin("4111111111111111"))
        assertEquals("41111111XXXX", CardType.getBin("411111111111"))
        assertEquals("41111111", CardType.getBin("41111111"))
        assertEquals("4111", CardType.getBin("4111"))
        assertEquals("378282XXXX",CardType.getBin("3782822412"))
    }

    @Test
    fun testGetUpsertColumn() {
        val jsonObj = JSONObject()
        jsonObj.put("table","cards")
        jsonObj.put("column","card_number")
        val options = JSONArray()
        options.put(jsonObj)
        assertEquals("card_number", Utils.getUpsertColumn("cards",options,LogLevel.DEBUG))
        assertEquals("", Utils.getUpsertColumn("cards", null,LogLevel.DEBUG))
    }

    @Test
    fun testNoTableKeyInUpsert() {
        val jsonObj = JSONObject()
        jsonObj.put("column","card_number")
        val options = JSONArray()
        options.put(jsonObj)
        try{
            assertEquals("card_number", Utils.getUpsertColumn("cards",options,LogLevel.DEBUG))
        }
        catch (e: SkyflowError) {
            assertEquals(String.format(Messages.NO_TABLE_KEY_IN_UPSERT.message,0), e.getInternalErrorMessage())
        }
    }
    @Test
    fun testNoColumnKeyInUpsert() {
        val jsonObj = JSONObject()
        jsonObj.put("table","cards")
        val options = JSONArray()
        options.put(jsonObj)
        try{
            assertEquals("card_number", Utils.getUpsertColumn("cards",options,LogLevel.DEBUG))
        }
        catch (e: SkyflowError) {
            assertEquals(String.format(Messages.NO_COLUMN_KEY_IN_UPSERT.message,0), e.getInternalErrorMessage())
        }
    }

    @Test
    fun testEmptyTableKeyInUpsert() {
        val jsonObj = JSONObject()
        jsonObj.put("table","")
        jsonObj.put("column","card_number")
        val options = JSONArray()
        options.put(jsonObj)
        try{
            assertEquals("card_number", Utils.getUpsertColumn("cards",options,LogLevel.DEBUG))
        }
        catch (e: SkyflowError) {
            assertEquals(String.format(Messages.INVALID_TABLE_IN_UPSERT_OPTION.message,0), e.getInternalErrorMessage())
        }
    }
    @Test
    fun testEmptyColumnKeyInUpsert() {
        val jsonObj = JSONObject()
        jsonObj.put("table","cards")
        jsonObj.put("column","")
        val options = JSONArray()
        options.put(jsonObj)
        try{
            assertEquals("card_number", Utils.getUpsertColumn("cards",options,LogLevel.DEBUG))
        }
        catch (e: SkyflowError) {
            assertEquals(String.format(Messages.INVALID_COLUMN_IN_UPSERT_OPTION.message,0), e.getInternalErrorMessage())
        }
    }

    @Test
    fun testNOnStringTableKeyInUpsert() {
        val jsonObj = JSONObject()
        jsonObj.put("table",true)
        jsonObj.put("column","card_number")
        val options = JSONArray()
        options.put(jsonObj)
        try{
            assertEquals("card_number", Utils.getUpsertColumn("cards",options,LogLevel.DEBUG))
        }
        catch (e: SkyflowError) {
            assertEquals(String.format(Messages.INVALID_TABLE_IN_UPSERT_OPTION.message,0), e.getInternalErrorMessage())
        }
    }
    @Test
    fun testNonStringColumnKeyInUpsert() {
        val jsonObj = JSONObject()
        jsonObj.put("table","cards")
        jsonObj.put("column",true)
        val options = JSONArray()
        options.put(jsonObj)
        try{
            assertEquals("card_number", Utils.getUpsertColumn("cards",options,LogLevel.DEBUG))
        }
        catch (e: SkyflowError) {
            assertEquals(String.format(Messages.INVALID_COLUMN_IN_UPSERT_OPTION.message,0), e.getInternalErrorMessage())
        }
    }

    @Test
    fun testEmptyUpsertValue() {
        try{
            assertEquals("card_number", Utils.getUpsertColumn("cards",JSONArray(),LogLevel.DEBUG))
        }
        catch (e: SkyflowError) {
            assertEquals(String.format(Messages.EMPTY_UPSERT_OPTIONS_ARRAY.message,0), e.getInternalErrorMessage())
        }
    }

    @Test
    fun testNotJSONObjectInUpsert() {

        try{
            assertEquals("", Utils.getUpsertColumn("cards",JSONArray().put("123"),LogLevel.DEBUG))
        }
        catch (e: SkyflowError) {
            assertEquals(Messages.ALLOW_JSON_OBJECT_IN_UPSERT.message, e.getInternalErrorMessage())
        }
    }
}

class APITokenProviderForSuccess : TokenProvider {

    override fun getBearerToken(callback: Callback) {
        callback.onSuccess("token")
    }
}


class APITokenProviderForFail : TokenProvider {

    override fun getBearerToken(callback: Callback) {
        callback.onFailure("invalid token")
    }
}

