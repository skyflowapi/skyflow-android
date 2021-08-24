package com.skyflow_android.reveal.client

import com.skyflow_android.core.protocol.SkyflowCallback
import com.skyflow_android.reveal.elements.SkyflowLabel
import org.json.JSONArray
import org.json.JSONObject

class RevealValueCallback(var callback: SkyflowCallback, var revealElements: MutableList<SkyflowLabel>) :
    SkyflowCallback {
    override fun success(responseBody: String) {
        val elementsMap = HashMap<String, SkyflowLabel>()
        for (element in revealElements){
            elementsMap[element.revealInput.id] = element
        }
        val responseJSON = JSONObject(responseBody)
        val recordsArray = responseJSON.getJSONArray("records")
        for (i in 0 until  recordsArray.length()){
            val recordObj = recordsArray[i] as JSONObject
            val tokenId = recordObj.get("id")
            val fieldsObj = recordObj.getJSONObject("fields")
            val value = fieldsObj.get(fieldsObj.keys().next()).toString()
            elementsMap[tokenId]!!.label.text = value
            recordObj.remove("fields")
        }
        val revealResponse = responseJSON.toString().replace("\"records\":", "\"success\":")
        callback.success(revealResponse)
    }

    override fun failure(exception: Exception?) {
        callback.failure(exception)
    }
}
