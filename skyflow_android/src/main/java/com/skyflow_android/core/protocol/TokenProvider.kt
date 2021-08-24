package com.skyflow_android.core.protocol

interface TokenProvider {
    fun getAccessToken(callback: SkyflowCallback)
}