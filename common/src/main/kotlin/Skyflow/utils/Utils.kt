package Skyflow.utils

import Skyflow.*
import Skyflow.LogLevel
import Skyflow.core.Logger
import android.os.Build
import Skyflow.core.Messages
import Skyflow.core.getMessage
import android.util.Log
import android.webkit.URLUtil
import com.skyflow_android.BuildConfig
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

        //check whether pci element is valid or not inside requestbody of connectionConfig
        fun checkElement(element: Element, callback: Callback, logLevel: LogLevel): Boolean {
            val state = element.getState()
            var errors = ""
            var labelName = element.columnName
            if (labelName == "") {
                labelName = element.collectInput.label
            }
            if ((state["isRequired"] as Boolean) && (state["isEmpty"] as Boolean)) {
                errors = "$labelName is empty\n"
            }
            if (!(state["isValid"] as Boolean)) {
                errors = "for " + labelName + " " + (state["validationError"] as String) + "\n"
            }
            if (errors != "") {
                val error =
                    SkyflowInternalError(SkyflowErrorCode.INVALID_INPUT, tag, logLevel, arrayOf(errors))
                callback.onFailure(constructError(error))
                return false
            }
            return true
        }

        fun getUpsertColumn(tableName: String, options: JSONArray?, logLevel: LogLevel): String {
            if (options != null) {
                if (options.length() == 0) {
                    throw SkyflowInternalError(
                        SkyflowErrorCode.EMPTY_UPSERT_OPTIONS_ARRAY,
                        tag,
                        logLevel
                    )
                }
                for (index in 0..options.length() - 1) {
                    if (options.get(index) !is JSONObject) {
                        throw SkyflowInternalError(
                            SkyflowErrorCode.ALLOW_JSON_OBJECT_IN_UPSERT, tag, logLevel,
                            arrayOf("$index")
                        )
                    }
                    if (!options.getJSONObject(index).has("table")) {
                        throw SkyflowInternalError(
                            SkyflowErrorCode.NO_TABLE_KEY_IN_UPSERT, tag, logLevel,
                            arrayOf(index.toString())
                        )
                    }
                    if (!options.getJSONObject(index).has("column")) {
                        throw SkyflowInternalError(
                            SkyflowErrorCode.NO_COLUMN_KEY_IN_UPSERT, tag, logLevel,
                            arrayOf(index.toString())
                        )
                    }
                    if (options.getJSONObject(index)
                            .get("table") !is String || options.getJSONObject(index).get("table")
                            .toString().isEmpty()
                    ) {
                        throw SkyflowInternalError(
                            SkyflowErrorCode.INVALID_TABLE_IN_UPSERT_OPTION, tag, logLevel,
                            arrayOf(index.toString())
                        )
                    }
                    if (options.getJSONObject(index)
                            .get("column") !is String || options.getJSONObject(index).get("column")
                            .toString().isEmpty()
                    ) {
                        throw SkyflowInternalError(
                            SkyflowErrorCode.INVALID_COLUMN_IN_UPSERT_OPTION, tag, logLevel,
                            arrayOf(index.toString())
                        )
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
            if (keys != null) {
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
                        // A malformed sub-field is skipped rather than aborting response shaping,
                        // but no longer silently — surface it for diagnosis.
                        Log.w(tag, "removeEmptyAndNullFields: skipping malformed field '$key'", e)
                    }
                }
            }
        }

        fun checkIfElementsMounted(element: Label): Boolean {
            if (!element.isAttachedToWindow())
                return false
            return true
        }

        fun checkIfElementsMounted(element: Element): Boolean {
            if (!element.isAttachedToWindow())
                return false
            return true
        }

        fun copyJSON(records: JSONObject, finalRecords: JSONObject) {
            val keys = records.names()
            if (keys != null) {
                for (j in 0 until keys.length()) {
                    finalRecords.put(keys.getString(j), records.get(keys.getString(j)))
                }
            }
        }

        fun constructMessage(message: String, vararg values: String?): String {
            return String.format(message, *values)
        }

        fun constructError(e: Exception, code: Int = 400): JSONObject {
            val skyflowError = if (e is SkyflowInternalError) e
            else SkyflowInternalError(params = arrayOf(e.message))

            skyflowError.setErrorCode(code)
            
            val errorWrapper = JSONObject()
            errorWrapper.put("error", skyflowError)
            
            val errors = JSONArray()
            errors.put(errorWrapper)
            
            val finalError = JSONObject()
            finalError.put("errors", errors)
            return finalError
        }

        fun constructErrorResponse(code: Int, description: String): JSONObject {
            val errorResponse = JSONObject()
            val errorsArray = JSONArray()
            val errorObject = JSONObject()
            val errorDetails = JSONObject()
            
            errorDetails.put("code", code)
            errorDetails.put("description", description)
            errorDetails.put("type", "$code")
            
            errorObject.put("error", errorDetails)
            errorsArray.put(errorObject)
            errorResponse.put("errors", errorsArray)
            
            return errorResponse
        }

        fun constructErrorResponse(e: Exception, defaultCode: Int = 400): JSONObject {
            val code = if (e is SkyflowInternalError) e.getErrorcode() else defaultCode
            val description = e.message ?: "An error occurred"
            return constructErrorResponse(code, description)
        }

        fun constructErrorObject(code: Int, description: String): JSONObject {
            val errorObject = JSONObject()
            val errorDetails = JSONObject()
            
            errorDetails.put("code", code)
            errorDetails.put("description", description)
            errorDetails.put("type", "$code")
            
            errorObject.put("error", errorDetails)
            return errorObject
        }

        fun findMatches(regex: String, text: String): MutableList<String> {
            val allMatches: MutableList<String> = ArrayList()
            val m: Matcher = Pattern.compile(regex)
                .matcher(text)
            while (m.find()) {
                allMatches.add(m.group())
            }
            return allMatches
        }

        fun getValueForLabel(
            label: Label,
            tokenValueMap: HashMap<String, String?>,
            tokenIdMap: HashMap<String, String>,
            tokenLabelMap: HashMap<String, Label>,
            tag: String? = "",
            logLevel: LogLevel
        ): String {
            val formatRegex = label.options.formatRegex
            val replaceText = label.options.replaceText
            val value: String? = label.actualValue
            if (formatRegex.isNotEmpty() && value == null) {
                tokenValueMap.put(label.getToken(), null)
                tokenIdMap.put(label.getToken(), label.getID())
                tokenLabelMap.put(label.getToken(), label)
                return label.getID()
            } else if (value != null && formatRegex.isNotEmpty() && replaceText == null) {
                val regex = Regex(formatRegex)
                val matches = regex.find(value)
                if (matches != null)
                    return matches.value
                else {
                    Log.w(tag, "no match found for regex - $formatRegex")
                }
            } else if (value != null && formatRegex.isNotEmpty() && replaceText != null) {
                try {
                    val replacedValue = value.replace(Regex(formatRegex), replaceText)
                    return replacedValue
                } catch (e: Exception) {
                    Log.w(tag, "invalid replaceText - $replaceText")
                }
            }
            return label.getValueForConnections()
        }

        fun setValueForLabel(label: Label, value: String) {
            val formatRegex = label.options.formatRegex
            val replaceText = label.options.replaceText

            label.placeholder.setTextIsSelectable(true)
            label.enableCopy(value)

            val format = label.options.format
            val translation = label.options.translation
            val DEFAULT_TRANSLATION = hashMapOf(Pair('X', "[0-9]"))

            if (format.isNotEmpty() && translation == null) {
                label.options.translation = DEFAULT_TRANSLATION
            }

            label.options.createRegexMap()

            if (formatRegex.isNotEmpty() && replaceText == null) {
                val regex = Regex(formatRegex)
                val matches = regex.find(value)
                if (matches != null) {
                    label.setText(matches.value)
                } else {
                    Log.w(tag, "no match is found for regex - $formatRegex")
                    label.setText(value)
                }
            } else if (formatRegex.isNotEmpty() && replaceText != null) {
                try {
                    val replacedValue = value.replace(Regex(formatRegex), replaceText)
                    label.setText(replacedValue)
                } catch (e: Exception) {
                    Log.w(tag, "invalid replaceText - $replaceText")
                    label.setText(value)
                }
            } else {
                label.setText(value)
            }
        }

        fun setErrorForLabel(label: Label) {
            label.setError("invalid token")
            label.showError()
        }

        // fill labelWithRegexMap with actual values from api
        fun doTokenMap(responseBody: Any, tokenValueMap: HashMap<String, String?>) {
            val records = (responseBody as JSONObject).getJSONArray("records")
            for (i in 0 until records.length()) {
                val record = records[i] as JSONObject
                val token = record.getString("token")
                val value = record.getString("value")
                tokenValueMap.put(token, value)
            }
        }

        // do regex on value after detokenize and put it in labelWithRegexMap
        fun doformatRegexForMap(
            tokenValueMap: HashMap<String, String?>,
            tokenLabelMap: HashMap<String, Label>,
            tag: String? = ""
        ) {
            tokenValueMap.forEach {
                val formatRegex = tokenLabelMap.get(it.key)!!.options.formatRegex
                val replaceText = tokenLabelMap.get(it.key)!!.options.replaceText
                if (formatRegex.isNotEmpty() && replaceText == null) {
                    val regex = Regex(formatRegex)
                    val matches = regex.find(it.value!!)
                    if (matches != null) {
                        tokenValueMap.put(it.key, matches.value)
                    } else {
                        Log.w(tag, "no match found for regex - $formatRegex")
                        tokenValueMap.put(it.key, it.value)
                    }
                } else if (formatRegex.isNotEmpty() && replaceText != null) {
                    try {
                        val replacedValue = it.value!!.replace(Regex(formatRegex), replaceText)
                        tokenValueMap.put(it.key, replacedValue)
                    } catch (e: Exception) {
                        Log.w(Companion.tag, "invalid replaceText - $replaceText")
                        tokenValueMap.put(it.key, it.value)
                    }
                } else {
                    tokenValueMap.put(it.key, it.value)
                }
            }
        }

        internal fun checkVaultDetails(configuration: BaseConfiguration) {
            if (configuration.vaultURL.isEmpty() || configuration.vaultURL == "/v1/vaults/") {
                throw SkyflowInternalError(
                    SkyflowErrorCode.EMPTY_VAULT_URL,
                    tag,
                    configuration.options.logLevel
                )
            }
            if (configuration.vaultID.isEmpty()) {
                throw SkyflowInternalError(
                    SkyflowErrorCode.EMPTY_VAULT_ID,
                    tag,
                    configuration.options.logLevel
                )
            }
            if (!checkUrl(configuration.vaultURL)) {
                throw SkyflowInternalError(
                    SkyflowErrorCode.INVALID_VAULT_URL,
                    tag,
                    configuration.options.logLevel,
                    arrayOf(configuration.vaultURL)
                )
            }
        }

        fun appendRequestId(message: String, requestId: String): String {
            if (requestId.isEmpty() || requestId.equals("null"))
                return message
            return message + " - requestId : " + requestId
        }

        fun r_urlencode(
            parents: MutableList<Any>,
            pairs: HashMap<String, String>,
            data: Any
        ): HashMap<String, String> {
            if (data is JSONArray) { //  || data is Array<*>
                for (i in 0..data.length() - 1) {
                    parents.add(i)
                    r_urlencode(parents, pairs, data[i])
                    parents.removeAt(parents.size - 1)
                }
            } else if (data is Array<*>) {
                for (i in 0..data.size - 1) {
                    parents.add(i)
                    r_urlencode(parents, pairs, data.get(i)!!)
                    parents.removeAt(parents.size - 1)
                }
            } else if (data is JSONObject) { //|| data is HashMap<*,*>
                val keys = data.names()
                if (keys != null) {
                    for (j in 0 until keys.length()) {
                        val key = keys.getString(j)
                        parents.add(key)
                        r_urlencode(parents, pairs, data.get(key))
                        parents.removeAt(parents.size - 1)
                    }
                }
            } else if (data is HashMap<*, *>) {
                data.forEach { (key, value) ->
                    parents.add(key)
                    r_urlencode(parents, pairs, value)
                    parents.removeAt(parents.size - 1)
                }
            } else {
                pairs[renderKey(parents)] = data.toString()
            }
            return pairs
        }

        fun renderKey(parents: MutableList<Any>): String {
            var depth = 0
            var outputString = ""
            for (parent in parents) {
                if (depth > 0 || (parent is Int)) {
                    outputString = outputString + "[$parent]"
                } else {
                    outputString = outputString + parent
                }
                depth = depth + 1
            }
            return outputString
        }

        fun encode(str: String): String {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.toString());
        }

        fun convertJSONToQueryString(body: JSONObject): String {
            val map = r_urlencode(mutableListOf(), HashMap(), body)
            var queryString = ""
            map.forEach { (key, value) ->
                queryString = queryString + encode(key) + "=" + encode(value) + "&"
            }
            return queryString.substring(0, queryString.length - 1)
        }


        fun currentTwoDigitYear(): Int {
            return Calendar.getInstance().get(Calendar.YEAR) % 100
        }

        fun currentFourDigitYear(): Int {
            return Calendar.getInstance().get(Calendar.YEAR)
        }

        fun currentMonth(): Int {
            return Calendar.getInstance().get(Calendar.MONTH) + 1
        }

        fun fetchMetrics(): JSONObject {
            val metrics = JSONObject()
            try {
                metrics.put(
                    "sdk_name_version",
                    "${BuildConfig.SDK_NAME}@${BuildConfig.SDK_VERSION}"
                )
                metrics.put("sdk_client_device_model", "${Build.BRAND} ${Build.MODEL}")
                metrics.put("sdk_client_os_details", "android-${Build.VERSION.RELEASE}")
                metrics.put("sdk_runtime_details", "kotlin-${KotlinVersion.CURRENT}")
            } catch (err: Exception) {
                Log.d(tag, "fetching SDK metrics failed")
            }
            return metrics
        }


        fun checkInputFormatOptions(
            type: SkyflowElementType,
            options: CollectElementOptions,
            logLevel: LogLevel
        ) {

            if (SkyflowElementType.getUnsupportedInputFormatElements().contains(type)) {
                if (options.translation != null || options.format.isNotEmpty()) {
                    Logger.warn(
                        tag,
                        Messages.INPUT_FORMATTING_NOT_SUPPORTED.getMessage(type.toString()),
                        logLevel,
                    )
                }
            } else if (!SkyflowElementType.getSupportedInputFormatElements().contains(type)) {
                if (options.translation != null) {
                    Logger.warn(
                        tag,
                        Messages.INVALID_INPUT_TRANSLATION.getMessage(type.toString()),
                        logLevel
                    )
                }
            } else { // supported elements

                // neither translation nor format passed
                if (options.format.isEmpty() && options.translation == null) return

                // only format passed
                if (options.translation == null) {
                    val DEFAULT_TRANSLATION = hashMapOf(Pair('X', "[0-9]"))
                    Logger.warn(
                        tag,
                        Messages.EMPTY_INPUT_TRANSLATION.getMessage(DEFAULT_TRANSLATION.toString()),
                        logLevel
                    )
                    options.translation = DEFAULT_TRANSLATION
                }
                createRegexMapForTranslation(options)
            }
        }

        private fun createRegexMapForTranslation(options: CollectElementOptions) {
            options.createRegexMap()
        }
    }
}