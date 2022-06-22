/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package  com.Skyflow.collect.elements.validations

import Skyflow.collect.elements.utils.CardType
import Skyflow.collect.elements.validations.SkyflowInternalValidationProtocol
import java.util.regex.Pattern

/**
Validate input in the scope of matching supported cards.
 */
internal class SkyflowValidateCardNumber(override var error: SkyflowValidationError = "") : ValidationRule,SkyflowInternalValidationProtocol {

    override fun validate(text: String?) : Boolean {
        val cardNumber = text!!.replace(" ", "").replace("-", "")
        if (text.isEmpty()) {
            return true
        }
        val pattern = Pattern.compile("^$|^[\\s]*?([0-9]{2,6}[ -]?){3,5}[\\s]*$")
        if(!pattern.matcher(cardNumber).matches())
            return false
        val cardType = CardType.forCardNumber(text)
        if(cardType.equals(CardType.EMPTY)) return false
        else if(!cardType.cardLength.contains(cardNumber.length)) return false
        return isLuhnValid(cardNumber)
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


