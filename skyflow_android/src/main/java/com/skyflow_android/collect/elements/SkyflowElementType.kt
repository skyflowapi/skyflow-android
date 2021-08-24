package com.skyflow_android.collect.elements

import com.skyflow_android.collect.elements.validations.*
import com.skyflow_android.collect.elements.validations.SkyflowValidateCardNumber
import com.skyflow_android.collect.elements.validations.SkyflowValidateExpirationDate
import com.skyflow_android.collect.elements.validations.SkyflowValidateLengthMatch
import com.skyflow_android.collect.elements.validations.SkyflowValidatePattern
import com.skyflow_android.collect.elements.validations.SkyflowValidationErrorType

class Type(var formatPattern:String, var regex: String,
           var validation: SkyflowValidationSet, var keyboardType: String) {

}

enum class SkyflowElementType {



    /// Field type that requires Cardholder Name input formatting and validation.
    CARDHOLDERNAME,

    /// Field type that requires Credit Card Number input formatting and validation.
    CARDNUMBER,

    /// Field type that requires Expire Date input formatting and validation.
    EXPIRATIONDATE,

    /// Field type that requires Card CVV input formatting and validation.
    CVV;


    fun getType(): Type {
        val rules = SkyflowValidationSet()
        when (this) {
            CARDHOLDERNAME -> {
                rules.add(
                    SkyflowValidatePattern("^([a-zA-Z0-9\\ \\,\\.\\-\\']{2,})$",
                    SkyflowValidationErrorType.pattern.rawValue)
                )
                return Type("", "^([a-zA-Z0-9\\ \\,\\.\\-\\']{2,})$",
                    rules, ".alphabet")
            }
            CARDNUMBER -> {
                rules.add(SkyflowValidateCardNumber(SkyflowValidationErrorType.cardNumber.rawValue))
                return Type("#### #### #### ####",
                    "^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})$",
                    rules, ".numberPad")
            }
            CVV -> {
                rules.add(
                    SkyflowValidatePattern("\\d*$",
                    SkyflowValidationErrorType.pattern.rawValue)
                )
                rules.add(
                    SkyflowValidateLengthMatch(intArrayOf(3, 4),
                    SkyflowValidationErrorType.lengthMathes.rawValue)
                )
                return Type("####", "\\d*$",
                    rules, ".numberPad")
            }
            EXPIRATIONDATE -> {
                rules.add(
                    SkyflowValidatePattern("^(0[1-9]|1[0-2])\\/?([0-9]{4}|[0-9]{2})$",
                    SkyflowValidationErrorType.pattern.rawValue)
                )
                rules.add(
                    SkyflowValidateExpirationDate(
                        SkyflowValidationErrorType.expireDate.rawValue
                    )
                )
                return Type("##/##", "^(0[1-9]|1[0-2])\\/?([0-9]{4}|[0-9]{2})$",
                    rules, ".numberPad")
            }
        }
    }

}