package Skyflow.reveal

import Skyflow.Callback
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

class RevealResponse(var size: Int, var callback: Callback){

    var responseBody = JSONObject()

    var successResponses = 0

    var failureResponses = 0


    @Synchronized fun insertResponse(responseObject :JSONObject? = null, isSuccess:Boolean = false){
        if(responseObject != null && isSuccess) {
            successResponses +=1
            if(!responseBody.has("records")){
                responseBody.put("records", responseObject.get("records"))
            }
            else{
                val responseArray = responseObject.get("records") as JSONArray
                 for(i in 0 until  responseArray.length()) {
                     (responseBody.get("records") as JSONArray).put(responseArray[i])
                 }
            }
        }
        else if(responseObject != null && !isSuccess){
            successResponses +=1
            if(!responseBody.has("errors")){
                val errorsArray = JSONArray()
                errorsArray.put(responseObject)
                responseBody.put("errors", errorsArray)
            }
            else{
                (responseBody.get("errors") as JSONArray).put(responseObject)
            }
        }else{
            failureResponses += 1
        }

        if(successResponses + failureResponses == size) {
            if (successResponses == 0) {
                callback.onFailure(Exception("Reveal elements failed"))
            } else {
                callback.onSuccess(responseBody)
            }
        }
    }
}