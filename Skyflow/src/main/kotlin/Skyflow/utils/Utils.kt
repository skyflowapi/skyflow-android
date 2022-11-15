package Skyflow.utils

import Skyflow.*
import Skyflow.LogLevel
import android.util.Log
import android.webkit.URLUtil
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.Exception
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

public class Utils {

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
                    map["upsert"] = getUpsertColumn(jsonObj.getString("table"), options.upsert,logLevel)
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

        fun getUpsertColumn(tableName: String, options:JSONArray?,logLevel: LogLevel): String {
           if(options != null) {
               if(options.length() == 0) {
                   throw SkyflowError(SkyflowErrorCode.EMPTY_UPSERT_OPTIONS_ARRAY,
                       tag,
                       logLevel)
               }
               for (index in 0..options.length() - 1) {
                   if (options.get(index) !is JSONObject) {
                       throw SkyflowError(SkyflowErrorCode.ALLOW_JSON_OBJECT_IN_UPSERT,
                           tag,
                           logLevel)
                   }
                   if (!options.getJSONObject(index).has("table")) {
                       throw SkyflowError(SkyflowErrorCode.NO_TABLE_KEY_IN_UPSERT, tag, logLevel,
                           arrayOf(index.toString()))
                   }
                   if (!options.getJSONObject(index).has("column")) {
                       throw SkyflowError(SkyflowErrorCode.NO_COLUMN_KEY_IN_UPSERT,
                           tag,
                           logLevel,
                           arrayOf(index.toString()))
                   }
                   if (options.getJSONObject(index)
                           .get("table") !is String || options.getJSONObject(index).get("table")
                           .toString().isEmpty()
                   ) {
                       throw SkyflowError(SkyflowErrorCode.INVALID_TABLE_IN_UPSERT_OPTION,
                           tag,
                           logLevel,
                           arrayOf(index.toString()))
                   }
                   if (options.getJSONObject(index)
                           .get("column") !is String || options.getJSONObject(index).get("column")
                           .toString().isEmpty()
                   ) {
                       throw SkyflowError(SkyflowErrorCode.INVALID_COLUMN_IN_UPSERT_OPTION,
                           tag,
                           logLevel,
                           arrayOf(index.toString()))
                   }
                   if (tableName.equals(options.getJSONObject(index).get("table").toString())) {
                       return options.getJSONObject(index).get("column").toString();
                   }
               }
           }
            return ""
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
                }
            }
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

        fun getValueForLabel(label : Label, tokenValueMap:HashMap<String,String?>, tokenIdMap:HashMap<String,String>, tokenLabelMap:HashMap<String,Label>, tag:String?="", logLevel: LogLevel) : String {
            val formatRegex = label.options.formatRegex
            val replaceText = label.options.replaceText
            val value : String? = label.actualValue
            if(formatRegex.isNotEmpty() && value == null){
                tokenValueMap.put(label.getToken(),null)
                tokenIdMap.put(label.getToken(),label.getID())
                tokenLabelMap.put(label.getToken(),label)
                return label.getID()
            }
            else if(value!= null && formatRegex.isNotEmpty() && replaceText == null) {
                val regex = Regex(formatRegex)
                val matches =  regex.find(value)
                if(matches != null)
                    return matches.value
                else
                {
                    Log.w(tag,"no match found for regex - $formatRegex")
                }
            }
            else if(value!= null && formatRegex.isNotEmpty() && replaceText !=null)
            {
                try {
                    val replacedValue = value.replace(Regex(formatRegex),replaceText)
                    return replacedValue
                }
                catch (e:Exception)
                {
                    Log.w(tag,"invalid replaceText - $replaceText")
                }
            }
            return label.getValueForConnections()
        }
        fun setValueForLabel(label: Label, value:String) {
            val formatRegex = label.options.formatRegex
            val replaceText = label.options.replaceText
            if(formatRegex.isNotEmpty() && replaceText == null) {
                val regex = Regex(formatRegex)
                val matches = regex.find(value)
                if (matches != null)
                {
                    label.setText(matches.value)
                }
                else {
                    Log.w(tag,"no match is found for regex - $formatRegex")
                    label.setText(value)
                }
            }
            else if(formatRegex.isNotEmpty() && replaceText != null)
            {
                try {
                    val replacedValue = value.replace(Regex(formatRegex),replaceText)
                    label.setText(replacedValue)
                }
                catch (e:Exception)
                {
                    Log.w(tag,"invalid replaceText - $replaceText")
                    label.setText(value)
                }
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

        fun doformatRegexForMap(tokenValueMap:HashMap<String,String?>,tokenLabelMap:HashMap<String,Label>,tag:String?="") { // do regex on value after detokenize and put it in labelWithRegexMap
            tokenValueMap.forEach {
                val formatRegex = tokenLabelMap.get(it.key)!!.options.formatRegex
                val replaceText = tokenLabelMap.get(it.key)!!.options.replaceText
                if(formatRegex.isNotEmpty() && replaceText == null) {
                    val regex = Regex(formatRegex)
                    val matches =  regex.find(it.value!!)
                    if(matches != null) {
                        tokenValueMap.put(it.key, matches.value)
                    }
                    else
                    {
                        Log.w(tag,"no match found for regex - $formatRegex" )
                        tokenValueMap.put(it.key,it.value)
                    }
                }
                else if(formatRegex.isNotEmpty() && replaceText != null)
                {
                    try {
                        val replacedValue = it.value!!.replace(Regex(formatRegex),replaceText)
                        tokenValueMap.put(it.key,replacedValue)
                    }
                    catch (e:Exception)
                    {
                        Log.w(Companion.tag,"invalid replaceText - $replaceText")
                        tokenValueMap.put(it.key,it.value)
                    }
                }
                else {
                    tokenValueMap.put(it.key,it.value)
                }
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
            if(!checkUrl(configuration.vaultURL)) {
                throw SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL, tag, configuration.options.logLevel, arrayOf(configuration.vaultURL))
            }
        }

        fun appendRequestId(message:String, requestId:String): String {
            if(requestId.isEmpty() || requestId.equals("null"))
                return message
            return message + " - requestId : " + requestId
        }

        fun r_urlencode(parents:MutableList<Any>,pairs:HashMap<String,String>,data:Any) : HashMap<String,String> {
            if(data is JSONArray) { //  || data is Array<*>
                for(i in 0..data.length()-1)
                {
                    parents.add(i)
                    r_urlencode(parents,pairs,data[i])
                    parents.removeAt(parents.size-1)
                }
            }
            else if(data is Array<*>) {
                for(i in 0..data.size-1)
                {
                    parents.add(i)
                    r_urlencode(parents,pairs,data.get(i)!!)
                    parents.removeAt(parents.size-1)
                }
            }
            else if(data is JSONObject) { //|| data is HashMap<*,*>
                val keys = data.names()
                if(keys !=null) {
                    for (j in 0 until keys.length()) {
                        val key = keys.getString(j)
                        parents.add(key)
                        r_urlencode(parents,pairs,data.get(key))
                        parents.removeAt(parents.size-1)
                    }
                }
            }
            else if(data is HashMap<*,*>) {
                data.forEach { (key, value) ->
                    parents.add(key)
                    r_urlencode(parents,pairs,value)
                    parents.removeAt(parents.size-1)
                }
            }
            else {
                pairs[renderKey(parents)] = data.toString()
            }
            return pairs
        }
        fun renderKey(parents: MutableList<Any>) : String {
            var depth = 0
            var outputString = ""
            for(parent in parents) {
                if(depth>0 || (parent is Int)) {
                    outputString = outputString + "[$parent]"
                }
                else {
                    outputString = outputString + parent
                }
                depth = depth + 1
            }
            return outputString
        }
        fun encode(str:String) : String {
            return  URLEncoder.encode(str, StandardCharsets.UTF_8.toString());
        }
        fun convertJSONToQueryString(body: JSONObject) : String{
            val map = r_urlencode(mutableListOf(),HashMap(),body)
            var queryString = ""
            map.forEach { (key, value) -> queryString = queryString + encode(key)+"=" + encode(value) + "&" }
            return queryString.substring(0,queryString.length-1)
        }
        fun getRequestbodyForConnection(requestBody: JSONObject, contentType: String): RequestBody {
            val mediaType = contentType.toMediaTypeOrNull()
            if(contentType.equals(ContentType.FORMURLENCODED.type)) {
                return convertJSONToQueryString(requestBody).toRequestBody(mediaType)
            }
            else if(contentType.equals(ContentType.FORMDATA.type)) {
                val map = r_urlencode(mutableListOf(), HashMap(), requestBody)
                val mutlipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                map.forEach { (key, value) -> mutlipartBody.addPart(
                    Headers.headersOf("Content-Disposition", "form-data; name=\"$key\""),
                    "$value".toRequestBody(null))
                }
                return mutlipartBody.build()
            }
            else {
                return requestBody.toString().toRequestBody(mediaType)
            }
        }

        fun currentTwoDigitYear(): Int
        {
            return Calendar.getInstance().get(Calendar.YEAR) %100
        }
        fun currentFourDigitYear() : Int
        {
            return Calendar.getInstance().get(Calendar.YEAR)
        }
        fun currentMonth(): Int
        {
            return Calendar.getInstance().get(Calendar.MONTH)+1
        }
    }
}