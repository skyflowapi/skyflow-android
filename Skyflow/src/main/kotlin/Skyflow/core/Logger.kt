package Skyflow.core

import android.util.Log

internal class Logger {
    companion object {
        fun debug(tag: String? = null, message: String, logLevel: LogLevel) {
            if(logLevel.ordinal < 2) {
                Log.d(tag, "debug: $message")
            }
        }

        fun info(tag: String? = null, message: String, logLevel: LogLevel){
            if(logLevel.ordinal < 2)
            Log.i(tag, "info: $message")
        }

        fun error(tag:String? = null, message: String, logLevel: LogLevel){
            Log.e(tag, "error: $message")
        }

    }
}