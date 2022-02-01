package Skyflow.core

import Skyflow.*
import Skyflow.Callback
import Skyflow.utils.Utils
import android.os.Handler
import android.os.Looper
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

    internal var connectionUrl = ""
    private var requestBody = JSONObject()
    internal var headerMap = HashMap<String,String>()
    internal var queryMap = HashMap<String,String>()
    internal var tokenIdMap = HashMap<String,String>() //key token,value is label id
    internal var tokenValueMap = HashMap<String,String?>() //key token, value is null before detokenize,value is value from api after detokenize
    internal var tokenLabelMap = HashMap<String,Label>() // key is token,value is label
    override fun onSuccess(responseBody: Any) {
        try{
            val token = responseBody.toString()
            validateResponseBody(connectionConfig.responseBody,HashSet())
            convertElementsHelper()
            if(tokenValueMap.isEmpty()){
                val requestBuild = getRequestBuild(token, requestBody.toString())
                if(requestBuild == null ) return
                sendRequest(requestBuild)
            }
            else {
               sendDetokenizeRequest(token)
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

    fun sendDetokenizeRequest(token: String) {
        client.detokenize(createRequestBodyForDetokenize(),object : Callback {
            override fun onSuccess(responseBody: Any) {
                try {
                    val requestBuild = createRequestForConnections(responseBody,token)
                    if(requestBuild == null ) return
                    sendRequest(requestBuild)
                }
                catch (e:Exception){
                    callback.onFailure(Utils.constructError(SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, tag, logLevel, params = arrayOf(e.message))))
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
                    callback.onFailure(Utils.constructError(SkyflowError(SkyflowErrorCode.NOT_VALID_TOKENS, tag, logLevel, params = arrayOf(tokens))))
                }
                catch (e:Exception){
                    callback.onFailure(exception)
                }
            }
        })
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

    fun createRequestForConnections(responseBody: Any, token: String): Request? {
        Utils.doTokenMap(responseBody,tokenValueMap)
        Utils.doformatRegexForMap(tokenValueMap,tokenLabelMap,tag,logLevel)
        var requestBodyString = requestBody.toString()
        var queryString = queryMap.toString().substring(1,queryMap.toString().length-1)
        tokenValueMap.forEach {
            queryString = queryString.replace(tokenIdMap.get(it.key)!!,it.value!!)
            connectionUrl = connectionUrl.replace(tokenIdMap.get(it.key)!!,it.value!!)
            requestBodyString = requestBodyString.replace(tokenIdMap.get(it.key)!!,it.value!!.trim())
        }
        if(queryString.isNotEmpty()) {
            val queryMapWithDetokenizeValues = queryString.split(",").associate {
                val (left, right) = it.split("=")
                left to right
            }
            queryMap = queryMapWithDetokenizeValues as HashMap<String, String>
        }
        val requestBuild = getRequestBuild(token,requestBodyString)
        return requestBuild
    }

    fun convertElementsHelper()  {  //convert skyflow elements,arrays into values
        Utils.copyJSON(connectionConfig.requestBody,requestBody)
        constructRequestBodyForConnection(requestBody)
        connectionConfig.requestBody = JSONObject()
        constructRequestBodyForConnection(connectionConfig.requestBody)
        connectionUrl = addPathParamsToURL()
        addQueryParams()
        addRequestHeader()
    }

    fun getRequestBuild(responseBody: Any, requestBodyString: String): Request? { //create requestBuild
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
        val body: RequestBody = requestBodyString
            .toRequestBody("application/json".toByteArray().toString().toMediaTypeOrNull())
        val request = Request
            .Builder()
            .method(connectionConfig.methodName.toString(), body)
            .addHeader("X-Skyflow-Authorization",responseBody.toString().split("Bearer ")[1])
            .addHeader("Content-Type","application/json")
            .url(requestUrl)
        //adding header
        headerMap.forEach {
            if(it.key.equals("X-Skyflow-Authorization"))
                request.removeHeader(it.key)
            else if(it.key.equals("Content-Type"))
                request.removeHeader(it.key)
            request.addHeader(it.key,it.value)
        }
        val  requestBuild = request.build()
        return requestBuild
    }
    fun sendRequest(requestBuild: Request) { //send request to Connection
        okHttpClient.newCall(requestBuild).enqueue(object : okhttp3.Callback{
            override fun onFailure(call: Call, e: IOException) {
                callback.onFailure(Utils.constructError(e))
            }

            override fun onResponse(call: Call, response: Response) {
                verifyResponse(response)
            }
        })
    }
    internal fun verifyResponse(response: Response)
    {
        response.use {
            try {
                if (!response.isSuccessful && response.body != null)
                {
                    callback.onFailure(Utils.constructError(Exception(" ${response.body?.string()}"),
                        response.code
                    ))
                }
                else if(response.isSuccessful && response.body != null)
                {
                    try {
                        val responseFromConnection =JSONObject(response.body!!.string())
                        parseResponse(connectionConfig.responseBody,responseFromConnection)
                        callback.onSuccess(responseFromConnection)
                    }
                    catch (e:Exception){
                        callback.onFailure(Utils.constructError(e))
                    }

                }
                else
                {
                    val skyflowError = SkyflowError(SkyflowErrorCode.BAD_REQUEST, tag = tag, logLevel = logLevel)
                    callback.onFailure(Utils.constructError(skyflowError, response.code))
                }
            }
            catch (e:Exception)
            {
                val skyflowError = SkyflowError(SkyflowErrorCode.UNKNOWN_ERROR, tag = tag, logLevel = logLevel, arrayOf(e.message.toString()))
                skyflowError.setErrorCode(400)
                callback.onFailure(Utils.constructError(skyflowError, response.code))
            }
        }
    }
    private var arrayInRequestBody : JSONArray = JSONArray()
    //changing pci elements to actual values in it for request to connection
    fun constructRequestBodyForConnection(records: JSONObject) {
        val keys = records.names()
        if(keys !=null) {
            for (j in 0 until keys.length()) {
                if(keys.getString(j).isEmpty())
                {
                    throw SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_REQUEST_BODY,
                        tag, logLevel)
                }
                var value: Any
                if (records.get(keys.getString(j)) is Element) {
                    val element = (records.get(keys.getString(j)) as Element)
                    if(!Utils.checkIfElementsMounted(element))
                        throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(keys.getString(j)))
                    checkForValidElement(element)
                    value = (records.get(keys.getString(j)) as Element).getValue()
                } else if (records.get(keys.getString(j)) is Label) {
                    if(!Utils.checkIfElementsMounted(records.get(keys.getString(j)) as Label))
                        throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(keys.getString(j)))
                    else if ((records.get(keys.getString(j)) as Label).isTokenNull) 
                       throw SkyflowError(SkyflowErrorCode.MISSING_TOKEN_IN_CONNECTION_REQUEST, tag, logLevel, arrayOf(keys.getString(j)))
                     else if ((records.get(keys.getString(j)) as Label).revealInput.token!!.isEmpty()) 
                        throw SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID, tag, logLevel)
                    value = Utils.getValueForLabel(records.get(keys.getString(j)) as Label,tokenValueMap,tokenIdMap,tokenLabelMap,tag,logLevel)
                } else if (records.get(keys.getString(j)) is JSONObject) {
                    constructRequestBodyForConnection(records.get(keys.getString(j)) as JSONObject, )
                    value = JSONObject(records.get(keys.getString(j)).toString())
                }
                else if(records.get(keys.getString(j)) is JSONArray)
                {
                    val arrayValue = (records.get(keys.getString(j)) as JSONArray)
                    for(k in 0 until arrayValue.length())
                    {
                        if(arrayValue.get(k) is Element)
                        {
                            val element = (arrayValue.get(k) as Element)
                            if(!Utils.checkIfElementsMounted(element))
                                throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(keys.getString(j)))
                            checkForValidElement(element)
                            value = (arrayValue.get(k)  as Element).getValue()
                        }
                        else if(arrayValue.get(k) is Label)
                        {
                            if(!Utils.checkIfElementsMounted(arrayValue.get(k) as Label))
                                throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(keys.getString(j)))
                            else if ((arrayValue.get(k) as Label).isTokenNull) 
                                throw SkyflowError(SkyflowErrorCode.MISSING_TOKEN, tag, logLevel)
                                else if ((arrayValue.get(k) as Label).revealInput.token!!.isEmpty()) 
                                throw SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID, tag, logLevel)
                            value = Utils.getValueForLabel(arrayValue.get(k) as Label,tokenValueMap,tokenIdMap,tokenLabelMap,tag,logLevel)
                        }
                        else if(arrayValue.get(k) is JSONObject)
                        {
                            constructRequestBodyForConnection(arrayValue.get(k)  as JSONObject)
                            value = JSONObject(arrayValue.get(k) .toString())
                        }
                        else if(arrayValue.get(k) is String || arrayValue.get(k) is Number || arrayValue.get(k) is Boolean)
                            value = arrayValue.get(k) .toString()
                        else
                            throw SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_BODY, tag, logLevel, arrayOf(keys.getString(j)))
                        arrayValue.put(k,value)
                    }
                    value = arrayValue
                }
                else if(records.get(keys.getString(j)) is Array<*>)
                {
                    val arrayValue =(records.get(keys.getString(j)) as Array<*>)
                    for(k in arrayValue.indices)
                    {
                        if(arrayValue[k] is Element)
                        {
                            val element = (arrayValue[k] as Element)
                            if(!Utils.checkIfElementsMounted(element))
                                throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(keys.getString(j)))
                            checkForValidElement(element)
                            value = (arrayValue[k] as Element).getValue()
                        }
                        else if(arrayValue[k] is Label)
                        {
                            if(!Utils.checkIfElementsMounted(arrayValue[k] as Label))
                            
                                throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(keys.getString(j)))
                            else if ((arrayValue[k] as Label).isTokenNull) 
                                throw SkyflowError(SkyflowErrorCode.MISSING_TOKEN, tag, logLevel)
                              else if ((arrayValue[k] as Label).revealInput.token!!.isEmpty()) 
                                throw SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID, tag, logLevel)
                            else
                                value = Utils.getValueForLabel(arrayValue[k] as Label,tokenValueMap,tokenIdMap,tokenLabelMap,tag,logLevel)
                        }
                        else if(arrayValue[k] is JSONObject)
                        {
                            constructRequestBodyForConnection(JSONObject(arrayValue[k].toString()))
                            value = JSONObject(arrayValue[k].toString())
                        }
                        else if(arrayValue[k] is String || arrayValue[k] is Number || arrayValue[k] is Boolean)
                            value = arrayValue[k].toString()
                        else
                            throw SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_BODY, tag, logLevel, arrayOf(keys.getString(j)))
                        arrayInRequestBody.put(k,value)
                    }
                    value = arrayInRequestBody
                    arrayInRequestBody = JSONArray()
                }
                else if (records.get(keys.getString(j)) is String || records.get(keys.getString(j)) is Number || records.get(keys.getString(j)) is Boolean)
                    value = records.get(keys.getString(j)).toString()
                else {
                    throw SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_BODY, tag, logLevel, arrayOf(keys.getString(j)))
                }
                records.put(keys.getString(j), value)
            } }
    }

    //adding requestHeader for connection url
     fun addRequestHeader() {
        val headers = (connectionConfig.requestHeader as JSONObject).names()
        if (headers != null) {
            for(i in 0 until headers.length())
            {

                if(headers.getString(i).isEmpty())
                {
                    throw SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_REQUEST_HEADER_PARAMS) //empty key
                }
                if(headers.getString(i).equals("X-Skyflow-Authorization"))
                    headerMap.remove("X-Skyflow-Authorization")
                if(connectionConfig.requestHeader.get(headers.getString(i)) is String || connectionConfig.requestHeader.get(headers.getString(i)) is Number
                    || connectionConfig.requestHeader.get(headers.getString(i)) is Boolean)
                    headerMap.put(headers.getString(i),connectionConfig.requestHeader.getString(headers.getString(i)))
                else
                {
                    //callback.onFailure(Exception("invalid field \"${headers.getString(i)}\" present in requestHeader"))
                    val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_HEADER_PARAMS, tag, logLevel, arrayOf(headers.getString(i)))
                    throw skyflowError
                }
            }
        }
    }
    //adding query params for connection url
     fun addQueryParams() {
        val queryParams = (connectionConfig.queryParams).names()
        if(queryParams != null) {
            for (i in 0 until queryParams.length()) {
                if(queryParams.getString(i).isEmpty())
                {
                    throw SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_QUERY_PARAMS)//empty key
                }
                val value = connectionConfig.queryParams.get(queryParams.getString(i))
                if(value is Array<*>)
                {
                    for(j in 0 until value.size)
                    {
                       helperForQueryParams(value[j], queryParams.getString(i))
                    } }
                else {
                    helperForQueryParams(value, queryParams.getString(i))
                }
            }
        }
    }

     fun helperForQueryParams(
        value: Any?, key: String
    )
    {

        if (value is Element)
        {
            if(!Utils.checkIfElementsMounted(value))
            {
                throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(key))
            }
            checkForValidElement(value)
            queryMap.put(key, value.getValue())
        }
        else if (value is Label)
        {
            if(!Utils.checkIfElementsMounted(value))
            {
                throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(key))
            }
            else if (value.isTokenNull) {
                val error = SkyflowError(SkyflowErrorCode.MISSING_TOKEN, tag, logLevel)
               throw  error
            }  else if (value.revealInput.token!!.isEmpty()) {
                val error = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID, tag, logLevel)
                throw error
            }
            queryMap.put(key,Utils.getValueForLabel(value,tokenValueMap,tokenIdMap,tokenLabelMap,tag,logLevel))
        }
        else if (value is Number || value is String || value is Boolean || value is JSONObject)
        {
            queryMap.put(key, value.toString())
        }
        else {
            //callback.onFailure(Exception("invalid field \"${key}\" present in queryParams"))
            val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_QUERY_PARAMS, tag, logLevel, arrayOf(key))
            throw skyflowError
        }
    }
    //adding path params to connection url
    fun addPathParamsToURL() :String
    {
            var newURL = connectionConfig.connectionURL
            val params = connectionConfig.pathParams
            val keys =(connectionConfig.pathParams).names()
            if(keys !=null) {
                for (j in 0 until keys.length()) {
                    if(keys.getString(j).isEmpty())
                    {
                       throw SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_PATH_PARAMS, tag, logLevel) //empty key
                    }
                    var value = params.get(keys.getString(j))
                    if (value is Element) {
                        val element = value
                        if(!Utils.checkIfElementsMounted(element))
                        {
                            val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(keys.getString(j)))
                            throw error
                        }
                        checkForValidElement(element)
                        newURL = newURL.replace("{" + keys.getString(j) + "}", element.getValue())
                    } else if (value is Label) {
                        if(!Utils.checkIfElementsMounted(value))
                        {
                            val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(keys.getString(j)))
                            throw error
                        }
                        else if (value.isTokenNull) {
                            val error = SkyflowError(SkyflowErrorCode.MISSING_TOKEN, tag, logLevel)
                            throw error
                        }  else if (value.revealInput.token!!.isEmpty()) {
                            val error = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID, tag, logLevel)
                            throw error
                        }
                        else
                            value = Utils.getValueForLabel(value,tokenValueMap,tokenIdMap,tokenLabelMap,tag,logLevel)
                        newURL = newURL.replace("{" + keys.getString(j) + "}", value)
                    } else if (value is String || value is Number || value is Boolean) {
                        value = value.toString()
                        newURL = newURL.replace("{" + keys.getString(j) + "}", value)
                    } else {
                        val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_PATH_PARAMS, tag, logLevel, arrayOf(keys.getString(j)))
                        throw skyflowError
                    }
                }
            }
        return newURL

    }

    fun checkForValidElement(element: Element)
    {
        val state = element.getState()
        var errors = ""
        var labelName = element.columnName
        if(labelName == ""){
            labelName = element.collectInput.label
        }
        if ((state["isRequired"] as Boolean) && (state["isEmpty"] as Boolean)) {
            errors = "$labelName is empty\n"
        }
        if (!(state["isValid"] as Boolean)) {
            errors = "for " + labelName + " " + (state["validationError"] as String) + "\n"
        }
        if (errors != "") {
            val error = SkyflowError(SkyflowErrorCode.INVALID_INPUT, tag, logLevel, arrayOf(errors))
            throw error
        }
    }
    // checking duplicate fields present in responseBody of connectionConfig
    fun  validateResponseBody(
        responseBody: JSONObject,
        elementList: HashSet<String>
    )
    {
        val keys = responseBody.names()
        if(keys != null) {
            for (j in 0 until keys.length()) {

                if(responseBody.get(keys.getString(j)) is Element)
                {
                    val element = (responseBody.get(keys.getString(j))) as Element
                    if(!Utils.checkIfElementsMounted(element))
                        throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, Utils.tag, logLevel, arrayOf(keys.getString(j)))
                }
                else if(responseBody.get(keys.getString(j)) is Label)
                {
                    val element = (responseBody.get(keys.getString(j))) as Label
                    if(!Utils.checkIfElementsMounted(element))
                        throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, Utils.tag, logLevel, arrayOf(keys.getString(j)))
                }
                else if (responseBody.get(keys.getString(j)) is JSONObject) {
                    validateResponseBody(responseBody.get(keys.getString(j)) as JSONObject, elementList)
                } else if (responseBody.get(keys.getString(j)) !is Element && !(responseBody.get(keys.getString(j)) is Label))
                    throw Exception("invalid field " + keys.getString(j) + " present in response body")
                if (responseBody.get(keys.getString(j)) is Element || responseBody.get(keys.getString(j)) is Label) {
                    if (elementList.contains(responseBody.get(keys.getString(j)).hashCode().toString()))
                        throw SkyflowError(SkyflowErrorCode.DUPLICATE_ELEMENT_FOUND, Utils.tag,logLevel)
                    else
                        elementList.add(responseBody.get(keys.getString(j)).hashCode().toString())
                }
            }
        }
    }
    //response for invokeConnection
    fun parseResponse(
        responseBody: JSONObject,
        responseFromConnection: JSONObject,
    ): JSONObject {
            val finalResponse = helperForParseResponse(responseBody,responseFromConnection)
            Utils.removeEmptyAndNullFields(responseFromConnection)
            return finalResponse
    }

    //displaying data to pci elements and removing pci element values from response
    private var connectionResponse = JSONObject()
    fun helperForParseResponse(
        responseBody: JSONObject,
        responseFromConnection: JSONObject
    ) : JSONObject
    {
        val keys = responseBody.names()
        if(keys != null) {
            for (j in 0 until keys.length()) {
                try {


                    if (responseBody.get(keys.getString(j)) is Element) {
                        val ans = responseFromConnection.getString(keys.getString(j))
                        Handler(Looper.getMainLooper()).post(Runnable {
                            (responseBody.get(keys.getString(j)) as TextField).setText(ans)
                        })
                        responseFromConnection.remove(keys.getString(j))
                    } else if (responseBody.get(keys.getString(j)) is Label) {
                        val ans = responseFromConnection.getString(keys.getString(j))
                        Handler(Looper.getMainLooper()).post(Runnable {
                            Utils.getValueForLabel(responseBody.get(keys.getString(j)) as Label,ans,tag,logLevel)
                        })
                        (responseBody.get(keys.getString(j)) as Label).actualValue = ans
                        responseFromConnection.remove(keys.getString(j))
                    } else if (responseBody.get(keys.getString(j)) is JSONObject) {
                        helperForParseResponse(responseBody.get(keys.getString(j)) as JSONObject,
                            responseFromConnection.getJSONObject(keys.getString(j)))
                    }
                }
                catch (e:Exception)
                {
                    if(e is SkyflowError)
                        throw e
                    else
                    throw SkyflowError(SkyflowErrorCode.NOT_FOUND_IN_RESPONSE,
                        Utils.tag, logLevel, arrayOf(keys.getString(j)))
                }
            } }
        connectionResponse.put("success",responseFromConnection)
        return connectionResponse
    }
}