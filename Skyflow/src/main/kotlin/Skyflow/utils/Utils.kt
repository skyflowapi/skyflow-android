package Skyflow.utils

import Skyflow.*
import android.webkit.URLUtil
import okhttp3.HttpUrl
import okhttp3.Request
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

        //checking invalidfields in response body of gatewayconfig
        private fun checkInvalidFields(responseBody: JSONObject, responseFromGateway: JSONObject, callback: Callback): Boolean {
            val keys = responseBody.names()
            for (j in 0 until keys!!.length()) {
                try
                {
                    if (responseBody.get(keys.getString(j)) is JSONObject) {
                        val check = checkInvalidFields(responseBody.get(keys.getString(j)) as JSONObject,
                            responseFromGateway.getJSONObject(keys.getString(j)),callback)
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

        //displaying data to pci elements and removing pci element values from response
        fun constructJsonKeyForGatewayResponse(
            responseBody: JSONObject,
            responseFromGateway: JSONObject,
            callback: Callback
        )
        {
            val keys = responseBody.names()
            for (j in 0 until keys!!.length()) {
               if(responseFromGateway.has(keys.getString(j)))
               {
                   if (responseBody.get(keys.getString(j)) is Element)
                   {
                       val ans = responseFromGateway.getString(keys.getString(j))
                       (responseBody.get(keys.getString(j)) as TextField).inputField.setText(ans)
                       responseFromGateway.remove(keys.getString(j))
                   }
                   else if (responseBody.get(keys.getString(j)) is Label)
                   {
                       val ans = responseFromGateway.getString(keys.getString(j))
                       (responseBody.get(keys.getString(j)) as Label).placeholder.setText(ans)
                       responseFromGateway.remove(keys.getString(j))
                   }
                   else if (responseBody.get(keys.getString(j)) is JSONObject)
                   {
                           constructJsonKeyForGatewayResponse(responseBody.get(keys.getString(j)) as JSONObject,
                               responseFromGateway.getJSONObject(keys.getString(j)),
                               callback)

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

        //changing pci elements to actual values in it for request to gateway
        private fun constructJsonKeyForGatewayRequest(records: JSONObject, callback: Callback) : Boolean {
                if(records.toString().equals("{}"))
                    return false
                val keys = records.names()
                for (j in 0 until keys!!.length()) {
                    var value:Any
                    if(records.get(keys.getString(j)) is Element )
                    {
                        val element = (records.get(keys.getString(j)) as Element)
                        val check = checkElement(element,callback)
                        if(check)
                            value = (records.get(keys.getString(j)) as Element).getOutput()
                        else
                            return false
                    }
                    else if(records.get(keys.getString(j)) is Label)
                    {
                        value = (records.get(keys.getString(j)) as Label).revealInput.token
                    }
                    else if(records.get(keys.getString(j)) is JSONObject)
                    {
                        val isValid = constructJsonKeyForGatewayRequest(records.get(keys.getString(j)) as JSONObject,
                            callback)
                        if(isValid)
                            value = JSONObject(records.get(keys.getString(j)).toString())
                        else
                            return false

                    }
                    else
                        value = records.get(keys.getString(j)).toString()
                        records.put(keys.getString(j),value)
                }
            return true
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

        fun addPathparamsToURL(url: String, params: JSONObject, callback: Callback) : String
        {
            try {
                var newURL = url
                val keys = params.names()
                for (j in 0 until keys!!.length()) {
                    var value: Any
                    if (params.get(keys.getString(j)) is Element) {
                        val element = (params.get(keys.getString(j)) as Element)
                        val check = checkElement(element,callback)
                        if(check)
                        {
                            newURL = newURL.replace("{" + keys.getString(j) + "}", element.getOutput())
                        }
                        else
                            return ""
                    } else if (params.get(keys.getString(j)) is Label) {
                        value = (params.get(keys.getString(j)) as Label).revealInput.token
                        newURL = newURL.replace("{" + keys.getString(j) + "}", value)
                    }  else if(params.get(keys.getString(j)) is String ||params.get(keys.getString(j)) is Int) {
                        value = params.get(keys.getString(j)).toString()
                        newURL = newURL.replace("{" + keys.getString(j) + "}", value)
                    }
                    else
                    {
                        callback.onFailure(Exception("invalid field \"${keys.getString(j)}\" present in pathparams"))
                        return ""
                    }
                }
                return newURL
            }
            catch (e:Exception)
            {
                return ""
            }
        }

        fun addQueryParams(
            requestUrlBuilder: HttpUrl.Builder,
            gatewayConfig: GatewayConfiguration,
            callback: Callback
        ): Boolean {
            val queryParams = (gatewayConfig.queryParams as JSONObject).names()
            if(queryParams != null) {
                for (i in 0 until queryParams.length()) {
                    if (gatewayConfig.queryParams.get(queryParams.getString(i)) is Element) {
                        requestUrlBuilder.addQueryParameter(queryParams.getString(i),
                            (gatewayConfig.queryParams.get(queryParams.getString(i)) as Element).getOutput())
                    } else if (gatewayConfig.queryParams.get(queryParams.getString(i)) is Label) {
                        requestUrlBuilder.addQueryParameter(queryParams.getString(i),
                            (gatewayConfig.queryParams.get(queryParams.getString(i)) as Label).revealInput.token)
                    } else if (gatewayConfig.queryParams.get(queryParams.getString(i)) is String || gatewayConfig.queryParams.get(
                            queryParams.getString(i)) is Int
                    )
                        requestUrlBuilder.addQueryParameter(queryParams.getString(i),
                            gatewayConfig.queryParams.getString(queryParams.getString(i)))
                    else {
                        callback.onFailure(java.lang.Exception("invalid field \"${
                            queryParams.getString(i)
                        }\" present in queryParams"))
                        return false
                    }
                }
            }
            return true

        }

        fun addRequestHeader(request: Request.Builder, gatewayConfig: GatewayConfiguration, callback: Callback): Boolean {
            val headers = (gatewayConfig.requestHeader as JSONObject).names()
            if (headers != null) {
                for(i in 0 until headers.length())
                {
                    if(gatewayConfig.requestHeader.get(headers.getString(i)) is String)
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

    }
}