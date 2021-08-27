package Skyflow.reveal

import Skyflow.Callback
import Skyflow.Label
import org.json.JSONObject

class RevealValueCallback(var callback: Callback, var revealElements: MutableList<Label>) :
    Callback {
    override fun onSuccess(responseBody: Any) {
        val elementsMap = HashMap<String, Label>()
        for (element in revealElements){
            elementsMap[element.revealInput.id] = element
        }
        val responseJSON = JSONObject(responseBody.toString())
        val recordsArray = responseJSON.getJSONArray("records")
        for (i in 0 until  recordsArray.length()){
            val recordObj = recordsArray[i] as JSONObject
            val tokenId = recordObj.get("id")
            val fieldsObj = recordObj.getJSONObject("fields")
            val value = fieldsObj.get(fieldsObj.keys().next()).toString()
            elementsMap[tokenId]!!.placeholder.text = value
            recordObj.remove("fields")
        }
        val revealResponse = responseJSON.toString().replace("\"records\":", "\"success\":")
        callback.onSuccess(revealResponse)
    }

    override fun onFailure(exception: Exception) {
        callback.onFailure(exception)
    }
}
