package Skyflow.collect.elements.utils

import com.skyflow_android.R
import java.util.regex.Pattern

internal class Card
    (var defaultName:String,var regex: String,var cardLength:IntArray,
     var formatPattern: IntArray,var securityCodeLength: Int,var securityCodeName: String,image:Int)
{
}

enum class  CardType (var defaultName:String,var regex: String,var cardLength:IntArray,
                      var formatPattern: IntArray,var securityCodeLength: Int,var securityCodeName: String,var image:Int)
{

    VISA(
        "Visa" , "^4\\d*", intArrayOf(13,16), intArrayOf(4,8,12),
        3, SecurityCode.cvv.rawValue, R.drawable.ic_visa),
    MASTERCARD(
        "Mastercard","^(5[1-5]|222[1-9]|22[3-9]|2[3-6]|27[0-1]|2720)\\d*",
        intArrayOf(16),intArrayOf(4,8,12),
        3,  SecurityCode.cvc.rawValue, R.drawable.ic_mastercard),
    DISCOVER(
        "Discover", "^(6011|65|64[4-9]|622)\\d*",
        intArrayOf(16,17,18,19),intArrayOf(4,8,12,16),
        3, SecurityCode.cid.rawValue, R.drawable.ic_discover),
    AMEX(
        "Amex","^3[47]\\d*",
        intArrayOf(15),intArrayOf(4,10),
        4, SecurityCode.cid.rawValue, R.drawable.ic_amex),
    DINERS_CLUB(
        "DinersClub","^(36|38|30[0-5])\\d*",
        intArrayOf(14,15,16,17,18,19),intArrayOf(4,10),
        3, SecurityCode.cvv.rawValue, R.drawable.ic_diners),
    JCB(
        "Jcb","^35\\d*",
        intArrayOf(16,17,18,19),intArrayOf(4,8,12,16),
        3, SecurityCode.cvv.rawValue, R.drawable.ic_jcb),
    MAESTRO(
        "Maestro","^(5018|5020|5038|5043|5[6-9]|6020|6304|6703|6759|676[1-3])\\d*",
        intArrayOf(12,13,14,15,16,17,18,19),intArrayOf(4,8,12,16),
        3, SecurityCode.cvc.rawValue, R.drawable.ic_maestro),
    UNIONPAY(
        "Unionpay","^62\\d*",
        intArrayOf(16,17,18,19),intArrayOf(4,8,12,16),
        3, SecurityCode.cvn.rawValue, R.drawable.ic_unionpay),
    HIPERCARD(
        "Hipercard","^606282\\d*",
        intArrayOf(14,15,16,17,18,19),intArrayOf(4,8,12,16),
        3, SecurityCode.cvc.rawValue, R.drawable.ic_hypercard),
    CARTES_BANCAIRES("Cartes Bancaires", "^4\\d*",
        intArrayOf(13,16), intArrayOf(4,8,12), 3,
        SecurityCode.cvv.rawValue, R.drawable.ic_cartes_bancaires),
    UNKNOWN(
        "Unknown","\\d+",
        intArrayOf(8,9,10,11,12,13,14,15,16,17,18,19),intArrayOf(4,8,12,16),
        3, SecurityCode.cvv.rawValue, R.drawable.ic_emptycard),
    EMPTY(
        "Empty","^$",
        intArrayOf(12,13,14,15,16,17,18,19),intArrayOf(4,8,12,16),
        3, SecurityCode.cvv.rawValue, R.drawable.ic_emptycard);


    companion object
    {
        private val AMEX_SPACE_INDICES = intArrayOf(4, 10)
        private val DEFAULT_SPACE_INDICES = intArrayOf(4, 8, 12)

        fun forCardNumber(cardNumber: String) : CardType {
            val patternMatch = forCardPattern(cardNumber.replace(" ", "").replace("-", ""))
            if (patternMatch.defaultName != "Empty") {
                return patternMatch
            }
            else
            {
                return EMPTY
            }
        }


        private fun forCardPattern(cardNumber: String) : CardType {
            val cards = enumValues<CardType>()
            if(cardNumber.isEmpty()) return EMPTY
            cards.forEach {
                val pattern = Pattern.compile(it.regex)
                if (pattern.matcher(cardNumber).matches() && cardNumber.length <= it.cardLength.get(it.cardLength.size-1) && !it.equals(UNKNOWN)) {
                    return it
                }
            }
            return UNKNOWN
        }
        fun getBin(cardNumber: String) : String {
            var binCount = 8
            val card = forCardNumber(cardNumber);
            if(card === AMEX) binCount = 6;
            var result = ""
            var numbers = 0
            for(char in cardNumber) {
                if(numbers >= binCount) {
                    result += "X"
                } else {
                    numbers += 1
                    result += char
                }
            }
            return result
        }

        internal fun getCardType(name: String): CardType {
            val cards = enumValues<CardType>()
            cards.forEach {
                if (it.defaultName == name) {
                    return it
                }
            }
            return UNKNOWN
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