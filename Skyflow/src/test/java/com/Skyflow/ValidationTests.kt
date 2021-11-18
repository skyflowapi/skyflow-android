package com.Skyflow

import com.Skyflow.collect.elements.validations.RegexMatch
import com.Skyflow.collect.elements.validations.SkyflowValidationError
import com.Skyflow.collect.elements.validations.SkyflowValidationSet
import com.Skyflow.collect.elements.validations.SkyflowValidator
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
}