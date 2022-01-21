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
            responseFromConnection: JSONObject
        ) {
                val keys = responseBody.names()
                if(keys !=null) {
                    for (j in 0 until keys.length()) {
                        if (responseBody.get(keys.getString(j)) is JSONObject) {
                                checkInvalidFields(responseBody.get(keys.getString(j)) as JSONObject,
                                    responseFromConnection.getJSONObject(keys.getString(j)))
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
                            }
                        }
                    }
                }
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

        fun getValueForLabel(label : Label,tokenValueMap:HashMap<String,String?>,tokenIdMap:HashMap<String,String>,tokenLabelMap:HashMap<String,Label>) : String {
            val formatRegex = label.options.formatRegex
            val value : String? = label.actualValue
            if(formatRegex.isNotEmpty() && value == null){
                tokenValueMap.put(label.getToken(),null)
                tokenIdMap.put(label.getToken(),label.getID())
                tokenLabelMap.put(label.getToken(),label)
                return label.getID()
            }
            else if(value!= null && formatRegex.isNotEmpty()) {
                val regex = Regex(formatRegex)
                val matches =  regex.find(value)
                return if(matches != null) matches.value else ""
            }
            return label.getValueForConnections()
        }

        fun doTokenMap(responseBody: Any,tokenValueMap:HashMap<String,String?>) { // fill labelWithRegexMap with actual values from api
            val records = (responseBody as JSONObject).getJSONArray("records")
            for(i in 0  until records.length()) {
                val record = records[i] as JSONObject
                val token = record.getString("token")
                val value = record.getString("value")
                tokenValueMap.put(token,value)
            }
        }

        fun doformatRegexForMap(tokenValueMap:HashMap<String,String?>,tokenLabelMap:HashMap<String,Label>) { // do regex on value after detokenize and put it in labelWithRegexMap
            tokenValueMap.forEach {
                val formatRegex = tokenLabelMap.get(it.key)!!.options.formatRegex
                val regex = Regex(formatRegex)
                val matches =  regex.find(it.value!!)
                val value = if(matches != null) matches.value else ""
                tokenValueMap.put(it.key,value)

            }
        }
    }



}