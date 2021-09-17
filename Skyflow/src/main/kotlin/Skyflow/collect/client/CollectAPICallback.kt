package Skyflow.collect.client

import Skyflow.core.APIClient
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import Skyflow.Callback
import Skyflow.InsertOptions
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
        val jsonBody: JSONObject = apiClient.constructBatchRequestBody(records, options)
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
                callback.onFailure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if(!response.isSuccessful)
                    {
                        callback.onFailure(IOException("Unexpected code ${response.body()?.string()}"))
                        return
                    }
                    val responsebody = response.body()!!.string()
                    callback.onSuccess(buildResponse(JSONObject(responsebody)["responses"] as JSONArray))
                }
            }
        })}catch (e: Exception){
            callback.onFailure(e)
        }
    }

    override fun onFailure(exception: Exception) {
        callback.onFailure(exception)
    }

    private fun buildResponse(responseJson: JSONArray) : JSONObject{
        val inputRecords = this.records["records"] as JSONArray
        val recordsArray = JSONArray()
        val responseObject = JSONObject()
        if(this.options.tokens){
            for (i in responseJson.length()/2 until responseJson.length()){
                val record = JSONObject(responseJson[i].toString().replace("\"*\":", "\"skyflow_id\":"))
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