package com.Skyflow

import android.app.Application
import com.skyflow_android.R

internal class TestApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.Theme_SkyflowElements)
    }
}
