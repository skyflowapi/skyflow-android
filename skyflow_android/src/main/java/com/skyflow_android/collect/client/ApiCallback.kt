package com.skyflowandroid.collect.client

interface ApiCallback {

    fun success(responseBody: String)

    fun failure(exception: Exception?)
}