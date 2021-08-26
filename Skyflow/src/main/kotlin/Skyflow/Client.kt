package Skyflow

import com.Skyflow.core.APIClient
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONObject
import Skyflow.reveal.RevealRequestRecord
import kotlin.reflect.KClass

class Client (
    configuration: Configuration
){

//    enum class skyflow.SkyflowElementType{
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
//        fun getType(): skyflow.Type {
//            val rules = SkyflowValidationSet()
//            when (this) {
//                CARDHOLDERNAME -> {
//                    rules.add(
//                        SkyflowValidatePattern("^([a-zA-Z0-9\\ \\,\\.\\-\\']{2,})$",
//                            SkyflowValidationErrorType.pattern.rawValue)
//                    )
//                    return skyflow.Type("", "^([a-zA-Z0-9\\ \\,\\.\\-\\']{2,})$",
//                        rules, ".alphabet")
//                }
//                CARDNUMBER -> {
//                    rules.add(SkyflowValidateCardNumber(SkyflowValidationErrorType.cardNumber.rawValue))
//                    return skyflow.Type("#### #### #### ####",
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
//                    return skyflow.Type("####", "\\d*$",
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
//                    return skyflow.Type("##/##", "^(0[1-9]|1[0-2])\\/?([0-9]{4}|[0-9]{2})$",
//                        rules, ".numberPad")
//                }
//            }
//        }
//    }

    private val apiClient = APIClient(configuration.vaultId, configuration.workspaceUrl, configuration.tokenProvider)

    fun insert(records:  String, options: InsertOptions? = InsertOptions(), callback: Callback){
        apiClient.post(records, callback, options!!)
    }

    fun <T:ContainerProtocol> container(type: KClass<T>) : Container<T> {
        return Container<T>(this)
    }


    fun get(records: String, options: RevealOptions? = RevealOptions(), callback: Callback)   {
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