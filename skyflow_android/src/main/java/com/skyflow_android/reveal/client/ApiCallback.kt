package com.skyflowandroid.reveal.client

interface ApiCallback {

    fun success(responseBody: String)

    fun failure(exception: Exception?)
}