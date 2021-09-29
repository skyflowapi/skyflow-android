package Skyflow

import org.json.JSONObject

data class GatewayConfiguration(
    val gatewayURL: String,
    val methodName: RequestMethod,
    val pathParams: JSONObject = JSONObject(),
    val queryParams: JSONObject = JSONObject(),
    val requestBody: JSONObject = JSONObject(),
    val requestHeader: JSONObject = JSONObject(),
    val responseBody: JSONObject = JSONObject()
) {

}