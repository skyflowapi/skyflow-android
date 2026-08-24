package  com.Skyflow.collect.elements.validations

/**
Validate input in scope of length.
 */
class LengthMatchRule(val minLength: Int, val maxLength: Int,
                      override var error: SkyflowValidationError = "validation failed") : ValidationRule {


    /// validate length of text
      override fun validate(text: String?) : Boolean {

        if(text!!.isEmpty())
        {
            return true
        }
        return text.length in minLength..maxLength
    }


}
