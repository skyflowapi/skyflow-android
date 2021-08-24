package com.skyflow_android.core.elements.state

import com.skyflow_android.collect.elements.SkyflowTextField
import com.skyflow_android.collect.elements.validations.SkyflowValidationError

class StateforText(tf: SkyflowTextField) : State(tf.columnName, tf.isRequired) {

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