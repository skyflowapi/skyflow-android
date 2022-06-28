/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package Skyflow

import org.json.JSONObject

data class ConnectionConfig(
    val connectionURL: String,
    val methodName: RequestMethod,
    val pathParams: JSONObject = JSONObject(),
    val queryParams: JSONObject = JSONObject(),
    var requestBody: JSONObject = JSONObject(),
    val requestHeader: JSONObject = JSONObject(),
    val responseBody: JSONObject = JSONObject()
) {

}