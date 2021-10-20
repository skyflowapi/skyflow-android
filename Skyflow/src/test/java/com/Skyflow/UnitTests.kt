package com.Skyflow

import Skyflow.*
import Skyflow.collect.client.CollectAPICallback
import Skyflow.collect.client.CollectRequestBody
import Skyflow.collect.elements.utils.Card
import Skyflow.collect.elements.utils.CardType
import Skyflow.collect.elements.utils.DateValidator
import Skyflow.collect.elements.utils.SecurityCode
import Skyflow.core.APIClient
import Skyflow.core.Logger
import Skyflow.core.elements.state.StateforText
import Skyflow.reveal.GetByIdRecord
import Skyflow.reveal.RevealApiCallback
import Skyflow.reveal.RevealByIdCallback
import Skyflow.reveal.RevealRequestRecord
import Skyflow.utils.Utils
import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import android.widget.CheckBox
import com.Skyflow.collect.elements.validations.SkyflowValidationError
import com.skyflow_android.R
import io.mockk.MockKAnnotations
import junit.framework.Assert
import junit.framework.TestCase.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

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
            "https://sb1.area51.vault.skyflowapis.tech",
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
    fun testCreateSkyflowElement(){
        val padding = Padding(10,10,5,5)
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val styles = Styles(base = Style(padding = padding))
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number",inputStyles = styles
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111")
        activity.addContentView(card_number,layoutParams)
        card_number.inputField.setText("4111 1111 1111 1111")
        Assert.assertEquals(card_number.getValue(), "4111 1111 1111 1111")
        Assert.assertEquals(10,card_number.collectInput.inputStyles.base.padding.top)
        Assert.assertEquals(10,card_number.collectInput.inputStyles.base.padding.left)
        Assert.assertEquals(5,card_number.collectInput.inputStyles.base.padding.right)
        Assert.assertEquals(5,card_number.collectInput.inputStyles.base.padding.bottom)
        assertNotNull(card_number.validate())
    }
    @Test
    fun testEmptyState()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","cvv",
            SkyflowElementType.CVV,placeholder = "cvv"
        )
        val cvv = container.create(activity,collectInput,CollectElementOptions())
        cvv.inputField.setText("123")
        cvv.state = StateforText(cvv)

    }

    @Test
    fun testCheckCollectContainer()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","expiry_date",
            SkyflowElementType.EXPIRATION_DATE,label = "expiry date"
        )
        val date = container.create(activity,collectInput, options) as? TextField
        Assert.assertEquals(container.elements.count(), 1)
        Assert.assertTrue(container.elements[0].fieldType == SkyflowElementType.EXPIRATION_DATE)
    }

    @Test
    fun testCheckRevealContainer()
    {
        val container = skyflow.container(ContainerType.REVEAL)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "expire_date",inputStyles = Styles()

        )
        val revealElement = container.create(activity, revealInput, Skyflow.RevealElementOptions())
        Assert.assertEquals("51b1406a-0a30-49bf-b303-0eef66bd502d", container.revealElements[0].revealInput.token)
    }
    @Test
    fun testCollectElementNotMounted()
    {

        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cardNumber = container.create(activity,collectInput)
        assertEquals(false,Utils.checkIfElementsMounted(cardNumber))
    }


    @Test
    fun testCollectElementMounted()
    {

        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cardNumber = container.create(activity,collectInput)
        activity.addContentView(cardNumber,layoutParams)
        assertEquals(true,Utils.checkIfElementsMounted(cardNumber))
    }

    @Test
    fun testRevealElementNotMounted()
    {

        val container = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "card number",inputStyles = Styles(null)

        )
        val cardNumber = container.create(activity,revealInput,RevealElementOptions())
        assertEquals(false,Utils.checkIfElementsMounted(cardNumber))
    }


    @Test
    fun testRevealElementMounted()
    {

        val container = skyflow.container(ContainerType.REVEAL)
        val revealInput = RevealElementInput(
            "51b1406a-0a30-49bf-b303-0eef66bd502d",RedactionType.PLAIN_TEXT,
            label =  "card number",inputStyles = Styles(null)

        )
        val cardNumber = container.create(activity,revealInput)
        activity.addContentView(cardNumber,layoutParams)
        assertEquals(true,Utils.checkIfElementsMounted(cardNumber))
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

    //getbyid

    @Test
    fun testNoRecords()
    {
        val configuration = Configuration(
            "1234",
            "https://sb1.area51.vault.skyflowapis.tech",
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
                Assert.assertEquals(getErrorMessage(exception as JSONObject),
                    skyflowError.getErrorMessage())
            }

        })
    }

    @Test
    fun testInvalidRecords()
    {
        val configuration = Configuration(
            "1234",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val records = JSONObject()
        records.put("records",JSONObject())
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORDS)
                Assert.assertEquals(getErrorMessage(exception as JSONObject),
                    skyflowError.getErrorMessage())
            }

        })
    }

    @Test
    fun testEmptyRecords()
    {
        val configuration = Configuration(
            "1234",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val records = JSONObject()
        records.put("records",JSONArray())
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_RECORDS)
                Assert.assertEquals(getErrorMessage(exception as JSONObject),
                    skyflowError.getErrorMessage())
            }

        })
    }

    @Test
    fun testMissingIds()
    {
        val configuration = Configuration(
            "1234",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val client = Client(configuration)
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
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_IDS)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyIds()
    {
        val configuration = Configuration(
            "1234",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testEmptySkyflowId()
    {
        val configuration = Configuration(
            "1234",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    @Test
    fun testInvalidIds()
    {
        val configuration = Configuration(
            "1234",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

        record.put("ids",JSONObject())
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORD_IDS)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testMissingRedaction()
    {
        val configuration = Configuration(
            "1234",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        //record.put("redaction",RedactionType.PLAIN_TEXT)

        record.put("ids",JSONObject())
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.REDACTION_KEY_ERROR)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testMissingTable()
    {
        val configuration = Configuration(
            "1234",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        //record.put("table","cards")
        record.put("redaction",RedactionType.PLAIN_TEXT)

        record.put("ids",JSONObject())
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.TABLE_KEY_ERROR)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidTableType()
    {
        val configuration = Configuration(
            "1234",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table",JSONObject())
        record.put("redaction",RedactionType.PLAIN_TEXT)

        record.put("ids",JSONObject())
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        client.getById(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testInvalidRedaction()
    {
        val configuration = Configuration(
            "1234",
            "https://sb1.area51.vault.skyflowapis.tech",
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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testEmptyRedaction()
    {
        val configuration = Configuration(
            "1234",
            "https://sb1.area51.vault.skyflowapis.tech",
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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyTableName()
    {
        val configuration = Configuration(
            "1234",
            "https://sb1.area51.vault.skyflowapis.tech",
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
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TABLE_NAME)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
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
        record.put("redaction",RedactionType.PLAIN_TEXT)

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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyVaultId()
    {
        val configuration = Configuration(
            "",
            "https://sb1.area51.vault.skyflowapis.tech",
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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testValidRequestForGetById()
    {
        val configuration = Configuration(
            "23456",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","cards")
        record.put("redaction",RedactionType.REDACTED)

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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }
    //end getbyid


    //detokenize

    @Test
    fun testEmptyTokenForDetokenize()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testMissingTokenForDetokenize()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testRecordsForDetokenize()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testInvalidRecordsForDetokenize()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
        val revealRecords = JSONObject()
        revealRecords.put("records", JSONObject())
        apiClient.get(revealRecords, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORDS)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testEmptyRecordsForDetokenize()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
        val revealRecords = JSONObject()
        revealRecords.put("records", JSONArray())
        apiClient.get(revealRecords, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_RECORDS)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }



    @Test
    fun testEmptyVaultID()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "",
            "https://sb1.area51.vault.skyflowapis.tech",
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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testEmptyVaultURL2()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "b359c43f1b844ff4bea0f098d2",
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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidVaultURL2()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "b359c43f1b844ff4bea0f098d2c",
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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testValidRequestForDetokenize()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
         recordObj.put("token", "cards")
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        apiClient.get(revealRecords, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {

            }

        })

    }

    @Test
    fun testValidRequestForDetokenizeInClient()
    {
        val configuration = Configuration(
            "",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val client = Client(configuration)
        val revealRecords = JSONObject()
        client.detokenize(revealRecords, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {

            }

        })

    }






    //end detokenize


    //invoke gateway

    @Test
    fun testEmptyVaultIdForGateway()
    {
        val configuration = Configuration(
            "",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )

        val client = Client(configuration)
        val url = "BuildConfig.GATEWAY_CVV_GEN_URL " // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        client.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testEmptyVaultURLForGateway() {
        val skyflowConfiguration = Skyflow.Configuration(
            "9898989898",
            "",
            AccessTokenProvider()
        )

        val client = Client(skyflowConfiguration)
        val url = "BuildConfig.GATEWAY_CVV_GEN_URL " // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        client.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })

    }

    @Test
    fun testInvalidVaultURLForGateway()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "http://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient =  Client(skyflowConfiguration)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL,params = arrayOf(skyflowConfiguration.vaultURL))
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })


    }
    @Test
    fun emptyGatewayURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient =  Client(skyflowConfiguration)
        val url = "" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_GATEWAY_URL)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }

    @Test
    fun testInvalidGatewayURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient =  Client(skyflowConfiguration)
        val url = "something" // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST)
        skyflowClient.invokeGateway(gatewayRequestBody,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_GATEWAY_URL,params = arrayOf(gatewayRequestBody.gatewayURL))
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testAddQueryParams()
    {
        val queryParams = JSONObject()
        queryParams.put("card_number","4111")
        queryParams.put("cvv","123")
        val gatewayConfiguration = GatewayConfiguration("https://www.google.com",RequestMethod.POST,queryParams = queryParams)
        val requestUrlBuilder = gatewayConfiguration.gatewayURL.toHttpUrlOrNull()?.newBuilder()
        Utils.addQueryParams(requestUrlBuilder!!,gatewayConfiguration,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
            }

        },LogLevel.ERROR)

        val newRequest = "https://www.google.com?card_number=4111&cvv=123".toHttpUrlOrNull()?.newBuilder()

        assertEquals(requestUrlBuilder.toString().trim(),newRequest.toString().trim())
    }

    @Test
    fun testAddPathParams()
    {
        val pathParams = JSONObject()
        pathParams.put("card_number","4111")
        pathParams.put("cvv","123")
        val url = "https://www.google.com/{card_number}/{cvv}"
       val generatedUrl = Utils.addPathparamsToURL(url,pathParams,object : Callback{
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
            }

        },LogLevel.ERROR)
        assertEquals(generatedUrl,"https://www.google.com/4111/123")
    }

    @Test
    fun testDuplicateInResponseBody()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "29182989857575878",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val revealContainer = skyflowClient.container(ContainerType.REVEAL)
        val cvv = revealContainer.create(activity,RevealElementInput())
        val responseBody = JSONObject()
        responseBody.put("cardNumber",cvv)
        responseBody.put("cvv",cvv)
        activity.addContentView(cvv,layoutParams)
        val url = "https://www.google.com" // eg:  url.../{cardNumber}/...
        Utils.checkDuplicateInResponseBody(responseBody,object :Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.DUPLICATE_ELEMENT_FOUND)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    getErrorMessage(exception as JSONObject))
            }

        }, HashSet(),LogLevel.ERROR)
    }

    @Test
    fun testRemoveNullJSON()
    {
        val records = JSONObject()
        records.put("card_number","1234")
        records.put("cvv","123")
        records.put("name",JSONObject())

        Utils.removeEmptyAndNullFields(records)
        assertTrue(!records.has("name"))

    }

    @Test
    fun testCheckInvalidFields() //in response body
    {
        val records = JSONObject()
        records.put("card_number","1234")
        Utils.checkInvalidFields(records, JSONObject(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                Assert.assertEquals("invalid field card_number present in response body",
                    getErrorMessage(exception as JSONObject))
            }

        })
    }


    @Test
    fun testConstructRequestBody()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards",null,
            SkyflowElementType.CARDHOLDER_NAME,placeholder = "name"
        )
        val name = container.create(activity,collectInput, options) as? TextField
        name!!.inputField.setText("4111 1111 1111 1111")
        val records = JSONObject()
        records.put("name",name)
        activity.addContentView(name,layoutParams)
        val isConstructed = Utils.constructRequestBodyForGateway(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {

            }

        },LogLevel.ERROR)

        assertTrue(isConstructed)
    }

    @Test
    fun testConstructRequestBodyFailed()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards",null,
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        val records = JSONObject()
        records.put("cardNumber",card_number)
        val containerOptions = ContainerOptions()
       // activity.addContentView(card_number,layoutParams)
        val isConstructed = Utils.constructRequestBodyForGateway(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {

            }

        },LogLevel.ERROR)

        assertFalse(isConstructed)
    }

    @Test
    fun testConstructRequestBodyFailedForInvalidElement()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards",null,
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        val records = JSONObject()
        records.put("cardNumber",card_number)
        activity.addContentView(card_number,layoutParams)
        card_number!!.inputField.setText("4111 11 1111 1111")
        val isConstructed = Utils.constructRequestBodyForGateway(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {

            }

        },LogLevel.ERROR)

        assertFalse(isConstructed)
    }


    //end invokegateway


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
        Utils.constructBatchRequestBody(records, InsertOptions(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                Assert.assertEquals((exception as SkyflowError).message.toString(),
                    SkyflowErrorCode.RECORDS_KEY_NOT_FOUND.getMessage())
            }

        },LogLevel.ERROR)
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
        Utils.constructBatchRequestBody(records, InsertOptions(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                Assert.assertEquals((exception as SkyflowError).message,
                    SkyflowErrorCode.INVALID_RECORDS.getMessage())
            }

        },LogLevel.ERROR)
    }

    @Test
    fun testMissingTableForInsert()
    {
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        fields.put("table", "")
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
       val x = Utils.constructBatchRequestBody(records, InsertOptions(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                Assert.assertEquals((exception as SkyflowError).message.toString(),
                    SkyflowErrorCode.MISSING_TABLE.getMessage())
            }

        },LogLevel.ERROR)

        assertEquals(x.toString().trim(),JSONObject().toString().trim())
    }

    @Test
    fun testInvalidTableTypeForInsert()
    {
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        fields.put("table", JSONObject())
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        val x = Utils.constructBatchRequestBody(records, InsertOptions(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
            }

        },LogLevel.ERROR)

        assertEquals(x.toString().trim(),JSONObject().toString().trim())
    }

    @Test
    fun testEmptyTableForInsert()
    {
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        fields.put("table", "")
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        val x = Utils.constructBatchRequestBody(records, InsertOptions(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
            }

        },LogLevel.ERROR)

        assertEquals(x.toString().trim(),JSONObject().toString().trim())
    }

    @Test
    fun testEmptyColumnName()
    {
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        fields.put("table", "")
        fields.put("", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        val x = Utils.constructBatchRequestBody(records, InsertOptions(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                Assert.assertEquals((exception as SkyflowError).message.toString(),
                    SkyflowErrorCode.MISSING_TABLE.getMessage())
            }

        },LogLevel.ERROR)

        assertEquals(x.toString().trim(),JSONObject().toString().trim())
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
    fun testConstructError()
    {
        val error = Exception("Not found")
        val constructedError = Utils.constructError(error,404)
        assertEquals(error.message,getErrorMessage(constructedError))
    }


    @Test
    fun testBearerTokenFunction() //success
    {
        val client = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",APITokenProviderForSuccess(),LogLevel.ERROR,"")
        client.getAccessToken(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
                    assertEquals("Bearer token",responseBody.toString())
            }

            override fun onFailure(exception: Any) {

            }

        })
    }

    @Test
    fun testBearerTokenFunctionFailed() // invalid token
    {
        val client = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",APITokenProviderForFail(),LogLevel.ERROR)
        client.getAccessToken(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                assertEquals("bearer token is invalid or expired",(exception as SkyflowError).getErrorMessage())
            }

        })
    }

    fun getErrorMessage(error: JSONObject): String {
        val errors = error.getJSONArray("errors")
        val skyflowError = errors.getJSONObject(0).get("error") as SkyflowError
        return skyflowError.message
    }



    //collect

    @Test
    fun testduplicateElement()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","cvv",
            SkyflowElementType.CVV,placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cvv = container.create(activity,collectInput,CollectElementOptions())
        val cvv1 = container.create(activity,collectInput,CollectElementOptions())
        val card_number = container.create(activity,collectInput1,CollectElementOptions())

        CollectRequestBody.createRequestBody(container.elements, JSONObject(),object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.DUPLICATE_COLUMN_FOUND,params = arrayOf(collectInput.table,collectInput.column))
                assertEquals(skyflowError.getErrorMessage(),(exception as SkyflowError).getErrorMessage())
            }

        },LogLevel.ERROR)
    }

    @Test
    fun testEmptyTableInAdditionalFields()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","cvv",
            SkyflowElementType.CVV,placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cvv = container.create(activity,collectInput,CollectElementOptions())
        val card_number = container.create(activity,collectInput1,CollectElementOptions())

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

        CollectRequestBody.createRequestBody(container.elements, records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TABLE_NAME)
                assertEquals(skyflowError.getErrorMessage(),(exception as SkyflowError).getErrorMessage())
            }

        },LogLevel.ERROR)
    }

    @Test
    fun testMissingTableInAdditionalFields()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","cvv",
            SkyflowElementType.CVV,placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cvv = container.create(activity,collectInput,CollectElementOptions())
        val card_number = container.create(activity,collectInput1,CollectElementOptions())

        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        //fields.put("table", "")
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)

        CollectRequestBody.createRequestBody(container.elements, records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TABLE)
                assertEquals(skyflowError.getErrorMessage(),(exception as SkyflowError).getErrorMessage())
            }

        },LogLevel.ERROR)
    }


    @Test
    fun testInvalidTableInAdditionalFields()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","cvv",
            SkyflowElementType.CVV,placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cvv = container.create(activity,collectInput,CollectElementOptions())
        val card_number = container.create(activity,collectInput1,CollectElementOptions())

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

        CollectRequestBody.createRequestBody(container.elements, records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME)
                assertEquals(skyflowError.getErrorMessage(),(exception as SkyflowError).getErrorMessage())
            }

        },LogLevel.ERROR)
    }

    @Test
    fun testMissingFieldsInAdditionalFields()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","cvv",
            SkyflowElementType.CVV,placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cvv = container.create(activity,collectInput,CollectElementOptions())
        val card_number = container.create(activity,collectInput1,CollectElementOptions())

        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        record.put("table", "")
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date","11/22")
       // record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)

        CollectRequestBody.createRequestBody(container.elements, records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.FIELDS_KEY_ERROR)
                assertEquals(skyflowError.getErrorMessage(),(exception as SkyflowError).getErrorMessage())
            }

        },LogLevel.ERROR)
    }

    @Test
    fun testEmptyFieldsInAdditionalFields()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","cvv",
            SkyflowElementType.CVV,placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cvv = container.create(activity,collectInput,CollectElementOptions())
        val card_number = container.create(activity,collectInput1,CollectElementOptions())

        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        record.put("table","cards")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)

        CollectRequestBody.createRequestBody(container.elements, records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.FIELDS_KEY_ERROR)
                assertEquals(skyflowError.getErrorMessage(),(exception as SkyflowError).getErrorMessage())
            }

        },LogLevel.ERROR)
    }

    @Test
    fun testEmptyRecordsInAdditionalFields()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","cvv",
            SkyflowElementType.CVV,placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cvv = container.create(activity,collectInput,CollectElementOptions())
        val card_number = container.create(activity,collectInput1,CollectElementOptions())

        val records = JSONObject()
        val recordsArray = JSONArray()
        records.put("records", recordsArray)

        CollectRequestBody.createRequestBody(container.elements, records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_RECORDS)
                assertEquals(skyflowError.getErrorMessage(),(exception as SkyflowError).getErrorMessage())
            }

        },LogLevel.ERROR)
    }

    @Test
    fun testInvalidRecordsInAdditionalFields()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","cvv",
            SkyflowElementType.CVV,placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cvv = container.create(activity,collectInput,CollectElementOptions())
        val card_number = container.create(activity,collectInput1,CollectElementOptions())

        val records = JSONObject()
        records.put("records", JSONObject())

        CollectRequestBody.createRequestBody(container.elements, records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORDS)
                assertEquals(skyflowError.getErrorMessage(),(exception as SkyflowError).getErrorMessage())
            }

        },LogLevel.ERROR)
    }


    @Test
    fun testDuplicatesInAdditionalFields()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","cvv",
            SkyflowElementType.CVV,placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cvv = container.create(activity,collectInput,CollectElementOptions())
        val card_number = container.create(activity,collectInput1,CollectElementOptions())

        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        record.put("table","cards")
        fields.put("card_number","123")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)

        CollectRequestBody.createRequestBody(container.elements, records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.DUPLICATE_COLUMN_FOUND,params = arrayOf(collectInput1.table,collectInput1.column))
                assertEquals(skyflowError.getErrorMessage(),(exception as SkyflowError).getErrorMessage())
            }

        },LogLevel.ERROR)
    }







    //end collect


    //client

    @Test
    fun testInsertEmptyVaultID()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "",
            "https://sb1.area51.vault.skyflowapis.tech",
            AccessTokenProvider()
        )
        val records = JSONObject()
        val skyflowClient = Client(skyflowConfiguration)
        skyflowClient.insert(records = records, InsertOptions(), object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
            }

        })

    }

    @Test
    fun testInsertEmptyVaultURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "b359c43f1b844ff4bea0f098d2",
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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
            }

        })
    }

    @Test
    fun testInsertInvalidVaultURL()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "b359c43f1b844ff4bea0f098d2c",
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
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
            }

        })
    }

    @Test
    fun testValidRequestForInsert()
    {
        val skyflowConfiguration = Skyflow.Configuration(
            "b359c43f1b844ff4bea0f098d2c",
            "https://www.google.com",
            AccessTokenProvider()
        )
        val records = JSONObject()
        val skyflowClient = Client(skyflowConfiguration)
        skyflowClient.insert(records = records, InsertOptions(), object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {

            }

        })
    }


    //end client


    //api client

    @Test
    fun testPostMethod()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
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
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TABLE_NAME)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
            }

        }, InsertOptions())
    }

    @Test
    fun testValidRequestForPostMethod()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
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
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TABLE_NAME)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
            }

        }, InsertOptions())
    }


    @Test
    fun testInvokeGateway()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
        val requestRecord = JSONObject()
        requestRecord.put("xxx",CheckBox(activity))
        val url = "BuildConfig.GATEWAY_CVV_GEN_URL " // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST,requestBody = requestRecord)

        apiClient.invokeGateway(gatewayRequestBody, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD)
                Assert.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
            }

        })

    }

    @Test
    fun testValidForInvokeGateway()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
        val requestRecord = JSONObject()
        requestRecord.put("card_number","41111")
        val url = "BuildConfig.GATEWAY_CVV_GEN_URL " // eg:  url.../{cardNumber}/...
        val gatewayRequestBody = GatewayConfiguration(gatewayURL = url,methodName = RequestMethod.POST,requestBody = requestRecord)

        apiClient.invokeGateway(gatewayRequestBody, object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {

            }

        })

    }
    //end api client

    //element

    @Test
    fun testValidateFunction()
    {
        val element = Element(activity)
        assertEquals(element.getValue(),"")
        assertEquals(element.validate(), mutableListOf<SkyflowValidationError>())
    }
    @Test
    fun testStateClass()
    {
        val state= State("card_number",true)
        assertNotNull(state.show())
        assertEquals(state.getInternalState().get("columnName"),"card_number")
    }
    @Test
    fun testLoggerClass()
    {
        val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
        Logger.debug("",skyflowError.getErrorMessage(),LogLevel.DEBUG)
        Logger.error("",skyflowError.getErrorMessage(),LogLevel.DEBUG)
        Logger.info("",skyflowError.getErrorMessage(),LogLevel.DEBUG)
        Logger.warn("",skyflowError.getErrorMessage(),LogLevel.DEBUG)
    }

    @Test
    fun testCardType()
    {
        val newCard = Card("new card","[123]",1,3,"{}",3,"cvv")
        val card = CardType.AMEX
        assertEquals(card.minCardLength,15)
        assertEquals(newCard.maxCardLength,3)
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
        assertTrue(date.isValid("10","22"))
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
    //end element


    //collectapicallback

    @Test
    fun testCollectApiCallback()
    {
        val apiClient = APIClient("b359c43f1b844ff4bea0f098d2c09193","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "careeds")
        val fields = JSONObject()
        fields.put("fullname", "san")
        fields.put("card_number", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        Log.d("xx","yy")
        val collectAPICallback = CollectAPICallback(apiClient,records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                Log.d("Exception",exception.toString())
                assertEquals(2,(exception as Exception).message.toString())

            }

        }, InsertOptions(),LogLevel.ERROR)

        collectAPICallback.onSuccess("token")

    }

    //end collectapicallback


    //RevealApicallback

    @Test
    fun testRevealApiCallback()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
        val revealRecords = mutableListOf<RevealRequestRecord>()
        revealRecords.add(RevealRequestRecord("1234","null"))
        revealRecords.add(RevealRequestRecord("3456","null"))
        val revealApiCallback = RevealApiCallback(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {

            }

        },apiClient,records = revealRecords)
        revealApiCallback.onSuccess("token")
    }

    //end revealapicallback


    //revealbyid callback

    @Test
    fun testRevealByIdCallback()
    {
        val records = mutableListOf<GetByIdRecord>()
        records.add(GetByIdRecord(arrayListOf("1234"),"cards",RedactionType.REDACTED.toString()))
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.ERROR)
        val revealByidCallback = RevealByIdCallback(
            object : Callback
            {
                override fun onSuccess(responseBody: Any) {

                }

                override fun onFailure(exception: Any) {

                }

            }
        ,apiClient,records = records)

        revealByidCallback.onSuccess("token")
    }

    //end revealbyid callback
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

