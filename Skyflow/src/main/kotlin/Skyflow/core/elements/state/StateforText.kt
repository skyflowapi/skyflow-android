package Skyflow.core.elements.state

import Skyflow.TextField
import com.Skyflow.collect.elements.validations.SkyflowValidationError
import Skyflow.State

class StateforText(tf: TextField) : State(tf.columnName, tf.isRequired) {

    var isValid:Boolean = false
    var isEmpty:Boolean = false
    var inputLength:Int = 0
    var validationErrors:MutableList<SkyflowValidationError> = mutableListOf()

    init {
        validationErrors = tf.validate()
        isValid = validationErrors.count() == 0
        isEmpty = (tf.inputField.text!!.length == 0)
        inputLength = tf.inputField.length()
    }

    override fun show():String
    {
        var result = ""

        result = """
                                "$columnName": {
                                    "isRequired": $isRequired,
                                    "isValid": $isValid,
                                    "isEmpty": $isEmpty,
                                    "validationErrors": $validationErrors,
                                    "inputLength": $inputLength
                                }
                                """
        return result
    }

    override fun getState() : HashMap<String,Any>
    {
        val result = HashMap<String,Any>()
        result["isRequired"] = isRequired!!
        result["columnName"] = columnName
        result["isEmpty"] = isEmpty
        result["isValid"] = isValid
        result["inputLength"] = inputLength
        result["validationErrors"] = validationErrors.toString()

        return result
    }

}