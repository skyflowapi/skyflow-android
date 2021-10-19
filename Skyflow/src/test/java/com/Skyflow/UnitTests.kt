package com.Skyflow

import Skyflow.*
import Skyflow.core.APIClient
import Skyflow.core.LogLevel
import Skyflow.core.elements.state.StateforText
import Skyflow.utils.Utils
import android.app.Activity
import android.view.ViewGroup
import com.Skyflow.AccessTokenProvider
import com.Skyflow.TestApplication
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
@Config(application = TestApplication::class)
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
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111")
        activity.addContentView(card_number,layoutParams)
        card_number.inputField.setText("4111 1111 1111 1111")
        Assert.assertEquals(card_number.getValue(), "4111 1111 1111 1111")
    }
    @Test
    fun testEmptyState()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cardNumber = container.create(activity,collectInput)
        cardNumber.inputField.setText("4111 1111 1111 1111")
        cardNumber.state = StateforText(cardNumber)

    }

    @Test
    fun testCheckCollectContainer()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        Assert.assertEquals(container.elements.count(), 1)
        Assert.assertTrue(container.elements[0].fieldType == SkyflowElementType.CARD_NUMBER)
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
        val cardNumber = container.create(activity,revealInput)
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
    //end getbyid


    //detokenize

    @Test
    fun testEmptyTokenForDetokenize()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.PROD)
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)
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
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.PROD)
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
       // recordObj.put("token", "")
        recordObj.put("redaction", RedactionType.PLAIN_TEXT)
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
    fun testMissingRedactionForDetokenize()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.PROD)
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "1234-134")
       // recordObj.put("redaction", RedactionType.PLAIN_TEXT)
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        apiClient.get(revealRecords, object : Callback
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
    fun testInvalidRedactionForDetokenize()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.PROD)
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "1234-134")
        recordObj.put("redaction", "some")
        revealRecordsArray.put(recordObj)
        revealRecords.put("records", revealRecordsArray)
        apiClient.get(revealRecords, object : Callback
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
    fun testRecordsForDetokenize()
    {
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.PROD)
        val revealRecords = JSONObject()
        val revealRecordsArray = JSONArray()
        val recordObj = JSONObject()
        recordObj.put("token", "1234-134")
         recordObj.put("redaction", RedactionType.PLAIN_TEXT)
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
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.PROD)
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
        val apiClient = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",AccessTokenProvider(),LogLevel.PROD)
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

        },LogLevel.PROD)

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

        },LogLevel.PROD)
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

        }, HashSet(),LogLevel.PROD)
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
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        val records = JSONObject()
        records.put("cardNumber",card_number)
        activity.addContentView(card_number,layoutParams)
        val isConstructed = Utils.constructRequestBodyForGateway(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {

            }

        },LogLevel.PROD)

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
       // activity.addContentView(card_number,layoutParams)
        val isConstructed = Utils.constructRequestBodyForGateway(records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {

            }

        },LogLevel.PROD)

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

        },LogLevel.PROD)

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

        },LogLevel.PROD)
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

        },LogLevel.PROD)
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

        },LogLevel.PROD)

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

        },LogLevel.PROD)

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

        },LogLevel.PROD)

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

        },LogLevel.PROD)

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

        },LogLevel.PROD)

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

        },LogLevel.PROD)

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
        val client = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",APITokenProviderForSuccess(),LogLevel.PROD)
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
        val client = APIClient("1234","https://sb1.area51.vault.skyflowapis.tech",APITokenProviderForFail(),LogLevel.PROD)
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

