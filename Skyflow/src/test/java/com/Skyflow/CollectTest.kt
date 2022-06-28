/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
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
    }

    @Test
    fun testExpireMonth()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","month",
            SkyflowElementType.EXPIRATION_MONTH,placeholder = "month"
        )
        val month = container.create(activity,collectInput) as? TextField

        month!!.setText("13")
        var state = StateforText(month).getInternalState()
        assertFalse(state["isValid"] as Boolean)


        month.setText("11")
        state = StateforText(month).getInternalState()
        assertTrue(state["isValid"] as Boolean) // valid

        month.setText("xxxx")
        state = StateforText(month).getInternalState()
        assertFalse(state["isValid"] as Boolean) //accepts only numbers

        month.setText("0")
        state = StateforText(month).getInternalState()
        assertFalse(state["isValid"] as Boolean) //zero

        month.setText("-5")
        state = StateforText(month).getInternalState()
        assertFalse(state["isValid"] as Boolean) //negative
    }

    @Test
    fun testExpireYear()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","year",
            SkyflowElementType.EXPIRATION_YEAR,placeholder = "year"
        )
        val lognYear = container.create(activity,collectInput, CollectElementOptions(format = "yyyy")) as? TextField

        lognYear!!.setText("202")
        var state = StateforText(lognYear).getInternalState()
        assertFalse(state["isValid"] as Boolean)

        lognYear.setText("2021")
        state = StateforText(lognYear).getInternalState()
        assertFalse(state["isValid"] as Boolean)

        lognYear.setText("2022")
        state = StateforText(lognYear).getInternalState()
        assertTrue(state["isValid"] as Boolean) // valid

        lognYear.setText("2030")
        state = StateforText(lognYear).getInternalState()
        assertTrue(state["isValid"] as Boolean) //accepts only numbers

        lognYear.setText("2077")
        state = StateforText(lognYear).getInternalState()
        assertFalse(state["isValid"] as Boolean) //accepts only numbers

        val shortYear = container.create(activity,collectInput, CollectElementOptions(format = "yy")) as? TextField
        shortYear!!.setText("20")
        state = StateforText(shortYear).getInternalState()
        assertFalse(state["isValid"] as Boolean)

        shortYear.setText("xyz")
        state = StateforText(shortYear).getInternalState()
        assertFalse(state["isValid"] as Boolean) //not number

        shortYear.setText("22")
        state = StateforText(shortYear).getInternalState()
        assertTrue(state["isValid"] as Boolean)

        shortYear.setText("30")
        state = StateforText(shortYear).getInternalState()
        assertTrue(state["isValid"] as Boolean)

        lognYear.setText("77")
        state = StateforText(lognYear).getInternalState()
        assertFalse(state["isValid"] as Boolean) //accepts only numbers

    }

    @Test
    fun testExpireDate() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards", "year",
            SkyflowElementType.EXPIRATION_DATE, placeholder = "year"
        )
        val lognYear = container.create(activity,
            collectInput,
            CollectElementOptions(format = "mm/yyyy")) as? TextField

        lognYear!!.setText("12/2030")
        var state = StateforText(lognYear).getInternalState()
        assertTrue(state["isValid"] as Boolean)

        lognYear.setText("12/2080")
        state = StateforText(lognYear).getInternalState()
        assertFalse(state["isValid"] as Boolean)

    }

        @Test
    fun testInvalidValueForPIN()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","PIN",
            SkyflowElementType.PIN,placeholder = "pin"
        )
        val pin = container.create(activity,collectInput) as? TextField

        pin!!.setText("411")
        var state = StateforText(pin).getInternalState()
        assertFalse(state["isValid"] as Boolean)


        pin.setText("xyzzz")
        state = StateforText(pin).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean) //accepts only numbers

    }

    @Test
    fun testValidCardForUnknown()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,placeholder = "card number"
        )
        val cardNumber = container.create(activity,collectInput) as? TextField

        //invalid
        cardNumber!!.setText("1111111")
        var state = StateforText(cardNumber).getInternalState()
        assertFalse(state["isValid"] as Boolean)

        //valid
        cardNumber.setText("5066991111111118")
        state = StateforText(cardNumber).getInternalState()
        Assert.assertTrue(state["isValid"] as Boolean) //accepts only numbers
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
    fun testCheckElementsArray()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput("cards","card_number",
            SkyflowElementType.CARD_NUMBER,label = "card number"
        )
        val card_number = container.create(activity,collectInput, options) as? TextField
        assertEquals(container.collectElements.count(),1)
        Assert.assertTrue(container.collectElements[0].fieldType == SkyflowElementType.CARD_NUMBER)
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
                assertEquals((exception as SkyflowError).getInternalErrorMessage().trim(),skyflowError.getInternalErrorMessage().trim())
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
            card_number.actualValue = "4111 1111 1111 1111"
            cvv.inputField.setText("142")
            activity.addContentView(card_number,layoutParams)
            activity.addContentView(cvv,layoutParams)
            card_number.state = StateforText(card_number)
            card_number.state.show()
            cvv.state = StateforText(cvv)

            container.collect(object : Callback
            {
                override fun onSuccess(responseBody: Any) {

                }

                override fun onFailure(exception: Any) {
                    val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_INPUT,params = arrayOf("for cvv value is empty"))
                    assertEquals((exception as SkyflowError).getInternalErrorMessage().trim(),skyflowError.getInternalErrorMessage().trim())
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
        cvv.inputField.setText("123")
        cvv.actualValue = "123"
        cvv.state = StateforText(cvv)
        activity.addContentView(card_number,layoutParams)
        activity.addContentView(cvv,layoutParams)

        container.collect(object : Callback
        {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_INPUT,params = arrayOf("for card_number value is empty"))
                assertEquals((exception as SkyflowError).getInternalErrorMessage().trim(),skyflowError.getInternalErrorMessage().trim())
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
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                assertEquals((exception as SkyflowError).getInternalErrorMessage(),skyflowError.getInternalErrorMessage())
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
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                assertEquals((exception as SkyflowError).getInternalErrorMessage(),skyflowError.getInternalErrorMessage())
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
                assertEquals((exception as SkyflowError).getInternalErrorMessage(),skyflowError.getInternalErrorMessage())
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
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TABLE_IN_ELEMENT, params = arrayOf(card_number.fieldType.toString()))
                assertEquals((exception as SkyflowError).getInternalErrorMessage(),skyflowError.getInternalErrorMessage())
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
                Log.d("exc",(exception as SkyflowError).getInternalErrorMessage())
                val skyflowError = SkyflowError(SkyflowErrorCode.ELEMENT_EMPTY_TABLE_NAME, params = arrayOf(card_number.fieldType.toString()))
                assertEquals((exception as SkyflowError).getInternalErrorMessage(),skyflowError.getInternalErrorMessage())
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
                val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_COLUMN, params = arrayOf(card_number.fieldType.toString()))
                assertEquals((exception as SkyflowError).getInternalErrorMessage(),skyflowError.getInternalErrorMessage())
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
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_COLUMN_NAME, params = arrayOf(card_number.fieldType.toString()))
                assertEquals((exception as SkyflowError).getInternalErrorMessage(),skyflowError.getInternalErrorMessage())
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
        Assert.assertEquals(container.collectElements.count(), 1)
        Assert.assertTrue(container.collectElements[0].fieldType == SkyflowElementType.EXPIRATION_DATE)
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
                TestCase.assertEquals(skyflowError.getInternalErrorMessage(),(exception as Exception).message.toString())
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

        try {
            CollectRequestBody.createRequestBody(container.collectElements, JSONObject(),LogLevel.ERROR)
        }
        catch (e:Exception)
        {
            val skyflowError = SkyflowError(SkyflowErrorCode.DUPLICATE_COLUMN_FOUND,params = arrayOf(collectInput.table,collectInput.column))
            TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                (e as SkyflowError).getInternalErrorMessage())
        }

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

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records,LogLevel.ERROR)
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_TABLE_KEY)
            TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }
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

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records,LogLevel.ERROR)
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError(SkyflowErrorCode.MISSING_TABLE_KEY)
            TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }
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

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records,LogLevel.ERROR)
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME)
            TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }
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

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records,LogLevel.ERROR)
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError(SkyflowErrorCode.FIELDS_KEY_ERROR)
            TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }
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

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records,LogLevel.ERROR)
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_FIELDS)
            TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }
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

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records,LogLevel.ERROR)
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_RECORDS)
            TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }
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

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records,LogLevel.ERROR)
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_RECORDS)
            TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }
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
        try {
            CollectRequestBody.createRequestBody(container.collectElements, records,LogLevel.ERROR)
        }
        catch (exception:Exception)
        {
            val skyflowError = SkyflowError(SkyflowErrorCode.DUPLICATE_COLUMN_FOUND,params = arrayOf(collectInput1.table,collectInput1.column))
            TestCase.assertEquals(skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage())
        }
    }

    //end collect

    @Test
    fun testmmyyExpireDate()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","expire date",
            SkyflowElementType.EXPIRATION_DATE,placeholder = "expire date"
        )
        val expireDate = container.create(activity,collectInput,CollectElementOptions(format = "mm/yy"))
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
        val expireDate = container.create(activity,collectInput,CollectElementOptions(format = "mm/yyyy"))
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
        val expireDate = container.create(activity,collectInput,CollectElementOptions(format = "yy/mm"))
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
        val expireDate = container.create(activity,collectInput,CollectElementOptions(format = "yyyy/mm"))
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
        val expireDate = container.create(activity,collectInput,CollectElementOptions(format = "yyyy/mm"))
        activity.addContentView(expireDate,layoutParams)
        expireDate.inputField.setText("209/12")
        expireDate.state = StateforText(expireDate)
        expireDate.setError("custom error")
        assertEquals("custom error",expireDate.getErrorText())

        expireDate.resetError()
        assertEquals("INVALID_EXPIRE_DATE",expireDate.getErrorText())


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

    @Test
    fun testClearValue()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","pin",
            SkyflowElementType.EXPIRATION_DATE,placeholder = "enter pin"
        )
        val pin = container.create(activity,collectInput)
        pin.setText("1234")

        assertEquals(pin.actualValue,"1234")
        pin.clearValue()
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