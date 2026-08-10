package Skyflow.collect.elements.validations

import android.text.TextUtils
import com.Skyflow.collect.elements.validations.SkyflowValidationError
import com.Skyflow.collect.elements.validations.ValidationRule

class SkyflowValidateMonth(override var error: SkyflowValidationError = "INVALID_EXPIRE_MONTH") : ValidationRule,SkyflowInternalValidationProtocol {
    override fun validate(text: String?): Boolean {
        if(text!!.isEmpty()) {
            return true
        }
        if (!TextUtils.isDigitsOnly(text)) {
            return false
        }
        val month = text.toInt()
        if (month < 1 || month > 12) {
            return false
        }
        return true
    }
}