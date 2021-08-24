package  com.skyflow_android.collect.elements.validations

/**
Validate input in scope of length.
 */
internal class SkyflowValidateLengthMatch( val lengths:IntArray,override var error: SkyflowValidationError) : SkyflowValidationProtocol() {


    /// validate length of text
    override fun validate(text: String?) : Boolean {
        if(text!!.isEmpty())
        {
            return true
        }
        return lengths.contains(text.length)
    }


}