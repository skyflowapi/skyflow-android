package Skyflow.reveal

import Skyflow.Callback
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import Skyflow.core.APIClient
import Skyflow.utils.Utils
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
                    val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL)
                    error.setErrorResponse(apiClient.vaultURL)
                    callback.onFailure(Utils.constructError(error))

                    return
                }
                for( id in record.skyflow_ids)
                requestUrlBuilder.addQueryParameter("skyflow_ids", id)

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
                                    val responsebody =response.body()!!.string()
                                    val resObj = JSONObject()
                                    val responseErrorBody = JSONObject(responsebody)
                                    val skyflowError = SkyflowError()
                                    skyflowError.setErrorMessage((responseErrorBody.get("error") as JSONObject).get("message").toString())
                                    skyflowError.setErrorCode( response.code())
                                    resObj.put("error", skyflowError)
                                    resObj.put("ids", record.skyflow_ids)
                                    revealResponse.insertResponse(JSONArray().put(resObj), false)
                                } else if (response.body() != null) {
                                    val fields =JSONObject(response.body()!!.string().replace("\"skyflow_id\":", "\"id\":")).getJSONArray("records")
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
                                    val skyflowError = SkyflowError(SkyflowErrorCode.BAD_REQUEST)
                                    resObj.put("error", skyflowError)
                                    resObj.put("ids", record.skyflow_ids)
                                    revealResponse.insertResponse(JSONArray().put(resObj), false)
                                }
                            }catch (e: Exception){
                                callback.onFailure(Utils.constructError(e))
                            }
                        }
                    }
                })
            }
        }catch (e: Exception){
            callback.onFailure(Utils.constructError(e))
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
