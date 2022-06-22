/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package Skyflow.reveal

import Skyflow.Callback
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import Skyflow.LogLevel
import Skyflow.utils.Utils
import org.json.JSONArray
import org.json.JSONObject

internal class RevealResponseByID(var size: Int, var callback: Callback, val logLevel: LogLevel = LogLevel.ERROR) {
    var responseBody = JSONObject().put("success", JSONArray())
        .put("errors", JSONArray())

    var successResponses = 0
    var failureResponses = 0
    var emptyResponses = 0
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
            emptyResponses += 1
        }

        if(successResponses + failureResponses  + emptyResponses == size) {
            if (successResponses + failureResponses == 0) {
                val skyflowError = SkyflowError(SkyflowErrorCode.FAILED_TO_REVEAL, tag, logLevel)
                callback.onFailure(Utils.constructError(skyflowError))
            } else {
                if(failureResponses==0) {
                    responseBody.remove("errors")
                    callback.onSuccess(responseBody)
                }
                else if(successResponses == 0)
                {
                    responseBody.remove("success")
                    callback.onFailure(responseBody)
                }
                else
                    callback.onFailure(responseBody)
            }
        }
    } }