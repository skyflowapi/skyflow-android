package Skyflow.core.elements.state

import Skyflow.Env
import Skyflow.SkyflowElementType
import Skyflow.TextField
import com.Skyflow.collect.elements.validations.SkyflowValidationError
import Skyflow.State
import Skyflow.collect.elements.utils.CardType
import org.json.JSONObject

class StateforText internal constructor(val tf: TextField) : State(tf.columnName, tf.isRequired) {

    var isValid: Boolean = false
    var isEmpty: Boolean = false
    var inputLength: Int = 0
    var validationError: SkyflowValidationError = ""
    var isFocused: Boolean = false
    var fieldType: SkyflowElementType
    var selectedCardScheme: String

    init {
        validationError = tf.validate()
        isValid = validationError.isEmpty()
        isEmpty = (tf.inputField.text!!.isEmpty())
        inputLength = tf.inputField.length()
        isFocused = tf.inputField.hasFocus()
        fieldType = tf.collectInput.type
        selectedCardScheme = getCardSchemeString()
    }

    private fun getCardSchemeString(): String {
        return if (tf.cardType === CardType.EMPTY || !tf.isCustomCardBrandSelected) ""
        else tf.cardType.defaultName.uppercase()
    }

    override fun show(): String {
        return """
            "$columnName": {
                "isRequired": $isRequired,
                "isValid": $isValid,
                "isEmpty": $isEmpty,
                "validationErrors": $validationError,
                "inputLength": $inputLength
                "selectedCardScheme": $selectedCardScheme
            }
        """
    }

    override fun getInternalState(): JSONObject {
        val result = JSONObject()

        result.put("isRequired", isRequired)
        result.put("elementType", fieldType)
        result.put("columnName", columnName)
        result.put("isEmpty", isEmpty)
        result.put("isValid", isValid)
        result.put("inputLength", inputLength)
        result.put("validationError", validationError)
        result.put("isFocused", isFocused)
        result.put("selectedCardScheme", selectedCardScheme)

        return result
    }

    internal fun getState(env: Env): JSONObject {
        val state = JSONObject()
        state.put("elementType", fieldType)
        state.put("isEmpty", isEmpty)
        state.put("isRequired", isRequired)
        state.put("isFocused", isFocused)
        state.put("isValid", isValid)
        var value = ""
        if (env == Env.DEV) {
            value = tf.getValue()
        } else if (env == Env.PROD && tf.fieldType == SkyflowElementType.CARD_NUMBER) {
            value = CardType.getBin(tf.getValue())
        }
        if (tf.fieldType == SkyflowElementType.CARD_NUMBER) {
            state.put("selectedCardScheme", selectedCardScheme)
        }
        state.put("value", value)
        return state
    }

}