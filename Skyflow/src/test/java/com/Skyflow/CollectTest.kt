package com.Skyflow

import Skyflow.*
import Skyflow.collect.client.CollectAPICallback
import Skyflow.collect.client.CollectRequestBody
import Skyflow.collect.elements.validations.ElementValueMatchRule
import Skyflow.core.APIClient
import Skyflow.core.elements.state.StateforText
import Skyflow.utils.EventName
import Skyflow.utils.Utils
import android.app.Activity
import android.view.ViewGroup
import com.skyflow_android.R
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import java.io.IOException
import android.util.Log
import com.Skyflow.collect.elements.validations.ValidationSet
import junit.framework.Assert
import junit.framework.Assert.*
import junit.framework.TestCase
import okhttp3.Call
import org.json.JSONArray
import org.json.JSONObject


@RunWith(RobolectricTestRunner::class)
class CollectTest {
        lateinit var skyflow : Client
        private lateinit var activityController: ActivityController<Activity>
        private lateinit var activity: Activity
        lateinit var layoutParams : ViewGroup.LayoutParams
        @Before
        fun setup()
        {
            val configuration = Configuration(
                "b359c43f1b844ff4bea0f098",
                "https://vaulturl.com",
                AccessTokenProvider()
            )
            val container = CollectContainer()
            skyflow = Client(configuration)
             layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()
        }



    @Test
    fun testValidValueSkyflowElement()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")

