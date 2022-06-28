/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package Skyflow.collect.elements.validations

import Skyflow.Element
import com.Skyflow.collect.elements.validations.SkyflowValidationError
import com.Skyflow.collect.elements.validations.ValidationRule

class ElementValueMatchRule(var element:Element, override var error: SkyflowValidationError = "validation failed"): ValidationRule,SkyflowInternalValidationProtocol {
    override fun validate(text: String?): Boolean {
        return element.getValue().equals(text)
    } }