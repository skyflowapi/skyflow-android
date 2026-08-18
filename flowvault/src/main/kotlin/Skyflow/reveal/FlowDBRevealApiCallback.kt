package Skyflow.reveal

import Skyflow.Callback
import Skyflow.core.FlowDBAPIClient
import Skyflow.utils.Utils
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

internal class FlowDBRevealApiCallback(
    private val callback: Callback,
    private val apiClient: FlowDBAPIClient,
    private val requestBody: JSONObject
) : Callback {
    private val tag = FlowDBRevealApiCallback::class.qualifiedName
    private val okHttpClient = apiClient.okHttpClient

    override fun onSuccess(responseBody: Any) {
        try {
            val url = "${apiClient.vaultURL.trimEnd('/')}/v2/tokens/detokenize"
            val body = requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val metrics = Utils.fetchMetrics()
            val request = Request.Builder()
                .method("POST", body)
                .addHeader("Authorization", "$responseBody")
                .addHeader("sky-metadata", "$metrics")
                .url(url)
                .build()
            sendRequest(request)
        } catch (e: Exception) {
            callback.onFailure(Utils.constructErrorResponse(e))
        }
    }

    override fun onFailure(exception: Any) {
        callback.onFailure(exception)
    }

    private fun sendRequest(request: Request) {
        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(Utils.constructErrorResponse(e, 500))
            }

            override fun onResponse(call: Call, response: Response) {
                verifyResponse(response)
            }
        })
    }

    private fun verifyResponse(response: Response) {
        response.use {
            // Build the response INSIDE the try (parse errors -> onFailure), but deliver onSuccess
            // AFTER it — so an exception thrown by the app's own onSuccess handler is NOT caught here
            // and turned into a second onFailure. Exactly one of onSuccess/onFailure must fire.
            val result: JSONObject = try {
                val bodyStr = response.body?.string() ?: ""
                val responseJson = try { JSONObject(bodyStr) } catch (e: JSONException) { null }

                if (responseJson?.has("response") == true) {
                    buildResponse(responseJson)
                } else if (!response.isSuccessful) {
                    // Whole-request failure (auth error, malformed request, etc.)
                    val message = try {
                        responseJson?.getJSONObject("error")?.getString("message") ?: bodyStr
                    } catch (e: JSONException) { bodyStr }
                    val requestId = response.headers["x-request-id"] ?: ""
                    callback.onFailure(Utils.constructErrorResponse(
                        response.code,
                        Utils.appendRequestId(message, requestId)
                    ))
                    return
                } else {
                    buildResponse(responseJson ?: JSONObject())
                }
            } catch (e: Exception) {
                callback.onFailure(Utils.constructErrorResponse(e, 500))
                return
            }
            callback.onSuccess(result)
        }
    }

    private fun buildResponse(responseJson: JSONObject): JSONObject {
        val responseArray = responseJson.optJSONArray("response") ?: JSONArray()
        val allRecords = JSONArray()

        for (i in 0 until responseArray.length()) {
            val entry = responseArray.getJSONObject(i)
            val httpCode = entry.optInt("httpCode", 200)
            val originalToken = entry.optString("token")

            if (httpCode == 200) {
                val rawMeta = entry.optJSONObject("metadata")
                val normalizedMeta = rawMeta?.let { m ->
                    JSONObject().also { out ->
                        m.keys().asSequence().forEach { k ->
                            val normalizedKey = if (k == "skyflowID") "skyflowId" else k
                            out.put(normalizedKey, m.opt(k))
                        }
                    }
                }
                allRecords.put(
                    JSONObject()
                        .put("token", originalToken)
                        .put("value", entry.optString("value"))
                        .put("tokenGroupName", entry.optString("tokenGroupName"))
                        .put("httpCode", httpCode)
                        .put("metadata", normalizedMeta)
                )
            } else {
                allRecords.put(
                    JSONObject()
                        .put("token", originalToken)
                        .put("error", entry.optString("error", "Detokenize failed"))
                        .put("httpCode", httpCode)
                )
            }
        }

        return JSONObject().put("records", allRecords)
    }
}
