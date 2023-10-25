package Skyflow.get

import Skyflow.Callback
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import Skyflow.core.APIClient
import Skyflow.utils.Utils
import okhttp3.Call
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

internal class GetAPICallback(
    var callback: Callback,
    var apiClient: APIClient,
    var records: MutableList<GetRecord>,
    var options: GetOptions?
) : Callback {

    private val tag = GetAPICallback::class.qualifiedName
    private val getResponse = GetResponse(records.size, callback, apiClient.logLevel)
    private val okHttpClient = OkHttpClient();

    override fun onSuccess(responseBody: Any) {
        try {
            for (record in records) {
                val request = buildRequest(responseBody, record)
                sendRequest(request, record)
            }
        } catch (e: Exception) {
            callback.onFailure(Utils.constructError(e))
        }
    }

    private fun buildRequest(responseBody: Any, record: GetRecord): Request {
        val url = "${apiClient.vaultURL}${apiClient.vaultId}/${record.table}"
        val requestUrlBuilder = url.toHttpUrlOrNull()?.newBuilder()
            ?: throw SkyflowError(
                SkyflowErrorCode.INVALID_VAULT_URL, tag, apiClient.logLevel,
                arrayOf(apiClient.vaultURL)
            )

        if (record.skyflowIds != null) {
            for (id in record.skyflowIds) {
                requestUrlBuilder.addQueryParameter("skyflow_ids", id)
            }
        } else if (record.columnName != null) {
            requestUrlBuilder.addQueryParameter("column_name", record.columnName)
            for (value in record.columnValues!!) {
                requestUrlBuilder.addQueryParameter("column_values", value)
            }
        }

        if (record.redaction != null) {
            requestUrlBuilder.addQueryParameter("redaction", record.redaction)
        }
        val requestUrl = requestUrlBuilder
            .addQueryParameter("tokenization", options?.tokens.toString())
            .build()

        val metrics = Utils.fetchMetrics()
        return Request
            .Builder()
            .addHeader("Authorization", "$responseBody")
            .addHeader("sky-metadata", "$metrics")
            .url(requestUrl)
            .build()
    }

    private fun sendRequest(request: Request, record: GetRecord) {
        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                val skyflowError = SkyflowError(
                    SkyflowErrorCode.UNKNOWN_ERROR, tag = tag, logLevel = apiClient.logLevel,
                    params = arrayOf(e.message.toString())
                )
                val responseObject = constructErrorResponseForGet(record, skyflowError)
                getResponse.insertResponse(JSONArray().put(responseObject), false)
            }

            override fun onResponse(call: Call, response: Response) {
                verifyResponse(response, record)
            }
        })
    }

    private fun verifyResponse(response: Response, record: GetRecord) {
        response.use {
            try {
                if (!response.isSuccessful && response.body != null) {
                    val responseBody = response.body!!.string()
                    try {
                        val responseErrorBody = JSONObject(responseBody)
                        val requestId = response.headers["x-request-id"].toString()
                        val skyflowError = SkyflowError(
                            SkyflowErrorCode.SERVER_ERROR, tag, apiClient.logLevel,
                            arrayOf(
                                Utils.appendRequestId(
                                    responseErrorBody.getJSONObject("error").getString("message"),
                                    requestId
                                )
                            )
                        )
                        skyflowError.setErrorCode(response.code)
                        val responseObject = constructErrorResponseForGet(record, skyflowError)
                        getResponse.insertResponse(JSONArray().put(responseObject), false)
                    } catch (e: Exception) {
                        val skyflowError = SkyflowError(
                            SkyflowErrorCode.SERVER_ERROR, tag, apiClient.logLevel,
                            arrayOf(responseBody)
                        )
                        skyflowError.setErrorCode(response.code)
                        val responseObject = constructErrorResponseForGet(record, skyflowError)
                        getResponse.insertResponse(JSONArray().put(responseObject), false)
                    }

                } else if (response.isSuccessful && response.body != null) {
                    val fields = JSONObject(
                        response.body!!.string().replace("\"skyflow_id\":", "\"id\":")
                    ).getJSONArray("records")

                    val newJsonArray = JSONArray()
                    for (i in 0 until fields.length()) {
                        val jsonObject = JSONObject()
                        jsonObject.put("fields", fields.getJSONObject(i).getJSONObject("fields"))
                        jsonObject.put("table", record.table)
                        newJsonArray.put(jsonObject)
                    }
                    getResponse.insertResponse(newJsonArray, true)
                } else {
                    val skyflowError = SkyflowError(
                        SkyflowErrorCode.BAD_REQUEST, tag, apiClient.logLevel
                    )
                    val responseObject = constructErrorResponseForGet(record, skyflowError)
                    getResponse.insertResponse(JSONArray().put(responseObject), false)
                }
            } catch (e: Exception) {
                val skyflowError = SkyflowError(
                    SkyflowErrorCode.UNKNOWN_ERROR, tag, apiClient.logLevel,
                    arrayOf(e.message.toString())
                )
                skyflowError.setErrorCode(response.code)
                val responseObject = constructErrorResponseForGet(record, skyflowError)
                getResponse.insertResponse(JSONArray().put(responseObject), false)
            }
        }
    }

    private fun constructErrorResponseForGet(
        record: GetRecord,
        skyflowError: SkyflowError
    ): JSONObject {
        val responseObject = JSONObject()
        responseObject.put("error", skyflowError)

        if (record.skyflowIds != null) {
            responseObject.put("ids", record.skyflowIds)
        } else if (record.columnName != null) {
            responseObject.put("columnName", record.columnName)
            responseObject.put("columnValues", record.columnValues)
        }

        return responseObject
    }

    override fun onFailure(exception: Any) {
        if (exception is Exception) {
            callback.onFailure(Utils.constructError(exception))
        } else {
            callback.onFailure(exception)
        }
    }

}