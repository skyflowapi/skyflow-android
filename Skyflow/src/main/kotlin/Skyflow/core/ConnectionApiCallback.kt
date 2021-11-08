package Skyflow.core

import Skyflow.*
import Skyflow.Callback
import Skyflow.utils.Utils
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ConnectionApiCallback(
    val connectionConfig : ConnectionConfiguration,
    val callback: Callback,
    val logLevel: LogLevel = LogLevel.ERROR,
) : Callback{

    private val okHttpClient = OkHttpClient()

    private val tag = ConnectionApiCallback::class.qualifiedName

    override fun onSuccess(responseBody: Any) {
        try{
            //adding path params
            val connectionUrl = Utils.addPathparamsToURL(connectionConfig.connectionURL,
                connectionConfig.pathParams,callback, logLevel)
            if(connectionUrl.equals(""))
                return
            val requestUrlBuilder = connectionUrl.toHttpUrlOrNull()?.newBuilder()
            if(requestUrlBuilder == null){
                val error = SkyflowError(SkyflowErrorCode.INVALID_CONNECTION_URL,
                    tag, logLevel, arrayOf(connectionConfig.connectionURL))
                callback.onFailure(Utils.constructError(error))
                return
            }
            //creating url with query params
            val isQueryparamsAdded = Utils.addQueryParams(requestUrlBuilder,connectionConfig,callback, logLevel)
            if(!isQueryparamsAdded)
                return
            val requestUrl = requestUrlBuilder.build()

            //body for API
            val body: RequestBody = connectionConfig.requestBody.toString()
                .toRequestBody("application/json".toByteArray().toString().toMediaTypeOrNull())
            val request = Request
                .Builder()
                .method(connectionConfig.methodName.toString(), body)
                .addHeader("X-Skyflow-Authorization",responseBody.toString().split("Bearer ")[1])
                .addHeader("Content-Type","application/json")
                .url(requestUrl)
            //adding header
            val isHeaderAdded = Utils.addRequestHeader(request,connectionConfig,callback, logLevel)
            if(!isHeaderAdded)
                return

              // Building request
           val  requestBuild = request.build()
           okHttpClient.newCall(requestBuild).enqueue(object : okhttp3.Callback{
                override fun onFailure(call: Call, e: IOException) {
                    callback.onFailure(Utils.constructError(e))
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful)
                        {
                            callback.onFailure(Utils.constructError(Exception(" ${response.body?.string()}"),
                                response.code
                            ))

                        }
                        else
                        {
                            val responseFromConnection =JSONObject(response.body!!.string())
                            val finaleResponse = Utils.constructResponseBodyFromConnection(connectionConfig.responseBody,
                                responseFromConnection,callback,logLevel)
                            callback.onSuccess(finaleResponse)
                        }
                    }
                }
            })
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