package Skyflow

import org.json.JSONObject

internal data class ConnectionConfig(
    val connectionURL: String,
    val methodName: RequestMethod,
    val pathParams: JSONObject = JSONObject(),
    val queryParams: JSONObject = JSONObject(),
    var requestBody: JSONObject = JSONObject(),
    val requestHeader: JSONObject = JSONObject(),
    val responseBody: JSONObject = JSONObject()
) {

}