        val state = StateforText(card_number).getInternalState()
        assertTrue(state["isValid"] as Boolean)

    }

    @Test
    fun testValidValueForPIN()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","PIN",
            SkyflowElementType.PIN,placeholder = "pin"
        )
        val pin = container.create(activity,collectInput) as? TextField
        pin!!.inputField.setText("4111111")
        var state = StateforText(pin).getInternalState()
        assertTrue(state["isValid"] as Boolean)

        pin.inputField.setText("411")
        state = StateforText(pin).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean)


        pin.inputField.setText("xyzzz")
        state = StateforText(pin).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean) //accepts only numbers

    }

    @Test
    fun testEmptyStateForSkyflowElement()
    {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098",
            "https://vaulturl.com/",
            AccessTokenProvider()
        )
        val collectContainer = CollectContainer()

        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")

        val state = StateforText(card_number).getInternalState()
        Assert.assertFalse(state["isEmpty"] as Boolean)
    }

    @Test
    fun testNonEmptyStateforSkyflowElement()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput) as? TextField

        val state = StateforText(card_number!!).getInternalState()
        Assert.assertTrue(state["isEmpty"] as Boolean)
    }

    @Test
    fun testInvalidValueSkyflowElement()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput) as? TextField
        card_number!!.inputField.setText("4111")
        card_number.state = StateforText(card_number)
        val state = StateforText(card_number)
        Assert.assertFalse(state.getInternalState()["isValid"] as Boolean)
        assertNotNull(card_number.state.show())
        assertEquals(state.getInternalState().get("inputLength"),4)
        assertEquals(state.getState(Env.DEV).get("value"),"") // value will change when text event afterTextChanged triggers

    }

    @Test
    fun testCheckElementsArray()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        assertEquals(container.elements.count(),1)
        Assert.assertTrue(container.elements[0].fieldType == SkyflowElementType.CARD_NUMBER)
    }

    @Test
    fun testElementNotMounted()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(activity.resources.getString(R.string.bt_cvc),"card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val collectInput2 = CollectElementInput("cards","cvv",SkyflowElementType.CVV, label = "cvv")
        val card_number : TextField = container.create(activity,collectInput1, options)
        val cvv : TextField = container.create(activity,collectInput2, options)

        card_number.inputField.setText("4111 1111 1111 1111")
        cvv.inputField.setText("2")
        card_number.state = StateforText(card_number)
        cvv.state = StateforText(cvv)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(card_number.columnName))
                assertEquals((exception as SkyflowError).message.trim(),skyflowError.getErrorMessage().trim())
                assertEquals(400,skyflowError.getErrorcode())
            }
        })
    }

        @Test
        fun testContainerInsertMixedInvalidInput() {
            val container = skyflow.container(ContainerType.COLLECT)
            val options = CollectElementOptions(true)
            val collectInput1 = CollectElementInput(activity.resources.getString(R.string.bt_cvc),"card_number",
                SkyflowElementType.CARD_NUMBER,label = "card number"
            )
            val collectInput2 = CollectElementInput("cards","cvv",SkyflowElementType.CVV, label = "cvv")
            val card_number : TextField = container.create(activity,collectInput1, options)
            val cvv : TextField = container.create(activity,collectInput2, options)

            card_number.inputField.setText("4111 1111 1111 1111")
            cvv.inputField.setText("2")
            activity.addContentView(card_number,layoutParams)
            activity.addContentView(cvv,layoutParams)
            card_number.state = StateforText(card_number)
            cvv.state = StateforText(cvv)

            container.collect(object : Callback
            {
                override fun onSuccess(responseBody: Any) {

                }

                override fun onFailure(exception: Any) {
                    val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_INPUT,params = arrayOf("for cvv INVALID_LENGTH_MATCH"))
                    assertEquals((exception as SkyflowError).message.trim(),skyflowError.getErrorMessage().trim())
                }
            })
        }


    @Test
    fun testContainerInsertIsRequiredAndEmpty() //to do
    {

        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val collectInput2 = CollectElementInput("cards","cvv",SkyflowElementType.CVV, label = "cvv")
        val card_number = container.create(activity,collectInput1, options)
        val cvv = container.create(activity,collectInput2,options)
        activity.addContentView(card_number,layoutParams)
        activity.addContentView(cvv,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_INPUT,params = arrayOf("card_number is empty\n" +
                        "cvv is empty"))
                assertEquals((exception as SkyflowError).message.trim(),skyflowError.getErrorMessage().trim())
            }
        })
    }

    @Test
    fun testEmptyVaultIDWithSkyflowElement()
    {
        val skyflowConfiguration = Configuration( "",
            "https://vaulturl.com",
            AccessTokenProvider())
        val skyflow = Client(skyflowConfiguration)
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val card_number = container.create(activity,collectInput1, options)
        card_number.inputField.setText("4111111111111111")
        card_number.state = StateforText(card_number)
        activity.addContentView(card_number,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
                assertEquals((exception as SkyflowError).message,SkyflowErrorCode.EMPTY_VAULT_ID.getMessage())
            }
        })

    }

    @Test
    fun testEmptyVaultURLWithSkyflowElement()
    {
        val configuration = Configuration( "b359c43f1b844ff4bea0f098d2c09",
            "",
            AccessTokenProvider())
        val skyflow = Client(configuration)
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number",altText = "41111"
        )
        val collectInput2 = CollectElementInput("cards","expiry_date",SkyflowElementType.EXPIRATION_DATE, label = "expire date")
        val card_number = container.create(activity,collectInput1, options)
        val expire = container.create(activity,collectInput2,options)

        card_number.inputField.setText("4111111111111111")
        expire.inputField.setText("11/22")
        card_number.state = StateforText(card_number)
        expire.state = StateforText(expire)
        activity.addContentView(card_number,layoutParams)
        activity.addContentView(expire,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                assertEquals((exception as SkyflowError).message,SkyflowErrorCode.EMPTY_VAULT_URL.getMessage())
            }


        })
    }

    @Test
    fun testInvalidVaultURLWithSkyflowElement()
    {
        val configuration = Configuration( "b359c43f1b844ff4bea0f098d2c09",
            "http://www.google.com",
            AccessTokenProvider())
        val skyflow = Client(configuration)
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val collectInput2 = CollectElementInput("cards","expiry_date",SkyflowElementType.EXPIRATION_DATE, label = "expire date")

        val card_number = container.create(activity,collectInput1, options)
        val expire = container.create(activity,collectInput2,options)

        card_number.inputField.setText("4111111111111111")
        expire.inputField.setText("11/22")
        card_number.state = StateforText(card_number)
        expire.state = StateforText(expire)
        activity.addContentView(card_number,layoutParams)
        activity.addContentView(expire,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL,params = arrayOf(configuration.vaultURL))
                assertEquals((exception as SkyflowError).message,skyflowError.getErrorMessage())
            }


        })
    }
    @Test
    fun testNullTableName()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(null,"card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                assertEquals((exception as SkyflowError).message,SkyflowErrorCode.MISSING_TABLE.getMessage())
            }


        })
    }
    @Test
    fun testEmptyTableName()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30,20,20,20)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                Log.d("exc",(exception as SkyflowError).message)
                assertEquals((exception as SkyflowError).message,SkyflowErrorCode.EMPTY_TABLE_NAME.getMessage())
            }


        })
    }
    @Test
    fun testNullColumnName()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards",null,
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                assertEquals((exception as SkyflowError).message,SkyflowErrorCode.MISSING_COLUMN.getMessage())
            }


        })

    }

    @Test
    fun testEmptyColumnName()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30,20,20,20)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                assertEquals((exception as SkyflowError).message,SkyflowErrorCode.EMPTY_COLUMN_NAME.getMessage())
            }


        })
    }

    @Test
    fun testEmptyStyles()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30,20,20,20)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number,layoutParams)
        assertEquals(2,card_number.collectInput.inputStyles.base.borderWidth)
    }

    @Test
    fun testEmptyStyle()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30,20,20,20)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number",inputStyles = Styles()
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number,layoutParams)
        assertEquals(2,card_number.collectInput.inputStyles.base.borderWidth)
    }

    @Test
    fun testNullStyle()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30,20,20,20)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number",inputStyles =Styles(null)
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number,layoutParams)
        assertEquals(2,card_number.collectInput.inputStyles.base.borderWidth)
        card_number.setErrorText("error occured")
        assertTrue(card_number.error.text.toString().equals("error occured"))
        card_number.unmount()
        assertTrue(card_number.actualValue.isEmpty())
    }

    @Test
    fun testOnReadyListener() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false,enableCardIcon = true)
        val collectInput = CollectElementInput("cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        var onReadyCalled = false
        val card_number = container.create(activity, collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number, layoutParams)
        card_number.on(EventName.FOCUS) { state ->
            Log.d("state",state.toString())
        }

        card_number.on(EventName.BLUR) { state ->
            Log.d("state",state.toString())
        }

        card_number.on(EventName.CHANGE) { state ->
            Log.d("state",state.toString())
        }

        card_number.on(EventName.READY) { state ->
            Log.d("state",state.toString())
            onReadyCalled = true
        }
        assertFalse(onReadyCalled)
        card_number.setupField(collectInput,options)
        assertEquals(card_number.isRequired,false)
    }


    @Test
    fun testValidCollect()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                //its valid
            }


        })
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
        TestCase.assertNotNull(card_number.validate())
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
    fun testCollectElementNotMounted()
    {

        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cardNumber = container.create(activity,collectInput)
        TestCase.assertEquals(false, Utils.checkIfElementsMounted(cardNumber))
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
        TestCase.assertEquals(true, Utils.checkIfElementsMounted(cardNumber))
    }




    //collectapicallback

    @Test
    fun testCollectApiCallback()
    {
        val apiClient = APIClient("b359c43f1b84f098d2c09193","https://vaulturl.com/v1/vaults",AccessTokenProvider(),LogLevel.ERROR)
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
        val collectAPICallback = CollectAPICallback(apiClient,records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                //valid request
            }

        }, InsertOptions(),LogLevel.ERROR)

        collectAPICallback.onSuccess("token")

    }


    @Test
    fun testOnfailureInCollectApiCallback()
    {
        val apiClient = APIClient("78789","https://vaulturl.com",AccessTokenProvider(),LogLevel.ERROR)
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
        records.put("records", recordsArray)
        val collectAPICallback = CollectAPICallback(apiClient,records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL,params = arrayOf(apiClient.vaultURL))
                TestCase.assertEquals(skyflowError.getErrorMessage(),(exception as Exception).message.toString())
            }

        }, InsertOptions(),LogLevel.ERROR)

        collectAPICallback.onSuccess("token")

    }

    @Test
    fun testBuildResponse()
    {
        val apiClient = APIClient("78789","https://vaulturl.com",AccessTokenProvider(),LogLevel.ERROR)
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
        records.put("records", recordsArray)
        val collectAPICallback = CollectAPICallback(apiClient,records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
            }

        }, InsertOptions(),LogLevel.ERROR)

        val response = """
            [{"records":[{"skyflow_id":"376ddd3a-7f29-4e68-9a48-aafcbd87c6f5"}]},{"fields":{"card_number":"5ec9cd13-c410-4588-bf22-54cbb771b263","cvv":"082b3265-559a-4068-87af-7d83946a04c2","expiry_date":"658ef0a3-0fd6-4bdd-9989-f5aceb8c6cf6","fullname":"e0e8cc7f-9124-42e4-9850-c322d72fec14"}}]
        """.trimIndent()

        val responseFromApi = JSONArray(response)

        val responsetoClient = collectAPICallback.buildResponse(responseFromApi)

        val recordsInResponse = responsetoClient.getJSONArray("records").getJSONObject(0)

        TestCase.assertTrue(recordsInResponse.has("fields"))
        TestCase.assertTrue(recordsInResponse.has("table"))
        TestCase.assertTrue(recordsInResponse.getJSONObject("fields").has("skyflow_id"))
    }

    @Test
    fun testBuildResponseWithoutTokens()
    {
        val apiClient = APIClient("78789","https://vaulturl.com",AccessTokenProvider(),LogLevel.ERROR)
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
        records.put("records", recordsArray)
        val collectAPICallback = CollectAPICallback(apiClient,records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {
            }
            override fun onFailure(exception: Any) {
            }

        }, InsertOptions(false),LogLevel.ERROR)

        val response = """
            [{"records":[{"skyflow_id":"376ddd3a-7f29-4e68-9a48-aafcbd87c6f5"}]}]"""

        val responseFromApi = JSONArray(response)

        val responsetoClient = collectAPICallback.buildResponse(responseFromApi)

        val recordsInResponse = responsetoClient.getJSONArray("records").getJSONObject(0)

        TestCase.assertTrue(recordsInResponse.has("skyflow_id"))
        TestCase.assertTrue(recordsInResponse.has("table"))
    }

    //end collectapicallback


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
                TestCase.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
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
                TestCase.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
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
                TestCase.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
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
                TestCase.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
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
        record.put("table", "cards")
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
                TestCase.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
            }

        },LogLevel.ERROR)
    }


    @Test
    fun testMissingColumnInAdditionalFields()
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
        record.put("table", "cards")
        fields.put("", "41111111111")
        fields.put("expiry_date","11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)

        CollectRequestBody.createRequestBody(container.elements, records,object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_COLUMN_NAME)
                TestCase.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
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
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_FIELDS)
                TestCase.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
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
                TestCase.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
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
                TestCase.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
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
        val cvv = container.create(activity,collectInput,CollectElementOptions(enableCardIcon = false))
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
                TestCase.assertEquals(skyflowError.getErrorMessage(),
                    (exception as SkyflowError).getErrorMessage())
            }

        },LogLevel.ERROR)
    }

    //end collect

    @Test
    fun testmmyyExpireDate()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","expire date",
            SkyflowElementType.EXPIRATION_DATE,placeholder = "expire date"
        )
        val expireDate = container.create(activity,collectInput,CollectElementOptions(expiryDateFormat = "mm/yy"))
        activity.addContentView(expireDate,layoutParams)
        expireDate.inputField.setText("12/39")
        expireDate.state = StateforText(expireDate)
        assertTrue(expireDate.state.getInternalState().get("isValid") as Boolean)

        expireDate.inputField.setText("12/20")
        expireDate.state = StateforText(expireDate)
        assertFalse(expireDate.state.getInternalState().get("isValid") as Boolean)

    }

    @Test
    fun testmmyyyyExpireDate()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","expire date",
            SkyflowElementType.EXPIRATION_DATE,placeholder = "expire date"
        )
        val expireDate = container.create(activity,collectInput,CollectElementOptions(expiryDateFormat = "mm/yyyy"))
        activity.addContentView(expireDate,layoutParams)
        expireDate.inputField.setText("12/2039")
        expireDate.state = StateforText(expireDate)
        assertTrue(expireDate.state.getInternalState().get("isValid") as Boolean)

        expireDate.inputField.setText("12/20njn")
        expireDate.state = StateforText(expireDate)
        assertFalse(expireDate.state.getInternalState().get("isValid") as Boolean)

    }
    @Test
    fun testyymmExpireDate()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","expire date",
            SkyflowElementType.EXPIRATION_DATE,placeholder = "expire date"
        )
        val expireDate = container.create(activity,collectInput,CollectElementOptions(expiryDateFormat = "yy/mm"))
        activity.addContentView(expireDate,layoutParams)
        expireDate.inputField.setText("39/12")
        expireDate.state = StateforText(expireDate)
        assertTrue(expireDate.state.getInternalState().get("isValid") as Boolean)

        expireDate.inputField.setText("12/20")
        expireDate.state = StateforText(expireDate)
        assertFalse(expireDate.state.getInternalState().get("isValid") as Boolean)

    }

    @Test
    fun testyyyymmExpireDate()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","expire date",
            SkyflowElementType.EXPIRATION_DATE,placeholder = "expire date"
        )
        val expireDate = container.create(activity,collectInput,CollectElementOptions(expiryDateFormat = "yyyy/mm"))
        activity.addContentView(expireDate,layoutParams)
        expireDate.inputField.setText("2039/12")
        expireDate.state = StateforText(expireDate)
        assertTrue(expireDate.state.getInternalState().get("isValid") as Boolean)

        expireDate.inputField.setText("12/20")
        expireDate.state = StateforText(expireDate)
        assertFalse(expireDate.state.getInternalState().get("isValid") as Boolean)

    }

    @Test
    fun testSetAndResetError()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","expire date",
            SkyflowElementType.EXPIRATION_DATE,placeholder = "expire date"
        )
        val expireDate = container.create(activity,collectInput,CollectElementOptions(expiryDateFormat = "yyyy/mm"))
        activity.addContentView(expireDate,layoutParams)
        expireDate.inputField.setText("209/12")
        expireDate.state = StateforText(expireDate)
        expireDate.setError("custom error")
        assertEquals("custom error",expireDate.getErrorText())

        expireDate.resetError()
        assertEquals("INVALID_EXPIRE_DATE",expireDate.getErrorText())


    }

    @Test
    fun testElementMatchRuleWithDuplicateTableAndColumn() //invalid
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","pin",
            SkyflowElementType.EXPIRATION_DATE,placeholder = "enter pin"
        )
        val pin = container.create(activity,collectInput)
        val validationSet = ValidationSet()
        val collectInput1 = CollectElementInput("cards","pin",
            SkyflowElementType.EXPIRATION_DATE,placeholder = "confirm pin", validations = validationSet
        )
        val confirmPin = container.create(activity,collectInput1)

        activity.addContentView(pin,layoutParams)
        activity.addContentView(confirmPin,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                assertEquals("Duplicate element with cards and pin found in container",(exception as Exception).message.toString())
            }

        })


    }


    @Test
    fun testValidElementMatchRuleWithDuplicateTableAndColumn() //valid
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","pin",
            SkyflowElementType.EXPIRATION_DATE,placeholder = "enter pin"
        )
        val pin = container.create(activity,collectInput)
        val validationSet = ValidationSet()
        validationSet.add(ElementValueMatchRule(pin,"not matched with pin"))
        val collectInput1 = CollectElementInput("cards","pin",
            SkyflowElementType.EXPIRATION_DATE,placeholder = "confirm pin", validations = validationSet
        )
        val confirmPin = container.create(activity,collectInput1)

        activity.addContentView(pin,layoutParams)
        activity.addContentView(confirmPin,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                assertEquals("no error",(exception as Exception).message.toString())
            }

        })


    }




}


class AccessTokenProvider: TokenProvider {
    override fun getBearerToken(callback: Skyflow.Callback) {
        val url = "https://go-server.skyflow.dev/sa-token/b359c43f1b844ff4bea0f098d2c0"
        val request = okhttp3.Request.Builder().url(url).build()
        val okHttpClient = OkHttpClient()
        try {
            val thread = Thread {
                run {
                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful)
                            throw IOException("Unexpected code $response")
                        //  val accessTokenObject = JSONObject(response.body()!!.string().toString())
                        //  val accessToken = accessTokenObject["accessToken"]
                        val accessToken = ""
                        callback.onSuccess("$accessToken")
                    }
                }
            }
        }
        catch (e:Exception)
        {

        }
    }
}