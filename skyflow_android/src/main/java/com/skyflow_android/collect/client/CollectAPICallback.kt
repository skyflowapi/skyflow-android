package com.skyflow_android.collect.client

import com.google.gson.JsonObject
import com.skyflow_android.core.APIClient
import com.skyflow_android.core.SkyflowCallback
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class CollectAPICallback(
    private val apiClient: APIClient,
    private val records: JSONObject,
    val callback: SkyflowCallback,
    private val options: InsertOptions,
) : SkyflowCallback
        {
            private val okHttpClient = OkHttpClient();

    override fun success(responseBody: String) {
        val url =apiClient.vaultURL + apiClient.vaultId
        val jsonBody: JSONObject = apiClient.constructBatchRequestBody(records, options)
        val body: RequestBody = RequestBody.create(
            MediaType.parse("application/json"), jsonBody.toString()
        )
        val request = okhttp3.Request.Builder().method("POST", body).url(url).build()
        try {
            val thread = Thread {
                run {
                    try {
                        val call: Call = okHttpClient.newCall(request)
                        val response: Response = call.execute()

                    } catch (e: IOException) {
                        callback.failure(e)
                    }
                }
            }
            thread.start()
        }catch (e: Exception){
            callback.failure(e)
        }
        }

    override fun failure(exception: Exception?) {
        callback.failure(exception)
    }

}