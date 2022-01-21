package Skyflow.core

import Skyflow.*
import Skyflow.Callback
import Skyflow.utils.Utils
import android.util.Log
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

internal class ConnectionApiCallback(
    val connectionConfig: ConnectionConfig,
    val callback: Callback,
    val logLevel: LogLevel = LogLevel.ERROR,
    val client: Client,
) : Callback{

    private val okHttpClient = OkHttpClient()

    private val tag = ConnectionApiCallback::class.qualifiedName

    private var connectionUrl = ""
    private var requestBody = ""
    internal var headerMap = HashMap<String,String>()
    internal var queryMap = HashMap<String,String>()
    override fun onSuccess(responseBody: Any) {
        try{
            val token = responseBody.toString()
            if(!convertElements()) return
            requestBody = connectionConfig.requestBody.toString()
            if(Utils.tokenValueMap.isEmpty()){
                val requestBuild = getRequestBuild(token)
                if(requestBuild == null ) return
                doRequest(requestBuild)
            }
            else {
                val records = JSONObject()
                val array = JSONArray()
                Utils.tokenValueMap.forEach {
                    val recordObj = JSONObject()
                    recordObj.put("token",it.key)
                    array.put(recordObj)
                }
                records.put("records",array)
                client.detokenize(records,object : Callback {
                    override fun onSuccess(responseBody: Any) {
                        doTokenMap(responseBody)
                        doformatRegexForMap()
                        var requestBody = connectionConfig.requestBody.toString()
                        var queryString = queryMap.toString().substring(1,queryMap.toString().length-1)
                        Utils.tokenValueMap.forEach {
                            queryString = queryString.replace(Utils.tokenIdMap.get(it.key)!!,it.value!!)
                            connectionUrl = connectionUrl.replace(Utils.tokenIdMap.get(it.key)!!,it.value!!)
                            requestBody = requestBody.replace(Utils.tokenIdMap.get(it.key)!!,it.value!!.trim())
                        }
                        val queryMapWithDetokenizeValues = queryString.split(",").associate {
                            val (left, right) = it.split("=")
                            left to right
                        }
                        queryMap  = queryMapWithDetokenizeValues as HashMap<String, String>
                        val requestBuild = getRequestBuild(token)
                        if(requestBuild == null ) return
                        doRequest(requestBuild)
                    }
                    override fun onFailure(exception: Any) {
                        val result = exception as JSONObject
                        result.remove("records")
                        callback.onFailure(result)
                    }

                })
            }
        }catch (e: Exception){
            callback.onFailure(Utils.constructError(e))
        }
    }

    private fun doformatRegexForMap() { // do regex on value after detokenize and put it in labelWithRegexMap
        Utils.tokenValueMap.forEach {
            val formatRegex = Utils.tokenLabelMap.get(it.key)!!.options.formatRegex
            val regex = Regex(formatRegex)
            val matches =  regex.find(it.value!!)
            val value = if(matches != null) matches.value else ""
            Utils.tokenValueMap.put(it.key,value)

        }
    }

    private fun doTokenMap(responseBody: Any) { // fill labelWithRegexMap with actual values from api
            val records = (responseBody as JSONObject).getJSONArray("records")
            for(i in 0  until records.length()) {
                val record = records[i] as JSONObject
                val token = record.getString("token")
                val value = record.getString("value")
                Utils.tokenValueMap.put(token,value)
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

    fun convertElements() : Boolean {  //convert skyflow elements,arrays into values
        connectionUrl = Utils.addPathParamsToURL(connectionConfig.connectionURL,
            connectionConfig.pathParams,callback, logLevel)
        if(connectionUrl == "")
            return false
        val isQueryParamAdded = Utils.addQueryParams(queryMap,connectionConfig,callback, logLevel)
        if(!isQueryParamAdded)
            return false
        val isHeaderAdded = Utils.addRequestHeader(headerMap,connectionConfig,callback, logLevel)
        if(!isHeaderAdded)
            return false
        return true
    }


    fun getRequestBuild(responseBody: Any): Request? { //create requestBuild
        val requestUrlBuilder = connectionUrl.toHttpUrlOrNull()?.newBuilder()
        if(requestUrlBuilder == null){
            val error = SkyflowError(SkyflowErrorCode.INVALID_CONNECTION_URL,
                tag, logLevel, arrayOf(connectionConfig.connectionURL))
            callback.onFailure(Utils.constructError(error))
            return null
        }
        //creating url with query params
        queryMap.forEach{
            requestUrlBuilder.addQueryParameter(it.key,it.value)
        }
        val requestUrl = requestUrlBuilder.build()

        //body for API
        val body: RequestBody = requestBody
            .toRequestBody("application/json".toByteArray().toString().toMediaTypeOrNull())
        val request = Request
            .Builder()
            .method(connectionConfig.methodName.toString(), body)
            .addHeader("X-Skyflow-Authorization",responseBody.toString().split("Bearer ")[1])
            .addHeader("Content-Type","application/json")
            .url(requestUrl)
        //adding header
        headerMap.forEach {
            request.addHeader(it.key,it.value)
        }
        val  requestBuild = request.build()
        return requestBuild
    }

    fun doRequest(requestBuild: Request) { //send request to Connection
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
    }
}