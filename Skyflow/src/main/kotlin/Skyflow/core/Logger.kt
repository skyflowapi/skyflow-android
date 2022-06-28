/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package Skyflow.core

import Skyflow.LogLevel
import android.util.Log

internal class Logger {
    companion object {
        fun debug(tag: String? = null, message: String, logLevel: LogLevel) {
            if(logLevel.ordinal < 1) {
                Log.d(tag, message)
            }
        }

        fun info(tag: String? = null, message: String, logLevel: LogLevel){
            if(logLevel.ordinal < 2)
                Log.i(tag, message)
        }

        fun warn(tag: String? = null, message: String, logLevel: LogLevel){
            if(logLevel.ordinal < 3)
                Log.w(tag,  message)
        }

        fun error(tag:String? = null, message: String, logLevel: LogLevel){
            if(logLevel.ordinal < 4) {
                Log.e(tag, message)
            }
        }

    }
}