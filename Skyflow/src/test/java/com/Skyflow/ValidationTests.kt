/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package com.Skyflow

import Skyflow.*
import Skyflow.collect.elements.validations.ElementValueMatchRule
import android.app.Activity
import com.Skyflow.collect.elements.validations.*
import org.junit.Assert.assertEquals
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
        val skyflowValidationSet = ValidationSet()
        val regexMatch = RegexMatchRule("[0-9][A-Za-z]", "Regex validation Failed")
        skyflowValidationSet.add(regexMatch)
        val sampleSuccessInput = "1e"
        val sampleErrorInput = "e1"
        val successResponse = SkyflowValidator.validate(sampleSuccessInput, skyflowValidationSet)
        val error = SkyflowValidator.validate(sampleErrorInput, skyflowValidationSet)
        assertEquals(successResponse, "")
        assertEquals("Regex validation Failed", error)
    }

    @Test
    fun testLengthMatch()
    {
        val match = LengthMatchRule(2,10,"failed")
        val successInput = 209
        val failedInput = 2
        val skyflowValidationSet = ValidationSet()
        skyflowValidationSet.add(match)
        assertEquals("",SkyflowValidator.validate(successInput.toString(),skyflowValidationSet))
        assertEquals("failed",SkyflowValidator.validate(failedInput.toString(),skyflowValidationSet))
        assertEquals("",SkyflowValidator.validate("",skyflowValidationSet)) //empty string
    }

    @Test
    fun testLengthMatchForArray()
    {
        val match = SkyflowValidateLengthMatch(intArrayOf(2,3,4),"failed")
        val successInput = 209
        val failedInput = 2
        val skyflowValidationSet = ValidationSet()
        skyflowValidationSet.add(match)
        assertEquals("failed",SkyflowValidator.validate(failedInput.toString(),skyflowValidationSet))
        assertEquals("",SkyflowValidator.validate("",skyflowValidationSet)) //empty string

    }

    @Test
    fun testValidateCardNumber()
    {
        val match = SkyflowValidateCardNumber("failed")
        val successInput = "4111111111111111"
        val failedInput = "1111"
        val failedInputWithAlphabets = "xyz"
        val skyflowValidationSet = ValidationSet()
        skyflowValidationSet.add(match)
        assertEquals("failed",SkyflowValidator.validate(failedInput,skyflowValidationSet))
        assertEquals("failed",SkyflowValidator.validate(failedInputWithAlphabets,skyflowValidationSet))
        assertEquals("",SkyflowValidator.validate("",skyflowValidationSet)) // empty string
    }

    @Test
    fun testValidateExpiryDate()
    {
        val match = SkyflowValidateExpireDate("MM/YY","failed")
        val failedInput = "1111" //not in expire date format
        val failedInputWithAlphabets = "xy/xz" //no digits
        val failedInput2 = "11/20"
        val skyflowValidationSet = ValidationSet()
        skyflowValidationSet.add(match)
        assertEquals("failed",SkyflowValidator.validate(failedInput,skyflowValidationSet))
        assertEquals("failed",SkyflowValidator.validate(failedInputWithAlphabets,skyflowValidationSet))
    }

    @Test
    fun testElementMatchRule() {
        val container = skyflow.container(ContainerType.COLLECT)
        val collectInput = CollectElementInput("cards", "PIN",
            SkyflowElementType.PIN, placeholder = "enter pin"
        )
        val pin = container.create(activity, collectInput) as? TextField
        pin!!.inputField.setText("4111111")
        pin.actualValue = "4111111"
        val validationSet = ValidationSet()
        validationSet.add(ElementValueMatchRule(pin, "not matched"))
        val collectInput1 = CollectElementInput("cards", "PIN",
            SkyflowElementType.PIN, placeholder = "confirm pin", validations = validationSet
        )
        val confirmPin = container.create(activity, collectInput1) as? TextField
        confirmPin!!.inputField.setText("11111")
        assertEquals("not matched", confirmPin.validate())
    }
}