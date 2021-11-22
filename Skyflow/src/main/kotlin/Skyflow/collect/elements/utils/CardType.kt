package Skyflow.collect.elements.utils

import com.skyflow_android.R
import java.util.regex.Pattern

internal class Card
    (var defaultName:String,var regex: String,var minCardLength: Int,var maxCardLength: Int,
     var formatPattern: String,var securityCodeLength: Int,var securityCodeName: String,image:Int)
{
}

enum class  CardType (var defaultName:String,var regex: String,var minCardLength: Int,var maxCardLength: Int,
                      var formatPattern: String,var securityCodeLength: Int,var securityCodeName: String,var image:Int)
{

    VISA(
        "Visa" , "^4\\d*", 13, 19, "#### #### #### #### ###",
        3, SecurityCode.cvv.rawValue, R.drawable.ic_visa),
    MASTERCARD(
        "MasterCard","^(5[1-5]|222[1-9]|22[3-9]|2[3-6]|27[0-1]|2720)\\d*",
        16,16,"#### #### #### ####",
        3,  SecurityCode.cvc.rawValue, R.drawable.ic_mastercard),
    DISCOVER(
        "Discover", "^(6011|65|64[4-9]|622)\\d*",
        16,16,"#### #### #### ####",
        3, SecurityCode.cid.rawValue, R.drawable.ic_discover),
    AMEX(
        "Amex","^3[47]\\d*",
        15,15,"#### ###### #####",
        4, SecurityCode.cid.rawValue, R.drawable.ic_amex),
    DINERS_CLUB(
        "Diners Club","^(36|38|30[0-5])\\d*",
        14,16,"#### ###### #####",
        3, SecurityCode.cvv.rawValue, R.drawable.ic_diners),
    JCB(
        "JCB","^35\\d*",
        16,19,"#### #### #### #### ###",
        3, SecurityCode.cvv.rawValue, R.drawable.ic_jcb),
    MAESTRO(
        "Maestro","^(5018|5020|5038|5043|5[6-9]|6020|6304|6703|6759|676[1-3])\\d*",
        12,19,"#### #### #### #### ###",
        3, SecurityCode.cvc.rawValue, R.drawable.ic_maestro),
    UNIONPAY(
        "UnionPay","^62\\d*",
        16,19,"#### #### #### #### ###",
        3, SecurityCode.cvn.rawValue, R.drawable.ic_unionpay),
    HIPERCARD(
        "HiperCard","^606282\\d*",
        14, 19,"#### #### #### #### ###",
        3, SecurityCode.cvc.rawValue, R.drawable.ic_hypercard),
    UNKNOWN(
        "Unknown","\\d+",
        12, 19,"#### #### #### #### ###",
        3, SecurityCode.cvv.rawValue, R.drawable.ic_emptycard),
    EMPTY(
        "Empty","^$",
        12,19,"#### #### #### #### ###",
        3, SecurityCode.cvv.rawValue, R.drawable.ic_emptycard);


    companion object
    {
        private val AMEX_SPACE_INDICES = intArrayOf(4, 10)
        private val DEFAULT_SPACE_INDICES = intArrayOf(4, 8, 12)

        fun forCardNumber(cardNumber: String) : CardType {
            val strippedCardNumber = cardNumber.replace("-","").replace(" ", "")
            val patternMatch = forCardPattern(cardNumber)
            return if (patternMatch.defaultName != "Empty" && patternMatch.defaultName != "Unknown") {
                patternMatch
            } else {
                EMPTY
            }
        }


        private fun forCardPattern(cardNumber: String) : CardType {
            val cards = enumValues<CardType>()
            cards.forEach {
                val pattern = Pattern.compile(it.regex)
                if (pattern.matcher(cardNumber).matches() && cardNumber.length <= it.maxCardLength) {
                    return it
                }
            }
            return EMPTY
        }
    }
    open fun getSpaceIndices(): IntArray {
        return if (this == AMEX) CardType.AMEX_SPACE_INDICES else CardType.DEFAULT_SPACE_INDICES
    }
}


internal enum class SecurityCode(var rawValue:String) {
    cvv("cvv"),
    cvc("cvc"),
    cvn("cvn"),
    cid("cid")
}