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
                val requestUrlBuilder = HttpUrl.parse(url)?.newBuilder()
                if(requestUrlBuilder == null){
                    val error = SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL)
                    error.setErrorResponse(apiClient.vaultURL)
                    callback.onFailure(Utils.constructError(error))
                    return
                }
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
                                    val responseErrorBody = JSONObject(response.body()!!.string())
                                    val skyflowError = SkyflowError()
                                    skyflowError.setErrorMessage((responseErrorBody.get("error") as JSONObject).get("message").toString())
                                    skyflowError.setErrorCode( response.code())
                                    resObj.put("error", skyflowError)
                                    resObj.put("token", record.token)
                                    revealResponse.insertResponse(resObj, false)
                                } else if (response.body() != null) {
                                    val responseString = response.body()!!.string().toString()
                                    revealResponse.insertResponse(
                                        JSONObject(
                                            responseString.replace(
                                                "\"token_id\":", "\"token\":"
                                            )
                                        ), true
                                    )
                                } else {
                                    val resObj = JSONObject()
                                    val skyflowError = SkyflowError(SkyflowErrorCode.BAD_REQUEST)
                                    resObj.put("error", skyflowError)
                                    resObj.put("token", record.token)
                                    revealResponse.insertResponse(resObj, false)
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
