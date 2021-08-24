package com.skyflow_android.reveal.client

import android.util.Log
import com.skyflow_android.core.protocol.SkyflowCallback
import org.json.JSONArray
import org.json.JSONObject

class RevealResponse(var size: Int, var callback: SkyflowCallback){

    var responseBody = JSONObject()

    var currentSize = 0;


    @Synchronized fun insertResponse(responseObject :JSONObject? = null, isSuccess:Boolean? = false){
        currentSize += 1
        if(responseObject != null && isSuccess!!) {
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
        else if(responseObject != null && !isSuccess!!){
            if(!responseBody.has("errors")){
                val errorsArray = JSONArray()
                errorsArray.put(responseObject)
                responseBody.put("errors", errorsArray)
            }
            else{
                (responseBody.get("errors") as JSONArray).put(responseObject)
            }
        }

        if(currentSize == size){
            callback.success(responseBody.toString())
        }
    }
}