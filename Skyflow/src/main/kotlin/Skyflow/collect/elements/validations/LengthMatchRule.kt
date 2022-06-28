/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package  com.Skyflow.collect.elements.validations

import Skyflow.collect.elements.validations.SkyflowInternalValidationProtocol

/**
Validate input in scope of length.
 */
class LengthMatchRule(val minLength: Int, val maxLength: Int,
                      override var error: SkyflowValidationError = "validation failed") : ValidationRule,SkyflowInternalValidationProtocol {


    /// validate length of text
      override fun validate(text: String?) : Boolean {

        if(text!!.isEmpty())
        {
            return true
        }
        return text.length in minLength..maxLength
    }


}