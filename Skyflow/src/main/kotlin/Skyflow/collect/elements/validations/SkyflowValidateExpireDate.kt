/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package  com.Skyflow.collect.elements.validations

import Skyflow.collect.elements.validations.SkyflowInternalValidationProtocol
import Skyflow.utils.Utils
import android.text.TextUtils
import java.util.*

/**
Validate input in scope of length.
 */
internal class SkyflowValidateExpireDate(var format:String ="mm/yy", override var error: SkyflowValidationError = "INVALID_EXPIRE_DATE") : ValidationRule,SkyflowInternalValidationProtocol {


    /// validate length of text
    override fun validate(text: String?) : Boolean {

        if(text!!.isEmpty())
        {
            return true
        }
        val monthChars : Int
        val yearChars : Int
        if(format.contains("yyyy")){
            monthChars = SkyflowExpireDateFormat.LONGYEAR.monthCharacters
            yearChars = SkyflowExpireDateFormat.LONGYEAR.yearCharacters


        }else if(format.contains("yy")){
            monthChars = SkyflowExpireDateFormat.SHORTYEAR.monthCharacters
            yearChars = SkyflowExpireDateFormat.SHORTYEAR.yearCharacters
        }else{
            return false
        }

        if (text.length != monthChars + yearChars + 1)  { return false }

        var monthString = ""
        var yearString = ""
        if(format.toLowerCase().startsWith("m")) {
             monthString = text.substring(0, monthChars)
             yearString = text.substring(text.length - yearChars, text.length)
        }
        else
        {
             yearString = text.substring(0,yearChars)
             monthString = text.substring(text.length-monthChars,text.length)
        }
        if (TextUtils.isEmpty(monthString)) {
            return false
        }
        if (TextUtils.isEmpty(yearString)) {
            return false
        }
        if (!TextUtils.isDigitsOnly(monthString) || !TextUtils.isDigitsOnly(yearString)) {
            return false
        }
        val month = monthString.toInt()
        if (month < 1 || month > 12) {
            return false
        }
        val currentYear:Int
        val year:Int = yearString.toInt()
        val yearLength = yearString.length;
        if (yearLength == 2) {
            currentYear = Utils.currentTwoDigitYear()
        } else if (yearLength == 4) {
            currentYear = Utils.currentFourDigitYear()
        } else {
            return false;
        }

        if (year == currentYear && month < Utils.currentMonth()) {
            return false;
        }

        if (year < currentYear || year > currentYear+50) {
            return false
        }
        return true;
    }

}


internal enum class SkyflowExpireDateFormat(var yearCharacters:Int,var monthCharacters:Int,var dateFormat: String) {

     SHORTYEAR(2,2,"yy"),

    LONGYEAR(4,2,"yyyy")

}