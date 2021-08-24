package  com.skyflow_android.collect.elements.validations

//import  com.skyflow_android.collect.elements.utils.CardType
import java.util.regex.Pattern

/**
Validate input in the scope of matching supported cards.
 */
internal class SkyflowValidateCardNumber(override var error: SkyflowValidationError) : SkyflowValidationProtocol() {

    override fun validate(text: String?) : Boolean {

        if (text!!.isEmpty()) {
            return true
        }
        val pattern = Pattern.compile("^$|^[\\s]*?([0-9]{2,6}[ -]?){3,5}[\\s]*$")
        if(!pattern.matcher(text).matches())
            return false
        return isLuhnValid(text.replace(" ", "").replace("-", ""))

    }

    /// Luhn Algorithm to validate card number
    private fun isLuhnValid(cardNumber: String?) :Boolean {
        val reversed = StringBuffer(cardNumber!!).reverse().toString()
        val len = reversed.length
        var oddSum = 0
        var evenSum = 0
        for (i in 0 until len) {
            val c = reversed[i]
            require(Character.isDigit(c)) { String.format("Not a digit: '%s'", c) }
            val digit = Character.digit(c, 10)
            if (i % 2 == 0) {
                oddSum += digit
            } else {
                evenSum += digit / 5 + 2 * digit % 10
            }
        }
        return (oddSum + evenSum) % 10 == 0
    }
}


