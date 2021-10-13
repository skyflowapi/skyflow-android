package Skyflow.core

import Skyflow.Callback
import Skyflow.GatewayConfiguration
import Skyflow.SkyflowError
import Skyflow.SkyflowErrorCode
import Skyflow.utils.Utils
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class GatewayApiCallback(
    val gatewayConfig : GatewayConfiguration,
    val callback: Callback,
    val logLevel: LogLevel = LogLevel.PROD,
) : Callback{

    private val okHttpClient = OkHttpClient()

    private val tag = GatewayApiCallback::class.qualifiedName

    override fun onSuccess(responseBody: Any) {
        try{
            //adding path params
            val gatewayUrl = Utils.addPathparamsToURL(gatewayConfig.gatewayURL,
                gatewayConfig.pathParams,callback, logLevel)
            if(gatewayUrl.equals(""))
                return
            val requestUrlBuilder = gatewayUrl.toHttpUrlOrNull()?.newBuilder()
            if(requestUrlBuilder == null){
                val error = SkyflowError(SkyflowErrorCode.INVALID_GATEWAY_URL,
                    tag, logLevel, arrayOf(gatewayConfig.gatewayURL))
                callback.onFailure(Utils.constructError(error))
                return
            }
            //creating url with query params
            val isQueryparamsAdded = Utils.addQueryParams(requestUrlBuilder,gatewayConfig,callback, logLevel)
            if(!isQueryparamsAdded)
                return
            val requestUrl = requestUrlBuilder.build()

            //body for API
            val body: RequestBody = gatewayConfig.requestBody.toString()
                .toRequestBody("application/json".toByteArray().toString().toMediaTypeOrNull())
            val request = Request
                .Builder()
                .method(gatewayConfig.methodName.toString(), body)
                .addHeader("X-Skyflow-Authorization",responseBody.toString().split("Bearer ")[1])
                .addHeader("Content-Type","application/json")
                .url(requestUrl)
            //adding header
            val isHeaderAdded = Utils.addRequestHeader(request,gatewayConfig,callback, logLevel)
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
                            val responseFromGateway =JSONObject(response.body!!.string())
                            Utils.constructResponseBodyFromGateway(gatewayConfig.responseBody,
                                responseFromGateway,callback,logLevel)
                            callback.onSuccess(responseFromGateway)
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