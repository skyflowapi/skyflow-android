package com.skyflowandroid.collect.client

import com.skyflow_android.core.SkyflowCallback

interface TokenProvider {
    fun getAccessToken(callback: SkyflowCallback)
}