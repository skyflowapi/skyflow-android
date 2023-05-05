package com.Skyflow

import Skyflow.*
import Skyflow.collect.elements.utils.CardType
import Skyflow.collect.elements.utils.Spacespan
import Skyflow.core.Messages
import Skyflow.core.getMessage
import Skyflow.utils.Utils
import android.app.Activity
import android.util.Log
import android.view.KeyEvent
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.shadows.ShadowLog

@RunWith(RobolectricTestRunner::class)
class InputFormattingTest {

    private lateinit var client: Client
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity

    private lateinit var nameInput: CollectElementInput
    private lateinit var cardNumberInput: CollectElementInput
    private lateinit var pinInput: CollectElementInput
    private lateinit var cvvInput: CollectElementInput
    private lateinit var monthInput: CollectElementInput
    private lateinit var yearInput: CollectElementInput
    private lateinit var dateInput: CollectElementInput
    private lateinit var phoneInput: CollectElementInput

    private fun createCollectElements() {

        nameInput = CollectElementInput(
            table = "cards",
            column = "Name",
            SkyflowElementType.CARDHOLDER_NAME,
            placeholder = "name",
        )

        cardNumberInput = CollectElementInput(
            table = "cards",
            column = "card_number",
            SkyflowElementType.CARD_NUMBER,
            placeholder = "card number",
        )

        cvvInput = CollectElementInput(
            table = "cards",
            column = "CVV",
            SkyflowElementType.CVV,
            placeholder = "cvv",
        )

        pinInput = CollectElementInput(
            table = "cards",
            column = "PIN",
            SkyflowElementType.PIN,
            placeholder = "pin",
        )

        monthInput = CollectElementInput(
            table = "cards",
            column = "exp_month",
            SkyflowElementType.EXPIRATION_MONTH,
            placeholder = "expiry month",
        )

        yearInput = CollectElementInput(
            table = "cards",
            column = "exp_year",
            SkyflowElementType.EXPIRATION_YEAR,
            placeholder = "yyyy",
        )

        dateInput = CollectElementInput(
            table = "cards",
            column = "exp_date",
            SkyflowElementType.EXPIRATION_DATE,
            placeholder = "mm/yy",
        )

        phoneInput = CollectElementInput(
            table = "cards",
            column = "Phone Number",
            SkyflowElementType.INPUT_FIELD,
            placeholder = "phone number",
        )
    }

    private fun dispatchKeyEvent(element: TextField) {
        element.inputField.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
    }

