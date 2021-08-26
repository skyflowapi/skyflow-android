package com.skyflow_android.core

import Type
import com.skyflow_android.collect.client.InsertOptions
import com.skyflow_android.collect.elements.validations.*
import com.skyflow_android.core.container.Container
import com.skyflow_android.core.container.ContainerProtocol
import com.skyflow_android.core.protocol.SkyflowCallback
import com.skyflow_android.reveal.client.RevealOptions
import com.skyflow_android.reveal.client.RevealRequestRecord
import org.json.JSONObject
import kotlin.reflect.KClass

class Skyflow (
    configuration: SkyflowConfiguration
){

//    enum class SkyflowElementType{
//        /// Field type that requires Cardholder Name input formatting and validation.
//        CARDHOLDERNAME,
//
//        /// Field type that requires Credit Card Number input formatting and validation.
//        CARDNUMBER,
//
//        /// Field type that requires Expire Date input formatting and validation.
//        EXPIRATIONDATE,
//
//        /// Field type that requires Card CVV input formatting and validation.
//        CVV;
//
//        fun getType(): Type {
//            val rules = SkyflowValidationSet()
//            when (this) {
//                CARDHOLDERNAME -> {
//                    rules.add(
//                        SkyflowValidatePattern("^([a-zA-Z0-9\\ \\,\\.\\-\\']{2,})$",
//                            SkyflowValidationErrorType.pattern.rawValue)
//                    )
//                    return Type("", "^([a-zA-Z0-9\\ \\,\\.\\-\\']{2,})$",
//                        rules, ".alphabet")
//                }
//                CARDNUMBER -> {
//                    rules.add(SkyflowValidateCardNumber(SkyflowValidationErrorType.cardNumber.rawValue))
//                    return Type("#### #### #### ####",
//                        "^(?:4[0-9]{12}(?:[0-9]{3})?|[25][1-7][0-9]{14}|6(?:011|5[0-9][0-9])[0-9]{12}|3[47][0-9]{13}|3(?:0[0-5]|[68][0-9])[0-9]{11}|(?:2131|1800|35\\d{3})\\d{11})$",
//                        rules, ".numberPad")
//                }
//                CVV -> {
//                    rules.add(
//                        SkyflowValidatePattern("\\d*$",
//                            SkyflowValidationErrorType.pattern.rawValue)
//                    )
//                    rules.add(
//                        SkyflowValidateLengthMatch(intArrayOf(3, 4),
//                            SkyflowValidationErrorType.lengthMathes.rawValue)
//                    )
//                    return Type("####", "\\d*$",
//                        rules, ".numberPad")
//                }
//                EXPIRATIONDATE -> {
//                    rules.add(
//                        SkyflowValidatePattern("^(0[1-9]|1[0-2])\\/?([0-9]{4}|[0-9]{2})$",
//                            SkyflowValidationErrorType.pattern.rawValue)
//                    )
//                    rules.add(
//                        SkyflowValidateExpirationDate(
//                            SkyflowValidationErrorType.expireDate.rawValue
//                        )
//                    )
//                    return Type("##/##", "^(0[1-9]|1[0-2])\\/?([0-9]{4}|[0-9]{2})$",
//                        rules, ".numberPad")
//                }
//            }
//        }
//    }

    private val apiClient = APIClient(configuration.vaultId, configuration.workspaceUrl, configuration.tokenProvider)

    fun insert(records:  String, options: InsertOptions? = InsertOptions(), callback: SkyflowCallback){
        apiClient.post(records, callback, options!!)
    }

    fun <T:ContainerProtocol> container(type: KClass<T>) : Container<T>{
        return Container<T>(this)
    }


    fun get(records: String, options: RevealOptions? = RevealOptions(), callback: SkyflowCallback)   {
        val jsonobj = JSONObject(records)
        val obj = jsonobj.getJSONArray("records")
        val list = mutableListOf<RevealRequestRecord>()
        var i = 0
        while(i<obj.length())
        {
            val jsonobj1 = obj.getJSONObject(i)
            list.add(RevealRequestRecord(jsonobj1.get("id").toString(),jsonobj1.get("redaction").toString()))
            i++
        }
        this.apiClient.get(list,callback)
    }
}