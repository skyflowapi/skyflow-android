package Skyflow.reveal

import Skyflow.Callback
import org.json.JSONArray
import org.json.JSONObject

class RevealResponseByID(var size: Int, var callback: Callback) {
    var responseBody = JSONObject().put("records", JSONArray())
        .put("errors", JSONArray())

    var successResponses = 0

    var failureResponses = 0


    @Synchronized fun insertResponse(responseObject : JSONArray? = null, isSuccess:Boolean = false){
        if(responseObject != null && isSuccess) {
            successResponses +=1
            var i = 0
            while(i<responseObject.length())
            {
                (responseBody.get("records") as JSONArray)
                    .put(responseObject.getJSONObject(i))
                i++
            }

        }
        else if(responseObject != null && !isSuccess){
            successResponses +=1
            (responseBody.get("errors") as JSONArray).put(responseObject.getJSONObject(0))
        }else{
            failureResponses += 1
        }

        if(successResponses + failureResponses == size) {
            if (successResponses == 0) {
                callback.onFailure(Exception("Reveal failed"))
            } else {
                try {
                    val records = responseBody.get("records") as JSONArray
                    val errors = responseBody.get("errors") as JSONArray
                    if(records.length() ==0)
                    {
                        responseBody.remove("records")
                        callback.onSuccess(responseBody)
                    }
                    else if(errors.length() ==0)
                    {
                        responseBody.remove("errors")
                        callback.onSuccess(responseBody)
                    }
                    else
                        callback.onSuccess(responseBody)
                }
                catch (e:Exception)
                {
                    callback.onFailure(e)
                }

            }
        }
    }
}