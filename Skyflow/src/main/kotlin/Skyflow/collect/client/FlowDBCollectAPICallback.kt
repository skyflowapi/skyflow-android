package Skyflow.collect.client

import Skyflow.*
import Skyflow.core.FlowDBAPIClient
import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import Skyflow.utils.Utils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

internal class FlowDBCollectAPICallback(
    private val apiClient: FlowDBAPIClient,
    private val requestBody: JSONObject,
    val callback: Skyflow.Callback,
    private val options: CollectOptions,
    val logLevel: LogLevel,
    private val endpoint: String = "insert"
) : Skyflow.Callback {
    private val okHttpClient = apiClient.okHttpClient
    private val tag = FlowDBCollectAPICallback::class.qualifiedName

    override fun onSuccess(responseBody: Any) {
        try {
            Logger.info(tag, Messages.VALIDATE_RECORDS.getMessage(), logLevel)
            val body = requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val metrics = Utils.fetchMetrics()
            val url = "${apiClient.vaultURL.trimEnd('/')}/v2/records/$endpoint"
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
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                callback.onFailure(Utils.constructErrorResponse(e, 500))
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                verifyResponse(response)
            }
        })
    }

    private fun verifyResponse(response: Response) {
        response.use {
            try {
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val parsed = try { JSONObject(bodyStr) } catch (e: Exception) { null }
                    if (parsed != null && parsed.has("records")) {
                        dispatchResponse(bodyStr)
                        return
                    }
                    val message = try {
                        parsed?.getJSONObject("error")?.getString("message") ?: bodyStr
                    } catch (e: JSONException) { bodyStr }
                    val requestId = response.headers["x-request-id"] ?: ""
                    callback.onFailure(Utils.constructErrorResponse(response.code, Utils.appendRequestId(message, requestId)))
                    return
                }
                dispatchResponse(bodyStr)
            } catch (e: Exception) {
                callback.onFailure(Utils.constructErrorResponse(e, 500))
            }
        }
    }

    private fun dispatchResponse(bodyStr: String) {
        val responseJson = JSONObject(bodyStr)
        val records = responseJson.optJSONArray("records") ?: JSONArray()
        val allRecords = JSONArray()
        val requestRecords = requestBody.optJSONArray("records")
        val topLevelTableName = requestBody.optString("tableName", "")
        val requestTableNames: List<String> = if (topLevelTableName.isNotEmpty()) {
            // All records in this request belong to one table
            List(requestRecords?.length() ?: 1) { topLevelTableName }
        } else {
            (0 until (requestRecords?.length() ?: 0)).map { i ->
                requestRecords?.optJSONObject(i)?.optString("tableName", "") ?: ""
            }
        }

        for (i in 0 until records.length()) {
            val record = records.getJSONObject(i)
            val httpCode = record.optInt("httpCode", 200)
            val fallbackTableName = requestTableNames.getOrElse(i) { "" }
            val tableName = record.optString("tableName", "").ifEmpty { fallbackTableName }

            if (httpCode != 200) {
                allRecords.put(
                    JSONObject()
                        .put("error", record.optString("error", ""))
                        .put("skyflowId", record.opt("skyflowID") ?: record.opt("skyflowId"))
                        .put("tableName", tableName)
                        .put("httpCode", httpCode)
                )
                continue
            }

            val skyflowId = record.optString("skyflowID", "").ifEmpty { record.optString("skyflowId", "") }
            val fieldsObject = JSONObject()

            val tokensObj = record.optJSONObject("tokens")
            if (tokensObj != null) {
                val fieldNames = tokensObj.keys()
                while (fieldNames.hasNext()) {
                    val fieldName = fieldNames.next()
                    fieldsObject.put(fieldName, tokensObj.getJSONArray(fieldName))
                }
            }

            val resultRecord = JSONObject()
                .put("tableName", tableName)
                .put("skyflowId", skyflowId)
                .put("tokens", fieldsObject)
                .put("httpCode", httpCode)
            val hashedData = record.optJSONObject("hashedData")
            if (hashedData != null) resultRecord.put("hashedData", hashedData)
            allRecords.put(resultRecord)
        }

        callback.onSuccess(JSONObject().put("records", allRecords))
    }
}
