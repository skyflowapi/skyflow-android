package Skyflow.utils

import Skyflow.*
import Skyflow.LogLevel
import Skyflow.soap.SoapConnectionConfig
import android.webkit.URLUtil
import org.json.JSONArray
import org.json.JSONObject
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.Exception

internal class Utils {

    companion object {
        val tag = Utils::class.qualifiedName
        fun checkUrl(url: String): Boolean {
            if (!URLUtil.isValidUrl(url) || !URLUtil.isHttpsUrl(url)) {
                return false
            }
            return true
        }
        //for collect element
        fun constructBatchRequestBody(records: JSONObject, options: InsertOptions,logLevel: LogLevel) : JSONObject{
            val postPayload:MutableList<Any> = mutableListOf()
            val insertTokenPayload:MutableList<Any> = mutableListOf()
            if(records == {}){
                throw SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND, tag, logLevel)
            }
            else if (!records.has("records")) {
                throw SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND, tag, logLevel)
            }
            else if(records.get("records").toString().isEmpty())
            {
                throw SkyflowError(SkyflowErrorCode.EMPTY_RECORDS, tag, logLevel)
            }
            else if(records.get("records") !is JSONArray)
            {
                throw SkyflowError(SkyflowErrorCode.INVALID_RECORDS, tag, logLevel)
            }
            else {
                val obj1 = records.getJSONArray("records")
                var i = 0
                while (i < obj1.length()) {
                    val jsonObj = obj1.getJSONObject(i)
                    if(!jsonObj.has("table"))
                    {
                        throw SkyflowError(SkyflowErrorCode.MISSING_TABLE_KEY, tag, logLevel)
                    }
                    else if(jsonObj.get("table") !is String)
                    {
                        throw SkyflowError(SkyflowErrorCode.INVALID_TABLE_NAME, tag, logLevel)
                    }
                    else if (jsonObj.get("table").toString().isEmpty()) {
                        throw SkyflowError(SkyflowErrorCode.EMPTY_TABLE_KEY, tag, logLevel)
                    }
                    else if(!jsonObj.has("fields"))
                    {
                        throw SkyflowError(SkyflowErrorCode.FIELDS_KEY_ERROR, tag, logLevel)
                    }
                    else if(jsonObj.getJSONObject("fields").toString().equals("{}"))
                    {
                        throw SkyflowError(SkyflowErrorCode.EMPTY_FIELDS, tag, logLevel)
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
                            throw SkyflowError(SkyflowErrorCode.EMPTY_COLUMN_KEY, tag, logLevel)
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

        fun getValueForLabel(label : Label,tokenValueMap:HashMap<String,String?>,tokenIdMap:HashMap<String,String>,tokenLabelMap:HashMap<String,Label>,tag:String?="",logLevel: LogLevel) : String {
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
                if(matches != null)
                    return matches.value
                throw SkyflowError(SkyflowErrorCode.INVALID_FORMAT_REGEX,tag,logLevel, params = arrayOf(formatRegex))
            }
            return label.getValueForConnections()
        }
        fun getValueForLabel(label: Label,value:String,tag:String?="",logLevel: LogLevel) {
            val formatRegex = label.options.formatRegex
            if(formatRegex.isNotEmpty()) {
                val regex = Regex(formatRegex)
                val matches = regex.find(value)
                if (matches != null)
                    label.setText(matches.value)
                else
                    throw SkyflowError(SkyflowErrorCode.INVALID_FORMAT_REGEX,tag,logLevel, params = arrayOf(formatRegex))
            }
            else
                label.setText(value)
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

        fun doformatRegexForMap(tokenValueMap:HashMap<String,String?>,tokenLabelMap:HashMap<String,Label>,tag:String?="",logLevel: LogLevel) { // do regex on value after detokenize and put it in labelWithRegexMap
            tokenValueMap.forEach {
                val formatRegex = tokenLabelMap.get(it.key)!!.options.formatRegex
                val regex = Regex(formatRegex)
                val matches =  regex.find(it.value!!)
                if(matches != null)
                    tokenValueMap.put(it.key,matches.value)
                else
                    throw SkyflowError(SkyflowErrorCode.INVALID_FORMAT_REGEX,tag,logLevel, params = arrayOf(formatRegex))

            }
        }

        internal fun checkVaultDetails(configuration: Configuration)
        {
            if(configuration.vaultURL.isEmpty() || configuration.vaultURL == "/v1/vaults/")
            {
               throw SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL, tag, configuration.options.logLevel)

            }
            if(configuration.vaultID.isEmpty())
            {
                throw SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID, tag, configuration.options.logLevel)
            }
            if(!checkUrl(configuration.vaultURL))
               throw SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL, tag, configuration.options.logLevel, arrayOf(configuration.vaultURL))
        }

    }



}