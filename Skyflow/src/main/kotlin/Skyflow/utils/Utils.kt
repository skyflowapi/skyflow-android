package Skyflow.utils

import Skyflow.*
import Skyflow.core.LogLevel
import android.util.Log
import android.webkit.URLUtil
import okhttp3.HttpUrl
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import kotlin.Exception
import kotlin.math.log

class Utils {

    companion object {
        val tag = Utils::class.qualifiedName
        fun checkUrl(url: String): Boolean {
            if (!URLUtil.isValidUrl(url) || !URLUtil.isHttpsUrl(url)) {
                //Logger.error(logTag, Messages.INVALID_URL.getMessage(url), logLevel)
                return false
            }
            return true
        }

        //response for invokegateway
        fun constructResponseBodyFromGateway(
            responseBody: JSONObject,
            responseFromGateway: JSONObject,
            callback: Callback,
            logLevel: LogLevel
        ) : JSONObject
        {
            try {
                val isValid = checkInvalidFields(responseBody,responseFromGateway,callback)
                if(isValid)
                {
                    constructJsonKeyForGatewayResponse(responseBody,responseFromGateway,callback, logLevel)
                    removeEmptyAndNullFields(responseFromGateway)
                    return responseFromGateway
                }
                else
                    return JSONObject()
            }
            catch (e:Exception)
            {
                return JSONObject()
            }
        }


        //displaying data to pci elements and removing pci element values from response
        var errors = JSONArray()
        var gatewayResponse = JSONObject()
        fun constructJsonKeyForGatewayResponse(
            responseBody: JSONObject,
            responseFromGateway: JSONObject,
            callback: Callback,
            logLevel: LogLevel
        ) : JSONObject
        {
            val keys = responseBody.names()
            if(keys != null) {
                for (j in 0 until keys.length()) {
                    try {


                            if (responseBody.get(keys.getString(j)) is Element) {
                                val ans = responseFromGateway.getString(keys.getString(j))
                                (responseBody.get(keys.getString(j)) as TextField).inputField.setText(
                                    ans)
                                responseFromGateway.remove(keys.getString(j))
                            } else if (responseBody.get(keys.getString(j)) is Label) {
                                val ans = responseFromGateway.getString(keys.getString(j))
                                (responseBody.get(keys.getString(j)) as Label).placeholder.setText(
                                    ans)
                                (responseBody.get(keys.getString(j)) as Label).actualValue = ans
                                responseFromGateway.remove(keys.getString(j))
                            } else if (responseBody.get(keys.getString(j)) is JSONObject) {
                                constructJsonKeyForGatewayResponse(responseBody.get(keys.getString(j)) as JSONObject,
                                    responseFromGateway.getJSONObject(keys.getString(j)),
                                    callback,logLevel)

                            }

                    }
                    catch (e:Exception)
                    {

                        val error = SkyflowError(SkyflowErrorCode.NOT_FOUND_IN_RESPONSE, tag, logLevel, arrayOf(keys.getString(j)))
                        val finalError = JSONObject()
                        finalError.put("error",error)
                        if(!gatewayResponse.has("errors"))
                            gatewayResponse.put("errors",JSONArray())
                        gatewayResponse.getJSONArray("errors").put(finalError)
                        responseFromGateway.remove(keys.getString(j))
                    }
                }
            }
            if(!gatewayResponse.has("success"))
                gatewayResponse.put("success",JSONArray())
            gatewayResponse.getJSONArray("success").put(0,responseFromGateway)
            gatewayResponse.getJSONArray("success").remove(1)
            return gatewayResponse
        }

        //requestbody for invokegateway
        fun constructRequestBodyForGateway(records: JSONObject, callback: Callback, logLevel: LogLevel) : Boolean
        {
            return try {
                constructJsonKeyForGatewayRequest(records, callback, logLevel)
            } catch (e: Exception) {
                false
            }

        }

        var arrayInRequestBody : JSONArray = JSONArray()
        //changing pci elements to actual values in it for request to gateway
        private fun constructJsonKeyForGatewayRequest(records: JSONObject, callback: Callback, logLevel: LogLevel) : Boolean {
            val keys = records.names()
            if(keys !=null) {
                for (j in 0 until keys.length()) {
                    if(keys.getString(j).isEmpty())
                    {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_COLUMN_NAME, tag, logLevel))
                        return false
                    }
                    var value: Any
                    if (records.get(keys.getString(j)) is Element) {
                        val element = (records.get(keys.getString(j)) as Element)
                        if(!checkIfElementsMounted(element))
                        {
                            val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf(element.collectInput.column))
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
                                tag, logLevel, arrayOf((records.get(keys.getString(j)) as Label).revealInput.label))
                            callback.onFailure(constructError(error))
                            return false
                        }
                        value = (records.get(keys.getString(j)) as Label).revealInput.token!!
                    } else if (records.get(keys.getString(j)) is JSONObject) {
                        val isValid =
                            constructJsonKeyForGatewayRequest(records.get(keys.getString(j)) as JSONObject,
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
                                        tag, logLevel, arrayOf(element.collectInput.column))
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
                                    val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, logLevel, arrayOf((arrayValue.get(k) as Label).revealInput.label))
                                    callback.onFailure(constructError(error))
                                    return false
                                }
                                value = (arrayValue.get(k)  as Label).revealInput.token!!
                            }
                            else if(arrayValue.get(k) is JSONObject)
                            {
                                val isValid =
                                    constructJsonKeyForGatewayRequest(arrayValue.get(k)  as JSONObject,
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
                                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD, tag,
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
                                            tag, logLevel, arrayOf(element.collectInput.column))
                                        callback.onFailure(constructError(error))
                                        return false
                                    }
                                    val check = checkElement(element, callback, logLevel)
                                    if (check)
                                    {
                                        value = (arrayValue.get(k)  as Element).getValue()
                                    }
                                    else
                                        return false
                                }
                                else if(arrayValue.get(k) is Label)
                                {
                                    if(!checkIfElementsMounted(arrayValue.get(k) as Label))
                                    {
                                        val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
                                            tag, logLevel, arrayOf((arrayValue[k] as Label).revealInput.label))
                                        callback.onFailure(constructError(error))
                                        return false
                                    }
                                    else
                                    value = (arrayValue.get(k)  as Label).revealInput.token!!
                                }
                                else if(arrayValue[k] is JSONObject)
                                {
                                    val isValid =
                                        constructJsonKeyForGatewayRequest(arrayValue.get(k)  as JSONObject,
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
                                    val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD,
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
                        val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD, tag,
                            logLevel, arrayOf(keys.getString(j)))
                        callback.onFailure(skyflowError)
                        return false
                    }
                    records.put(keys.getString(j), value)
                }
            }
            return true
        }

        //adding path params to gatewaye url
        fun addPathparamsToURL(
            url: String,
            params: JSONObject,
            callback: Callback,
            logLevel: LogLevel
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
                                    tag, logLevel, arrayOf(element.collectInput.column))
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
                                    tag, logLevel, arrayOf(value.revealInput.label))
                                callback.onFailure(constructError(error))
                                return ""
                            }
                            else
                            value = value.revealInput.token!!
                            newURL = newURL.replace("{" + keys.getString(j) + "}", value)
                        } else if (value is String || value is Number || value is Boolean) {
                            value = value.toString()
                            newURL = newURL.replace("{" + keys.getString(j) + "}", value)
                        } else {
                            val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_PATH_PARAMS,
                                tag, logLevel, arrayOf(keys.getString(j)))
                            callback.onFailure(constructError(skyflowError))
                            return ""
                        }
                    }
                }
                return newURL
            }
            catch (e:Exception)
            {
                return ""
            }
        }

        //adding query params for gateway url
        fun addQueryParams(
            requestUrlBuilder: HttpUrl.Builder,
            gatewayConfig: GatewayConfiguration,
            callback: Callback,
            logLevel: LogLevel
        ): Boolean {
            val queryParams = (gatewayConfig.queryParams).names()
            if(queryParams != null) {
                for (i in 0 until queryParams.length()) {
                    if(queryParams.getString(i).isEmpty())
                    {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_QUERY_PARAMS))//empty key
                        return false
                    }
                    val value = gatewayConfig.queryParams.get(queryParams.getString(i))
                    if(value is Array<*>)
                    {
                        for(j in 0 until value.size)
                        {
                            val isValid = helperForQueryParams(value[j],requestUrlBuilder,
                                queryParams.getString(i),callback, logLevel)
                            if(!isValid)
                                return false
                        }
                    }
                    else {
                        val isValid = helperForQueryParams(value,requestUrlBuilder,
                            queryParams.getString(i),callback, logLevel)
                        if(!isValid)
                            return false
                    }
                }
            }
            return true

        }

        private fun helperForQueryParams(
            value: Any?,
            requestUrlBuilder: HttpUrl.Builder,
            key: String,
            callback: Callback,
            logLevel: LogLevel
        ): Boolean
        {

            if (value is Element)
            {
                val element = value
                if(!checkIfElementsMounted(element))
                {
                    val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag,
                        logLevel, arrayOf(element.collectInput.column))
                    callback.onFailure(constructError(error))
                    return false
                }
                val isValid = checkElement(element, callback, logLevel)
                if (isValid)
                    requestUrlBuilder.addQueryParameter(key,element.getValue())
                else
                    return false
            }
            else if (value is Label)
            {
                if(!checkIfElementsMounted(value))
                {
                    val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag,
                        logLevel, arrayOf(value.revealInput.label))
                    callback.onFailure(constructError(error))
                    return false
                }
                requestUrlBuilder.addQueryParameter(key, value.revealInput.token!!)
            }
            else if (value is Number || value is String || value is Boolean || value is JSONObject)
                requestUrlBuilder.addQueryParameter(key,value.toString())
            else {
                //callback.onFailure(Exception("invalid field \"${key}\" present in queryParams"))
                val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_QUERY_PARAMS,
                    tag, logLevel, arrayOf(key))
                callback.onFailure(constructError(skyflowError))
                return false
            }
            return true
        }


        //adding requestHeader for gateway url
        fun addRequestHeader(request: Request.Builder, gatewayConfig: GatewayConfiguration,
                             callback: Callback, logLevel: LogLevel): Boolean {
            val headers = (gatewayConfig.requestHeader as JSONObject).names()
            if (headers != null) {
                for(i in 0 until headers.length())
                {
                    if(headers.getString(i).isEmpty())
                    {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_KEY_IN_REQUEST_HEADER_PARAMS)) //empty key
                        return false
                    }
                    if(gatewayConfig.requestHeader.get(headers.getString(i)) is String || gatewayConfig.requestHeader.get(headers.getString(i)) is Number
                        || gatewayConfig.requestHeader.get(headers.getString(i)) is Boolean)
                        request.addHeader(headers.getString(i),gatewayConfig.requestHeader.getString(headers.getString(i)))
                    else
                    {
                        //callback.onFailure(Exception("invalid field \"${headers.getString(i)}\" present in requestHeader"))
                        val skyflowError = SkyflowError(SkyflowErrorCode.INVALID_FIELD_IN_REQUEST_HEADER_PARAMS, tag, logLevel, arrayOf(headers.getString(i)))
                        callback.onFailure(constructError(skyflowError))
                        return false
                    }
                }
            }
            return true
        }

        //for collect element
        fun constructBatchRequestBody(
            records: JSONObject,
            options: InsertOptions,
            callback: Callback,
            logLevel: LogLevel
        ) : JSONObject{
            val postPayload:MutableList<Any> = mutableListOf()
            val insertTokenPayload:MutableList<Any> = mutableListOf()
            if (!records.has("records")) {
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
                        callback.onFailure(SkyflowError(SkyflowErrorCode.MISSING_TABLE, tag, logLevel))
                        return JSONObject()
                    }
                    else if(jsonObj.get("table") !is String)
                    {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME, tag, logLevel))
                        return JSONObject()
                    }
                    else if (jsonObj.get("table").toString().isEmpty()) {
                        callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_TABLE_NAME, tag, logLevel))
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
                            callback.onFailure(SkyflowError(SkyflowErrorCode.EMPTY_COLUMN_NAME, tag, logLevel))
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

        //check whether pci element is valid or not inside requestbody of gatewayconfig
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
                errors = "for " + labelName + " " + (state["validationErrors"] as String) + "\n"
            }
            if (errors != "") {
                val error = SkyflowError(SkyflowErrorCode.INVALID_INPUT, tag, logLevel, arrayOf(errors))
                callback.onFailure(error)
                return false
            }
            return true
        }

        //checking invalidfields in response body of gatewayconfig
         fun checkInvalidFields(
            responseBody: JSONObject,
            responseFromGateway: JSONObject,
            callback: Callback,
        ): Boolean {
            try {
            val keys = responseBody.names()
            if(keys !=null) {
                for (j in 0 until keys.length()) {
                        if (responseBody.get(keys.getString(j)) is JSONObject) {
                            val check =
                                checkInvalidFields(responseBody.get(keys.getString(j)) as JSONObject,
                                    responseFromGateway.getJSONObject(keys.getString(j)),
                                    callback)
                            if (!check)
                                return false
                        } else if (!(responseBody.get(keys.getString(j)) is Element) && !(responseBody.get(
                                keys.getString(j)) is Label))
                            throw Exception("invalid field " + keys.getString(j) + " present in response body")
                        else if(responseBody.get(keys.getString(j)) is Element)
                        {
                            val element = (responseBody.get(keys.getString(j))) as Element
                            if(!checkIfElementsMounted(element))
                            {
                                val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(element.collectInput.column))
                                throw error
                            }
                        }
                        else if(responseBody.get(keys.getString(j)) is Label)
                        {
                            val element = (responseBody.get(keys.getString(j))) as Label
                            if(!checkIfElementsMounted(element))
                            {
                                val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,params = arrayOf(element.revealInput.label))
                                throw error
                            }
                        }
                    }
                }
            }
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
                        if (response.isNull(key) || response.getJSONObject(key).toString()
                                .equals("{}")
                        ) {
                            response.remove(key);
                        } else {
                            removeEmptyAndNullFields(response.getJSONObject(key));
                            if (response.getJSONObject(key).toString().equals("{}"))
                                response.remove(key)
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }

        // checking duplicate fields present in responseBody of gatewayconfig
        fun  checkDuplicateInResponseBody(
            responseBody: JSONObject,
            callback: Callback, elementList: HashSet<String>, logLevel: LogLevel
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
                                    arrayOf(element.collectInput.column))
                                throw error
                            }
                        }
                        else if(responseBody.get(keys.getString(j)) is Label)
                        {
                            val element = (responseBody.get(keys.getString(j))) as Label
                            if(!checkIfElementsMounted(element))
                            {
                                val error = SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED,tag, logLevel,
                                    arrayOf(element.revealInput.label))
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
                        } else if (!(responseBody.get(keys.getString(j)) is Element) && !(responseBody.get(
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
            }
        }

        fun constructMessage(message:String, vararg values : String?): String{
            return String.format(message, *values)
        }

        fun constructError(e:Exception,code:Int=400) : JSONObject
        {
            val skyflowError = SkyflowError()
            skyflowError.setErrorMessage(e.message.toString())
            skyflowError.setErrorCode(code)
            val finalError = JSONObject()
            val errors = JSONArray()
            val error = JSONObject()
            error.put("error",skyflowError)
            errors.put(error)
            finalError.put("errors",errors)
            return finalError
        }

    }


}