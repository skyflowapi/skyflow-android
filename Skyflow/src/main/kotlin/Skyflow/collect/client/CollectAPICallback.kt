package Skyflow.collect.client

import Skyflow.core.APIClient
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import Skyflow.Callback
import Skyflow.InsertOptions
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import Skyflow.utils.Utils
import android.util.Log
import java.io.IOException


internal class CollectAPICallback(
    private val apiClient: APIClient,
    private val records: JSONObject,
    val callback: Callback,
    private val options: InsertOptions,
) : Callback
{
    private val okHttpClient = OkHttpClient()

    override fun onSuccess(responseBody: Any) {
        try{
        val url =apiClient.vaultURL + apiClient.vaultId
        val jsonBody: JSONObject = Utils.constructBatchRequestBody(records, options,callback)
        if(jsonBody.toString().equals("{}")) return
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"), jsonBody.toString()
        )
        val request = Request
            .Builder()
            .method("POST", body)
            .addHeader("Authorization", "$responseBody")
            .url(url)
            .build()
        okHttpClient.newCall(request).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL)
                error.setErrorResponse(apiClient.vaultURL)
                callback.onFailure(error)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful)
                    {
                        val skyflowError = SkyflowError()
                        skyflowError.setErrorMessage("Unexpected code ${response.body()?.string()}")
                        skyflowError.setErrorCode(400)
                        callback.onFailure(skyflowError)
                    }
                    else
                    {
                        val responsebody = response.body()!!.string()
                        Log.d("response",responsebody)
                        callback.onSuccess(buildResponse(JSONObject(responsebody)["responses"] as JSONArray))
                    }
                }
            }
        })}catch (e: Exception){
            val skyflowError = SkyflowError()
            skyflowError.setErrorMessage(e.message.toString())
            skyflowError.setErrorCode(400)
            callback.onFailure(skyflowError)
        }
    }

    override fun onFailure(exception: Any) {
        callback.onFailure(exception)
    }

    private fun buildResponse(responseJson: JSONArray) : JSONObject{
        val inputRecords = this.records["records"] as JSONArray
        val recordsArray = JSONArray()
        val responseObject = JSONObject()
        if(this.options.tokens){
            for (i in responseJson.length()/2 until responseJson.length()){
                val record = JSONObject(responseJson[i].toString())
                val inputRecord = inputRecords.get(i - responseJson.length()/2) as JSONObject
                record.put("table", inputRecord["table"])
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