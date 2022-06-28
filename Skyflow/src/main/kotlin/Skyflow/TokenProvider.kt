/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package Skyflow


interface TokenProvider {
    fun getBearerToken(callback: Callback)
}