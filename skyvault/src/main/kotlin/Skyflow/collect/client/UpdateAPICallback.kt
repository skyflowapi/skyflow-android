package Skyflow.collect.client

import Skyflow.*
import Skyflow.core.APIClient
import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import Skyflow.utils.Utils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

internal class UpdateAPICallback(
    private val apiClient: APIClient,
    private val updateRecords: MutableList<UpdateRequestRecord>,
    private val callback: Skyflow.Callback,
    private val options: InsertOptions,
    val logLevel: LogLevel
) : Skyflow.Callback {
    private val okHttpClient = OkHttpClient()
    private val tag = UpdateAPICallback::class.qualifiedName
    private val responses = mutableListOf<JSONObject>()
    private val errors = mutableListOf<JSONObject>()
    private var completedCalls = 0
    private val totalCalls = updateRecords.size

    override fun onSuccess(responseBody: Any) {
        try {
            // Make individual update calls for each record
            for (updateRecord in updateRecords) {
                val request = buildUpdateRequest(responseBody.toString(), updateRecord)
                sendRequest(request, updateRecord)
            }
        } catch (e: Exception) {
            if (e is SkyflowError)
                callback.onFailure(e)
            else {
                val skyflowError = SkyflowError(
                    SkyflowErrorCode.UNKNOWN_ERROR,
                    tag = tag,
                    logLevel = apiClient.logLevel,
                    arrayOf(e.message.toString())
                )
                skyflowError.setErrorCode(400)
                callback.onFailure(skyflowError)
            }
        }
    }

    private fun buildUpdateRequest(token: String, updateRecord: UpdateRequestRecord): Request {
        val url = "${apiClient.vaultURL}${apiClient.vaultId}/${updateRecord.table}/${updateRecord.skyflowID}"
        Logger.info(tag, "Making update request for skyflowID: ${updateRecord.skyflowID}", logLevel)
        
        val fieldsObject = JSONObject()
        for ((column, value) in updateRecord.columns) {
            fieldsObject.put(column, value)
        }
        
        val recordObject = JSONObject()
        recordObject.put("fields", fieldsObject)
        
        val requestBody = JSONObject()
        requestBody.put("record", recordObject)
        requestBody.put("tokenization", options.tokens)

        val body: RequestBody = requestBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

        val metrics = Utils.fetchMetrics()
        return Request.Builder()
            .method("PUT", body)
            .addHeader("Authorization", token)
            .addHeader("sky-metadata", "$metrics")
            .url(url)
            .build()
    }

    override fun onFailure(exception: Any) {
        callback.onFailure(exception)
    }

    private fun sendRequest(request: Request, updateRecord: UpdateRequestRecord) {
        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                synchronized(this@UpdateAPICallback) {
                    errors.add(Utils.constructErrorObject(500, e.message.toString()))
                    completedCalls++
                    checkIfAllCallsCompleted()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                verifyResponse(response, updateRecord)
            }
        })
    }

    private fun verifyResponse(response: Response, updateRecord: UpdateRequestRecord) {
        response.use {
            synchronized(this@UpdateAPICallback) {
                try {
                    if (!response.isSuccessful && response.body != null) {
                        val body = response.body!!.string()
                        val message = try {
                            val responseJson = JSONObject(body)
                            responseJson.getJSONObject("error").getString("message")
                        } catch (e: JSONException) {
                            body
                        }

                        val requestId = response.headers.get("x-request-id").toString()
                        errors.add(Utils.constructErrorObject(
                            response.code, 
                            Utils.appendRequestId(message, requestId)
                        ))
                    } else if (response.isSuccessful && response.body != null) {
                        val responseBody = response.body!!.string()
                        val responseJson = JSONObject(responseBody)
                        
                        // Transform update response to match collect response format
                        val recordObject = JSONObject()
                        recordObject.put("table", updateRecord.table)
                        
                        if (options.tokens) {
                            val fieldsObject = JSONObject()
                            fieldsObject.put("skyflow_id", responseJson.getString("skyflow_id"))
                            
                            if (responseJson.has("tokens")) {
                                val tokens = responseJson.getJSONObject("tokens")
                                val tokenKeys = tokens.keys()
                                while (tokenKeys.hasNext()) {
                                    val key = tokenKeys.next()
                                    fieldsObject.put(key, tokens.getString(key))
                                }
                            }
                            
                            recordObject.put("fields", fieldsObject)
                        } else {
                            recordObject.put("skyflow_id", responseJson.getString("skyflow_id"))
                        }
                        
                        responses.add(recordObject)
                    } else {
                        errors.add(Utils.constructErrorObject(response.code, "Bad request"))
                    }
                } catch (e: Exception) {
                    errors.add(Utils.constructErrorObject(500, e.message.toString()))
                }
                
                completedCalls++
                checkIfAllCallsCompleted()
            }
        }
    }

    private fun checkIfAllCallsCompleted() {
        if (completedCalls == totalCalls) {
            // Build the response JSON with both records and errors
            val result = JSONObject()
            
            // Add successful records if any
            if (responses.isNotEmpty()) {
                val recordsArray = JSONArray()
                for (response in responses) {
                    recordsArray.put(response)
                }
                result.put("records", recordsArray)
            }
            
            // Add errors if any
            if (errors.isNotEmpty()) {
                val errorsArray = JSONArray()
                for (error in errors) {
                    errorsArray.put(error)
                }
                result.put("errors", errorsArray)
            }
            
            // If there are any errors, call onFailure with the JSON response (like JS SDK)
            if (errors.isNotEmpty()) {
                callback.onFailure(result)
                return
            }
            
            // Only return success if ALL operations succeeded (no errors)
            callback.onSuccess(result)
        }
    }
}
