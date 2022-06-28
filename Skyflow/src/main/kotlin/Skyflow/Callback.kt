/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package Skyflow


interface Callback {

        fun onSuccess(responseBody: Any)

        fun onFailure(exception: Any)
    }