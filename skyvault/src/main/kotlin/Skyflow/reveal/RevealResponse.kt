package Skyflow.reveal

import Skyflow.Callback
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import Skyflow.LogLevel
import Skyflow.utils.Utils
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

internal class RevealResponse(var size: Int, var callback: Callback, val logLevel: LogLevel = LogLevel.ERROR){

    var responseBody = JSONObject().put("records", JSONArray())
        .put("errors", JSONArray())
    var successResponses = 0
    var failureResponses = 0
    var emptyResponses = 0
    private val tag = RevealResponse::class.qualifiedName

    @Synchronized fun insertResponse(responseObject :JSONObject? = null, isSuccess:Boolean = false){
        if(responseObject != null && isSuccess) {
            successResponses +=1
            val revealRecord = JSONObject(responseObject.getJSONArray("records")[0].toString())
            if(revealRecord.has("valueType"))
                revealRecord.remove("valueType")
            (responseBody.get("records") as JSONArray)
                .put(revealRecord)
        }
        else if(responseObject != null && !isSuccess){
            failureResponses +=1
            (responseBody.get("errors") as JSONArray).put(responseObject)
        }
        else{
            emptyResponses += 1
        }

        if(successResponses + failureResponses + emptyResponses == size) {
            if (successResponses + failureResponses == 0) {
                val skyflowError = SkyflowError(SkyflowErrorCode.FAILED_TO_REVEAL, tag, logLevel)
                callback.onFailure(Utils.constructError(skyflowError))
            } else {
                if(failureResponses==0)
                {
                    responseBody.remove("errors")
                    callback.onSuccess(responseBody)
                }
                else if(successResponses == 0)
                {
                    responseBody.remove("records")
                    callback.onFailure(responseBody)
                }
                else
                    callback.onFailure(responseBody)
            }
        }
    } }