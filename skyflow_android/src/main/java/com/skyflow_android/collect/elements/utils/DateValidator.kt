package com.skyflowandroid.collect.elements.utils

import android.text.TextUtils
import java.util.*


 class DateValidator {


    private var mCalendar: Calendar? = null

    constructor(){
        mCalendar = Calendar.getInstance()
    }


    fun isValid(monthString: String, yearString: String): Boolean {

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
        var currentYear:Int
        var year:Int = yearString.toInt()
        var yearLength = yearString.length;
        if (yearLength == 2) {
            currentYear = currentTwoDigitYear()
        } else if (yearLength == 4) {
           currentYear = currentFourDigitYear()
        } else {
            return false;
        }

        if (year == currentYear && month < currentMonth()) {
            return false;
        }

        if (year < currentYear) {
           return false
        }

        return true;
    }


    private fun currentMonth(): Int
    {
        return mCalendar!!.get(Calendar.MONTH)+1
    }


    private fun currentTwoDigitYear(): Int
    {
        return mCalendar!!.get(Calendar.YEAR) %100
    }
    private fun currentFourDigitYear() : Int
    {
        return mCalendar!!.get(Calendar.YEAR)
    }
}
