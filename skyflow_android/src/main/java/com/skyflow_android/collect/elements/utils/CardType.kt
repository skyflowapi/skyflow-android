package com.skyflowandroid.collect.elements.utils

import android.text.TextUtils
import com.skyflow_android.R
import java.util.regex.Pattern

enum class  CardType(
    var regex: String, var minCardLength: Int, var maxCardLength: Int, var securityCodeLength: Int,
    var securityCodeName: Int
) {

    VISA(
        "^4\\d*",
        16, 16,
        3, R.string.bt_cvv
    ),
    MASTERCARD(
        "^(5[1-5]|222[1-9]|22[3-9]|2[3-6]|27[0-1]|2720)\\d*",
        16, 16,
        3, R.string.bt_cvc
    ),
    DISCOVER(
        "^(6011|65|64[4-9]|622)\\d*",
        16, 16,
        3, R.string.bt_cid
    ),
    AMEX(
        "^3[47]\\d*",
        15, 15,
        4, R.string.bt_cid
    ),
    DINERS_CLUB(
        "^(36|38|30[0-5])\\d*",
        14, 14,
        3, R.string.bt_cvv
    ),
    JCB(
        "^35\\d*",
        16, 16,
        3, R.string.bt_cvv
    ),
    MAESTRO(
        "^(5018|5020|5038|5043|5[6-9]|6020|6304|6703|6759|676[1-3])\\d*",
        12, 19,
        3, R.string.bt_cvc
    ),
    UNIONPAY(
        "^62\\d*",
        16, 19,
        3, R.string.bt_cvn
    ),
    HIPER(
        "^637(095|568|599|609|612)\\d*",
        16, 16,
        3, R.string.bt_cvc
    ),
    HIPERCARD(
        "^606282\\d*",
        16, 16,
        3, R.string.bt_cvc
    ),
    UNKNOWN(
        "\\d+",
        12, 19,
        3, R.string.bt_cvv
    ),
    EMPTY(
        "^$",
        12, 19,
        3, R.string.bt_cvv
    );



    companion object {

        private val AMEX_SPACE_INDICES = intArrayOf(4, 10)
        private val DEFAULT_SPACE_INDICES = intArrayOf(4, 8, 12)

        open fun forCardNumber(cardNumber: String): CardType {
            val patternMatch = forCardNumberPattern(cardNumber)
            return if (patternMatch != EMPTY && patternMatch != UNKNOWN) {
                return patternMatch
            } else EMPTY
        }


        private fun forCardNumberPattern(cardNumber: String): CardType {
            for (cardType in values()) {
                var pattern = Pattern.compile(cardType.regex)
                if (pattern.matcher(cardNumber).matches()) {
                    return cardType
                }
            }
            return EMPTY
        }



    }

    open fun validate(cardNumber: String): Boolean {
        if (TextUtils.isEmpty(cardNumber)) {
            return false
        } else if (!TextUtils.isDigitsOnly(cardNumber)) {
            return false
        }
        val numberLength = cardNumber.length
        if (numberLength < minCardLength || numberLength > maxCardLength) {
            return false
        } else if (!Pattern.compile(regex).matcher(cardNumber).matches() ) {
            return false
        }
        return isLuhnValid(cardNumber)
    }

    private fun isLuhnValid(cardNumber: String?): Boolean {
        val reversed = StringBuffer(cardNumber).reverse().toString()
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



    open fun getSpaceIndices(): IntArray {
        return if (this == AMEX) AMEX_SPACE_INDICES else DEFAULT_SPACE_INDICES
    }
}