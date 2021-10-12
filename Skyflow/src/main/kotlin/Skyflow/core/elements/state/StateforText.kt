package Skyflow.core.elements.state

import Skyflow.SkyflowElementType
import Skyflow.TextField
import com.Skyflow.collect.elements.validations.SkyflowValidationError
import Skyflow.State
import Skyflow.core.LogLevel
import org.json.JSONObject

class StateforText internal constructor(val tf: TextField) : State(tf.columnName, tf.isRequired) {

    var isValid:Boolean = false
    var isEmpty:Boolean = false
    var inputLength:Int = 0
    var validationErrors:MutableList<SkyflowValidationError> = mutableListOf()
    var isFocused:Boolean = false
    var fieldType:SkyflowElementType


    init {
        validationErrors = tf.validate()
        isValid = validationErrors.count() == 0
        isEmpty = (tf.inputField.text!!.length == 0)
        inputLength = tf.inputField.length()
        isFocused = tf.inputField.hasFocus()
        fieldType = tf.collectInput.type
    }

    override fun show(): String {

        return """
                                "$columnName": {
                                    "isRequired": $isRequired,
                                    "isValid": $isValid,
                                    "isEmpty": $isEmpty,
                                    "validationErrors": $validationErrors,
                                    "inputLength": $inputLength
                                }
                                """
    }

    override fun getInternalState() : JSONObject
    {
        val result = JSONObject()
        result.put("isRequired", isRequired)
//        result["isRequired"] = isRequired!!
        result.put("elementType", fieldType)
        result.put("columnName", columnName)
//        result["columnName"] = columnName
        result.put("isEmpty", isEmpty)
        result.put("isValid", isValid)
        result.put("inputLength", inputLength)
//        result["inputLength"] = inputLength
        result.put("validationErrors", validationErrors.toString())
        result.put("isFocused", isFocused)

        return result
    }

    internal fun getState(logLevel: LogLevel) : JSONObject{
        val state = JSONObject()
        state.put("elementType", fieldType)
        state.put("isEmpty", isEmpty)
        state.put("isFocused", isFocused)
        state.put("isValid", isValid)
        var value = ""
        if(logLevel == LogLevel.DEBUG || logLevel == LogLevel.DEMO) {
            value =  tf.getValue()
        }
        state.put("value", value)
        return state
    }

}