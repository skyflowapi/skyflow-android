/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package  com.Skyflow.collect.elements.validations

import Skyflow.collect.elements.validations.SkyflowInternalValidationProtocol
import java.util.regex.Pattern

/**
Validate input in scope of length.
 */
class RegexMatchRule(var regex:String, override var error: SkyflowValidationError = "validation failed") : ValidationRule,SkyflowInternalValidationProtocol {


    /// validate length of text
    override fun validate(text: String?) : Boolean {

        if(text!!.isEmpty())
        {
            return true
        }
        val pattern = Pattern.compile(this.regex)
        return pattern.matcher(text).matches()
    }

}