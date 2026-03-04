package Skyflow.collect.client

import Skyflow.*
import Skyflow.collect.elements.validations.ElementValueMatchRule
import org.json.JSONArray
import org.json.JSONObject

internal class CollectRequestBody {
    companion object {
        private val tag = CollectRequestBody::class.qualifiedName
        
        internal fun separateInsertAndUpdateRecords(
            elements: MutableList<TextField>,
            additionalFields: JSONObject?,
            logLevel: LogLevel
        ): Triple<MutableList<TextField>, JSONObject?, MutableList<UpdateRequestRecord>> {
            val insertElements = mutableListOf<TextField>()
            val updateRecordsMap = mutableMapOf<String, UpdateRequestRecord>()
            
            // Process elements first
            for (element in elements) {
                if (element.skyflowID != null && element.skyflowID!!.isNotEmpty()) {
                    // This is an update record
                    val key = "${element.tableName}_${element.skyflowID}"
                    if (updateRecordsMap.containsKey(key)) {
                        // Add column to existing update record
                        updateRecordsMap[key]!!.columns[element.columnName] = element.getValue()
                    } else {
                        // Create new update record
                        val columns = mutableMapOf<String, Any>(element.columnName to element.getValue())
                        updateRecordsMap[key] = UpdateRequestRecord(
                            table = element.tableName,
                            skyflowID = element.skyflowID!!,
                            columns = columns
                        )
                    }
                } else {
                    // This is an insert record
                    insertElements.add(element)
                }
            }
            
            // Process additionalFields if present
            var insertAdditionalFieldsRecords: JSONArray? = null
            if (additionalFields != null && additionalFields.has("records")) {
                val records = additionalFields.getJSONArray("records")
                val insertRecords = JSONArray()
                
                for (i in 0 until records.length()) {
                    val record = records.getJSONObject(i)
                    val fields = record.getJSONObject("fields")
                    
                    // Check if fields contain skyflowID
                    if (fields.has("skyflowID") && fields.getString("skyflowID").isNotEmpty()) {
                        // This is an update record from additionalFields
                        val table = record.getString("table")
                        val skyflowID = fields.getString("skyflowID")
                        val key = "${table}_${skyflowID}"
                        
                        // Remove skyflowID from fields as it's not a column to update
                        fields.remove("skyflowID")
                        
                        if (updateRecordsMap.containsKey(key)) {
                            // Merge with existing update record (from elements or previous additionalFields)
                            val fieldKeys = fields.keys()
                            while (fieldKeys.hasNext()) {
                                val fieldKey = fieldKeys.next()
                                updateRecordsMap[key]!!.columns[fieldKey] = fields.get(fieldKey)
                            }
                        } else {
                            // Create new update record
                            val columns = mutableMapOf<String, Any>()
                            val fieldKeys = fields.keys()
                            while (fieldKeys.hasNext()) {
                                val fieldKey = fieldKeys.next()
                                columns[fieldKey] = fields.get(fieldKey)
                            }
                            updateRecordsMap[key] = UpdateRequestRecord(
                                table = table,
                                skyflowID = skyflowID,
                                columns = columns
                            )
                        }
                    } else {
                        // This is an insert record from additionalFields
                        insertRecords.put(record)
                    }
                }
                
                // Create insertAdditionalFields object if there are insert records
                if (insertRecords.length() > 0) {
                    insertAdditionalFieldsRecords = insertRecords
                }
            }
            
            val insertAdditionalFieldsJson = if (insertAdditionalFieldsRecords != null) {
                JSONObject().put("records", insertAdditionalFieldsRecords)
            } else {
                null
            }
            
            return Triple(insertElements, insertAdditionalFieldsJson, updateRecordsMap.values.toMutableList())
        }
        
        internal fun createRequestBody(
            elements: MutableList<TextField>,
            additionalFields: JSONObject?,
            logLevel: LogLevel
        ): String {
            val tableMap: HashMap<String, MutableList<CollectRequestRecord>> = HashMap()
            val tableWithColumn: HashSet<String> = HashSet()
            for (element in elements) {
                if (tableMap[(element.tableName)] != null) {
                    if (tableWithColumn.contains(element.tableName + element.columnName)) {
                        var hasElementValueMatchRule: Boolean = false
                        for (validation in element.collectInput.validations.rules) {
                            if (validation is ElementValueMatchRule) {
                                hasElementValueMatchRule = true
                                break;
                            }
                        }
                        if (!hasElementValueMatchRule)
                            throw SkyflowError(
                                SkyflowErrorCode.DUPLICATE_COLUMN_FOUND, tag, logLevel,
                                arrayOf(element.tableName, element.columnName)
                            )
                        continue;
                    }
                    tableWithColumn.add(element.tableName + element.columnName)
                    val obj = CollectRequestRecord(element.columnName, element.getValue())
                    tableMap[(element.tableName)]!!.add(obj)
                } else {
                    val obj = CollectRequestRecord(element.columnName, element.getValue())
                    val tempArray = mutableListOf<CollectRequestRecord>()
                    tempArray.add(obj)
                    tableWithColumn.add(element.tableName + element.columnName)
                    tableMap[(element.tableName)] = tempArray
                }
            }

            if (additionalFields != null) {
                if (additionalFields.has("records")) {
                    if (additionalFields.get("records") !is JSONArray)
                        throw SkyflowError(
                            SkyflowErrorCode.ADDITIONAL_FIELDS_INVALID_RECORDS, tag, logLevel
                        )
                    val records = additionalFields.getJSONArray("records")
                    if (records.length() == 0) {
                        throw SkyflowError(
                            SkyflowErrorCode.ADDITIONAL_FIELDS_EMPTY_RECORDS, tag, logLevel
                        )
                    }
                    var i = 0
                    while (i < records.length()) {
                        val jsonobj = records.getJSONObject(i)
                        if (!jsonobj.has("table"))
                            throw SkyflowError(
                                SkyflowErrorCode.ADDITIONAL_FIELDS_TABLE_KEY_NOT_FOUND,
                                tag, logLevel, arrayOf("$i")
                            )
                        else if (!jsonobj.has("fields"))
                            throw SkyflowError(
                                SkyflowErrorCode.ADDITIONAL_FIELDS_FIELDS_KEY_NOT_FOUND,
                                tag, logLevel, arrayOf("$i")
                            )
                        else if (jsonobj.getJSONObject("fields").toString() == "{}")
                            throw SkyflowError(
                                SkyflowErrorCode.ADDITIONAL_FIELDS_EMPTY_FIELDS, tag, logLevel,
                                arrayOf("$i")
                            )
                        val tableName = jsonobj.get("table")
                        if (tableName !is String)
                            throw SkyflowError(
                                SkyflowErrorCode.ADDITIONAL_FIELDS_INVALID_TABLE_NAME,
                                tag, logLevel, arrayOf("$i")
                            )
                        if (tableName.isEmpty())
                            throw SkyflowError(
                                SkyflowErrorCode.ADDITIONAL_FIELDS_EMPTY_TABLE_KEY, tag, logLevel,
                                arrayOf("$i")
                            )
                        if (jsonobj.getJSONObject("fields").toString() != "{}") {
                            val fields = jsonobj.getJSONObject("fields")
                            val keys = fields.names()
                            val fieldList = mutableListOf<CollectRequestRecord>()
                            for (j in 0 until keys!!.length()) {
                                if (keys.getString(j).isEmpty()) {
                                    throw SkyflowError(
                                        SkyflowErrorCode.EMPTY_COLUMN_NAME, tag, logLevel
                                    )
                                }
                                val obj = CollectRequestRecord(
                                    keys.getString(j), fields.get(keys.getString(j))
                                )
                                fieldList.add(obj)
                            }
                            if (tableMap[tableName] != null) {
                                for (k in 0 until fieldList.size) {
                                    if (tableWithColumn.contains(tableName + fieldList[k].columnName))
                                        throw SkyflowError(
                                            SkyflowErrorCode.DUPLICATE_COLUMN_FOUND, tag, logLevel,
                                            arrayOf(tableName, fieldList[k].columnName)
                                        )
                                    else
                                        tableWithColumn.add(tableName + fieldList[k].columnName)

                                }
                                tableMap[tableName]!!.addAll(fieldList)
                            } else {
                                val tempArray = mutableListOf<CollectRequestRecord>()
                                for (k in 0 until fieldList.size) {
                                    if (tableWithColumn.contains(tableName + fieldList[k].columnName))
                                        throw SkyflowError(
                                            SkyflowErrorCode.DUPLICATE_COLUMN_FOUND,
                                            tag,
                                            logLevel,
                                            arrayOf(tableName, fieldList[k].columnName)
                                        )
                                    else
                                        tableWithColumn.add(tableName + fieldList[k].columnName)
                                }
                                tempArray.addAll(fieldList)
                                tableMap[tableName] = tempArray
                            }
                        }
                        i++
                    }
                } else
                    throw SkyflowError(
                        SkyflowErrorCode.ADDITIONAL_FIELDS_RECORDS_KEY_NOT_FOUND, tag, logLevel
                    )
            }
            val recordsArray = JSONArray()
            val requestObject = JSONObject()
            for ((key, value) in tableMap) {
                val recordObject = JSONObject()
                recordObject.put("table", key)
                val fieldsObject = JSONObject()
                for (element in value) {
                    createJSONKey(fieldsObject, element.columnName, element.value)
                }
                recordObject.put("fields", fieldsObject)
                recordsArray.put(recordObject)
            }
            requestObject.put("records", recordsArray)
            return requestObject.toString()
        }

        private fun createJSONKey(fieldsObject: JSONObject, columnName: String, value: Any) {
            val keys = columnName.split(".").toTypedArray()
            if (fieldsObject.has(keys[0])) {
                if (keys.size > 1) {
                    createJSONKey(
                        fieldsObject.get(keys[0]) as JSONObject,
                        keys.drop(1).joinToString("."),
                        value
                    )
                }
            } else {
                if (keys.size > 1) {
                    val tempObject = JSONObject()
                    fieldsObject.put(keys[0], tempObject)
                    createJSONKey(tempObject, keys.drop(1).joinToString("."), value)
                } else {
                    fieldsObject.put(keys[0], value)
                }
            }
        }
    }
}