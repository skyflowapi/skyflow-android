package Skyflow

interface Callback {

        fun onSuccess(responseBody: String)

        fun onFailure(exception: Exception?)
    }