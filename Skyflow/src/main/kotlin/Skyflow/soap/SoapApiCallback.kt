package Skyflow.soap

import Skyflow.*
import Skyflow.Callback
import Skyflow.utils.Utils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException



internal class SoapApiCallback(
    var soapConnectionConfig: SoapConnectionConfig,
    var callback: Callback,
    var logLevel: LogLevel,
    var client: Client,
) : Callback {
    private val okHttpClient = OkHttpClient()
    private val tag = SoapApiCallback::class.qualifiedName

    override fun onSuccess(responseBody: Any) {
        try{
            val url = soapConnectionConfig.connectionURL
            val requestBody = getRequestBody()
            if(requestBody == null) return
            val body: RequestBody = requestBody.toRequestBody("application/xml".toMediaTypeOrNull()) //adding body
            val request = Request
                .Builder()
                .method("POST", body)
                .addHeader("X-Skyflow-Authorization", responseBody.toString().split("Bearer ")[1])
                .url(url)
            soapConnectionConfig.httpHeaders.forEach { (key, value) ->
                    if(key.equals("X-Skyflow-Authorization"))
                        request.removeHeader("X-Skyflow-Authorization")
                    request.addHeader(key,value)
            } //adding headers
            val requestBuild = request.build()
            okHttpClient.newCall(requestBuild).enqueue(object : okhttp3.Callback{
                override fun onFailure(call: Call, e: IOException) {
                    val skyflowError = SkyflowError(params = arrayOf(e.message.toString()))
                    (this@SoapApiCallback).onFailure(skyflowError)
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful)
                        {
                            val res = response.body!!.string()
                            val skyflowError = SkyflowError(SkyflowErrorCode.SERVER_ERROR, tag = tag, logLevel = logLevel,
                                arrayOf(res))
                            skyflowError.setXml(res)
                            callback.onFailure(skyflowError)
                        }
                        else
                        {
                            val res = response.body!!.string()
                            callback.onSuccess(res)
                        }
                    }
                }
            })}catch (e: Exception){
            val skyflowError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, tag = tag, logLevel = this.logLevel, arrayOf(e.message.toString()))
            skyflowError.setErrorCode(400)
            callback.onFailure(skyflowError)
        }
    }

    override fun onFailure(exception: Any) {
        callback.onFailure(exception)
    }

    fun getRequestBody() : String? {
        val matches = Utils.findMatches("<skyflow>([\\s\\S]*?)<\\/skyflow>",soapConnectionConfig.requestXML)
        matches.addAll(Utils.findMatches("<Skyflow>([\\s\\S]*?)<\\/Skyflow>",soapConnectionConfig.requestXML))
        var tempXML = soapConnectionConfig.requestXML
        matches.map {
            var temp = it
            temp = temp.substring(9,temp.length-10)
            if(temp.trim().isEmpty()){
                //id is empty in request xml
                val error = SkyflowError(SkyflowErrorCode.EMPTY_ID_IN_REQUEST_XML,tag,logLevel)
                callback.onFailure(error)
                return null

            }
            val value = client.elementMap[temp.trim()]
            if(value != null) {
                if (value is TextField) {
                    if (!Utils.checkIfElementsMounted(value)) {
                        val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                            tag, logLevel, arrayOf(value.label.text.toString()))
                        callback.onFailure(error)
                        return null
                    }
                    else if(value.validate().isNotEmpty())
                    {
                        //invalid textfield
                        val error = SkyflowError(SkyflowErrorCode.INVALID_INPUT,tag,logLevel, params = arrayOf("invalid element - "+value.validate()))
                        callback.onFailure(error)
                        return null
                    }
                    else {
                        tempXML = tempXML.replace(it,value.getValue())
                    }
                } else if (value is Label) {
                    if (Utils.checkIfElementsMounted(value)) {
                        tempXML = tempXML.replace(it,value.getValue())
                    } else {
                        //element not mounted
                        val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                            tag, logLevel, arrayOf(value.label.text.toString()))
                        callback.onFailure(error)
                        return null
                    }
                }
            }
            else {
                // id not present in elementMap
                val error = SkyflowError(SkyflowErrorCode.INVALID_ID_IN_REQUEST_XML,tag,logLevel,params = arrayOf(temp.trim()))
                callback.onFailure(error)
                return null
            }
        }
        return tempXML
    }
}

