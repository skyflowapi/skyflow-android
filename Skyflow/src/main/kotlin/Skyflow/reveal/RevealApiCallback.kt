package Skyflow.reveal

import Skyflow.Callback
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import Skyflow.core.APIClient
import Skyflow.utils.Utils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

internal class RevealApiCallback(
    var callback: Callback,
    var apiClient: APIClient,
    var records: MutableList<RevealRequestRecord>
) :
    Callback {
    private val tag = RevealApiCallback::class.qualifiedName
    private val revealResponse = RevealResponse(records.size, callback, apiClient.logLevel)
    private val okHttpClient = OkHttpClient()
    override fun onSuccess(responseBody: Any) {
        try {
            for (record in records) {
                val request = buildRequest(responseBody,record)
                sendRequest(request,record)
            }
        }catch (e: Exception){
            val skyflowError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, tag = tag, logLevel = apiClient.logLevel, arrayOf(e.message.toString()))
            skyflowError.setErrorCode(400)
            callback.onFailure(Utils.constructError(e))
        }
    }

    internal fun buildRequest(responseBody: Any, record: RevealRequestRecord): Request {
        val url = apiClient.vaultURL + apiClient.vaultId + "/detokenize"
        val body = JSONObject()
        val detokenizationParameters = JSONArray()

        val parameterObject = JSONObject()
        parameterObject.put("token", record.token)
        parameterObject.put("redaction", record.redaction)
        detokenizationParameters.put(parameterObject)

        body.put("detokenizationParameters", detokenizationParameters)

        val request = Request
            .Builder()
            .method("POST", body.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .addHeader("Authorization", "$responseBody")
            .url(url)
            .build()
        return request
    }
    internal fun sendRequest(request: Request, record: RevealRequestRecord)
    {
        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                val resObj = JSONObject()
                val skyflowError = SkyflowError(params = arrayOf(e.message.toString()))
                resObj.put("error", skyflowError)
                resObj.put("token", record.token)
                revealResponse.insertResponse(resObj, false)
            }
            override fun onResponse(call: Call, response: Response) {
                verifyResponse(response,record)
            }
        })
    }

    internal fun verifyResponse(response: Response, record: RevealRequestRecord)
    {
        response.use {
            try {
                if (!response.isSuccessful && response.body != null) {
                    val responsebody = response.body!!.string()
                    try {
                        val resObj = JSONObject()
                        val responseErrorBody = JSONObject(responsebody)
                        val requestId = response.headers.get("x-request-id").toString()
                        val skyflowError = SkyflowError(SkyflowErrorCode.SERVER_ERROR, tag = tag, logLevel = apiClient.logLevel, arrayOf(Utils.appendRequestId((responseErrorBody.get("error") as JSONObject).get("message").toString(),requestId)))
                        skyflowError.setErrorCode(response.code)
                        resObj.put("error", skyflowError)
                        resObj.put("token", record.token)
                        revealResponse.insertResponse(resObj, false)
                    }
                    catch (e:Exception)
                    {
                        val skyflowError = SkyflowError(SkyflowErrorCode.SERVER_ERROR, tag = tag, logLevel = apiClient.logLevel, arrayOf(responsebody))
                        skyflowError.setErrorCode(response.code)
                        val resObj = JSONObject()
                        resObj.put("error", skyflowError)
                        resObj.put("token", record.token)
                        revealResponse.insertResponse(resObj, false)
                    }
                }
                else if (response.isSuccessful && response.body != null) {
                    val responseString = response.body!!.string()
                    revealResponse.insertResponse(JSONObject(responseString), true)
                } else {
                    val resObj = JSONObject()
                    val skyflowError = SkyflowError(SkyflowErrorCode.BAD_REQUEST, tag, apiClient.logLevel)
                    resObj.put("error", skyflowError)
                    resObj.put("token", record.token)
                    revealResponse.insertResponse(resObj, false)
                }
            }catch (e: Exception){
                val skyflowError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, tag = tag, logLevel = apiClient.logLevel, arrayOf(e.message.toString()))
                skyflowError.setErrorCode(400)
                val resObj = JSONObject()
                resObj.put("error", skyflowError)
                resObj.put("token", record.token)
                revealResponse.insertResponse(resObj, false)
            }
        }
    }
    override fun onFailure(exception: Any) {
        if(exception is Exception)
        {
            callback.onFailure(Utils.constructError(exception))
        }
        else
            callback.onFailure(exception)
    }
}
