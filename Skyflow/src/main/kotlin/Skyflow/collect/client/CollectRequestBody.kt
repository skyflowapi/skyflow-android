package Skyflow.collect.client

import Skyflow.*
import Skyflow.collect.elements.validations.ElementValueMatchRule
import org.json.JSONArray
import org.json.JSONObject
import android.util.Log
import com.google.gson.JsonObject
import kotlin.Exception

class CollectRequestBody {
    companion object {
        private val tag = CollectRequestBody::class.qualifiedName
        internal fun createRequestBody(
            elements: MutableList<TextField>,
            additionalFields: JSONObject?,
            callback: Callback,
            logLevel: LogLevel
        ) : String
        {
            val tableMap: HashMap<String,MutableList<CollectRequestRecord>> = HashMap()
            val tableWithColumn : HashSet<String> = HashSet()
            for (element in elements) {
                if (tableMap[(element.tableName)] != null){
                    if(tableWithColumn.contains(element.tableName+element.columnName))
                    {
                     //   callback.onFailure(Exception("duplicate column "+element.columnName+ " found in "+element.tableName))
                        var hasElementValueMatchRule: Boolean = false
                        for(validation in element.collectInput.validations.rules) {
                            if(validation is ElementValueMatchRule) {
                                hasElementValueMatchRule = true
                                break;
                            }
                        }
                        if(!hasElementValueMatchRule)
                        {
                            val error = SkyflowError(SkyflowErrorCode.DUPLICATE_COLUMN_FOUND,
                                tag, logLevel, arrayOf(element.tableName,element.columnName))
                            callback.onFailure(error)
                            return ""
                        }
                        continue;
                    }
                    tableWithColumn.add(element.tableName+element.columnName)
                    Log.d(tag, "createRequestBody: ${element.getValue()}")
                    val obj = CollectRequestRecord(element.columnName,element.getValue())
                    tableMap[(element.tableName)]!!.add(obj)
                }
                else{
                    val obj = CollectRequestRecord(element.columnName,element.getValue())
                    val tempArray = mutableListOf<CollectRequestRecord>()
                    tempArray.add(obj)
                    tableWithColumn.add(element.tableName+element.columnName)
                    tableMap[(element.tableName)] = tempArray
                }
            }

            if (additionalFields != null) {
                if(additionalFields.has("records")) {
                    try {
                        if (additionalFields.get("records") !is JSONArray) {
                            callback.onFailure(
                                SkyflowError(
                                    SkyflowErrorCode.INVALID_RECORDS,
                                    tag,
                                    logLevel
                                )
                            )
                            return ""
                        }
                        val records = additionalFields.getJSONArray("records")
                        if (records.length() == 0) {
                            throw SkyflowError(SkyflowErrorCode.EMPTY_RECORDS, tag, logLevel)
                        }
                        var i = 0
                        while (i < records.length()) {
                            val jsonobj = records.getJSONObject(i)
                            if (!jsonobj.has("table")) {
                                callback.onFailure(
                                    SkyflowError(
                                        SkyflowErrorCode.MISSING_TABLE_KEY,
                                        tag,
                                        logLevel
                                    )
                                )
                                return ""
                            } else if (!jsonobj.has("fields")) {
                                callback.onFailure(
                                    SkyflowError(
                                        SkyflowErrorCode.FIELDS_KEY_ERROR,
                                        tag,
                                        logLevel
                                    )
                                )
                                return ""

                            } else if (jsonobj.getJSONObject("fields").toString() == "{}") {
                                callback.onFailure(
                                    SkyflowError(
                                        SkyflowErrorCode.EMPTY_FIELDS,
                                        tag,
                                        logLevel
                                    )
                                )
                                return ""

                            }
                            val tableName = jsonobj.get("table")
                            if (tableName !is String)
                                throw SkyflowError(
                                    SkyflowErrorCode.INVALID_TABLE_NAME,
                                    tag,
                                    logLevel
                                )
                            if (tableName.isEmpty())
                                throw SkyflowError(SkyflowErrorCode.EMPTY_TABLE_KEY, tag, logLevel)
                            if (jsonobj.getJSONObject("fields").toString() != "{}") {
                                val fields = jsonobj.getJSONObject("fields")
                                val keys = fields.names()
                                val fieldList = mutableListOf<CollectRequestRecord>()
                                for (j in 0 until keys!!.length()) {
                                    if (keys.getString(j).isEmpty()) {
                                        callback.onFailure(
                                            SkyflowError(
                                                SkyflowErrorCode.EMPTY_COLUMN_NAME,
                                                tag,
                                                logLevel
                                            )
                                        )
                                        return ""
                                    }
                                    val obj = CollectRequestRecord(
                                        keys.getString(j),
                                        fields.get(keys.getString(j))
                                    )
                                    fieldList.add(obj)
                                }
                                if (tableMap[tableName] != null) {
                                    for (k in 0 until fieldList.size) {
                                        if (tableWithColumn.contains(tableName + fieldList[k].columnName)) {
                                            val error = SkyflowError(
                                                SkyflowErrorCode.DUPLICATE_COLUMN_FOUND,
                                                tag,
                                                logLevel,
                                                arrayOf(tableName, fieldList[k].columnName)
                                            )
                                            callback.onFailure(error)
                                            return ""

                                        } else {
                                            tableWithColumn.add(tableName + fieldList[k].columnName)
                                        }
                                    }
                                    tableMap[tableName]!!.addAll(fieldList)
                                } else {
                                    val tempArray = mutableListOf<CollectRequestRecord>()
                                    for (k in 0 until fieldList.size) {
                                        if (tableWithColumn.contains(tableName + fieldList[k].columnName)) {
                                            val error = SkyflowError(
                                                SkyflowErrorCode.DUPLICATE_COLUMN_FOUND,
                                                tag,
                                                logLevel,
                                                arrayOf(tableName, fieldList[k].columnName)
                                            )
                                            callback.onFailure(error)
                                            return ""

                                        } else
                                            tableWithColumn.add(tableName + fieldList[k].columnName)
                                    }
                                    tempArray.addAll(fieldList)
                                    tableMap[tableName] = tempArray
                                }
                            }
                            i++
                        }
                    } catch (e: Exception) {
                        callback.onFailure(exception = e)
                        return ""
                    }
                } else{
                    val error = SkyflowError(SkyflowErrorCode.ADDITIONAL_FIELDS_RECORDS_KEY_NOT_FOUND,
                        tag, logLevel)
                    callback.onFailure(error)
                    return ""
                }
            }
            val recordsArray = JSONArray()
            val requestObject = JSONObject()
            for ((key, value ) in tableMap){
                val recordObject = JSONObject()
                recordObject.put("table", key)
                val fieldsObject = JSONObject()
                for (element in value){
                    createJSONKey(fieldsObject, element.columnName, element.value)
                }
                recordObject.put("fields", fieldsObject)
                recordsArray.put(recordObject)
            }
            requestObject.put("records", recordsArray)
            return requestObject.toString()
        }

        private fun createJSONKey(fieldsObject: JSONObject, columnName: String, value: Any){
            val keys = columnName.split(".").toTypedArray()
            if(fieldsObject.has(keys[0])){
                if(keys.size > 1){
                    createJSONKey(fieldsObject.get(keys[0]) as JSONObject, keys.drop(1).joinToString("."), value)
                }
            }else{
                if(keys.size > 1){
                    val tempObject = JSONObject()
                    fieldsObject.put(keys[0], tempObject)
                    createJSONKey(tempObject, keys.drop(1).joinToString("."), value)
                }
                else {
                    fieldsObject.put(keys[0], value)
                }
            }
        }
    }
}