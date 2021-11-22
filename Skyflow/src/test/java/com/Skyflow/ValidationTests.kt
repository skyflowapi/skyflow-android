package com.Skyflow

import android.util.Log
import com.Skyflow.collect.elements.validations.*
import org.junit.Assert.assertEquals
import org.junit.Test


class ValidationTests{

    @Test
    fun testValidateFunction(){
        val skyflowValidationSet = SkyflowValidationSet()
        val regexMatch = RegexMatch("[0-9][A-Za-z]", "Regex validation Failed")
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
        val match = LengthMatch(2,10,"failed")
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
}