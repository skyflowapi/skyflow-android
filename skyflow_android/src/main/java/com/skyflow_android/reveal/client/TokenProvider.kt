package com.skyflowandroid.reveal.client

interface TokenProvider {
    fun getAccessToken(callback: ApiCallback)
}