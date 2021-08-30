package Skyflow.reveal

import Skyflow.Callback
import Skyflow.core.APIClient
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

internal class RevealApiCallback(
    var callback: Callback,
    var apiClient: APIClient,
    var records: MutableList<Skyflow.reveal.RevealRequestRecord>
) :
    Callback {

    override fun onSuccess(responseBody: Any) {
        try {
            val okHttpClient = OkHttpClient();

            val revealResponse = RevealResponse(records.size, callback)

            for (record in records) {
                val url = apiClient.vaultURL + apiClient.vaultId + "/tokens"
                val requestUrlBuilder = HttpUrl.parse(url)!!.newBuilder()
                requestUrlBuilder.addQueryParameter("token_ids", record.token)

                val requestUrl = requestUrlBuilder.addQueryParameter(
                    "redaction",
                    record.redaction
                ).build()
                val request = Request.Builder()
                    .addHeader("Authorization", "$responseBody").url(requestUrl).build()

                okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        revealResponse.insertResponse()
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            try {
                                if (!response.isSuccessful && response.body() != null) {
                                    val resObj = JSONObject()
                                    val errorObj = JSONObject()
                                    val responseErrorBody = JSONObject(response.body()!!.string())
                                    errorObj.put("code", response.code().toString())
                                    errorObj.put(
                                        "description",
                                        (responseErrorBody.get("error") as JSONObject).get("message")
                                    )
                                    resObj.put("error", errorObj)
                                    resObj.put("id", record.token)
                                    revealResponse.insertResponse(resObj, false)
                                } else if (response.body() != null) {
                                    val responseString = response.body()!!.string().toString()
                                    revealResponse.insertResponse(
                                        JSONObject(
                                            responseString.replace(
                                                "\"token_id\":", "\"id\":"
                                            )
                                        ), true
                                    )
                                } else {
                                    val resObj = JSONObject()
                                    val errorObj = JSONObject()
                                    errorObj.put("code", "400")
                                    errorObj.put("description", "Bad Request")
                                    resObj.put("error", errorObj)
                                    resObj.put("id", record.token)
                                    revealResponse.insertResponse(resObj, false)
                                }
                            }catch (e: Exception){
                                callback.onFailure(e)
                            }
                        }
                    }
            })
            }
        }catch (e: Exception){
            callback.onFailure(e)
        }

    }


    override fun onFailure(exception: Exception) {
        callback.onFailure(exception)
    }

}
