package com.skyflowandroid.collect.client

interface TokenProvider {
    fun getAccessToken(callback: ApiCallback)
}