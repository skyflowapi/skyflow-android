package Skyflow.reveal

import Skyflow.*
import Skyflow.utils.Utils
import android.os.Handler
import android.os.Looper
import org.json.JSONObject

@Suppress("DEPRECATION")
internal class RevealValueCallback(
    var callback: Callback,
    var revealElements: MutableList<Label>,
    var logLevel: LogLevel
) : Callback {

    private val tag = RevealValueCallback::class.qualifiedName
    private val elementsList = mutableListOf<Pair<String, Label>>()

    override fun onSuccess(responseBody: Any) {
        // Build/apply the response INSIDE the try (parse/apply errors -> onFailure), but deliver
        // onSuccess AFTER it so an exception thrown by the app's own onSuccess handler is NOT caught
        // here and turned into a second onFailure. Exactly one of onSuccess/onFailure must fire.
        val responseString: String = try {
            constructElementMap()
            val responseJSON = JSONObject(responseBody.toString())
            applyRecordsToElements(responseJSON)
            responseJSON.toString()
        } catch (e: Exception) {
            callback.onFailure(Utils.constructErrorResponse(e))
            return
        }
        callback.onSuccess(responseString)
    }

    override fun onFailure(exception: Any) {
        callback.onFailure(exception)
    }

    private fun constructElementMap() {
        for (element in revealElements) {
            elementsList.add(Pair(element.revealInput.token!!, element))
        }
    }

    private fun applyRecordsToElements(responseJSON: JSONObject) {
        val records = responseJSON.optJSONArray("records") ?: return
        for (i in 0 until records.length()) {
            val record = records.optJSONObject(i) ?: continue
            val token = record.optString("token")
            val httpCode = record.optInt("httpCode", 200)
            if (httpCode == 200) {
                val value = record.optString("value")
                Handler(Looper.getMainLooper()).post {
                    for (element in elementsList) {
                        if (element.first == token) Utils.setValueForLabel(element.second, value)
                    }
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    for (element in elementsList) {
                        if (element.first == token) Utils.setErrorForLabel(element.second)
                    }
                }
            }
        }
    }
}
