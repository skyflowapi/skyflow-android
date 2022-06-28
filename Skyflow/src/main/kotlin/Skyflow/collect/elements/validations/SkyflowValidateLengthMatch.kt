/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package  com.Skyflow.collect.elements.validations

import Skyflow.collect.elements.validations.SkyflowInternalValidationProtocol

/**
Validate input in scope of length.
 */
internal class SkyflowValidateLengthMatch( val lengths:IntArray,override var error: SkyflowValidationError = "") : ValidationRule,SkyflowInternalValidationProtocol {


    /// validate length of text
    override fun validate(text: String?) : Boolean {
        if(text!!.isEmpty())
        {
            return true
        }
        return lengths.contains(text.length)
    }


}