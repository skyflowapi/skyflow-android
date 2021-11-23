package com.Skyflow

import Skyflow.*
import Skyflow.collect.elements.validations.ElementValueMatchRule
import Skyflow.core.elements.state.StateforText
import android.app.Activity
import com.Skyflow.collect.elements.validations.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class ValidationTests{

    lateinit var skyflow : Client
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity
    @Before
    fun setup() {
        val configuration = Configuration(
            "b359c43f1b844ff4bea0f098",
            "https://vaulturl.com",
            AccessTokenProvider()
        )
        skyflow = Client(configuration)
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()
    }
    @Test
    fun testValidateFunction(){
        val skyflowValidationSet = SkyflowValidationSet()
        val regexMatch = RegexMatchRule("[0-9][A-Za-z]", "Regex validation Failed")
        skyflowValidationSet.add(regexMatch)
        val sampleSuccessInput = "1e"
        val sampleErrorInput = "e1"
        val successResponse = SkyflowValidator.validate(sampleSuccessInput, skyflowValidationSet)
        val errors = SkyflowValidator.validate(sampleErrorInput, skyflowValidationSet)
        assertEquals(successResponse, mutableListOf<SkyflowValidationError>())
        assertEquals(0, successResponse.size)
        assertEquals(1, errors.size)
        assertEquals("Regex validation Failed", errors[0])
    }

    @Test
    fun testLengthMatch()
    {
        val match = LengthMatchRule(2,10,"failed")
        val successInput = 209
        val failedInput = 2
        val skyflowValidationSet = SkyflowValidationSet()
        skyflowValidationSet.add(match)
        assertEquals(0,SkyflowValidator.validate(successInput.toString(),skyflowValidationSet).size)
        assertEquals(1,SkyflowValidator.validate(failedInput.toString(),skyflowValidationSet).size)
        assertEquals("failed",SkyflowValidator.validate(failedInput.toString(),skyflowValidationSet).get(0))
        assertEquals(0,SkyflowValidator.validate("",skyflowValidationSet).size) //empty string
    }

    @Test
    fun testLengthMatchForArray()
    {
        val match = SkyflowValidateLengthMatch(intArrayOf(2,3,4),"failed")
        val successInput = 209
        val failedInput = 2
        val skyflowValidationSet = SkyflowValidationSet()
        skyflowValidationSet.add(match)
        assertEquals(0,SkyflowValidator.validate(successInput.toString(),skyflowValidationSet).size)
        assertEquals(1,SkyflowValidator.validate(failedInput.toString(),skyflowValidationSet).size)
        assertEquals("failed",SkyflowValidator.validate(failedInput.toString(),skyflowValidationSet).get(0))
        assertEquals(0,SkyflowValidator.validate("",skyflowValidationSet).size) //empty string

    }

    @Test
    fun testValidateCardNumber()
    {
        val match = SkyflowValidateCardNumber("failed")
        val successInput = "4111111111111111"
        val failedInput = "1111"
        val failedInputWithAlphabets = "xyz"
        val skyflowValidationSet = SkyflowValidationSet()
        skyflowValidationSet.add(match)
        assertEquals(0,SkyflowValidator.validate(successInput,skyflowValidationSet).size)
        assertEquals(1,SkyflowValidator.validate(failedInput,skyflowValidationSet).size)
        assertEquals("failed",SkyflowValidator.validate(failedInput,skyflowValidationSet).get(0))
        assertEquals(1,SkyflowValidator.validate(failedInputWithAlphabets,skyflowValidationSet).size)
        assertEquals("failed",SkyflowValidator.validate(failedInputWithAlphabets,skyflowValidationSet).get(0))
        assertEquals(0,SkyflowValidator.validate("",skyflowValidationSet).size) // empty string
    }

    @Test
    fun testValidateExpiryDate()
    {
        val match = SkyflowValidateExpirationDate("failed")
        val failedInput = "1111" //not in expire date format
        val failedInputWithAlphabets = "xy/xz" //no digits
        val failedInput2 = "11/20"
        val skyflowValidationSet = SkyflowValidationSet()
        skyflowValidationSet.add(match)
        assertEquals(1,SkyflowValidator.validate(failedInput,skyflowValidationSet).size)
        assertEquals("failed",SkyflowValidator.validate(failedInput,skyflowValidationSet).get(0))
        assertEquals(1,SkyflowValidator.validate(failedInputWithAlphabets,skyflowValidationSet).size)
        assertEquals("failed",SkyflowValidator.validate(failedInputWithAlphabets,skyflowValidationSet).get(0))
        assertEquals(0,SkyflowValidator.validate("",skyflowValidationSet).size) // empty string
        assertEquals(1,SkyflowValidator.validate(failedInput2,skyflowValidationSet).size) // empty string
    }

    @Test
    fun testElementMatchRule()
    {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards","PIN",
            SkyflowElementType.PIN,placeholder = "enter pin"
        )
        val pin = container.create(activity,collectInput) as? TextField
        pin!!.inputField.setText("4111111")
        pin.actualValue = "4111111"
        val validationSet = SkyflowValidationSet()
        validationSet.add(ElementValueMatchRule(pin,"not matched"))
        val collectInput1 = CollectElementInput("cards","PIN",
            SkyflowElementType.PIN,placeholder = "confirm pin",validations = validationSet
        )
        val confirmPin = container.create(activity,collectInput1) as? TextField
        confirmPin!!.inputField.setText("11111")
        assertEquals("not matched",confirmPin.validate().get(0))

        confirmPin.inputField.setText("4111111")
        confirmPin.state = StateforText(confirmPin)
        assertTrue(confirmPin.validate().isEmpty())
    }
}