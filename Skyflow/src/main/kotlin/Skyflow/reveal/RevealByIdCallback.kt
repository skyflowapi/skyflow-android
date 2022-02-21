package Skyflow.reveal

import Skyflow.Callback
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import Skyflow.core.APIClient
import Skyflow.utils.Utils
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

internal class RevealByIdCallback(
    var callback: Callback,
    var apiClient: APIClient,
    var records: MutableList<GetByIdRecord>
) :
    Callback {
    private val tag = RevealByIdCallback::class.qualifiedName
    private val revealResponse = RevealResponseByID(records.size, callback, apiClient.logLevel)
    private val okHttpClient = OkHttpClient();

    override fun onSuccess(responseBody: Any) {
        try {

            for (record in records) {
               val request = buildRequest(responseBody,record)
                if(request == null) return
                sendRequest(request,record)
            }
        }catch (e: Exception){
            callback.onFailure(Utils.constructError(e))
        }
    }

    internal fun sendRequest(request: Request, record: GetByIdRecord) {
        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                val resObj = JSONObject()
                val skyflowError = SkyflowError(params = arrayOf(e.message.toString()))
                resObj.put("error", skyflowError)
                resObj.put("ids", record.skyflow_ids)
                revealResponse.insertResponse(JSONArray().put(resObj), false)
            }

            override fun onResponse(call: Call, response: Response) {
               verifyResponse(response,record)
            }
        })
    }

    internal fun buildRequest(responseBody: Any, record: GetByIdRecord): Request? {
        val url = apiClient.vaultURL + apiClient.vaultId + "/"+record.table
        val requestUrlBuilder = url.toHttpUrlOrNull()?.newBuilder()
        if(requestUrlBuilder == null){
            val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL, tag, apiClient.logLevel, arrayOf(apiClient.vaultURL))
            callback.onFailure(Utils.constructError(error))
            return null
        }
        for( id in record.skyflow_ids)
            requestUrlBuilder.addQueryParameter("skyflow_ids", id)

        val requestUrl = requestUrlBuilder.addQueryParameter("redaction", record.redaction).build()
        val request = Request.Builder()
                    .addHeader("Authorization", "$responseBody").url(requestUrl).build()
        return request
    }

    internal fun verifyResponse(response: Response, record: GetByIdRecord)
    {
        response.use {
            try {
                if (!response.isSuccessful && response.body != null) {
                    val responsebody = response.body!!.string()
                    try {
                        val resObj = JSONObject()
                        val responseErrorBody = JSONObject(responsebody)
                        val requestId = response.headers.get("x-request-id").toString()
                        val skyflowError = SkyflowError(SkyflowErrorCode.SERVER_ERROR ,tag=tag, logLevel = apiClient.logLevel, arrayOf(Utils.getErrorMessageWithRequestId((responseErrorBody.get("error") as JSONObject).get("message").toString(),requestId)))
                        skyflowError.setErrorCode(response.code)
                        resObj.put("error", skyflowError)
                        resObj.put("ids", record.skyflow_ids)
                        revealResponse.insertResponse(JSONArray().put(resObj), false)
                    }
                    catch (e:Exception)
                    {
                        val resObj = JSONObject()
                        val skyflowError = SkyflowError(SkyflowErrorCode.SERVER_ERROR ,tag=tag, logLevel = apiClient.logLevel, params = arrayOf(responsebody))
                        skyflowError.setErrorCode(response.code)
                        resObj.put("error", skyflowError)
                        resObj.put("ids", record.skyflow_ids)
                        revealResponse.insertResponse(JSONArray().put(resObj), false)
                    }

                } else if (response.isSuccessful && response.body != null) {
                    val fields =JSONObject(response.body!!.string().replace("\"skyflow_id\":", "\"id\":")).getJSONArray("records")
                    var i = 0
                    val newJsonArray = JSONArray()
                    while (i<fields.length())
                    {
                        val jsonobj = JSONObject()
                        jsonobj.put("fields",fields.getJSONObject(i).getJSONObject("fields"))
                        jsonobj.put("table",record.table)
                        i++
                        newJsonArray.put(jsonobj)

                    }
                    revealResponse.insertResponse(
                        newJsonArray, true
                    )
                } else {
                    val resObj = JSONObject()
                    val skyflowError = SkyflowError(SkyflowErrorCode.BAD_REQUEST, tag, apiClient.logLevel)
                    resObj.put("error", skyflowError)
                    resObj.put("ids", record.skyflow_ids)
                    revealResponse.insertResponse(JSONArray().put(resObj), false)
                }
            }catch (e: Exception){
                val resObj = JSONObject()
                val skyflowError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR ,tag=tag, logLevel = apiClient.logLevel, params = arrayOf(e.message.toString()))
                skyflowError.setErrorCode(response.code)
                resObj.put("error", skyflowError)
                resObj.put("ids", record.skyflow_ids)
                revealResponse.insertResponse(JSONArray().put(resObj), false)
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
