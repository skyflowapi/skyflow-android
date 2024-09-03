package com.Skyflow

import Skyflow.*
import Skyflow.collect.client.CollectAPICallback
import Skyflow.collect.client.CollectRequestBody
import Skyflow.collect.elements.utils.Card
import Skyflow.collect.elements.utils.CardType
import Skyflow.collect.elements.validations.ElementValueMatchRule
import Skyflow.core.APIClient
import Skyflow.core.elements.state.StateforText
import Skyflow.utils.EventName
import Skyflow.utils.Utils
import android.app.Activity
import android.graphics.Color
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
import android.view.Gravity
import android.view.MotionEvent
import com.Skyflow.collect.elements.validations.ValidationSet
//import junit.framework.Assert
//import junit.framework.Assert.*
import org.junit.Assert
import junit.framework.TestCase
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar


@RunWith(RobolectricTestRunner::class)
class CollectTest {
    lateinit var skyflow: Client
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity
    lateinit var layoutParams: ViewGroup.LayoutParams

    private fun getCardSchemes(len: Int): Array<CardType> {
        return if (len < 8) arrayOf(CardType.CARTES_BANCAIRES)
        else arrayOf(CardType.CARTES_BANCAIRES, CardType.MASTERCARD)
    }


    @Before
    fun setup() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val container = CollectContainer()
        skyflow = Client(configuration)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()
    }


    @Test
    fun testValidValueSkyflowElement() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val card_number = container.create(activity, collectInput) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")

        val state = StateforText(card_number).getInternalState()
        Assert.assertTrue(state["isValid"] as Boolean)

    }

    @Test
    fun testValidValueForPIN() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "PIN",
            SkyflowElementType.PIN, placeholder = "pin"
        )
        val pin = container.create(activity, collectInput) as? TextField
        pin!!.inputField.setText("4111111")
        var state = StateforText(pin).getInternalState()
        Assert.assertTrue(state["isValid"] as Boolean)
    }

    @Test
    fun testExpireMonth() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "month",
            SkyflowElementType.EXPIRATION_MONTH, placeholder = "month"
        )
        val month = container.create(activity, collectInput) as? TextField

        month!!.setText("13")
        var state = StateforText(month).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean)


        month.setText("11")
        state = StateforText(month).getInternalState()
        Assert.assertTrue(state["isValid"] as Boolean) // valid

        month.setText("xxxx")
        state = StateforText(month).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean) //accepts only numbers

        month.setText("0")
        state = StateforText(month).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean) //zero

        month.setText("-5")
        state = StateforText(month).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean) //negative
    }

    @Test
    fun testExpireYear() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "year",
            SkyflowElementType.EXPIRATION_YEAR, placeholder = "year"
        )

        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)

        val longYear = container.create(
            activity,
            collectInput,
            CollectElementOptions(format = "yyyy")
        ) as? TextField

        longYear!!.setText("year")
        var state = StateforText(longYear).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean) //accepts only numbers

        longYear.setText("77")
        state = StateforText(longYear).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean) //accepts only "yyyy" format

        for (i in 1..10) {
            val year = currentYear + i
            longYear.setText("$year")
            state = StateforText(longYear).getInternalState()
            Assert.assertTrue(state["isValid"] as Boolean)
        }

        for (i in 51..60) {
            val year = currentYear + i
            longYear.setText("$year")
            state = StateforText(longYear).getInternalState()
            Assert.assertFalse(state["isValid"] as Boolean)
        }

        for (i in 1..10) {
            val year = currentYear - i
            longYear.setText("$year")
            state = StateforText(longYear).getInternalState()
            Assert.assertFalse(state["isValid"] as Boolean)
        }

        val shortYear = container.create(
            activity,
            collectInput,
            CollectElementOptions(format = "yy")
        ) as? TextField

        shortYear!!.setText("xy")
        state = StateforText(shortYear).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean) //accepts only numbers

        shortYear.setText("2077")
        state = StateforText(shortYear).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean) //accepts only "yy" format

        for (i in 1..10) {
            val year = (currentYear % 100) + i
            shortYear.setText("$year")
            state = StateforText(shortYear).getInternalState()
            Assert.assertTrue(state["isValid"] as Boolean)
        }

        for (i in 51..60) {
            val year = (currentYear % 100) + i
            shortYear.setText("$year")
            state = StateforText(shortYear).getInternalState()
            Assert.assertFalse(state["isValid"] as Boolean)
        }

        for (i in 1..10) {
            val year = (currentYear % 100) - i
            shortYear.setText("$year")
            state = StateforText(shortYear).getInternalState()
            Assert.assertFalse(state["isValid"] as Boolean)
        }

        val incorrectFormatYear = container.create(
            activity,
            collectInput,
            CollectElementOptions(format = "yyy")
        ) as? TextField

        incorrectFormatYear!!.setText("201")
        state = StateforText(incorrectFormatYear).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean) //accepts either "yyyy" or "yy" format

    }

    @Test
    fun testExpireDate() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "year",
            SkyflowElementType.EXPIRATION_DATE, placeholder = "year"
        )
        val lognYear = container.create(
            activity,
            collectInput,
            CollectElementOptions(format = "mm/yyyy")
        ) as? TextField

        lognYear!!.setText("12/2030")
        var state = StateforText(lognYear).getInternalState()
        Assert.assertTrue(state["isValid"] as Boolean)

        lognYear.setText("12/2080")
        state = StateforText(lognYear).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean)

    }

    @Test
    fun testInvalidValueForPIN() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "PIN",
            SkyflowElementType.PIN, placeholder = "pin"
        )
        val pin = container.create(activity, collectInput) as? TextField

        pin!!.setText("411")
        var state = StateforText(pin).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean)


        pin.setText("xyzzz")
        state = StateforText(pin).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean) //accepts only numbers

    }

    @Test
    fun testValidCardForUnknown() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cardNumber = container.create(activity, collectInput) as? TextField

        //invalid
        cardNumber!!.setText("1111111")
        var state = StateforText(cardNumber).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean)

        //valid
        cardNumber.setText("5066991111111118")
        state = StateforText(cardNumber).getInternalState()
        Assert.assertTrue(state["isValid"] as Boolean) //accepts only numbers
    }

    @Test
    fun testEmptyStateForSkyflowElement() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098",
            "https://vaulturl.com/",
            AccessTokenProvider()
        )
        val collectContainer = CollectContainer()

        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val card_number = container.create(activity, collectInput) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")

        val state = StateforText(card_number).getInternalState()
        Assert.assertFalse(state["isEmpty"] as Boolean)
    }

    @Test
    fun testNonEmptyStateforSkyflowElement() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val card_number = container.create(activity, collectInput) as? TextField

        val state = StateforText(card_number!!).getInternalState()
        Assert.assertTrue(state["isEmpty"] as Boolean)
    }

    @Test
    fun testCheckElementsArray() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number"
        )
        val card_number = container.create(activity, collectInput, options) as? TextField
        Assert.assertEquals(container.collectElements.count(), 1)
        Assert.assertTrue(container.collectElements[0].fieldType == SkyflowElementType.CARD_NUMBER)
    }

    @Test
    fun testElementNotMounted() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(
            activity.resources.getString(R.string.bt_cvc), "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number"
        )
        val collectInput2 =
            CollectElementInput("cards", "cvv", SkyflowElementType.CVV, label = "cvv")
        val card_number: TextField = container.create(activity, collectInput1, options)
        val cvv: TextField = container.create(activity, collectInput2, options)

        card_number.inputField.setText("4111 1111 1111 1111")
        cvv.inputField.setText("2")
        card_number.state = StateforText(card_number)
        cvv.state = StateforText(cvv)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(
                    SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                    params = arrayOf(card_number.columnName)
                )
                Assert.assertEquals(
                    (exception as SkyflowError).getInternalErrorMessage().trim(),
                    skyflowError.getInternalErrorMessage().trim()
                )
                Assert.assertEquals(400, skyflowError.getErrorcode())
            }
        })
    }

    @Test
    fun testContainerInsertMixedInvalidInput() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(
            activity.resources.getString(R.string.bt_cvc), "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number"
        )
        val collectInput2 =
            CollectElementInput("cards", "cvv", SkyflowElementType.CVV, label = "cvv")
        val card_number: TextField = container.create(activity, collectInput1, options)
        val cvv: TextField = container.create(activity, collectInput2, options)

        card_number.inputField.setText("4111 1111 1111 1111")
        card_number.actualValue = "4111 1111 1111 1111"
        cvv.inputField.setText("142")
        activity.addContentView(card_number, layoutParams)
        activity.addContentView(cvv, layoutParams)
        card_number.state = StateforText(card_number)
        card_number.state.show()
        cvv.state = StateforText(cvv)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(
                    SkyflowErrorCode.INVALID_INPUT,
                    params = arrayOf("for cvv value is empty")
                )
                Assert.assertEquals(
                    (exception as SkyflowError).getInternalErrorMessage().trim(),
                    skyflowError.getInternalErrorMessage().trim()
                )
            }
        })
    }


    @Test
    fun testContainerInsertIsRequiredAndEmpty() //to do
    {

        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number"
        )
        val collectInput2 =
            CollectElementInput("cards", "cvv", SkyflowElementType.CVV, label = "cvv")
        val card_number = container.create(activity, collectInput1, options)
        val cvv = container.create(activity, collectInput2, options)
        cvv.inputField.setText("123")
        cvv.actualValue = "123"
        cvv.state = StateforText(cvv)
        activity.addContentView(card_number, layoutParams)
        activity.addContentView(cvv, layoutParams)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(
                    SkyflowErrorCode.INVALID_INPUT,
                    params = arrayOf("for card_number value is empty")
                )
                Assert.assertEquals(
                    (exception as SkyflowError).getInternalErrorMessage().trim(),
                    skyflowError.getInternalErrorMessage().trim()
                )
            }
        })
    }

    @Test
    fun testEmptyVaultIDWithSkyflowElement() {
        val skyflowConfiguration = Configuration(
            "",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        val skyflow = Client(skyflowConfiguration)
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number"
        )
        val card_number = container.create(activity, collectInput1, options)
        card_number.inputField.setText("4111111111111111")
        card_number.state = StateforText(card_number)
        activity.addContentView(card_number, layoutParams)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)
                Assert.assertEquals(
                    (exception as SkyflowError).getInternalErrorMessage(),
                    skyflowError.getInternalErrorMessage()
                )
            }
        })

    }

    @Test
    fun testEmptyVaultURLWithSkyflowElement() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098d2c09",
            "",
            AccessTokenProvider()
        )
        val skyflow = Client(configuration)
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number", altText = "41111"
        )
        val collectInput2 = CollectElementInput(
            "cards",
            "expiry_date",
            SkyflowElementType.EXPIRATION_DATE,
            label = "expire date"
        )
        val card_number = container.create(activity, collectInput1, options)
        val expire = container.create(activity, collectInput2, options)

        card_number.inputField.setText("4111111111111111")
        expire.inputField.setText("11/22")
        card_number.state = StateforText(card_number)
        expire.state = StateforText(expire)
        activity.addContentView(card_number, layoutParams)
        activity.addContentView(expire, layoutParams)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)
                Assert.assertEquals(
                    (exception as SkyflowError).getInternalErrorMessage(),
                    skyflowError.getInternalErrorMessage()
                )
            }


        })
    }

    @Test
    fun testInvalidVaultURLWithSkyflowElement() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098d2c09",
            "http://www.google.com",
            AccessTokenProvider()
        )
        val skyflow = Client(configuration)
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number"
        )
        val collectInput2 = CollectElementInput(
            "cards",
            "expiry_date",
            SkyflowElementType.EXPIRATION_DATE,
            label = "expire date"
        )

        val card_number = container.create(activity, collectInput1, options)
        val expire = container.create(activity, collectInput2, options)

        card_number.inputField.setText("4111111111111111")
        expire.inputField.setText("11/22")
        card_number.state = StateforText(card_number)
        expire.state = StateforText(expire)
        activity.addContentView(card_number, layoutParams)
        activity.addContentView(expire, layoutParams)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(
                    SkyflowErrorCode.INVALID_VAULT_URL,
                    params = arrayOf(configuration.vaultURL)
                )
                Assert.assertEquals(
                    (exception as SkyflowError).getInternalErrorMessage(),
                    skyflowError.getInternalErrorMessage()
                )
            }


        })
    }

    @Test
    fun testNullTableName() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            null, "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val card_number = container.create(activity, collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number, layoutParams)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(
                    SkyflowErrorCode.MISSING_TABLE_IN_ELEMENT,
                    params = arrayOf(card_number.fieldType.toString())
                )
                Assert.assertEquals(
                    (exception as SkyflowError).getInternalErrorMessage(),
                    skyflowError.getInternalErrorMessage()
                )
            }


        })
    }

    @Test
    fun testEmptyTableName() {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30, 20, 20, 20)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val card_number = container.create(activity, collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number, layoutParams)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                Log.d("exc", (exception as SkyflowError).getInternalErrorMessage())
                val skyflowError = SkyflowError(
                    SkyflowErrorCode.ELEMENT_EMPTY_TABLE_NAME,
                    params = arrayOf(card_number.fieldType.toString())
                )
                Assert.assertEquals(
                    (exception as SkyflowError).getInternalErrorMessage(),
                    skyflowError.getInternalErrorMessage()
                )
            }


        })
    }

    @Test
    fun testNullColumnName() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", null,
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val card_number = container.create(activity, collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number, layoutParams)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(
                    SkyflowErrorCode.MISSING_COLUMN,
                    params = arrayOf(card_number.fieldType.toString())
                )
                Assert.assertEquals(
                    (exception as SkyflowError).getInternalErrorMessage(),
                    skyflowError.getInternalErrorMessage()
                )
            }


        })

    }

    @Test
    fun testEmptyColumnName() {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30, 20, 20, 20)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", "",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val card_number = container.create(activity, collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number, layoutParams)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(
                    SkyflowErrorCode.EMPTY_COLUMN_NAME,
                    params = arrayOf(card_number.fieldType.toString())
                )
                Assert.assertEquals(
                    (exception as SkyflowError).getInternalErrorMessage(),
                    skyflowError.getInternalErrorMessage()
                )
            }


        })
    }

    @Test
    fun testEmptyStyles() {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30, 20, 20, 20)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", "",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val card_number = container.create(activity, collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number, layoutParams)
        Assert.assertEquals(2, card_number.collectInput.inputStyles.base.borderWidth)
    }

    @Test
    fun testEmptyStyle() {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30, 20, 20, 20)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", "",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number", inputStyles = Styles()
        )
        val card_number = container.create(activity, collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number, layoutParams)
        Assert.assertEquals(2, card_number.collectInput.inputStyles.base.borderWidth)
    }

    @Test
    fun testNullStyle() {
        val container = skyflow.container(ContainerType.COLLECT)
        val padding = Padding(30, 20, 20, 20)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", "",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number", inputStyles = Styles(null)
        )
        val card_number = container.create(activity, collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number, layoutParams)
        Assert.assertEquals(2, card_number.collectInput.inputStyles.base.borderWidth)
        card_number.setErrorText("error occured")
        Assert.assertTrue(card_number.error.text.toString().equals("error occured"))
        card_number.unmount()
        Assert.assertTrue(card_number.actualValue.isEmpty())
    }

    @Test
    fun testOnReadyListener() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false, enableCardIcon = true)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        var onReadyCalled = false
        val card_number = container.create(activity, collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number, layoutParams)
        card_number.on(EventName.FOCUS) { state ->
            Log.d("state", state.toString())
        }

        card_number.on(EventName.BLUR) { state ->
            Log.d("state", state.toString())
        }

        card_number.on(EventName.CHANGE) { state ->
            Log.d("state", state.toString())
        }

        card_number.on(EventName.READY) { state ->
            Log.d("state", state.toString())
            onReadyCalled = true
        }
        Assert.assertFalse(onReadyCalled)
        card_number.setupField(collectInput, options)
        Assert.assertEquals(card_number.isRequired, false)
    }


    @Test
    fun testValidCollect() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val card_number = container.create(activity, collectInput, options) as? TextField
        card_number!!.inputField.setText("4111 1111 1111 1111")
        activity.addContentView(card_number, layoutParams)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                //its valid
            }


        })
    }

    @Test
    fun testCreateSkyflowElement() {
        val padding = Padding(10, 10, 5, 5)
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val styles = Styles(base = Style(padding = padding))
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number", inputStyles = styles
        )
        val card_number = container.create(activity, collectInput, options) as? TextField
        card_number!!.inputField.setText("4111")
        activity.addContentView(card_number, layoutParams)
        card_number.inputField.setText("4111 1111 1111 1111")
        Assert.assertEquals(card_number.getValue(), "4111 1111 1111 1111")
        Assert.assertEquals(10, card_number.collectInput.inputStyles.base.padding.top)
        Assert.assertEquals(10, card_number.collectInput.inputStyles.base.padding.left)
        Assert.assertEquals(5, card_number.collectInput.inputStyles.base.padding.right)
        Assert.assertEquals(5, card_number.collectInput.inputStyles.base.padding.bottom)
        TestCase.assertNotNull(card_number.validate())
    }

    @Test
    fun testCheckCollectContainer() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", "expiry_date",
            SkyflowElementType.EXPIRATION_DATE, label = "expiry date"
        )
        val date = container.create(activity, collectInput, options) as? TextField
        Assert.assertEquals(container.collectElements.count(), 1)
        Assert.assertTrue(container.collectElements[0].fieldType == SkyflowElementType.EXPIRATION_DATE)
    }

    @Test
    fun testCollectElementNotMounted() {

        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cardNumber = container.create(activity, collectInput)
        TestCase.assertEquals(false, Utils.checkIfElementsMounted(cardNumber))
    }


    @Test
    fun testCollectElementMounted() {

        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cardNumber = container.create(activity, collectInput)
        activity.addContentView(cardNumber, layoutParams)
        TestCase.assertEquals(true, Utils.checkIfElementsMounted(cardNumber))
    }

    //collectapicallback

    @Test
    fun testCollectApiCallback() {
        val apiClient = APIClient(
            "b359c43f1b84f098d2c09193",
            "https://vaulturl.com/v1/vaults",
            AccessTokenProvider(),
            LogLevel.ERROR
        )
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "careeds")
        val fields = JSONObject()
        fields.put("fullname", "san")
        fields.put("card_number", "41111111111")
        fields.put("expiry_date", "11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        val collectAPICallback = CollectAPICallback(apiClient, records, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                //valid request
            }
        }, InsertOptions(), LogLevel.ERROR)

        collectAPICallback.onSuccess("token")

    }


    @Test
    fun testOnfailureInCollectApiCallback() {
        val apiClient =
            APIClient("78789", "https://vaulturl.com", AccessTokenProvider(), LogLevel.ERROR)
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        val fields = JSONObject()
        fields.put("fullname", "san")
        fields.put("card_number", "41111111111")
        fields.put("expiry_date", "11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        val collectAPICallback = CollectAPICallback(apiClient, records, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
                val skyflowError = SkyflowError(
                    SkyflowErrorCode.INVALID_VAULT_URL,
                    params = arrayOf(apiClient.vaultURL)
                )
                TestCase.assertEquals(
                    skyflowError.getInternalErrorMessage(),
                    (exception as Exception).message.toString()
                )
            }

        }, InsertOptions(), LogLevel.ERROR)

        collectAPICallback.onSuccess("token")

    }

    @Test
    fun testBuildResponse() {
        val apiClient =
            APIClient("78789", "https://vaulturl.com", AccessTokenProvider(), LogLevel.ERROR)
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        val fields = JSONObject()
        fields.put("fullname", "san")
        fields.put("card_number", "41111111111")
        fields.put("expiry_date", "11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        val collectAPICallback = CollectAPICallback(apiClient, records, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
            }

        }, InsertOptions(), LogLevel.ERROR)

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
    fun testBuildResponseWithoutTokens() {
        val apiClient =
            APIClient("78789", "https://vaulturl.com", AccessTokenProvider(), LogLevel.ERROR)
        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table", "cards")
        val fields = JSONObject()
        fields.put("fullname", "san")
        fields.put("card_number", "41111111111")
        fields.put("expiry_date", "11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        val collectAPICallback = CollectAPICallback(apiClient, records, object : Callback {
            override fun onSuccess(responseBody: Any) {
            }

            override fun onFailure(exception: Any) {
            }

        }, InsertOptions(false), LogLevel.ERROR)

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
    fun testduplicateElement() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "cvv",
            SkyflowElementType.CVV, placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cvv = container.create(activity, collectInput, CollectElementOptions())
        val cvv1 = container.create(activity, collectInput, CollectElementOptions())
        val card_number = container.create(activity, collectInput1, CollectElementOptions())

        try {
            CollectRequestBody.createRequestBody(
                container.collectElements,
                JSONObject(),
                LogLevel.ERROR
            )
        } catch (e: Exception) {
            val skyflowError = SkyflowError(
                SkyflowErrorCode.DUPLICATE_COLUMN_FOUND,
                params = arrayOf(collectInput.table, collectInput.column)
            )
            TestCase.assertEquals(
                skyflowError.getInternalErrorMessage(),
                (e as SkyflowError).getInternalErrorMessage()
            )
        }

    }

    @Test
    fun testEmptyTableInAdditionalFields() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "cvv",
            SkyflowElementType.CVV, placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cvv = container.create(activity, collectInput, CollectElementOptions())
        val card_number = container.create(activity, collectInput1, CollectElementOptions())

        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        record.put("table", "")
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date", "11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records, LogLevel.ERROR)
        } catch (exception: Exception) {
            val skyflowError = SkyflowError(
                SkyflowErrorCode.ADDITIONAL_FIELDS_EMPTY_TABLE_KEY,
                params = arrayOf("0")
            )
            TestCase.assertEquals(
                skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage()
            )
        }
    }

    @Test
    fun testMissingTableInAdditionalFields() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "cvv",
            SkyflowElementType.CVV, placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cvv = container.create(activity, collectInput, CollectElementOptions())
        val card_number = container.create(activity, collectInput1, CollectElementOptions())

        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        //fields.put("table", "")
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date", "11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records, LogLevel.ERROR)
        } catch (exception: Exception) {
            val skyflowError =
                SkyflowError(
                    SkyflowErrorCode.ADDITIONAL_FIELDS_TABLE_KEY_NOT_FOUND,
                    params = arrayOf("0")
                )
            TestCase.assertEquals(
                skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage()
            )
        }
    }


    @Test
    fun testInvalidTableInAdditionalFields() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "cvv",
            SkyflowElementType.CVV, placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cvv = container.create(activity, collectInput, CollectElementOptions())
        val card_number = container.create(activity, collectInput1, CollectElementOptions())

        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        record.put("table", JSONObject())
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date", "11/22")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records, LogLevel.ERROR)
        } catch (exception: Exception) {
            val skyflowError =
                SkyflowError(
                    SkyflowErrorCode.ADDITIONAL_FIELDS_INVALID_TABLE_NAME,
                    params = arrayOf("0")
                )
            TestCase.assertEquals(
                skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage()
            )
        }
    }

    @Test
    fun testMissingFieldsInAdditionalFields() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "cvv",
            SkyflowElementType.CVV, placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cvv = container.create(activity, collectInput, CollectElementOptions())
        val card_number = container.create(activity, collectInput1, CollectElementOptions())

        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        record.put("table", "cards")
        fields.put("cardNumber", "41111111111")
        fields.put("expiry_date", "11/22")
        // record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records, LogLevel.ERROR)
        } catch (exception: Exception) {
            val skyflowError =
                SkyflowError(
                    SkyflowErrorCode.ADDITIONAL_FIELDS_FIELDS_KEY_NOT_FOUND,
                    params = arrayOf("0")
                )
            TestCase.assertEquals(
                skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage()
            )
        }
    }


    @Test
    fun testEmptyFieldsInAdditionalFields() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "cvv",
            SkyflowElementType.CVV, placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cvv = container.create(activity, collectInput, CollectElementOptions())
        val card_number = container.create(activity, collectInput1, CollectElementOptions())

        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        record.put("table", "cards")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records, LogLevel.ERROR)
        } catch (exception: Exception) {
            val skyflowError =
                SkyflowError(SkyflowErrorCode.ADDITIONAL_FIELDS_EMPTY_FIELDS, params = arrayOf("0"))
            TestCase.assertEquals(
                skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage()
            )
        }
    }

    @Test
    fun testEmptyRecordsInAdditionalFields() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "cvv",
            SkyflowElementType.CVV, placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cvv = container.create(activity, collectInput, CollectElementOptions())
        val card_number = container.create(activity, collectInput1, CollectElementOptions())

        val records = JSONObject()
        val recordsArray = JSONArray()
        records.put("records", recordsArray)

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records, LogLevel.ERROR)
        } catch (exception: Exception) {
            val skyflowError = SkyflowError(SkyflowErrorCode.ADDITIONAL_FIELDS_EMPTY_RECORDS)
            TestCase.assertEquals(
                skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage()
            )
        }
    }

    @Test
    fun testInvalidRecordsInAdditionalFields() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "cvv",
            SkyflowElementType.CVV, placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cvv = container.create(activity, collectInput, CollectElementOptions())
        val card_number = container.create(activity, collectInput1, CollectElementOptions())

        val records = JSONObject()
        records.put("records", JSONObject())

        try {
            CollectRequestBody.createRequestBody(container.collectElements, records, LogLevel.ERROR)
        } catch (exception: Exception) {
            val skyflowError = SkyflowError(SkyflowErrorCode.ADDITIONAL_FIELDS_INVALID_RECORDS)
            TestCase.assertEquals(
                skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage()
            )
        }
    }


    @Test
    fun testDuplicatesInAdditionalFields() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "cvv",
            SkyflowElementType.CVV, placeholder = "cvv"
        )
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cvv =
            container.create(activity, collectInput, CollectElementOptions(enableCardIcon = false))
        val card_number = container.create(activity, collectInput1, CollectElementOptions())

        val records = JSONObject()
        val recordsArray = JSONArray()
        val record = JSONObject()
        val fields = JSONObject()
        record.put("table", "cards")
        fields.put("card_number", "123")
        record.put("fields", fields)
        recordsArray.put(record)
        records.put("records", recordsArray)
        try {
            CollectRequestBody.createRequestBody(container.collectElements, records, LogLevel.ERROR)
        } catch (exception: Exception) {
            val skyflowError = SkyflowError(
                SkyflowErrorCode.DUPLICATE_COLUMN_FOUND,
                params = arrayOf(collectInput1.table, collectInput1.column)
            )
            TestCase.assertEquals(
                skyflowError.getInternalErrorMessage(),
                (exception as SkyflowError).getInternalErrorMessage()
            )
        }
    }

    //end collect

    @Test
    fun testmmyyExpireDate() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "expire date",
            SkyflowElementType.EXPIRATION_DATE, placeholder = "expire date"
        )
        val expireDate =
            container.create(activity, collectInput, CollectElementOptions(format = "mm/yy"))
        activity.addContentView(expireDate, layoutParams)
        expireDate.inputField.setText("12/39")
        expireDate.state = StateforText(expireDate)
        Assert.assertTrue(expireDate.state.getInternalState().get("isValid") as Boolean)

        expireDate.inputField.setText("12/20")
        expireDate.state = StateforText(expireDate)
        Assert.assertFalse(expireDate.state.getInternalState().get("isValid") as Boolean)

    }

    @Test
    fun testmmyyyyExpireDate() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "expire date",
            SkyflowElementType.EXPIRATION_DATE, placeholder = "expire date"
        )
        val expireDate =
            container.create(activity, collectInput, CollectElementOptions(format = "mm/yyyy"))
        activity.addContentView(expireDate, layoutParams)
        expireDate.inputField.setText("12/2039")
        expireDate.state = StateforText(expireDate)
        Assert.assertTrue(expireDate.state.getInternalState().get("isValid") as Boolean)

        expireDate.inputField.setText("12/20njn")
        expireDate.state = StateforText(expireDate)
        Assert.assertFalse(expireDate.state.getInternalState().get("isValid") as Boolean)

    }

    @Test
    fun testyymmExpireDate() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "expire date",
            SkyflowElementType.EXPIRATION_DATE, placeholder = "expire date"
        )
        val expireDate =
            container.create(activity, collectInput, CollectElementOptions(format = "yy/mm"))
        activity.addContentView(expireDate, layoutParams)
        expireDate.inputField.setText("39/12")
        expireDate.state = StateforText(expireDate)
        Assert.assertTrue(expireDate.state.getInternalState().get("isValid") as Boolean)

        expireDate.inputField.setText("12/20")
        expireDate.state = StateforText(expireDate)
        Assert.assertFalse(expireDate.state.getInternalState().get("isValid") as Boolean)

    }

    @Test
    fun testyyyymmExpireDate() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "expire date",
            SkyflowElementType.EXPIRATION_DATE, placeholder = "expire date"
        )
        val expireDate =
            container.create(activity, collectInput, CollectElementOptions(format = "yyyy/mm"))
        activity.addContentView(expireDate, layoutParams)
        expireDate.inputField.setText("2039/12")
        expireDate.state = StateforText(expireDate)
        Assert.assertTrue(expireDate.state.getInternalState().get("isValid") as Boolean)

        expireDate.inputField.setText("12/20")
        expireDate.state = StateforText(expireDate)
        Assert.assertFalse(expireDate.state.getInternalState().get("isValid") as Boolean)

    }

    @Test
    fun testSetAndResetError() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "expire date",
            SkyflowElementType.EXPIRATION_DATE, placeholder = "expire date"
        )
        val expireDate =
            container.create(activity, collectInput, CollectElementOptions(format = "yyyy/mm"))
        activity.addContentView(expireDate, layoutParams)
        expireDate.inputField.setText("209/12")
        expireDate.state = StateforText(expireDate)
        expireDate.setError("custom error")
        Assert.assertEquals("custom error", expireDate.getErrorText())

        expireDate.resetError()
        Assert.assertEquals("INVALID_EXPIRE_DATE", expireDate.getErrorText())


    }


    @Test
    fun testValidElementMatchRuleWithDuplicateTableAndColumn() //valid
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "pin",
            SkyflowElementType.EXPIRATION_DATE, placeholder = "enter pin"
        )
        val pin = container.create(activity, collectInput)
        val validationSet = ValidationSet()
        validationSet.add(ElementValueMatchRule(pin, "not matched with pin"))
        val collectInput1 = CollectElementInput(
            "cards",
            "pin",
            SkyflowElementType.EXPIRATION_DATE,
            placeholder = "confirm pin",
            validations = validationSet
        )
        val confirmPin = container.create(activity, collectInput1)

        activity.addContentView(pin, layoutParams)
        activity.addContentView(confirmPin, layoutParams)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {

            }

            override fun onFailure(exception: Any) {
                Assert.assertEquals("no error", (exception as Exception).message.toString())
            }

        })
    }

    @Test
    fun testClearValue() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "pin",
            SkyflowElementType.EXPIRATION_DATE, placeholder = "enter pin"
        )
        val pin = container.create(activity, collectInput)
        pin.setText("1234")

        Assert.assertEquals(pin.actualValue, "1234")
        pin.clearValue()
    }

    @Test
    fun testCopyWhenEnableCopyIsTrue() {
        val container = skyflow.container(ContainerType.COLLECT)
        val options = CollectElementOptions(enableCopy = true)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "XXXX"
        )
        val cardNumber = container.create(activity, collectInput, options)
        cardNumber.onAttachedToWindow()
        cardNumber.setText("4111111111111110")
        Assert.assertNull(cardNumber.inputField.compoundDrawablesRelative[2])
        cardNumber.setText("4111111111111111")
        Assert.assertNotNull(cardNumber.inputField.compoundDrawablesRelative[2])
    }

    @Test
    fun testCopyAndEnableCopyIsFalse() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "XXXX"
        )
        val cardNumber = container.create(activity, collectInput)
        cardNumber.onAttachedToWindow()
        cardNumber.setText("4111111111111110")
        Assert.assertNull(cardNumber.inputField.compoundDrawablesRelative[2])
        cardNumber.setText("4111111111111111")
        Assert.assertNull(cardNumber.inputField.compoundDrawablesRelative[2])
    }

    @Test
    fun testCollectElementStyles() {
        val container = skyflow.container(ContainerType.COLLECT)
        val baseStyle = Style(
            backgroundColor = Color.CYAN,
            borderColor = Color.BLUE,
            borderWidth = 3,
            cornerRadius = 25f,
            margin = Margin(15, 15, 15, 15),
            padding = Padding(15, 15, 15, 15),
            placeholderColor = Color.parseColor("#eeaaee"),
            textAlignment = Gravity.START,
            textColor = Color.GREEN
        )
        val inputStyles = Styles(base = baseStyle)
        val collectInput = CollectElementInput(
            table = "cards",
            column = "card_number",
            inputStyles = inputStyles,
            type = SkyflowElementType.CARD_NUMBER,
            placeholder = "XXXX"
        )
        val cardNumber = container.create(activity, collectInput)
        cardNumber.onAttachedToWindow()
        Assert.assertEquals(15, cardNumber.inputField.paddingLeft)
        Assert.assertEquals(15, cardNumber.inputField.paddingTop)
        Assert.assertEquals(15, cardNumber.inputField.paddingRight)
        Assert.assertEquals(15, cardNumber.inputField.paddingBottom)
        Assert.assertEquals(Color.parseColor("#eeaaee"), cardNumber.inputField.currentHintTextColor)
        Assert.assertEquals(Color.GREEN, cardNumber.inputField.currentTextColor)
    }

    @Test
    fun testUpdateCollectElementOptions() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            table = "cards",
            column = "card_number",
            type = SkyflowElementType.CARD_NUMBER,
            placeholder = "XXXX"
        )
        val options = CollectElementOptions(required = true, enableCopy = true)
        val cardNumber = container.create(activity, collectInput, options)
        cardNumber.onAttachedToWindow()

        cardNumber.on(EventName.CHANGE) { state ->
            val value = state.get("value")
            val cards = getCardSchemes(value.toString().length)
            cardNumber.update(CollectElementOptions(cardMetadata = CardMetadata(cards)))
        }

        var state = StateforText(cardNumber)
        Assert.assertTrue(state.getInternalState().getBoolean("isRequired"))
        Assert.assertNull(cardNumber.inputField.compoundDrawablesRelative[2])

        cardNumber.setText("5428480012345671")
        state = StateforText(cardNumber)
        Assert.assertTrue(state.getInternalState().getBoolean("isRequired"))
        Assert.assertNotNull(cardNumber.inputField.compoundDrawablesRelative[2])
        Assert.assertEquals(CardType.MASTERCARD, cardNumber.cardType)
    }
}


class AccessTokenProvider : TokenProvider {
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
        } catch (e: Exception) {

        }
    }
}