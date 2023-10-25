package Skyflow

import Skyflow.collect.elements.validations.SkyflowValidateMonth
import Skyflow.collect.elements.validations.SkyflowValidateYear
import android.text.InputType
import com.Skyflow.collect.elements.validations.*
import com.Skyflow.collect.elements.validations.LengthMatchRule
import com.Skyflow.collect.elements.validations.RegexMatchRule
import com.Skyflow.collect.elements.validations.SkyflowValidateCardNumber
import com.Skyflow.collect.elements.validations.SkyflowValidateExpireDate
import com.Skyflow.collect.elements.validations.SkyflowValidateLengthMatch
import com.Skyflow.collect.elements.validations.SkyflowValidationErrorType

class Type(
    var formatPattern: String, var regex: String,
    var validation: ValidationSet, var keyboardType: Int
) {

}

enum class SkyflowElementType {


    /// Field type that requires Cardholder Name input formatting and validation.
    CARDHOLDER_NAME,

    /// Field type that requires Credit Card Number input formatting and validation.
    CARD_NUMBER,

    /// Field type that requires Expire Date input formatting and validation.
    EXPIRATION_DATE,

    /// Field type that requires Card CVV input formatting and validation.
    CVV,

    INPUT_FIELD,

    PIN,

    EXPIRATION_MONTH,

    EXPIRATION_YEAR;


    fun getType(): Type {
        val rules = ValidationSet()
        when (this) {
            CARDHOLDER_NAME -> {
                rules.add(
                    RegexMatchRule(
                        "^([a-zA-Z0-9\\ \\,\\.\\-\\']{2,})$",
                        SkyflowValidationErrorType.pattern.rawValue
                    )
                )
                return Type(
                    "", "^([a-zA-Z0-9\\ \\,\\.\\-\\']{2,})$",
                    rules, InputType.TYPE_CLASS_TEXT
                )
            }
            CARD_NUMBER -> {
                rules.add(SkyflowValidateCardNumber(SkyflowValidationErrorType.cardNumber.rawValue))
                return Type(
                    "#### #### #### ####",
                    "^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})$",
                    rules, InputType.TYPE_CLASS_NUMBER
                )
            }
            CVV -> {
                rules.add(
                    RegexMatchRule(
                        "\\d*$",
                        SkyflowValidationErrorType.pattern.rawValue
                    )
                )
                rules.add(
                    SkyflowValidateLengthMatch(
                        intArrayOf(3, 4),
                        SkyflowValidationErrorType.lengthMathes.rawValue
                    )
                )
                return Type(
                    "####", "\\d*$",
                    rules, InputType.TYPE_CLASS_NUMBER
                )
            }
            EXPIRATION_DATE -> {
                rules.add(
                    RegexMatchRule(
                        "^(0[1-9]|1[0-2])\\/?([0-9]{4}|[0-9]{2})$",
                        SkyflowValidationErrorType.pattern.rawValue
                    )
                )
                return Type(
                    "##/##", "^(0[1-9]|1[0-2])\\/?([0-9]{4}|[0-9]{2})$",
                    rules, InputType.TYPE_CLASS_DATETIME
                )
            }
            INPUT_FIELD -> {
                return Type(
                    "####", "\\d*$",
                    rules, InputType.TYPE_CLASS_TEXT
                )
            }
            PIN -> {
                rules.add(
                    LengthMatchRule(
                        4, 12, SkyflowValidationErrorType.invalidPin.rawValue
                    )
                )
                rules.add(
                    RegexMatchRule(
                        "[0-9]*",
                        SkyflowValidationErrorType.allowNumbers.rawValue
                    )
                )
                return Type(
                    "####", "\\d*$",
                    rules, InputType.TYPE_CLASS_NUMBER
                )
            }
            EXPIRATION_MONTH -> {
                rules.add(SkyflowValidateMonth(SkyflowValidationErrorType.invalidmonth.rawValue))
                return Type(
                    "##", "\\d*$",
                    rules, InputType.TYPE_CLASS_NUMBER
                )
            }
            EXPIRATION_YEAR -> {
                rules.add(
                    SkyflowValidateYear(
                        SkyflowValidationErrorType.invalidmonth.rawValue,
                        "yy"
                    )
                )
                return Type(
                    "####", "\\d*$",
                    rules, InputType.TYPE_CLASS_NUMBER
                )
            }
        }
    }

    companion object {
        fun getUnsupportedInputFormatElements(): List<SkyflowElementType> {
            return listOf(
                CARDHOLDER_NAME,
                CVV,
                PIN,
                EXPIRATION_MONTH
            )
        }

        fun getSupportedInputFormatElements(): List<SkyflowElementType> {
            return listOf(INPUT_FIELD)
        }

        fun getAutoFocusSupportedElements(): List<SkyflowElementType> {
            return listOf(CARD_NUMBER, EXPIRATION_DATE, EXPIRATION_MONTH, EXPIRATION_YEAR)
        }
    }
}