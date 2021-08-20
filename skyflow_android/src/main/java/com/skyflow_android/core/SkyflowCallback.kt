package com.skyflow_android.core

interface SkyflowCallback {

        fun success(responseBody: String)

        fun failure(exception: Exception?)
    }