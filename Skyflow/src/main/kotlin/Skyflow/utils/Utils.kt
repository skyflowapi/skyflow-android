package Skyflow.utils

import Skyflow.*
import android.webkit.URLUtil
import okhttp3.HttpUrl
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import kotlin.Exception

class Utils {

    companion object {
        fun checkUrl(url: String): Boolean {
            if (!URLUtil.isValidUrl(url) || !URLUtil.isHttpsUrl(url))
                return false
            return true
        }

        //response for invokegateway
        fun constructResponseBodyFromGateway(
            responseBody: JSONObject,
            responseFromGateway: JSONObject,
            callback: Callback
        ) : JSONObject
        {
            try {
                val isValid = checkInvalidFields(responseBody,responseFromGateway,callback)
                if(isValid)
                {
                    constructJsonKeyForGatewayResponse(responseBody,responseFromGateway,callback)
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
        fun constructJsonKeyForGatewayResponse(
            responseBody: JSONObject,
            responseFromGateway: JSONObject,
            callback: Callback
        )
        {
            val keys = responseBody.names()
            if(keys != null) {
                for (j in 0 until keys.length()) {
                    if (responseFromGateway.has(keys.getString(j))) {
                        if (responseBody.get(keys.getString(j)) is Element) {
                            val ans = responseFromGateway.getString(keys.getString(j))
                            (responseBody.get(keys.getString(j)) as TextField).inputField.setText(
                                ans)
                            responseFromGateway.remove(keys.getString(j))
                        } else if (responseBody.get(keys.getString(j)) is Label) {
                            val ans = responseFromGateway.getString(keys.getString(j))
                            (responseBody.get(keys.getString(j)) as Label).placeholder.setText(ans)
                            responseFromGateway.remove(keys.getString(j))
                        } else if (responseBody.get(keys.getString(j)) is JSONObject) {
                            constructJsonKeyForGatewayResponse(responseBody.get(keys.getString(j)) as JSONObject,
                                responseFromGateway.getJSONObject(keys.getString(j)),
                                callback)

                        }
                    }
                }
            }
        }

        //requestbody for invokegateway
        fun constructRequestBodyForGateway(records: JSONObject, callback: Callback) : Boolean
        {
            try {
                val isValid = constructJsonKeyForGatewayRequest(records,callback)
                if(isValid)
                    return true
                else
                    return false

            }
            catch (e:Exception)
            {
                return false
            }

        }

        var arrayInRequestBody : JSONArray = JSONArray()
        //changing pci elements to actual values in it for request to gateway
        private fun constructJsonKeyForGatewayRequest(records: JSONObject, callback: Callback) : Boolean {
            val keys = records.names()
            if(keys !=null) {
                for (j in 0 until keys.length()) {
                    var value: Any
                    if (records.get(keys.getString(j)) is Element) {
                        val element = (records.get(keys.getString(j)) as Element)
                        val check = checkElement(element, callback)
                        if (check)
                            value = (records.get(keys.getString(j)) as Element).getOutput()
                        else
                            return false
                    } else if (records.get(keys.getString(j)) is Label) {
                        value = (records.get(keys.getString(j)) as Label).revealInput.token
                    } else if (records.get(keys.getString(j)) is JSONObject) {
                        val isValid =
                            constructJsonKeyForGatewayRequest(records.get(keys.getString(j)) as JSONObject,
                                callback)
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
                                val check = checkElement(element, callback)
                                if (check)
                                    value = (arrayValue.get(k)  as Element).getOutput()
                                else
                                    return false
                            }
                            else if(arrayValue.get(k) is Label)
                            {
                                value = (arrayValue.get(k)  as Label).revealInput.token
                            }
                            else if(arrayValue.get(k) is JSONObject)
                            {
                                val isValid =
                                    constructJsonKeyForGatewayRequest(arrayValue.get(k)  as JSONObject,
                                        callback)
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
                                callback.onFailure(Exception("invalid field \"${keys.getString(j)}\" present in requestbody"))
                                return false
                            }
                            arrayValue.put(k,value)
                        }
                        value = arrayValue
                    }
                    else if(records.get(keys.getString(j)) is Array<*>)
                    {
                            val arrayValue =(records.get(keys.getString(j)) as Array<*>)
                        for(k in 0 until arrayValue.size)
                            {
                                if(arrayValue.get(k) is Element)
                                {
                                    val element = (arrayValue.get(k)  as Element)
                                    val check = checkElement(element, callback)
                                    if (check)
                                    {
                                        value = (arrayValue.get(k)  as Element).getOutput()
                                    }
                                    else
                                        return false
                                }
                                else if(arrayValue.get(k) is Label)
                                {
                                    value = (arrayValue.get(k)  as Label).revealInput.token
                                }
                                else if(arrayValue.get(k) is JSONObject)
                                {
                                    val isValid =
                                        constructJsonKeyForGatewayRequest(arrayValue.get(k)  as JSONObject,
                                            callback)
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
                                    callback.onFailure(Exception("invalid field \"${keys.getString(j)}\" present in requestbody"))
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
                        callback.onFailure(Exception("invalid field \"${keys.getString(j)}\" present in requestbody"))
                        return false
                    }
                    records.put(keys.getString(j), value)
                }
            }
            return true
        }

        //adding path params to gatewaye url
        fun addPathparamsToURL(url: String, params: JSONObject, callback: Callback) : String
        {
            try {
                var newURL = url
                val keys = params.names()
                if(keys !=null) {
                    for (j in 0 until keys.length()) {
                        var value = params.get(keys.getString(j))
                        if (value is Element) {
                            val element = value
                            val check = checkElement(element, callback)
                            if (check)
                                newURL = newURL.replace("{" + keys.getString(j) + "}",
                                    element.getOutput())
                            else
                                return ""
                        } else if (value is Label) {
                            value = value.revealInput.token
                            newURL = newURL.replace("{" + keys.getString(j) + "}", value)
                        } else if (value is String || value is Number || value is Boolean) {
                            value = value.toString()
                            newURL = newURL.replace("{" + keys.getString(j) + "}", value)
                        } else {
                            callback.onFailure(Exception("invalid field \"${keys.getString(j)}\" present in pathparams"))
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
            callback: Callback
        ): Boolean {
            val queryParams = (gatewayConfig.queryParams).names()
            if(queryParams != null) {
                for (i in 0 until queryParams.length()) {
                    val value = gatewayConfig.queryParams.get(queryParams.getString(i))
                    if(value is Array<*>)
                    {
                        for(j in 0 until value.size)
                        {
                            val isValid = helperForQueryParams(value.get(j),requestUrlBuilder,queryParams.getString(i),callback)
                            if(!isValid)
                                return false
                        }
                    }
                    else {
                        val isValid = helperForQueryParams(value,requestUrlBuilder,queryParams.getString(i),callback)
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
            callback: Callback
        ): Boolean
        {

            if (value is Element)
            {
                val element = value
                val isValid = checkElement(element, callback)
                if (isValid)
                    requestUrlBuilder.addQueryParameter(key,element.getOutput())
                else
                    return false
            }
            else if (value is Label)
                requestUrlBuilder.addQueryParameter(key, value.revealInput.token)
            else if (value is Number || value is String || value is Boolean || value is JSONObject)
                requestUrlBuilder.addQueryParameter(key,value.toString())
            else {
                callback.onFailure(Exception("invalid field \"${key}\" present in queryParams"))
                return false
            }
            return true
        }


        //adding requestHeader for gateway url
        fun addRequestHeader(request: Request.Builder, gatewayConfig: GatewayConfiguration, callback: Callback): Boolean {
            val headers = (gatewayConfig.requestHeader as JSONObject).names()
            if (headers != null) {
                for(i in 0 until headers.length())
                {
                    if(gatewayConfig.requestHeader.get(headers.getString(i)) is String || gatewayConfig.requestHeader.get(headers.getString(i)) is Number
                        || gatewayConfig.requestHeader.get(headers.getString(i)) is Boolean)
                        request.addHeader(headers.getString(i),gatewayConfig.requestHeader.getString(headers.getString(i)))
                    else
                    {
                        callback.onFailure(Exception("invalid field \"${headers.getString(i)}\" present in requestHeader"))
                        return false
                    }
                }
            }
            return true
        }

        //for collect element
        fun constructBatchRequestBody(records:  JSONObject, options: InsertOptions) : JSONObject{
            val postPayload:MutableList<Any> = mutableListOf()
            val insertTokenPayload:MutableList<Any> = mutableListOf()
            val obj1 = records.getJSONArray("records")
            var i = 0
            while ( i < obj1.length())
            {
                val jsonObj = obj1.getJSONObject(i)
                val map = HashMap<String,Any>()
                map["tableName"] = jsonObj["table"]
                map["fields"] = jsonObj["fields"]
                map["method"] = "POST"
                map["quorum"] = true
                postPayload.add(map)
                if(options.tokens)
                {
                    val temp2 = HashMap<String,Any>()
                    temp2["method"] = "GET"
                    temp2["tableName"] = jsonObj["table"] as String
                    temp2["ID"] = "\$responses.$i.records.0.skyflow_id"
                    temp2["tokenization"] = true
                    insertTokenPayload.add(temp2)
                }
                i++
            }
            val body = HashMap<String,Any>()
            body["records"] = postPayload + insertTokenPayload
            return JSONObject(body as Map<*, *>)
        }

        //check whether pci element is valid or not inside requestbody of gatewayconfig
        private fun checkElement(element: Element, callback: Callback): Boolean
        {
            val state = element.getState()
            var error = ""
            if ((state["isRequired"] as Boolean) && (state["isEmpty"] as Boolean)) {
                error = element.columnName + " is empty" + "\n"
            }
            if (!(state["isValid"] as Boolean)) {
                error = "for " + element.columnName + " " + (state["validationErrors"] as String) + "\n"
            }
            if (error != "") {
                callback.onFailure(Exception(error))
                return false
            }
            return true
        }

        //checking invalidfields in response body of gatewayconfig
        private fun checkInvalidFields(
            responseBody: JSONObject,
            responseFromGateway: JSONObject,
            callback: Callback,
        ): Boolean {
            val keys = responseBody.names()
            for (j in 0 until keys!!.length()) {
                try
                {
                    if (responseBody.get(keys.getString(j)) is JSONObject) {
                        val check = checkInvalidFields(responseBody.get(keys.getString(j)) as JSONObject,
                            responseFromGateway.getJSONObject(keys.getString(j)),
                            callback)
                        if(!check)
                            return false
                    }
                    else if(!(responseBody.get(keys.getString(j)) is Element) && !(responseBody.get(keys.getString(j)) is Label))
                        throw Exception("invalid field "+keys.getString(j)+" present in response body")
                }
                catch (e:Exception)
                {
                    callback.onFailure(e)
                    return false
                }
            }
            return true
        }

        //removing empty json objects
        private fun removeEmptyAndNullFields(response: JSONObject) {
            val keys = response.names()
            for (j in 0 until keys!!.length()) {
                val key = keys.getString(j)
                try {
                    if (response.isNull(key) || response.getJSONObject(key).toString().equals("{}")) {
                        response.remove(key);
                    } else {
                        removeEmptyAndNullFields(response.getJSONObject(key));
                        if(response.getJSONObject(key).toString().equals("{}"))
                            response.remove(key)
                    }
                } catch (e: Exception) {
                }
            }
        }

        // checking duplicate fields present in responseBody of gatewayconfig
        fun  checkDuplicateInResponseBody(responseBody: JSONObject,
                                          callback: Callback,elementList:HashSet<String>) : Boolean
        {
            val keys = responseBody.names()
            for (j in 0 until keys!!.length()) {
                try {
                    if (responseBody.get(keys.getString(j)) is JSONObject) {
                        val check = checkDuplicateInResponseBody(responseBody.get(keys.getString(j)) as JSONObject,callback,elementList)
                        if(!check)
                            return false
                    }
                    if (responseBody.get(keys.getString(j)) is Element || responseBody.get(keys.getString(j)) is Label)
                    {
                        if (elementList.contains(responseBody.get(keys.getString(j)).hashCode().toString()))
                            throw Exception("duplicate field " + keys.getString(j) + " present in response body")
                        else
                            elementList.add(responseBody.get(keys.getString(j)).hashCode().toString())
                    }
                }
                catch (e: Exception)
                {
                    callback.onFailure(e)
                    return false
                }
            }
            return true
        }

    }
}