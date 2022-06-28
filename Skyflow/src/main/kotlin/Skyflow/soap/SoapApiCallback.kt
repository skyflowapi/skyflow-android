/*
	Copyright (c) 2022 Skyflow, Inc. 
*/
package Skyflow.soap

import Skyflow.*
import Skyflow.Callback
import Skyflow.utils.Utils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap


internal class SoapApiCallback(
    var soapConnectionConfig: SoapConnectionConfig,
    var callback: Callback,
    var logLevel: LogLevel,
    var client: Client,
) : Callback {
    private val okHttpClient = OkHttpClient()
    private val tag = SoapApiCallback::class.qualifiedName

    internal var tokenIdMap = HashMap<String,String>() //key token,value is label id
    internal var tokenValueMap = HashMap<String,String?>() //key token, value is null before detokenize,value is value from api after detokenize
    internal var tokenLabelMap = HashMap<String,Label>() // key is token,value is label
    override fun onSuccess(responseBody: Any) {
        try{
            val requestBody = getRequestBody()
            val token = responseBody.toString()
            if(tokenValueMap.isEmpty()){
                val requestBuild = getRequestBuild(token,requestBody)
                sendRequest(requestBuild)
            }
            else {
                sendDetokenizeRequest(token,requestBody)
            }
        }catch (e: Exception){
            if(e is SkyflowError)
                callback.onFailure(e)
            else {
                val skyflowError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, tag = tag, logLevel = this.logLevel, arrayOf(e.message.toString()))
                skyflowError.setErrorCode(400)
                callback.onFailure(skyflowError)
            }
        }
    }

    override fun onFailure(exception: Any) {
        callback.onFailure(exception)
    }

    fun createRequestBodyForDetokenize(): JSONObject {
        val records = JSONObject()
        val array = JSONArray()
        tokenValueMap.forEach {
            val recordObj = JSONObject()
            recordObj.put("token",it.key)
            array.put(recordObj)
        }
        records.put("records",array)
        return records
    }

    fun sendDetokenizeRequest(token: String, requestBody: String) {
        val records = createRequestBodyForDetokenize()
        client.detokenize(records,object : Callback {
            override fun onSuccess(responseBody: Any) {
                try {
                    val requestBuild = createRequestForConnections(responseBody,token,requestBody)
                    sendRequest(requestBuild)
                }
                catch (e:Exception){
                    if(e is SkyflowError)
                        callback.onFailure(e)
                    else {
                        callback.onFailure((SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, tag, logLevel, params = arrayOf(e.message))))
                    }
                }
            }
            override fun onFailure(exception: Any) {
                try {
                    val result = exception as JSONObject
                    val errors = result.getJSONArray("errors")
                    var tokens = ""
                    for(i in 0 until errors.length()){
                        val error = errors.getJSONObject(i)
                        if(tokens.isNotEmpty())
                            tokens = tokens+", "+error.getString("token")
                        else
                            tokens = error.getString("token")
                    }
                    callback.onFailure(SkyflowError(SkyflowErrorCode.NOT_VALID_TOKENS, tag, logLevel, params = arrayOf(tokens)))
                }
                catch (e:Exception){
                    if(e is SkyflowError)
                        callback.onFailure(e)
                    else
                    callback.onFailure(exception)
                }
            }

        })

    }

    fun createRequestForConnections(
        responseBody: Any,
        token: String,
        requestBody: String
    ):Request{
        var requestBodyForConnections = requestBody
        Utils.doTokenMap(responseBody,tokenValueMap)
        Utils.doformatRegexForMap(tokenValueMap,tokenLabelMap,tag)
        tokenValueMap.forEach {
            requestBodyForConnections = requestBodyForConnections.replace(tokenIdMap.get(it.key)!!,it.value!!.trim())
        }
        val requestBuild = getRequestBuild(token,requestBodyForConnections)
        return requestBuild
    }
    fun getRequestBuild(token:String,requestBody:String): Request {
        val url = soapConnectionConfig.connectionURL
        val body: RequestBody = requestBody.toRequestBody("application/xml".toMediaTypeOrNull()) //adding body
        val request = Request
            .Builder()
            .method("POST", body)
            .addHeader("x-skyflow-authorization", token.split("Bearer ")[1])
            .url(url)
        soapConnectionConfig.httpHeaders.forEach { (key, value) ->
            if(key.lowercase(Locale.getDefault()).equals("x-skyflow-authorization"))
                request.removeHeader("x-skyflow-authorization")
            request.addHeader(key,value)
        } //adding headers
        val requestBuild = request.build()
        return requestBuild
    }

    fun sendRequest(requestBuild: Request) {
        okHttpClient.newCall(requestBuild).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                val skyflowError = SkyflowError(params = arrayOf(e.message.toString()))
                (this@SoapApiCallback).onFailure(skyflowError)
            }

            override fun onResponse(call: Call, response: Response) {
                verifyResponse(response)
            }
        })
    }

    fun verifyResponse(response: Response)
    {
        response.use {
            try {
                if (!response.isSuccessful && response.body != null)
                {
                    val res = response.body!!.string()
                    val skyflowError = SkyflowError(SkyflowErrorCode.SERVER_ERROR, tag = tag, logLevel = logLevel,
                        arrayOf(res))
                    skyflowError.setXml(res)
                    callback.onFailure(skyflowError)
                }
                else if(response.isSuccessful && response.body != null)
                {
                    val res = response.body!!.string()
                    callback.onSuccess(res)
                }
                else {
                    val skyflowError = SkyflowError(SkyflowErrorCode.BAD_REQUEST, tag = tag, logLevel = logLevel)
                    callback.onFailure(skyflowError)
                }
            }
            catch (e:Exception)
            {
                val skyflowError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, tag = tag, logLevel = logLevel,
                    arrayOf(e.message.toString()))
                callback.onFailure(skyflowError)
            }
        }
    }
    fun getRequestBody() :String{
        val matches = Utils.findMatches("<skyflow>([\\s\\S]*?)<\\/skyflow>",soapConnectionConfig.requestXML)
        matches.addAll(Utils.findMatches("<Skyflow>([\\s\\S]*?)<\\/Skyflow>",soapConnectionConfig.requestXML))
        var tempXML = soapConnectionConfig.requestXML
        matches.map {
            var temp = it
            temp = temp.substring(9,temp.length-10)
            if(temp.trim().isEmpty()){
                //id is empty in request xml
                throw SkyflowError(SkyflowErrorCode.EMPTY_ID_IN_REQUEST_XML,tag,logLevel)
            }
            val value = client.elementMap[temp.trim()]
            if(value != null) {
                if (value is TextField) {
                    if (!Utils.checkIfElementsMounted(value)) {
                       throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                            tag, logLevel, arrayOf(value.label.text.toString()))
                    }
                    else if(value.validate().isNotEmpty())
                    {
                        //invalid textfield
                        throw SkyflowError(SkyflowErrorCode.INVALID_INPUT,tag,logLevel, params = arrayOf("invalid element - "+value.validate()))
                    }
                    else {
                        tempXML = tempXML.replace(it,value.getValue())
                    }
                } else if (value is Label) {
                    if (Utils.checkIfElementsMounted(value)) {
                        tempXML = tempXML.replace(it,Utils.getValueForLabel(value,tokenValueMap,tokenIdMap,tokenLabelMap,tag,logLevel))
                    } else {
                        //element not mounted
                        throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(value.label.text.toString()))
                    }
                }
            }
            else {
                // id not present in elementMap
                throw SkyflowError(SkyflowErrorCode.INVALID_ID_IN_REQUEST_XML,tag,logLevel,params = arrayOf(temp.trim()))
            }
        }
        return tempXML
    }
}

