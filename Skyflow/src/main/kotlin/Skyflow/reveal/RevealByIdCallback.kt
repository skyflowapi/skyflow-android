package Skyflow.reveal

import Skyflow.Callback
import Skyflow.core.APIClient
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

internal class RevealByIdCallback(
    var callback: Callback,
    var apiClient: APIClient,
    var records: MutableList<GetByIdRecord>
) :
    Callback {

    override fun onSuccess(responseBody: Any) {
        try {
            val okHttpClient = OkHttpClient();

            val revealResponse = RevealResponseByID(records.size, callback)

            for (record in records) {
                val url = apiClient.vaultURL + apiClient.vaultId + "/"+record.table
                val requestUrlBuilder = HttpUrl.parse(url)?.newBuilder()
                if(requestUrlBuilder == null){
                    onFailure(Exception("Bad or missing url"))
                    return
                }
                for( id in record.skyflow_ids)
                requestUrlBuilder.addQueryParameter("skyflow_ids", id)

                val requestUrl = requestUrlBuilder.addQueryParameter(
                    "redaction",
                    record.redaction.toString()
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
                                    val responsebody =response.body()!!.string()
                                    val resObj = JSONObject()
                                    val errorObj = JSONObject()
                                    val responseErrorBody = JSONObject(responsebody)
                                    errorObj.put("code", response.code().toString())
                                    errorObj.put(
                                        "description",
                                        (responseErrorBody.get("error") as JSONObject).get("message")
                                    )
                                    resObj.put("error", errorObj)
                                    resObj.put("skyflow_ids", record.skyflow_ids.joinToString(separator = ","))
                                    revealResponse.insertResponse(JSONArray().put(resObj), false)
                                } else if (response.body() != null) {
                                    val fields =JSONObject(response.body()!!.string()).getJSONArray("records")
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
                                    val errorObj = JSONObject()
                                    errorObj.put("code", "400")
                                    errorObj.put("description", "Bad Request")
                                    resObj.put("error", errorObj)
                                    resObj.put("skyflow_ids", record.skyflow_ids)
                                    revealResponse.insertResponse(JSONArray().put(resObj), false)
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
