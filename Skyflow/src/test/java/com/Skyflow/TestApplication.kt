package com.Skyflow

import Skyflow.Callback
import Skyflow.TokenProvider
import android.app.Application
import com.skyflow_android.R

internal class TestApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.Theme_SkyflowElements)
    }
}

class AccessTokenProvider : TokenProvider {
    override fun getBearerToken(callback: Callback) {
        callback.onSuccess("")
    }
}
