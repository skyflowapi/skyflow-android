package Skyflow.core

import Skyflow.LogLevel
import android.util.Log

internal class Logger {
    companion object {
        fun debug(tag: String? = null, message: String, logLevel: LogLevel) {
            if(logLevel.ordinal < 1) {
                Log.d(tag, "debug: $message")
            }
        }

        fun info(tag: String? = null, message: String, logLevel: LogLevel){
            if(logLevel.ordinal < 2)
                Log.i(tag, "info: $message")
        }

        fun warn(tag: String? = null, message: String, logLevel: LogLevel){
            if(logLevel.ordinal < 3)
                Log.w(tag,  "warn: $message", )
        }

        fun error(tag:String? = null, message: String, logLevel: LogLevel){
            if(logLevel.ordinal < 4) {
                Log.e(tag, "error: $message")
            }
        }

    }
}