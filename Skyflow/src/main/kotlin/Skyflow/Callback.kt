package Skyflow


interface Callback {

        fun onSuccess(responseBody: Any)

        fun onFailure(exception: Exception)
    }