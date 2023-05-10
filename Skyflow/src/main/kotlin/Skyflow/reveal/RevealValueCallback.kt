package Skyflow.reveal

import Skyflow.*
import Skyflow.utils.Utils
import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.lang.Exception

@Suppress("DEPRECATION")
internal class RevealValueCallback(
    var callback: Callback,
    var revealElements: MutableList<Label>,
    var logLevel: LogLevel
) : Callback {

    private val tag = RevealValueCallback::class.qualifiedName
    private val elementsList = mutableListOf<Pair<String, Label>>()

    override fun onSuccess(responseBody: Any) {
        try {
            constructElementMap()
            val responseJSON = JSONObject(responseBody.toString())
            revealSuccessRecords(responseJSON)
            val revealResponse = responseJSON.toString().replace("\"records\":", "\"success\":")
            callback.onSuccess(revealResponse)
        } catch (e: Exception) {
            callback.onFailure(Utils.constructError(e))
        }
    }

    override fun onFailure(exception: Any) {
        try {
            constructElementMap()
            val responseJSON = JSONObject(exception.toString())
            if (responseJSON.has("records")) {
                revealSuccessRecords(responseJSON)
            }
            revealErrors(responseJSON)
            val revealResponse = responseJSON.toString().replace("\"records\":", "\"success\":")
            callback.onFailure(revealResponse)
        } catch (e: Exception) {
            callback.onFailure(Utils.constructError(e))
        }
    }

    private fun constructElementMap() {
        for (element in revealElements) {
            elementsList.add(Pair(element.revealInput.token!!, element))
        }
    }

    private fun revealSuccessRecords(responseJSON: JSONObject) {
        val recordsArray = responseJSON.getJSONArray("records")
        for (i in 0 until recordsArray.length()) {
            val recordObj = recordsArray[i] as JSONObject
            val tokenId = recordObj.get("token")
            val value = recordObj.getString("value")
            Handler(Looper.getMainLooper()).post {
                for (element in elementsList) {
                    if (element.first == tokenId) {
                        Utils.setValueForLabel(element.second, value)
                    }
                }
            }
            recordObj.remove("value")
        }
    }

    private fun revealErrors(responseJSON: JSONObject) {
        val errorArray = responseJSON.getJSONArray("errors")

        var i = 0
        while (i < errorArray.length()) {
            val recordObj = errorArray[i] as JSONObject
            val tokenId = recordObj.get("token").toString()
            Handler(Looper.getMainLooper()).post {
                for (element in elementsList) {
                    if (element.first == tokenId) {
                        Utils.setErrorForLabel(element.second)
                    }
                }
            }

            i++
        }
    }
}
