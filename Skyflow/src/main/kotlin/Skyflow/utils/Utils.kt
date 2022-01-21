package Skyflow.utils

import Skyflow.*
import Skyflow.LogLevel
import android.webkit.URLUtil
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
    }



}