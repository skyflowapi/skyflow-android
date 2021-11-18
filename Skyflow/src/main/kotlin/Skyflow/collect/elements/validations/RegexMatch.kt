package  com.Skyflow.collect.elements.validations

import Skyflow.collect.elements.validations.SkyflowInternalValidationProtocol
import java.util.regex.Pattern

/**
Validate input in scope of length.
 */
class RegexMatch(var regex:String, override var error: SkyflowValidationError = "") : SkyflowValidationProtocol,SkyflowInternalValidationProtocol {


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