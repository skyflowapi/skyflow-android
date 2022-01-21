package Skyflow.utils

import Skyflow.*
import Skyflow.LogLevel
import android.webkit.URLUtil
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.Exception

class Utils {

    companion object {
        val tag = Utils::class.qualifiedName
        fun checkUrl(url: String): Boolean {
            if (!URLUtil.isValidUrl(url) || !URLUtil.isHttpsUrl(url)) {
                return false
            }
            return true
        }

        //response for invokeConnection
        fun constructResponseBodyFromConnection(
            responseBody: JSONObject,
            responseFromConnection: JSONObject,
            callback: Callback,
            logLevel: LogLevel
        ) : JSONObject
        {
            return try {
                val isValid = checkInvalidFields(responseBody,responseFromConnection,callback)
                if(isValid) {
                    constructJsonKeyForConnectionResponse(responseBody,responseFromConnection,callback, logLevel)
                    removeEmptyAndNullFields(responseFromConnection)
                    responseFromConnection
                } else
                    JSONObject()
            } catch (e:Exception) {
                JSONObject()
            }
        }

        fun convertValues(responseBody: JSONObject,
                             responseFromConnection: JSONObject,
                             callback: Callback,
                             logLevel: LogLevel) {



        }


        //displaying data to pci elements and removing pci element values from response
        var errors = JSONArray()
        var connectionResponse = JSONObject()
        fun constructJsonKeyForConnectionResponse(
            responseBody: JSONObject,
            responseFromConnection: JSONObject,
            callback: Callback,
            logLevel: LogLevel
        ) : JSONObject
        {
            val keys = responseBody.names()
            if(keys != null) {
                for (j in 0 until keys.length()) {
                    try {


                        if (responseBody.get(keys.getString(j)) is Element) {
                            val ans = responseFromConnection.getString(keys.getString(j))
                            (responseBody.get(keys.getString(j)) as TextField).inputField.setText(
                                ans)
                            responseFromConnection.remove(keys.getString(j))
                        } else if (responseBody.get(keys.getString(j)) is Label) {
                            val ans = responseFromConnection.getString(keys.getString(j))
                            (responseBody.get(keys.getString(j)) as Label).placeholder.setText(
                                ans)
                            (responseBody.get(keys.getString(j)) as Label).actualValue = ans
                            responseFromConnection.remove(keys.getString(j))
                        } else if (responseBody.get(keys.getString(j)) is JSONObject) {
                            constructJsonKeyForConnectionResponse(responseBody.get(keys.getString(j)) as JSONObject,
                                responseFromConnection.getJSONObject(keys.getString(j)),
                                callback,logLevel)

                        }

                    }
                    catch (e:Exception)
                    {

                        val error = SkyflowError(SkyflowErrorCode.NOT_FOUND_IN_RESPONSE, tag, logLevel, arrayOf(keys.getString(j)))
                        val finalError = JSONObject()
                        finalError.put("error",error)
                        if(!connectionResponse.has("errors"))
                            connectionResponse.put("errors",JSONArray())
                        connectionResponse.getJSONArray("errors").put(finalError)
                        responseFromConnection.remove(keys.getString(j))
                    }
                } }
            connectionResponse.put("success",responseFromConnection)
            return connectionResponse
        }

        //request body for invokeConnection
        fun constructRequestBodyForConnection(records: JSONObject, callback: Callback, logLevel: LogLevel) : Boolean
        {
            return try {
                constructJsonKeyForConnectionRequest(records, callback, logLevel)
            } catch (e: Exception) {
                false
            } }

        var arrayInRequestBody : JSONArray = JSONArray()
        //changing pci elements to actual values in it for request to connection
        internal fun constructJsonKeyForConnectionRequest(records: JSONObject, callback: Callback, logLevel: LogLevel) : Boolean {
            val keys = records.names()
            if(keys !=null) {
                for (j in 0 until keys.length()) {
                    if(keys.getString(j).isEmpty())
                    {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_REQUEST_BODY, tag, logLevel))
                        return false
                    }
                    var value: Any
                    if (records.get(keys.getString(j)) is Element) {
                        val element = (records.get(keys.getString(j)) as Element)
                        if(!checkIfElementsMounted(element))
                        {
                            val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(keys.getString(j)))
                            callback.onFailure(constructError(error))
                            return false
                        }
                        val check = checkElement(element, callback, logLevel)
                        if (check)
                            value = (records.get(keys.getString(j)) as Element).getValue()
                        else
                            return false
                    } else if (records.get(keys.getString(j)) is Label) {
                        if(!checkIfElementsMounted(records.get(keys.getString(j)) as Label))
                        {
                            val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                                tag, logLevel, arrayOf(keys.getString(j)))
                            callback.onFailure(constructError(error))
                            return false
                        }
                        else if ((records.get(keys.getString(j)) as Label).isTokenNull) {
                            val error = SkyflowError(SkyflowErrorCode.MISSING_TOKEN_IN_CONNECTION_REQUEST,
                                tag, logLevel, arrayOf(keys.getString(j)))
                            callback.onFailure(constructError(error))
                            return false
                        }  else if ((records.get(keys.getString(j)) as Label).revealInput.token!!.isEmpty()) {
                            val error = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID,
                                tag, logLevel)
                            callback.onFailure(constructError(error))
                            return false
                        }
                        value =getValueForLabel(records.get(keys.getString(j)) as Label)
                    } else if (records.get(keys.getString(j)) is JSONObject) {
                        val isValid =
                            constructJsonKeyForConnectionRequest(records.get(keys.getString(j)) as JSONObject,
                                callback, logLevel)
                        if (isValid)
                            value = JSONObject(records.get(keys.getString(j)).toString())
                        else
                            return false
                    }
                    else if(records.get(keys.getString(j)) is JSONArray)
                    {
                        val arrayValue = (records.get(keys.getString(j)) as JSONArray)
                        for(k in 0 until arrayValue.length())
                        {
                            if(arrayValue.get(k) is Element)
                            {
                                val element = (arrayValue.get(k) as Element)
                                if(!checkIfElementsMounted(element))
                                {
                                    val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                                        tag, logLevel, arrayOf(keys.getString(j)))
                                    callback.onFailure(constructError(error))
                                    return false
                                }
                                val check = checkElement(element, callback, logLevel)
                                if (check)
                                    value = (arrayValue.get(k)  as Element).getValue()
                                else
                                    return false
                            }
                            else if(arrayValue.get(k) is Label)
                            {
                                if(!checkIfElementsMounted(arrayValue.get(k) as Label))
                                {
                                    val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(keys.getString(j)))
                                    callback.onFailure(constructError(error))
                                    return false
                                }
                                else if ((arrayValue.get(k) as Label).isTokenNull) {
                                    val error = SkyflowError(SkyflowErrorCode.MISSING_TOKEN,
                                        tag, logLevel)
                                    callback.onFailure(constructError(error))
                                    return false
                                }  else if ((arrayValue.get(k) as Label).revealInput.token!!.isEmpty()) {
                                    val error = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID,
                                        tag, logLevel)
                                    callback.onFailure(constructError(error))
                                    return false
                                }
                                value = getValueForLabel(arrayValue.get(k)  as Label)
                            }
                            else if(arrayValue.get(k) is JSONObject)
                            {
                                val isValid =
                                    constructJsonKeyForConnectionRequest(arrayValue.get(k)  as JSONObject,
                                        callback,logLevel)
                                if (isValid)
                                    value = JSONObject(arrayValue.get(k) .toString())
                                else
                                    return false
                            }
                            else if(arrayValue.get(k) is String || arrayValue.get(k) is Number || arrayValue.get(k) is Boolean)
                            {
                                value = arrayValue.get(k) .toString()
                            }
                            else
                            {
                                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_BODY, tag,
                                    logLevel, arrayOf(keys.getString(j)))
                                callback.onFailure(skyflowError)
                                return false
                            }
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
                                if(!checkIfElementsMounted(element))
                                {
                                    val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                                        tag, logLevel, arrayOf(keys.getString(j)))
                                    callback.onFailure(constructError(error))
                                    return false
                                }
                                val check = checkElement(element, callback, logLevel)
                                if (check)
                                {
                                    value = (arrayValue[k] as Element).getValue()
                                }
                                else
                                    return false
                            }
                            else if(arrayValue[k] is Label)
                            {
                                if(!checkIfElementsMounted(arrayValue[k] as Label))
                                {
                                    val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                                        tag, logLevel, arrayOf(keys.getString(j)))
                                    callback.onFailure(constructError(error))
                                    return false
                                }
                                else if ((arrayValue[k] as Label).isTokenNull) {
                                    val error = SkyflowError(SkyflowErrorCode.MISSING_TOKEN,
                                        tag, logLevel)
                                    callback.onFailure(constructError(error))
                                    return false
                                }  else if ((arrayValue[k] as Label).revealInput.token!!.isEmpty()) {
                                    val error = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID,
                                        tag, logLevel)
                                    callback.onFailure(constructError(error))
                                    return false
                                }
                                else
                                    value = getValueForLabel(arrayValue[k] as Label)
                            }
                            else if(arrayValue[k] is JSONObject)
                            {
                                val isValid =
                                    constructJsonKeyForConnectionRequest(
                                        arrayValue[k] as JSONObject,
                                        callback, logLevel)
                                if (isValid)
                                    value = JSONObject(arrayValue[k].toString())
                                else
                                    return false
                            }
                            else if(arrayValue[k] is String || arrayValue[k] is Number || arrayValue[k] is Boolean)
                            {
                                value = arrayValue[k].toString()
                            }
                            else
                            {
                                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_BODY,
                                    tag, logLevel, arrayOf(keys.getString(j)))
                                callback.onFailure(skyflowError)
                                return false
                            }
                            arrayInRequestBody.put(k,value)
                        }
                        value = arrayInRequestBody
                        arrayInRequestBody = JSONArray()
                    }
                    else if (records.get(keys.getString(j)) is String || records.get(keys.getString(
                            j)) is Number || records.get(keys.getString(j)) is Boolean
                    )
                        value = records.get(keys.getString(j)).toString()
                    else {
                        val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_BODY, tag,
                            logLevel, arrayOf(keys.getString(j)))
                        callback.onFailure(skyflowError)
                        return false
                    }
                    records.put(keys.getString(j), value)
                } }
            return true
        }

        //adding path params to connection url
        fun addPathParamsToURL(
            url: String,
            params: JSONObject,
            callback: Callback,
            logLevel: LogLevel,
        ) : String
        {
            try {
                var newURL = url
                val keys = params.names()
                if(keys !=null) {
                    for (j in 0 until keys.length()) {
                        if(keys.getString(j).isEmpty())
                        {
                            callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_PATH_PARAMS,
                                tag, logLevel)) //empty key
                            return ""
                        }
                        var value = params.get(keys.getString(j))
                        if (value is Element) {
                            val element = value
                            if(!checkIfElementsMounted(element))
                            {
                                val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                                    tag, logLevel, arrayOf(keys.getString(j)))
                                callback.onFailure(constructError(error))
                                return ""
                            }
                            val check = checkElement(element, callback, logLevel)
                            if (check)
                                newURL = newURL.replace("{" + keys.getString(j) + "}",
                                    element.getValue())
                            else
                                return ""
                        } else if (value is Label) {
                            if(!checkIfElementsMounted(value))
                            {
                                val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                                    tag, logLevel, arrayOf(keys.getString(j)))
                                callback.onFailure(constructError(error))
                                return ""
                            }
                            else if (value.isTokenNull) {
                                val error = SkyflowError(SkyflowErrorCode.MISSING_TOKEN,
                                    tag, logLevel)
                                callback.onFailure(constructError(error))
                                return ""
                            }  else if (value.revealInput.token!!.isEmpty()) {
                                val error = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID,
                                    tag, logLevel)
                                callback.onFailure(constructError(error))
                                return ""
                            }
                            else
                                value = getValueForLabel(value)
                            newURL = newURL.replace("{" + keys.getString(j) + "}", value)
                        } else if (value is String || value is Number || value is Boolean) {
                            value = value.toString()
                            newURL = newURL.replace("{" + keys.getString(j) + "}", value)
                        } else {
                            val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_PATH_PARAMS,
                                tag, logLevel, arrayOf(keys.getString(j)))
                            callback.onFailure(constructError(skyflowError))
                            return ""
                        } } }
                return newURL
            }
            catch (e:Exception)
            {
                return ""
            } }

        //adding query params for connection url
        fun addQueryParams(
            queryMap: HashMap<String,String>,
            connectionConfig: ConnectionConfig,
            callback: Callback,
            logLevel: LogLevel,
        ): Boolean {
            val queryParams = (connectionConfig.queryParams).names()
            if(queryParams != null) {
                for (i in 0 until queryParams.length()) {
                    if(queryParams.getString(i).isEmpty())
                    {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_QUERY_PARAMS))//empty key
                        return false
                    }
                    val value = connectionConfig.queryParams.get(queryParams.getString(i))
                    if(value is Array<*>)
                    {
                        for(j in 0 until value.size)
                        {
                            val isValid = helperForQueryParams(value[j],queryMap,
                                queryParams.getString(i),callback, logLevel)
                            if(!isValid)
                                return false
                        } }
                    else {
                        val isValid = helperForQueryParams(value,queryMap,
                            queryParams.getString(i),callback, logLevel)
                        if(!isValid)
                            return false
                    } } }
            return true
        }

        private fun helperForQueryParams(
            value: Any?,
            queryMap: HashMap<String,String>,
            key: String,
            callback: Callback,
            logLevel: LogLevel,
        ): Boolean
        {

            if (value is Element)
            {
                if(!checkIfElementsMounted(value))
                {
                    val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag,
                        logLevel, arrayOf(key))
                    callback.onFailure(constructError(error))
                    return false
                }
                val isValid = checkElement(value, callback, logLevel)
                if (isValid)
                    queryMap.put(key, value.getValue())
                else
                    return false
            }
            else if (value is Label)
            {
                if(!checkIfElementsMounted(value))
                {
                    val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag,
                        logLevel, arrayOf(key))
                    callback.onFailure(constructError(error))
                    return false
                }
                else if (value.isTokenNull) {
                    val error = SkyflowError(SkyflowErrorCode.MISSING_TOKEN,
                        tag, logLevel)
                    callback.onFailure(constructError(error))
                    return false
                }  else if (value.revealInput.token!!.isEmpty()) {
                    val error = SkyflowError(SkyflowErrorCode.EMPTY_TOKEN_ID,
                        tag, logLevel)
                    callback.onFailure(constructError(error))
                    return false
                }
                queryMap.put(key, getValueForLabel(value))
            }
            else if (value is Number || value is String || value is Boolean || value is JSONObject)
                queryMap.put(key,value.toString())
            else {
                //callback.onFailure(Exception("invalid field \"${key}\" present in queryParams"))
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_QUERY_PARAMS,
                    tag, logLevel, arrayOf(key))
                callback.onFailure(constructError(skyflowError))
                return false
            }
            return true
        }


        //adding requestHeader for connection url
        fun addRequestHeader(
            headerMap: HashMap<String,String>, connectionConfig: ConnectionConfig,
            callback: Callback, logLevel: LogLevel,
        ): Boolean {
            val headers = (connectionConfig.requestHeader as JSONObject).names()
            if (headers != null) {
                for(i in 0 until headers.length())
                {

                    if(headers.getString(i).isEmpty())
                    {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_REQUEST_HEADER_PARAMS)) //empty key
                        return false
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
                        callback.onFailure(constructError(skyflowError))
                        return false
                    } } }
            return true
        }

        //for collect element
        fun constructBatchRequestBody(
            records: JSONObject,
            options: InsertOptions,
            callback: Callback,
            logLevel: LogLevel,
        ) : JSONObject{
            val postPayload:MutableList<Any> = mutableListOf()
            val insertTokenPayload:MutableList<Any> = mutableListOf()
            if(records == {}){
                callback.onFailure(SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND, tag, logLevel))
            }
            else if (!records.has("records")) {
                callback.onFailure(SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND, tag, logLevel))
            }
            else if(records.get("records").toString().isEmpty())
            {
                callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_RECORDS, tag, logLevel))
            }
            else if(records.get("records") !is JSONArray)
            {
                callback.onFailure(SkyflowError(SkyflowErrorCode.INVALID_RECORDS, tag, logLevel))
            }
            else {
                val obj1 = records.getJSONArray("records")
                var i = 0
                while (i < obj1.length()) {
                    val jsonObj = obj1.getJSONObject(i)
                    if(!jsonObj.has("table"))
                    {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.MISSING_TABLE_KEY, tag, logLevel))
                        return JSONObject()
                    }
                    else if(jsonObj.get("table") !is String)
                    {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME, tag, logLevel))
                        return JSONObject()
                    }
                    else if (jsonObj.get("table").toString().isEmpty()) {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_TABLE_KEY, tag, logLevel))
                        return JSONObject()
                    }
                    else if(!jsonObj.has("fields"))
                    {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.FIELDS_KEY_ERROR, tag, logLevel))
                        return JSONObject()
                    }
                    else if(jsonObj.getJSONObject("fields").toString().equals("{}"))
                    {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_FIELDS, tag, logLevel))
                        return JSONObject()
                    }

                    val map = HashMap<String, Any>()
                    map["tableName"] = jsonObj["table"]
                    map["fields"] = jsonObj["fields"]
                    map["method"] = "POST"
                    map["quorum"] = true
                    val jsonObject = jsonObj["fields"] as JSONObject
                    val keys: Iterator<String> = jsonObject.keys()

                    while (keys.hasNext()) {
                        val key = keys.next()
                        if (key.isEmpty()) {
                            callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_COLUMN_KEY, tag, logLevel))
                            return JSONObject()
                        }
                    }
                    postPayload.add(map)
                    if (options.tokens) {
                        val temp2 = HashMap<String, Any>()
                        temp2["method"] = "GET"
                        temp2["tableName"] = jsonObj["table"] as String
                        temp2["ID"] = "\$responses.$i.records.0.skyflow_id"
                        temp2["tokenization"] = true
                        insertTokenPayload.add(temp2)
                    }
                    i++
                }
                val body = HashMap<String, Any>()
                body["records"] = postPayload + insertTokenPayload
                return JSONObject(body as Map<*, *>)
            }
            return JSONObject()
        }

        //check whether pci element is valid or not inside requestbody of connectionConfig
        fun checkElement(element: Element, callback: Callback, logLevel: LogLevel): Boolean
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
                callback.onFailure(constructError(error))
                return false
            }
            return true
        }

        //checking invalid fields in response body of connectionConfig
        fun checkInvalidFields(
            responseBody: JSONObject,
            responseFromConnection: JSONObject,
            callback: Callback,
        ): Boolean {
            try {
                val keys = responseBody.names()
                if(keys !=null) {
                    for (j in 0 until keys.length()) {
                        if (responseBody.get(keys.getString(j)) is JSONObject) {
                            val check =
                                checkInvalidFields(responseBody.get(keys.getString(j)) as JSONObject,
                                    responseFromConnection.getJSONObject(keys.getString(j)),
                                    callback)
                            if (!check)
                                return false
                        } else if (responseBody.get(keys.getString(j)) !is Element && responseBody.get(
                                keys.getString(j)) !is Label
                        )
                            throw Exception("invalid field " + keys.getString(j) + " present in response body")
                        else if(responseBody.get(keys.getString(j)) is Element)
                        {
                            val element = (responseBody.get(keys.getString(j))) as Element
                            if(!checkIfElementsMounted(element))
                            {
                                throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(keys.getString(j)))
                            }
                        }
                        else if(responseBody.get(keys.getString(j)) is Label)
                        {
                            val element = (responseBody.get(keys.getString(j))) as Label
                            if(!checkIfElementsMounted(element))
                            {
                                val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(keys.getString(j)))
                                throw error
                            } } } } }
            catch (e: Exception) {
                callback.onFailure(constructError(e))
                return false
            }
            return true
        }

        //removing empty json objects
        fun removeEmptyAndNullFields(response: JSONObject) {
            val keys = response.names()
            if(keys !=null) {
                for (j in 0 until keys.length()) {
                    val key = keys.getString(j)
                    try {
                        if (response.isNull(key) || response.getJSONObject(key).toString() == "{}"
                        ) {
                            response.remove(key);
                        } else {
                            removeEmptyAndNullFields(response.getJSONObject(key));
                            if (response.getJSONObject(key).toString() == "{}")
                                response.remove(key)
                        }
                    } catch (e: Exception) {
                    }
                } } }

        // checking duplicate fields present in responseBody of connectionConfig
        fun  checkDuplicateInResponseBody(
            responseBody: JSONObject,
            callback: Callback, elementList: HashSet<String>, logLevel: LogLevel,
        ) : Boolean
        {
            val keys = responseBody.names()
            if(keys != null) {
                for (j in 0 until keys.length()) {

                    try {
                        if(responseBody.get(keys.getString(j)) is Element)
                        {
                            val element = (responseBody.get(keys.getString(j))) as Element
                            if(!checkIfElementsMounted(element))
                            {
                                val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,tag, logLevel,
                                    arrayOf(keys.getString(j)))
                                throw error
                            }
                        }
                        else if(responseBody.get(keys.getString(j)) is Label)
                        {
                            val element = (responseBody.get(keys.getString(j))) as Label
                            if(!checkIfElementsMounted(element))
                            {
                                val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,tag, logLevel,
                                    arrayOf(keys.getString(j)))
                                throw error
                            }
                        }
                        else if (responseBody.get(keys.getString(j)) is JSONObject) {
                            val check =
                                checkDuplicateInResponseBody(responseBody.get(keys.getString(j)) as JSONObject,
                                    callback,
                                    elementList,
                                    logLevel)
                            if (!check)
                                return false
                        } else if (responseBody.get(keys.getString(j)) !is Element && !(responseBody.get(
                                keys.getString(j)) is Label))
                            throw Exception("invalid field " + keys.getString(j) + " present in response body")
                        if (responseBody.get(keys.getString(j)) is Element || responseBody.get(keys.getString(
                                j)) is Label
                        ) {
                            if (elementList.contains(responseBody.get(keys.getString(j)).hashCode()
                                    .toString()))
                                throw SkyflowError(SkyflowErrorCode.DUPLICATE_ELEMENT_FOUND, tag,logLevel)
                            // throw SkyflowError("duplicate field " + keys.getString(j) + " present in response body")
                            else
                                elementList.add(responseBody.get(keys.getString(j)).hashCode()
                                    .toString())
                        }
                    } catch (e: Exception) {
                        callback.onFailure(constructError(e))
                        return false
                    }
                }
            }
            return true
        }

        fun checkIfElementsMounted(element:Label):Boolean{
            if (!element.isAttachedToWindow())
                return false
            return true
        }

        fun checkIfElementsMounted(element:Element):Boolean{
            if (!element.isAttachedToWindow())
                return false
            return true
        }

        fun copyJSON(records: JSONObject,finalRecords:JSONObject)
        {
            val keys = records.names()
            if(keys != null) {
                for (j in 0 until keys.length()) {
                    finalRecords.put(keys.getString(j),records.get(keys.getString(j)))
                }
            } }

        fun constructMessage(message:String, vararg values : String?): String{
            return String.format(message, *values)
        }

        fun constructError(e:Exception,code:Int=400) : JSONObject
        {
            val skyflowError = SkyflowError(params = arrayOf(e.message))
            skyflowError.setErrorCode(code)
            val finalError = JSONObject()
            val errors = JSONArray()
            val error = JSONObject()
            error.put("error",skyflowError)
            errors.put(error)
            finalError.put("errors",errors)
            return finalError
        }


        fun findMatches(regex:String,text:String) : MutableList<String> {
            val allMatches: MutableList<String> = ArrayList()
            val m: Matcher = Pattern.compile(regex)
                .matcher(text)
            while (m.find()) {
                allMatches.add(m.group())
            }
            return allMatches
        }
        internal var labelWithRegexMap = HashMap<String,String?>() //key token, value is null before detokenize,value is value from api after detokenize
        internal var tokenLabelMap = HashMap<String,Label>() // key is token,value is label
        fun getValueForLabel(label : Label) : String {
            val formatRegex = label.options.formatRegex
            val value : String? = label.actualValue
            if(formatRegex.isNotEmpty() && value == null){
                labelWithRegexMap.put(label.getToken(),null)
                tokenLabelMap.put(label.getToken(),label)
                return label.getToken()
            }
            else if(value!= null && formatRegex.isNotEmpty()) {
                val regex = Regex(formatRegex)
                val matches =  regex.find(value)
                return if(matches != null) matches.value else ""
            }
            return label.getValueForConnections()
        }
    }



}