    @Before
    fun setup() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098",
            "https://vaulturl.com",
            AccessTokenProvider(),
            options = Options(logLevel = LogLevel.DEBUG, env = Env.DEV)
        )
        client = Client(configuration)
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()
        createCollectElements()
    }

    @Test
    fun testUnsupportedElements() {
        val container = client.container(ContainerType.COLLECT)
        val options = CollectElementOptions(
            format = "+91 XXX-XXX-XXXX",
            translation = hashMapOf(Pair('X', "[A-Z]"))
        )

        val name = container.create(activity, nameInput, options) as? TextField
        name!!.onAttachedToWindow()
        name.setText("john doe")
        dispatchKeyEvent(name)

        val cvv = container.create(activity, cvvInput, options) as? TextField
        cvv!!.onAttachedToWindow()
        cvv.setText("111")
        dispatchKeyEvent(cvv)

        val pin = container.create(activity, pinInput, options) as? TextField
        pin!!.onAttachedToWindow()
        pin.setText("411")
        dispatchKeyEvent(pin)

        val month = container.create(activity, monthInput, options) as? TextField
        month!!.onAttachedToWindow()
        month.setText("11")
        dispatchKeyEvent(month)

        val elements = listOf(
            SkyflowElementType.CARDHOLDER_NAME,
            SkyflowElementType.CVV,
            SkyflowElementType.PIN,
            SkyflowElementType.EXPIRATION_MONTH
        )
        val logItems = ShadowLog.getLogs()
        val tag = Utils::class.qualifiedName
        var logFound = false
        var count = 0
        for (logItem in logItems) {
            if (logItem.type == Log.WARN) {
                Assert.assertEquals(tag, logItem.tag)
                Assert.assertEquals(
                    Messages.INPUT_FORMATTING_NOT_SUPPORTED.getMessage(elements[count].toString()),
                    logItem.msg
                )
                logFound = true
                count++
            }
        }

        Assert.assertTrue(logFound)
        Assert.assertEquals(4, count)
    }

    @Test
    fun testUnsupportedElementsDefault() {
        val container = client.container(ContainerType.COLLECT)

        val name = container.create(activity, nameInput) as? TextField
        name!!.onAttachedToWindow()
        name.setText("john doe")
        dispatchKeyEvent(name)

        val cvv = container.create(activity, cvvInput) as? TextField
        cvv!!.onAttachedToWindow()
        cvv.setText("111")
        dispatchKeyEvent(cvv)

        val pin = container.create(activity, pinInput) as? TextField
        pin!!.onAttachedToWindow()
        pin.setText("411")
        dispatchKeyEvent(pin)

        val month = container.create(activity, monthInput) as? TextField
        month!!.onAttachedToWindow()
        month.setText("11")
        dispatchKeyEvent(month)

        val logItems = ShadowLog.getLogs()
        val tag = Utils::class.qualifiedName
        var logFound = false
        var count = 0
        for (logItem in logItems) {
            if (logItem.type == Log.WARN) {
                Assert.assertEquals(tag, logItem.tag)
                logFound = true
                count++
            }
        }

        Assert.assertFalse(logFound)
        Assert.assertEquals(0, count)

        val elements = listOf(name, pin, cvv, month)
        for (element in elements) {
            Assert.assertEquals(String(), element.options.format)
            Assert.assertNull(element.options.translation)
        }
    }

    @Test
    fun testUnsupportedElementsWithTranslation() {
        val container = client.container(ContainerType.COLLECT)
        val options = CollectElementOptions(translation = hashMapOf(Pair('X', "[A-Z]")))

        val name = container.create(activity, nameInput, options) as? TextField
        name!!.onAttachedToWindow()
        name.setText("john doe")
        dispatchKeyEvent(name)

        val cvv = container.create(activity, cvvInput, options) as? TextField
        cvv!!.onAttachedToWindow()
        cvv.setText("111")
        dispatchKeyEvent(cvv)

        val pin = container.create(activity, pinInput, options) as? TextField
        pin!!.onAttachedToWindow()
        pin.setText("411")
        dispatchKeyEvent(pin)

        val month = container.create(activity, monthInput, options) as? TextField
        month!!.onAttachedToWindow()
        month.setText("11")
        dispatchKeyEvent(month)

        val elements = listOf(
            SkyflowElementType.CARDHOLDER_NAME,
            SkyflowElementType.CVV,
            SkyflowElementType.PIN,
            SkyflowElementType.EXPIRATION_MONTH
        )
        val logItems = ShadowLog.getLogs()
        val tag = Utils::class.qualifiedName
        var logFound = false
        var count = 0
        for (logItem in logItems) {
            if (logItem.type == Log.WARN) {
                Assert.assertEquals(tag, logItem.tag)
                Assert.assertEquals(
                    Messages.INPUT_FORMATTING_NOT_SUPPORTED.getMessage(elements[count].toString()),
                    logItem.msg
                )
                logFound = true
                count++
            }
        }

        Assert.assertTrue(logFound)
        Assert.assertEquals(4, count)
    }

    @Test
    fun testUnsupportedElementsWithFormat() {
        val container = client.container(ContainerType.COLLECT)
        val options = CollectElementOptions(format = "+91 XXX-XXX-XXXX")

        val name = container.create(activity, nameInput, options) as? TextField
        name!!.onAttachedToWindow()
        name.setText("john doe")
        dispatchKeyEvent(name)

        val cvv = container.create(activity, cvvInput, options) as? TextField
        cvv!!.onAttachedToWindow()
        cvv.setText("111")
        dispatchKeyEvent(cvv)

        val pin = container.create(activity, pinInput, options) as? TextField
        pin!!.onAttachedToWindow()
        pin.setText("411")
        dispatchKeyEvent(pin)

        val month = container.create(activity, monthInput, options) as? TextField
        month!!.onAttachedToWindow()
        month.setText("11")
        dispatchKeyEvent(month)

        val elements = listOf(
            SkyflowElementType.CARDHOLDER_NAME,
            SkyflowElementType.CVV,
            SkyflowElementType.PIN,
            SkyflowElementType.EXPIRATION_MONTH
        )
        val logItems = ShadowLog.getLogs()
        val tag = Utils::class.qualifiedName
        var logFound = false
        var count = 0
        for (logItem in logItems) {
            if (logItem.type == Log.WARN) {
                Assert.assertEquals(tag, logItem.tag)
                Assert.assertEquals(
                    Messages.INPUT_FORMATTING_NOT_SUPPORTED.getMessage(elements[count].toString()),
                    logItem.msg
                )
                logFound = true
                count++
            }
        }

        Assert.assertTrue(logFound)
        Assert.assertEquals(4, count)
    }

    @Test
    fun testSemiSupportedElements() {
        val container = client.container(ContainerType.COLLECT)
        val options = CollectElementOptions(
            format = "+91 XXX-XXX-XXXX",
            translation = hashMapOf(Pair('X', "[0-9]"))
        )

        val year = container.create(activity, yearInput, options) as? TextField
        year!!.onAttachedToWindow()
        year.setText("2023")
        dispatchKeyEvent(year)

        val date = container.create(activity, dateInput, options) as? TextField
        date!!.onAttachedToWindow()
        date.setText("12/23")
        dispatchKeyEvent(date)

        val elements = listOf(
            SkyflowElementType.EXPIRATION_YEAR,
            SkyflowElementType.EXPIRATION_DATE
        )

        val logItems = ShadowLog.getLogs()
        val tag = Utils::class.qualifiedName
        var logFound = false
        var count = 0
        for (i in 0 until logItems.size) {
            val logItem = logItems[i]
            if (logItem.type == Log.WARN && logItem.tag.equals(tag)) {
                Assert.assertEquals(
                    Messages.INVALID_INPUT_TRANSLATION.getMessage(elements[count].toString()),
                    logItem.msg
                )
                logFound = true
                count++
            }
        }

        Assert.assertTrue(logFound)
        Assert.assertEquals(2, count)
    }

    @Test
    fun testSemiSupportedElementsDefault() {
        val container = client.container(ContainerType.COLLECT)

        val year = container.create(activity, yearInput) as? TextField
        year!!.onAttachedToWindow()
        year.setText("2023")
        dispatchKeyEvent(year)

        val date = container.create(activity, dateInput) as? TextField
        date!!.onAttachedToWindow()
        date.setText("12/23")
        dispatchKeyEvent(date)

        val logItems = ShadowLog.getLogs()
        val tag = Utils::class.qualifiedName
        var logFound = false
        var count = 0
        for (i in 0 until logItems.size) {
            val logItem = logItems[i]
            if (logItem.type == Log.WARN && logItem.tag.equals(tag)) {
                logFound = true
                count++
            }
        }

        Assert.assertFalse(logFound)
        Assert.assertEquals(0, count)
    }

    @Test
    fun testSupportedElements() {
        val container = client.container(ContainerType.COLLECT)

        val options = CollectElementOptions(
            format = "+91 XXX-XXX-XXXX",
            translation = hashMapOf(Pair('X', "[A-Z]"))
        )
        val phone = container.create(activity, phoneInput, options) as? TextField
        phone!!.setText("1234567890")
        dispatchKeyEvent(phone)

        val logItems = ShadowLog.getLogs()
        var logFound = false
        var count = 0
        for (logItem in logItems) {
            if (logItem.type == Log.WARN) {
                logFound = true
                count++
            }
        }

        Assert.assertFalse(logFound)
        Assert.assertEquals(0, count)
    }

    @Test
    fun testSupportedElementsDefault() {
        val container = client.container(ContainerType.COLLECT)

        val phone = container.create(activity, phoneInput) as? TextField
        phone!!.setText("1234567890")
        dispatchKeyEvent(phone)

        val logItems = ShadowLog.getLogs()
        var logFound = false
        var count = 0
        for (logItem in logItems) {
            if (logItem.type == Log.WARN) {
                logFound = true
                count++
            }
        }

        Assert.assertFalse(logFound)
        Assert.assertEquals(0, count)
    }

    @Test
    fun testSupportedElementsWithFormat() {
        val container = client.container(ContainerType.COLLECT)

        val options = CollectElementOptions(format = "+91 XXX-XXX-XXXX")
        val phone = container.create(activity, phoneInput, options) as? TextField
        phone!!.onAttachedToWindow()
        phone.setText("12345678901234avc")
        dispatchKeyEvent(phone)

        val defTranslation = hashMapOf(Pair('X', "[0-9]"))
        val logItems = ShadowLog.getLogs()
        var logFound = false
        var count = 0
        for (logItem in logItems) {
            if (logItem.type == Log.WARN) {
                Assert.assertEquals(
                    Messages.EMPTY_INPUT_TRANSLATION.getMessage(defTranslation.toString()),
                    logItem.msg
                )
                logFound = true
                count++
            }
        }

        Assert.assertTrue(logFound)
        Assert.assertEquals(1, count)
        Assert.assertEquals("+91 123-456-7890", phone.getValue())
    }

    @Test
    fun testSupportedElementsWithEmptyTranslation() {
        val container = client.container(ContainerType.COLLECT)

        val format = "+91 XXX-XXX-XXXX"
        val options = CollectElementOptions(format = format, translation = hashMapOf())
        val phone = container.create(activity, phoneInput, options) as? TextField
        phone!!.onAttachedToWindow()
        phone.setText("1234567890")
        dispatchKeyEvent(phone)

        Assert.assertEquals(format, phone.getValue())
    }

    @Test
    fun testSupportedElementsWithEmptyTranslationValues() {
        val container = client.container(ContainerType.COLLECT)

        val options = CollectElementOptions(
            format = "+91 XXX-XXX-XXXX",
            translation = hashMapOf(Pair('X', ""))
        )
        val phone = container.create(activity, phoneInput, options) as? TextField
        phone!!.onAttachedToWindow()
        phone.setText("ABC911IN#\$test12345")
        dispatchKeyEvent(phone)

        Assert.assertEquals("+91 ABC-911-IN#$", phone.getValue())
    }

    @Test
    fun testSupportedElementsWithMultipleSameTranslationKeys() {
        val container = client.container(ContainerType.COLLECT)

        val options = CollectElementOptions(
            format = "+91 XXX-XXX-XXYYXX",
            translation = hashMapOf(Pair('X', "[0-9]"), Pair('Y', "[a-z]"), Pair('X', "[A-Z]"))
        )
        val phone = container.create(activity, phoneInput, options) as? TextField
        phone!!.onAttachedToWindow()
        phone.setText("411ABCDEF812WX45pQrYZ")
        dispatchKeyEvent(phone)

        Assert.assertEquals("+91 ABC-DEF-WXprYZ", phone.getValue())
    }

    @Test
    fun testSetAndClearValueMethodsForSupportedElements() {
        val container = client.container(ContainerType.COLLECT)

        val options = CollectElementOptions(
            format = "+91 XXX-XXX-XXXX",
            translation = hashMapOf(Pair('X', "[0-9]"))
        )
        val phone = container.create(activity, phoneInput, options) as? TextField
        phone!!.onAttachedToWindow()
        phone.setValue("1234567890abc%$")
        dispatchKeyEvent(phone)
        Assert.assertEquals("+91 123-456-7890", phone.getValue())

        phone.clearValue()
        dispatchKeyEvent(phone)
        Assert.assertEquals(String(), phone.getValue())
    }

    @Test
    fun testInputFormattingForCardNumberVisa() {
        val container = client.container(ContainerType.COLLECT)

        val spaceOptions = CollectElementOptions(format = "XXXX XXXX XXXX XXXX")
        val cardNumberSpace = container.create(activity, cardNumberInput, options = spaceOptions)
        cardNumberSpace.onAttachedToWindow()
        cardNumberSpace.setValue("4111111111111111")
        dispatchKeyEvent(cardNumberSpace)

        val hyphenOptions = CollectElementOptions(format = "XXXX-XXXX-XXXX-XXXX")
        val cardNumberHyphen = container.create(activity, cardNumberInput, options = hyphenOptions)
        cardNumberHyphen.onAttachedToWindow()
        cardNumberHyphen.setValue("4111111111111111")
        dispatchKeyEvent(cardNumberHyphen)

        val invalidOptions = CollectElementOptions(format = "XXXX-XXXX/XXXX|XXXX")
        val cardNumberOther = container.create(activity, cardNumberInput, options = invalidOptions)
        cardNumberOther.onAttachedToWindow()
        cardNumberOther.setValue("4111111111111111")
        dispatchKeyEvent(cardNumberOther)

        val separators = listOf(' ', '-', ' ')
        val cardNumbers = listOf(cardNumberSpace, cardNumberHyphen, cardNumberOther)

        for ((i, cardNumber) in cardNumbers.withIndex()) {
            val editable = cardNumber.inputField.editableText;
            val spans = editable.getSpans(0, editable.length, Spacespan::class.java)
            for ((j, span) in spans.withIndex()) {
                Assert.assertEquals(separators[i].toString(), span.separator)
                Assert.assertEquals(CardType.VISA.formatPattern[j], editable.getSpanEnd(span))
            }
            Assert.assertEquals(3, spans.size)
        }
    }

    @Test
    fun testInputFormattingForCardNumberAmex() {
        val container = client.container(ContainerType.COLLECT)

        val spaceOptions = CollectElementOptions(format = "XXXX XXXX XXXX XXXX")
        val cardNumberSpace = container.create(activity, cardNumberInput, options = spaceOptions)
        cardNumberSpace.onAttachedToWindow()
        cardNumberSpace.setValue("347012345688779")
        dispatchKeyEvent(cardNumberSpace)

        val hyphenOptions = CollectElementOptions(format = "XXXX-XXXX-XXXX-XXXX")
        val cardNumberHyphen = container.create(activity, cardNumberInput, options = hyphenOptions)
        cardNumberHyphen.onAttachedToWindow()
        cardNumberHyphen.setValue("347012345688779")
        dispatchKeyEvent(cardNumberHyphen)

        val invalidOptions = CollectElementOptions(format = "XXXX-XXXX/XXXX|XXXX")
        val cardNumberOther = container.create(activity, cardNumberInput, options = invalidOptions)
        cardNumberOther.onAttachedToWindow()
        cardNumberOther.setValue("347012345688779")
        dispatchKeyEvent(cardNumberOther)

        val separators = listOf(' ', '-', ' ')
        val cardNumbers = listOf(cardNumberSpace, cardNumberHyphen, cardNumberOther)

        for ((i, cardNumber) in cardNumbers.withIndex()) {
            val editable = cardNumber.inputField.editableText;
            val spans = editable.getSpans(0, editable.length, Spacespan::class.java)
            for ((j, span) in spans.withIndex()) {
                Assert.assertEquals(separators[i].toString(), span.separator)
                Assert.assertEquals(CardType.AMEX.formatPattern[j], editable.getSpanEnd(span))
            }
            Assert.assertEquals(2, spans.size)
        }
    }

    @Test
    fun testInputFormattingForCardNumberDiscover() {
        val container = client.container(ContainerType.COLLECT)

        val spaceOptions = CollectElementOptions(format = "XXXX XXXX XXXX XXXX")
        val cardNumberSpace = container.create(activity, cardNumberInput, options = spaceOptions)
        cardNumberSpace.onAttachedToWindow()
        cardNumberSpace.setValue("6011234567891234567")
        dispatchKeyEvent(cardNumberSpace)

        val hyphenOptions = CollectElementOptions(format = "XXXX-XXXX-XXXX-XXXX")
        val cardNumberHyphen = container.create(activity, cardNumberInput, options = hyphenOptions)
        cardNumberHyphen.onAttachedToWindow()
        cardNumberHyphen.setValue("6011234567891234567")
        dispatchKeyEvent(cardNumberHyphen)

        val invalidOptions = CollectElementOptions(format = "XXXX-XXXX/XXXX|XXXX")
        val cardNumberOther = container.create(activity, cardNumberInput, options = invalidOptions)
        cardNumberOther.onAttachedToWindow()
        cardNumberOther.setValue("6011234567891234567")
        dispatchKeyEvent(cardNumberOther)

        val separators = listOf(' ', '-', ' ')
        val cardNumbers = listOf(cardNumberSpace, cardNumberHyphen, cardNumberOther)

        for ((i, cardNumber) in cardNumbers.withIndex()) {
            val editable = cardNumber.inputField.editableText;
            val spans = editable.getSpans(0, editable.length, Spacespan::class.java)
            for ((j, span) in spans.withIndex()) {
                Assert.assertEquals(separators[i].toString(), span.separator)
                Assert.assertEquals(CardType.DISCOVER.formatPattern[j], editable.getSpanEnd(span))
            }
            Assert.assertEquals(4, spans.size)
        }
    }
}