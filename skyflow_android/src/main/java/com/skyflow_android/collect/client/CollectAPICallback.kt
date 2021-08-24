package com.skyflow_android.collect.client

import com.skyflow_android.core.APIClient
import com.skyflow_android.core.protocol.SkyflowCallback
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


internal class CollectAPICallback(
    private val apiClient: APIClient,
    private val records: String,
    val callback: SkyflowCallback,
    private val options: InsertOptions,
) : SkyflowCallback
{
    private val okHttpClient = OkHttpClient()

    override fun success(responseBody: String) {
        val url =apiClient.vaultURL + apiClient.vaultId
        val jsonBody: JSONObject = apiClient.constructBatchRequestBody(records, options)
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"), jsonBody.toString()
        )
        val request = Request
            .Builder()
            .method("POST", body)
            .addHeader("Authorization", responseBody)
            .url(url)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                callback.failure(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if(!response.isSuccessful) throw IOException("Unexpected code ${response.body()?.string()}")

                    callback.success(buildResponse(JSONObject(response.body()!!.string())["responses"] as JSONArray))
                }
            }
        })
    }

    override fun failure(exception: Exception?) {
        callback.failure(exception)
    }

    private fun buildResponse(responseJson: JSONArray) : String{
        val inputRecords = JSONObject(this.records)["records"] as JSONArray
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
        return responseObject.put("records", recordsArray).toString()
    }

}