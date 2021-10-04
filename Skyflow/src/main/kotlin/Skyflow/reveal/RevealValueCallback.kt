package Skyflow.reveal

import Skyflow.Callback
import Skyflow.Label
import androidx.core.content.res.ResourcesCompat
import org.json.JSONObject

@Suppress("DEPRECATION")
class RevealValueCallback(var callback: Callback, var revealElements: MutableList<Label>) :
    Callback {
    override fun onSuccess(responseBody: Any) {
        val elementsMap = HashMap<String, Label>()
        for (element in revealElements){
            elementsMap[element.revealInput.token!!] = element
        }
        val responseJSON = JSONObject(responseBody.toString())
        val recordsArray = responseJSON.getJSONArray("records")
        for (i in 0 until  recordsArray.length()){
            val recordObj = recordsArray[i] as JSONObject
            val tokenId = recordObj.get("token")
            val fieldsObj = recordObj.getJSONObject("fields")
            val value = fieldsObj.get(fieldsObj.keys().next()).toString()
            elementsMap[tokenId]!!.placeholder.text = value
            elementsMap[tokenId]!!.value = value
            recordObj.remove("fields")
        }
        val errorArray = responseJSON.getJSONArray("errors")
        var i=0
        while (i < errorArray.length())
        {
            val recordObj = errorArray[i] as JSONObject
            val tokenId = recordObj.get("token").toString()

            elementsMap[tokenId]!!.error.text = "invalid token"
            elementsMap[tokenId]!!.placeholder.typeface = ResourcesCompat.getFont(elementsMap[tokenId]!!.context,elementsMap[tokenId]!!.revealInput.inputStyles.invalid.font)
            elementsMap[tokenId]!!.placeholder.gravity = elementsMap[tokenId]!!.revealInput.inputStyles.invalid.textAlignment
            val padding =elementsMap[tokenId]!!.revealInput.inputStyles.invalid.padding
            elementsMap[tokenId]!!.placeholder.setPadding(padding.left,padding.top,padding.right,padding.bottom)
            elementsMap[tokenId]!!.placeholder.setTextColor( elementsMap[tokenId]!!.revealInput.inputStyles.invalid.textColor)
            elementsMap[tokenId]!!.border.setStroke(elementsMap[tokenId]!!.revealInput.inputStyles.invalid.borderWidth,elementsMap[tokenId]!!.revealInput.inputStyles.invalid.borderColor)
            elementsMap[tokenId]!!.border.cornerRadius = elementsMap[tokenId]!!.revealInput.inputStyles.invalid.cornerRadius
            elementsMap[tokenId]!!.placeholder.setBackgroundDrawable( elementsMap[tokenId]!!.border)

            i++
        }
        val revealResponse = responseJSON.toString().replace("\"records\":", "\"success\":")
        callback.onSuccess(revealResponse)
    }

    override fun onFailure(exception: Exception) {
        callback.onFailure(exception)
    }
}
