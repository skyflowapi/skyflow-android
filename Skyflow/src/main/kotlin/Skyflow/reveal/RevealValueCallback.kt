package Skyflow.reveal

import Skyflow.Callback
import Skyflow.Label
import org.json.JSONObject
import java.lang.Exception

@Suppress("DEPRECATION")
internal class RevealValueCallback(var callback: Callback, var revealElements: MutableList<Label>) :
    Callback {
    override fun onSuccess(responseBody: Any) {
        val elementsMap = HashMap<String, Label>()
        val responseJSON = JSONObject(responseBody.toString())
        revealSuccessRecords(responseJSON,elementsMap)
        val revealResponse = responseJSON.toString().replace("\"records\":", "\"success\":")
        callback.onSuccess(revealResponse)
    }

    override fun onFailure(exception: Any) {
        try {
            val elementsMap = HashMap<String, Label>()
            val responseJSON = JSONObject(exception.toString())
            if(responseJSON.has("records"))
                revealSuccessRecords(responseJSON,elementsMap)
            revealErrors(responseJSON,elementsMap)
            callback.onFailure(exception)
        }
        catch (e:Exception)
        {
            callback.onFailure(exception)
        }


    }

    fun revealSuccessRecords(responseJSON: JSONObject, elementsMap: HashMap<String, Label>)
    {
        try {
            for (element in revealElements){
                elementsMap[element.revealInput.token!!] = element
            }
            val recordsArray = responseJSON.getJSONArray("records")
            for (i in 0 until  recordsArray.length()) {
                val recordObj = recordsArray[i] as JSONObject
                val tokenId = recordObj.get("token")
                val element = elementsMap[tokenId]!!
                val value = recordObj.getString("value")
                elementsMap[tokenId]!!.setText(value)
                elementsMap[tokenId]!!.actualValue = value
                recordObj.remove("value")
            }
        }
        catch (e:Exception){}
    }

    fun revealErrors(responseJSON: JSONObject, elementsMap: HashMap<String, Label>)
    {
        try {
            val errorArray = responseJSON.getJSONArray("errors")
            var i = 0
            while (i < errorArray.length()) {
                val recordObj = errorArray[i] as JSONObject
                val tokenId = recordObj.get("token").toString()
                elementsMap[tokenId]!!.setErrorText("invalid token")
                elementsMap[tokenId]!!.showError()
                i++
            }
        }
        catch (e:Exception){}
    }
}

