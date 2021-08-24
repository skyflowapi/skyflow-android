package com.skyflow_android.collect.elements.utils

import java.util.regex.Pattern

internal class Card
    (var defaultName:String,var regex: String,var minCardLength: Int,var maxCardLength: Int,
     var formatPattern: String,var securityCodeLength: Int,var securityCodeName: String)
{
}

enum class  CardType (var defaultName:String,var regex: String,var minCardLength: Int,var maxCardLength: Int,
                      var formatPattern: String,var securityCodeLength: Int,var securityCodeName: String)
{

    VISA(
        "Visa" , "^4\\d*", 13, 19, "#### #### #### #### ###",
        3, SecurityCode.cvv.rawValue),
    MASTERCARD(
        "MasterCard","^(5[1-5]|222[1-9]|22[3-9]|2[3-6]|27[0-1]|2720)\\d*",
        16,16,"#### #### #### ####",
        3,  SecurityCode.cvc.rawValue),
    DISCOVER(
        "Discover", "^(6011|65|64[4-9]|622)\\d*",
        16,16,"#### #### #### ####",
        3, SecurityCode.cid.rawValue),
    AMEX(
        "Amex","^3[47]\\d*",
        15,15,"#### ###### #####",
        4, SecurityCode.cid.rawValue),
    DINERS_CLUB(
        "Diners Club","^(36|38|30[0-5])\\d*",
        14,16,"#### ###### #####",
        3, SecurityCode.cvv.rawValue),
    JCB(
        "JCB","^35\\d*",
        16,19,"#### #### #### #### ###",
        3, SecurityCode.cvv.rawValue),
    MAESTRO(
        "Maestro","^(5018|5020|5038|5043|5[6-9]|6020|6304|6703|6759|676[1-3])\\d*",
        12,19,"#### #### #### #### ###",
        3, SecurityCode.cvc.rawValue),
    UNIONPAY(
        "UnionPay","^62\\d*",
        16,19,"#### #### #### #### ###",
        3, SecurityCode.cvn.rawValue),
    HIPERCARD(
        "HiperCard","^606282\\d*",
        14, 19,"#### #### #### #### ###",
        3, SecurityCode.cvc.rawValue),
    UNKNOWN(
        "Unknown","\\d+",
        12, 19,"#### #### #### #### ###",
        3, SecurityCode.cvv.rawValue),
    EMPTY(
        "Empty","^$",
        12,19,"#### #### #### #### ###",
        3, SecurityCode.cvv.rawValue);


    companion object
    {
        fun forCardNumber(cardNumber: String) : CardType {
            var patternMatch = forCardPattern(cardNumber)
            if (patternMatch.defaultName != "Empty" && patternMatch.defaultName != "Unknown") {
                return patternMatch
            }
            else
            {
                return EMPTY
            }
        }


        private fun forCardPattern(cardNumber: String) : CardType {
            val cards = enumValues<CardType>()
            cards.forEach {
                var pattern = Pattern.compile(it.regex)
                if (pattern.matcher(cardNumber).matches()) {
                    return it
                }
            }
            return EMPTY
        }
    }
}


internal enum class SecurityCode(var rawValue:String) {
    cvv("cvv"),
    cvc("cvc"),
    cvn("cvn"),
    cid("cid")
}