package Skyflow.get

import Skyflow.Callback
import Skyflow.LogLevel
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import Skyflow.utils.Utils
import org.json.JSONArray
import org.json.JSONObject

internal class GetResponse(
    var size: Int,
    var callback: Callback,
    val logLevel: LogLevel = LogLevel.ERROR
) {

    var responseBody: JSONObject = JSONObject()
        .put("records", JSONArray())
        .put("errors", JSONArray())

    private var successResponses = 0
    private var failureResponses = 0
    private var emptyResponses = 0
    private val tag = GetResponse::class.qualifiedName

    @Synchronized
    fun insertResponse(responseObject: JSONArray? = null, isSuccess: Boolean = false) {
        if (responseObject != null && isSuccess) {
            successResponses += 1
            for (i in 0 until responseObject.length()) {
                responseBody.getJSONArray("records").put(responseObject.getJSONObject(i))
            }
        } else if (responseObject != null && !isSuccess) {
            failureResponses += 1
            responseBody.getJSONArray("errors").put(responseObject.getJSONObject(0))
        } else {
            emptyResponses += 1
        }

        if (successResponses + failureResponses + emptyResponses == size) {
            if (successResponses + failureResponses == 0) {
                val skyflowError = SkyflowError(SkyflowErrorCode.FAILED_TO_GET, tag, logLevel)
                callback.onFailure(Utils.constructError(skyflowError))
            } else {
                if (failureResponses == 0) {
                    responseBody.remove("errors")
                    callback.onSuccess(responseBody)
                } else if (successResponses == 0) {
                    responseBody.remove("records")
                    callback.onFailure(responseBody)
                } else
                    callback.onFailure(responseBody)
            }
        }
    }
}
