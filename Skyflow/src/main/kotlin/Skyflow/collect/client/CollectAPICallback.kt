package Skyflow.collect.client

import Skyflow.*
import Skyflow.Callback
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import Skyflow.core.*
import Skyflow.core.Logger
import Skyflow.utils.Utils
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException


internal class CollectAPICallback(
    private val apiClient: APIClient,
    private val records: JSONObject,
    val callback: Callback,
    private val options: InsertOptions,
    val logLevel : LogLevel
) : Callback
{
    private val okHttpClient = OkHttpClient()
    private val tag = CollectAPICallback::class.qualifiedName

    override fun onSuccess(responseBody: Any) {
        try{
            val url =apiClient.vaultURL + apiClient.vaultId
            Logger.info(tag, Messages.VALIDATE_RECORDS.getMessage(), apiClient.logLevel)
            val jsonBody: JSONObject = Utils.constructBatchRequestBody(records,
                options,
                callback,
                logLevel)
            if(jsonBody.toString() == "{}") return
            val body: RequestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
            val request = Request
                .Builder()
                .method("POST", body)
                .addHeader("Authorization", "$responseBody")
                .url(url)
                .build()
            okHttpClient.newCall(request).enqueue(object : okhttp3.Callback{
                override fun onFailure(call: Call, e: IOException) {
                    val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL, tag, apiClient.logLevel, arrayOf(apiClient.vaultURL))
                    (this@CollectAPICallback).onFailure(error)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful)
                        {
                            val skyflowError = SkyflowError(SkyflowErrorCode.SERVER_ERROR, tag= tag, logLevel = apiClient.logLevel, arrayOf(response.body?.string()))
                            skyflowError.setErrorCode(response.code)
                            callback.onFailure(skyflowError)
                        }
                        else
                        {
                            val responsebody = response.body!!.string()
                            callback.onSuccess(buildResponse(JSONObject(responsebody)["responses"] as JSONArray))
                        }
                    }
                }
            })}catch (e: Exception){
            val skyflowError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, tag = tag, logLevel = apiClient.logLevel, arrayOf(e.message.toString()))
            skyflowError.setErrorCode(400)
            callback.onFailure(skyflowError)
        }
    }

    override fun onFailure(exception: Any) {
        callback.onFailure(exception)
    }

    internal fun buildResponse(responseJson: JSONArray) : JSONObject{
        
        val inputRecords = this.records["records"] as JSONArray
        val recordsArray = JSONArray()
        val responseObject = JSONObject()
        if(this.options.tokens){
            for (i in responseJson.length()/2 until responseJson.length()){
                val skyflowIDsObject = JSONObject(responseJson[i -
                        (responseJson.length() - responseJson.length()/2)].toString())
                val skyflowIDs = skyflowIDsObject.getJSONArray("records")
                val skyflowID = JSONObject(skyflowIDs[0].toString()).get("skyflow_id")
                val record = JSONObject(responseJson[i].toString())
                val inputRecord = inputRecords.get(i - responseJson.length()/2) as JSONObject
                record.put("table", inputRecord["table"])
                val fields = JSONObject(record.get("fields").toString())
                fields.put("skyflow_id", skyflowID)
                record.put("fields", fields)
                recordsArray.put(record)
            }
        }
        else{
            for (i in 0 until responseJson.length()){
                val inputRecord = inputRecords.get(i) as JSONObject
                val record = (responseJson[i] as JSONObject)["records"] as JSONArray
                recordsArray.put((record.get(0) as JSONObject).put("table", inputRecord["table"]))
            }
        }
        return responseObject.put("records", recordsArray)
    }

}