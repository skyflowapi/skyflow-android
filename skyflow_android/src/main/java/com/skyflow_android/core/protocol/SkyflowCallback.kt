package com.skyflow_android.core.protocol

interface SkyflowCallback {

        fun success(responseBody: String)

        fun failure(exception: Exception?)
    }