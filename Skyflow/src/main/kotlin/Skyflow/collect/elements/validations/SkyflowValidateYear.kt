package Skyflow.collect.elements.validations

import Skyflow.utils.Utils
import android.text.TextUtils
import android.util.Log
import com.Skyflow.collect.elements.validations.SkyflowValidationError
import com.Skyflow.collect.elements.validations.ValidationRule

class SkyflowValidateYear(override var error: SkyflowValidationError = "INVALID_EXPIRE_YEAR",var format:String="yy") : ValidationRule,SkyflowInternalValidationProtocol {
    override fun validate(text: String?): Boolean {
        if(text!!.isEmpty()) {
            return true
        }
        if (!TextUtils.isDigitsOnly(text)) {
            return false
        }
        if(!(format.equals("yy") || format.equals("yyyy"))) return false
        if(text.length != format.length) {
            return false
        }
        val year = text.toInt()
        val currentYear = Utils.currentFourDigitYear()
        if(format.equals("yy") && (year<(currentYear%100) || year>((currentYear%100)+50))) {
            return false
        }
        else if(format.equals("yyyy") && (year<(currentYear) || year>(currentYear+50))) {
            return false
        }
        return true
    }
}