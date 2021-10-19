package Skyflow.reveal

import Skyflow.Callback
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import Skyflow.core.LogLevel
import Skyflow.utils.Utils
import org.json.JSONArray
import org.json.JSONObject

class RevealResponseByID(var size: Int, var callback: Callback, val logLevel: LogLevel = LogLevel.PROD) {
    var responseBody = JSONObject().put("success", JSONArray())
        .put("errors", JSONArray())

    var successResponses = 0

    var failureResponses = 0

    private val tag = RevealResponseByID::class.qualifiedName


    @Synchronized fun insertResponse(responseObject : JSONArray? = null, isSuccess:Boolean = false){
        if(responseObject != null && isSuccess) {
            successResponses +=1
            var i = 0
            while(i<responseObject.length())
            {
                (responseBody.get("success") as JSONArray)
                    .put(responseObject.getJSONObject(i))
                i++
            }

        }
        else if(responseObject != null && !isSuccess){
            failureResponses +=1
            (responseBody.get("errors") as JSONArray).put(responseObject.getJSONObject(0))
        }else{
            failureResponses += 1
        }

        if(successResponses + failureResponses == size) {
            if (successResponses == 0) {
                val skyflowError = SkyflowError(SkyflowErrorCode.FAILED_TO_REVEAL, tag, logLevel)
                callback.onFailure(Utils.constructError(skyflowError))
            } else {
                if(failureResponses==0) {
                    responseBody.remove("errors")
                    callback.onSuccess(responseBody)
                }                else
                    callback.onFailure(responseBody)
            }
        }
    }
}