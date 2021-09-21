package Skyflow.utils

import android.webkit.URLUtil

class Utils {

    companion object {
        fun checkUrl(url: String): Boolean {
            if (!URLUtil.isValidUrl(url) || !URLUtil.isHttpsUrl(url))
                return false
            return true
        }
    }
}