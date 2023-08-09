package com.Skyflow

import Skyflow.*
import Skyflow.composable.*
import Skyflow.core.Messages
import Skyflow.core.elements.state.StateforText
import Skyflow.utils.EventName
import Skyflow.utils.Utils
import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowLog
import java.util.*

@RunWith(RobolectricTestRunner::class)
class ComposableElementsTests {
    private lateinit var skyflow: Client
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity
    private lateinit var layoutParams: ViewGroup.LayoutParams

    private fun dispatchKeyEvent(element: TextField) {
        element.inputField.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
    }

    @Before
    fun setup() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        skyflow = Client(configuration)
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()

    }

    @Test
    fun testValidValueForCardNumber() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val collectInput = CollectElementInput(
            table = "cards", column = "card_number",
            type = SkyflowElementType.CARD_NUMBER,
            placeholder = "card number"
        )

        val cardNumber = container.create(activity, collectInput)
        cardNumber.inputField.setText("4111 1111 1111 1111")

        val state = StateforText(cardNumber).getInternalState()
        Assert.assertTrue(state["isValid"] as Boolean)
    }

    @Test
    fun testPINElement() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val collectInput = CollectElementInput(
            "cards", "PIN",
            SkyflowElementType.PIN, placeholder = "pin"
        )
        val pin = container.create(activity, collectInput)

        pin.setText("4111111")
        var state = StateforText(pin).getInternalState()
        Assert.assertTrue(state["isValid"] as Boolean)

        pin.setText("411")
        state = StateforText(pin).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean)

        pin.setText("xyzzz")
        state = StateforText(pin).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean) //accepts only numbers
    }

    @Test
    fun testExpireMonthElement() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
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
    fun testExpireYearElement() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
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
        )

        longYear.setText("year")
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
    fun testExpireDateElement() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val collectInput = CollectElementInput(
            "cards", "year",
            SkyflowElementType.EXPIRATION_DATE, placeholder = "year"
        )
        val expDate = container.create(
            activity,
            collectInput,
            CollectElementOptions(format = "mm/yyyy")
        )

        expDate.setText("12/2030")
        var state = StateforText(expDate).getInternalState()
        Assert.assertTrue(state["isValid"] as Boolean)

        expDate.setText("12/2080")
        state = StateforText(expDate).getInternalState()
        Assert.assertFalse(state["isValid"] as Boolean)
    }

    @Test
    fun testUnknownCardTypeForCardNumberElement() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
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
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cardNumber = container.create(activity, collectInput)

        var state = StateforText(cardNumber).getInternalState()
        Assert.assertTrue(state["isEmpty"] as Boolean)

        cardNumber.inputField.setText("4111 1111 1111 1111")

        state = StateforText(cardNumber).getInternalState()
        Assert.assertFalse(state["isEmpty"] as Boolean)
    }

    @Test
    fun testCheckElementsArray() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1, 2))
        )
        val options = CollectElementOptions(false)

        val cardNumberInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number"
        )

        val pinInput = CollectElementInput(
            "cards", "pin",
            SkyflowElementType.PIN, label = "pin"
        )

        val cardNumber = container.create(activity, cardNumberInput, options)
        val pin = container.create(activity, pinInput, options)

        Assert.assertEquals(2, container.collectElements.count())
        Assert.assertTrue(container.collectElements[0].fieldType == SkyflowElementType.CARD_NUMBER)
        Assert.assertTrue(container.collectElements[1].fieldType == SkyflowElementType.PIN)
    }

    @Test
    fun testStylesNotPassedOnElement() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", "",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cardNumber = container.create(activity, collectInput, options)

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        Assert.assertEquals(Color.BLACK, cardNumber.collectInput.inputStyles.base.borderColor)
        Assert.assertEquals(20f, cardNumber.collectInput.inputStyles.base.cornerRadius)
        Assert.assertEquals(2, cardNumber.collectInput.inputStyles.base.borderWidth)
        Assert.assertEquals(Typeface.NORMAL, cardNumber.collectInput.inputStyles.base.font)
        Assert.assertEquals(Gravity.LEFT, cardNumber.collectInput.inputStyles.base.textAlignment)
        Assert.assertEquals(Color.BLACK, cardNumber.collectInput.inputStyles.base.textColor)
        Assert.assertEquals(
            ViewGroup.LayoutParams.MATCH_PARENT,
            cardNumber.collectInput.inputStyles.base.width
        )
        Assert.assertEquals(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            cardNumber.collectInput.inputStyles.base.height
        )
    }

    @Test
    fun testEmptyStylesOnElement() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER,
            placeholder = "card number",
            inputStyles = Styles()
        )
        val cardNumber = container.create(activity, collectInput, options)

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        Assert.assertEquals(Color.BLACK, cardNumber.collectInput.inputStyles.base.borderColor)
        Assert.assertEquals(20f, cardNumber.collectInput.inputStyles.base.cornerRadius)
        Assert.assertEquals(2, cardNumber.collectInput.inputStyles.base.borderWidth)
        Assert.assertEquals(Typeface.NORMAL, cardNumber.collectInput.inputStyles.base.font)
        Assert.assertEquals(Gravity.LEFT, cardNumber.collectInput.inputStyles.base.textAlignment)
        Assert.assertEquals(Color.BLACK, cardNumber.collectInput.inputStyles.base.textColor)
        Assert.assertEquals(
            ViewGroup.LayoutParams.MATCH_PARENT,
            cardNumber.collectInput.inputStyles.base.width
        )
        Assert.assertEquals(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            cardNumber.collectInput.inputStyles.base.height
        )
    }

    @Test
    fun testNullStylesOnElement() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER,
            placeholder = "card number",
            inputStyles = Styles(null)
        )
        val cardNumber = container.create(activity, collectInput, options)

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        Assert.assertEquals(Color.BLACK, cardNumber.collectInput.inputStyles.base.borderColor)
        Assert.assertEquals(20f, cardNumber.collectInput.inputStyles.base.cornerRadius)
        Assert.assertEquals(2, cardNumber.collectInput.inputStyles.base.borderWidth)
        Assert.assertEquals(Typeface.NORMAL, cardNumber.collectInput.inputStyles.base.font)
        Assert.assertEquals(Gravity.LEFT, cardNumber.collectInput.inputStyles.base.textAlignment)
        Assert.assertEquals(Color.BLACK, cardNumber.collectInput.inputStyles.base.textColor)
        Assert.assertEquals(
            ViewGroup.LayoutParams.MATCH_PARENT,
            cardNumber.collectInput.inputStyles.base.width
        )
        Assert.assertEquals(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            cardNumber.collectInput.inputStyles.base.height
        )
    }

    @Test
    fun testSetResetAndUnmountOnElement() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val options = CollectElementOptions(true)
        val collectInput = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER,
            placeholder = "card number",
            inputStyles = Styles(null)
        )
        val cardNumber = container.create(activity, collectInput, options)

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        cardNumber.setErrorText("error occurred")
        Assert.assertTrue(cardNumber.error.text.toString() == "error occurred")

        cardNumber.resetError()
        Assert.assertTrue(cardNumber.error.text.toString() == "value is empty")

        cardNumber.unmount()
        Assert.assertTrue(cardNumber.actualValue.isEmpty())
    }

    @Test
    fun testSetAndClearValueOnElement() {
        val config = Configuration(
            vaultID = "vault_id",
            vaultURL = "vault_url",
            tokenProvider = AccessTokenProvider(),
            Options(env = Env.DEV)
        )
        val skyflow = init(config)
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val options = CollectElementOptions(true)
        val collectInput = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER,
            placeholder = "card number",
            inputStyles = Styles(null)
        )
        val cardNumber = container.create(activity, collectInput, options)

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        cardNumber.setValue("custom value")
        Assert.assertTrue(cardNumber.inputField.text.toString() == "custom value")

        cardNumber.clearValue()
        Assert.assertTrue(cardNumber.inputField.text.toString().isEmpty())
    }

    @Test
    fun testGetComposableLayout() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1, 1))
        )
        val options = CollectElementOptions(true)
        val collectInput = CollectElementInput(
            "cards",
            "card_number",
            SkyflowElementType.CARD_NUMBER,
            placeholder = "card number",
            inputStyles = Styles(null)
        )
        val cardNumber = container.create(activity, collectInput, options)

        val skyflowError = SkyflowError(SkyflowErrorCode.MISMATCH_ELEMENT_COUNT_LAYOUT_SUM)

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Assert.assertEquals(
                skyflowError.getInternalErrorMessage(),
                (e as SkyflowError).getInternalErrorMessage()
            )
        }
    }

    @Test
    fun testCollectElementMounted() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val collectInput = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cardNumber = container.create(activity, collectInput)
        try {
            val composableLayout = container.getComposableLayout()
            Assert.assertFalse(Utils.checkIfElementsMounted(cardNumber))
            activity.addContentView(composableLayout, layoutParams)
            Assert.assertTrue(Utils.checkIfElementsMounted(cardNumber))
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }
    }

    @Test
    fun testContainerInsertElementNotMounted() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1, 1))
        )
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(
            "cards", "card_number", SkyflowElementType.CARD_NUMBER, label = "card number"
        )
        val collectInput2 = CollectElementInput(
            "cards", "cvv", SkyflowElementType.CVV, label = "cvv"
        )
        val cardNumber = container.create(activity, collectInput1, options)
        val cvv = container.create(activity, collectInput2, options)

        cardNumber.inputField.setText("4111 1111 1111 1111")
        cvv.inputField.setText("2")
        cardNumber.state = StateforText(cardNumber)
        cvv.state = StateforText(cvv)

        try {
            val composableLayout = container.getComposableLayout()
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }


        val skyflowError = SkyflowError(
            SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
            params = arrayOf(cardNumber.columnName)
        )

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {}

            override fun onFailure(exception: Any) {
                Assert.assertEquals(
                    skyflowError.getInternalErrorMessage().trim(),
                    (exception as SkyflowError).getInternalErrorMessage().trim()
                )
                Assert.assertEquals(400, skyflowError.getErrorcode())
            }
        })
    }

    @Test
    fun testContainerInsertMixedInvalidInput() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1, 1))
        )
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number"
        )
        val collectInput2 = CollectElementInput(
            "cards", "cvv", SkyflowElementType.CVV, label = "cvv"
        )
        val cardNumber = container.create(activity, collectInput1, options)
        val cvv = container.create(activity, collectInput2, options)

        cardNumber.inputField.setText("4111 1111 1111 1111")
        cardNumber.actualValue = "4111 1111 1111 1111"
        cvv.inputField.setText("142")

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        cardNumber.state = StateforText(cardNumber)
        cardNumber.state.show()
        cvv.state = StateforText(cvv)

        val skyflowError = SkyflowError(
            SkyflowErrorCode.INVALID_INPUT,
            params = arrayOf("for cvv value is empty")
        )

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {}

            override fun onFailure(exception: Any) {
                Assert.assertEquals(
                    skyflowError.getInternalErrorMessage().trim(),
                    (exception as SkyflowError).getInternalErrorMessage().trim()
                )
            }
        })
    }

    @Test
    fun testContainerInsertIsRequiredAndEmpty() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(2))
        )
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number"
        )
        val collectInput2 = CollectElementInput(
            "cards", "cvv", SkyflowElementType.CVV, label = "cvv"
        )

        val cardNumber = container.create(activity, collectInput1, options)
        val cvv = container.create(activity, collectInput2, options)
        cvv.inputField.setText("123")
        cvv.actualValue = "123"
        cvv.state = StateforText(cvv)

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        val skyflowError = SkyflowError(
            SkyflowErrorCode.INVALID_INPUT,
            params = arrayOf("for card_number value is empty")
        )

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {}

            override fun onFailure(exception: Any) {
                Assert.assertEquals(
                    skyflowError.getInternalErrorMessage().trim(),
                    (exception as SkyflowError).getInternalErrorMessage().trim()
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
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number"
        )
        val cardNumber = container.create(activity, collectInput1, options)
        cardNumber.inputField.setText("4111111111111111")
        cardNumber.state = StateforText(cardNumber)

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {}

            override fun onFailure(exception: Any) {
                Assert.assertEquals(
                    skyflowError.getInternalErrorMessage(),
                    (exception as SkyflowError).getInternalErrorMessage()
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
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1, 1))
        )
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(
            "cards", "card_number",
            SkyflowElementType.CARD_NUMBER, label = "card number", placeholder = "41111"
        )
        val collectInput2 = CollectElementInput(
            "cards", "expiry_date",
            SkyflowElementType.EXPIRATION_DATE, label = "expire date"
        )
        val cardNumber = container.create(activity, collectInput1, options)
        val expire = container.create(activity, collectInput2, options)

        cardNumber.inputField.setText("4111111111111111")
        expire.inputField.setText("11/22")
        cardNumber.state = StateforText(cardNumber)
        expire.state = StateforText(expire)

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        val skyflowError = SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL)

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {}

            override fun onFailure(exception: Any) {
                Assert.assertEquals(
                    skyflowError.getInternalErrorMessage(),
                    (exception as SkyflowError).getInternalErrorMessage()
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
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1, 1))
        )
        val options = CollectElementOptions(true)
        val collectInput1 = CollectElementInput(
            "cards", "card_number", label = "card number",
            type = SkyflowElementType.CARD_NUMBER
        )
        val collectInput2 = CollectElementInput(
            "cards", "expiry_date", label = "expire date",
            type = SkyflowElementType.EXPIRATION_DATE,
        )

        val cardNumber = container.create(activity, collectInput1, options)
        val expire = container.create(activity, collectInput2, options)

        cardNumber.inputField.setText("4111111111111111")
        expire.inputField.setText("11/22")
        cardNumber.state = StateforText(cardNumber)
        expire.state = StateforText(expire)

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        val skyflowError = SkyflowError(
            SkyflowErrorCode.INVALID_VAULT_URL,
            params = arrayOf(configuration.vaultURL)
        )

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {}

            override fun onFailure(exception: Any) {
                Assert.assertEquals(
                    skyflowError.getInternalErrorMessage(),
                    (exception as SkyflowError).getInternalErrorMessage()
                )
            }
        })
    }

    @Test
    fun testContainerInsertNullTableName() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            null, "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cardNumber = container.create(activity, collectInput, options)
        cardNumber.inputField.setText("4111 1111 1111 1111")

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        val skyflowError = SkyflowError(
            SkyflowErrorCode.MISSING_TABLE_IN_ELEMENT,
            params = arrayOf(cardNumber.fieldType.toString())
        )

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {}

            override fun onFailure(exception: Any) {
                Assert.assertEquals(
                    skyflowError.getInternalErrorMessage(),
                    (exception as SkyflowError).getInternalErrorMessage()
                )
            }
        })
    }

    @Test
    fun testContainerInsertEmptyTableName() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "", "card_number",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cardNumber = container.create(activity, collectInput, options)
        cardNumber.inputField.setText("4111 1111 1111 1111")

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        val skyflowError = SkyflowError(
            SkyflowErrorCode.ELEMENT_EMPTY_TABLE_NAME,
            params = arrayOf(cardNumber.fieldType.toString())
        )

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {}

            override fun onFailure(exception: Any) {
                Assert.assertEquals(
                    skyflowError.getInternalErrorMessage(),
                    (exception as SkyflowError).getInternalErrorMessage()
                )
            }
        })
    }

    @Test
    fun testContainerInsertNullColumnName() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", null,
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cardNumber = container.create(activity, collectInput, options)
        cardNumber.inputField.setText("4111 1111 1111 1111")

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        val skyflowError = SkyflowError(
            SkyflowErrorCode.MISSING_COLUMN,
            params = arrayOf(cardNumber.fieldType.toString())
        )

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {}

            override fun onFailure(exception: Any) {
                Assert.assertEquals(
                    skyflowError.getInternalErrorMessage(),
                    (exception as SkyflowError).getInternalErrorMessage()
                )
            }
        })
    }

    @Test
    fun testContainerInsertEmptyColumnName() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )
        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", "",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cardNumber = container.create(activity, collectInput, options)
        cardNumber.inputField.setText("4111 1111 1111 1111")

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        val skyflowError = SkyflowError(
            SkyflowErrorCode.EMPTY_COLUMN_NAME,
            params = arrayOf(cardNumber.fieldType.toString())
        )

        container.collect(object : Callback {
            override fun onSuccess(responseBody: Any) {}

            override fun onFailure(exception: Any) {
                Assert.assertEquals(
                    skyflowError.getInternalErrorMessage(),
                    (exception as SkyflowError).getInternalErrorMessage()
                )
            }
        })
    }

    @Test
    fun testOnSubmitEventListenerOnElement() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1, 1))
        )

        val options = CollectElementOptions(false)
        val cardInput = CollectElementInput(
            "cards", "",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cardNumber = container.create(activity, cardInput, options)

        val pinInput = CollectElementInput(
            "cards", "",
            SkyflowElementType.PIN, placeholder = "pin"
        )
        val pin = container.create(activity, pinInput, options)

        cardNumber.inputField.setText("4111 1111 1111 1111")
        dispatchKeyEvent(cardNumber)

        pin.inputField.setText("123")
        dispatchKeyEvent(pin)

        cardNumber.on(EventName.SUBMIT) { state -> println(state) }
        pin.on(EventName.SUBMIT) { state -> println(state) }

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        val logItems = ShadowLog.getLogs()
        val tag = TextField::class.qualifiedName
        var logFound = false
        var count = 0
        for (logItem in logItems) {
            if (logItem.type == Log.ERROR) {
                Assert.assertEquals(tag, logItem.tag)
                Assert.assertEquals(Messages.INVALID_EVENT_TYPE.message, logItem.msg)
                logFound = true
                count++
            }
        }

        Assert.assertTrue(logFound)
        Assert.assertEquals(2, count)
    }

    @Test
    fun testEventListenersOnContainer() {
        val container = skyflow.container(
            ContainerType.COMPOSABLE, activity,
            ContainerOptions(layout = arrayOf(1))
        )

        container.on(EventName.SUBMIT) { println("submit event triggered") }
        container.on(EventName.READY) { println("ready event triggered") }
        container.on(EventName.FOCUS) { println("focus event triggered") }
        container.on(EventName.CHANGE) { println("change event triggered") }
        container.on(EventName.BLUR) { println("blur event triggered") }

        val options = CollectElementOptions(false)
        val collectInput = CollectElementInput(
            "cards", "",
            SkyflowElementType.CARD_NUMBER, placeholder = "card number"
        )
        val cardNumber = container.create(activity, collectInput, options)
        cardNumber.inputField.setText("4111 1111 1111 1111")
        dispatchKeyEvent(cardNumber)

        try {
            val composableLayout = container.getComposableLayout()
            activity.addContentView(composableLayout, layoutParams)
        } catch (e: Exception) {
            Log.e("COMPOSABLE LAYOUT", e.message.toString())
            Assert.fail()
        }

        val logItems = ShadowLog.getLogs()
        val tag = ComposableContainer::class.qualifiedName
        var logFound = false
        var count = 0
        for (logItem in logItems) {
            if (logItem.type == Log.ERROR) {
                Assert.assertEquals(tag, logItem.tag)
                Assert.assertEquals(Messages.INVALID_EVENT_TYPE.message, logItem.msg)
                logFound = true
                count++
            }
        }

        Assert.assertTrue(logFound)
        Assert.assertEquals(4, count)
    }

}