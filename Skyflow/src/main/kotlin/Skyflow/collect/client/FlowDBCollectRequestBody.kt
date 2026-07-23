package Skyflow.collect.client

import Skyflow.*
import Skyflow.collect.elements.validations.ElementValueMatchRule
import org.json.JSONArray
import org.json.JSONObject

internal class FlowDBCollectRequestBody {
    companion object {
        private val tag = FlowDBCollectRequestBody::class.qualifiedName

        internal fun buildRequestBody(
            vaultID: String,
            elements: MutableList<TextField>,
            options: CollectOptions,
            logLevel: LogLevel
        ): JSONObject {
            val tableMap = groupByTable(elements, logLevel)
            val upsertByTable = options.upsert?.associateBy { it.tableName } ?: emptyMap()

            val recordsArray = JSONArray()
            for ((tableName, columns) in tableMap) {
                val dataObject = JSONObject()
                for (record in columns) {
                    createJSONKey(dataObject, record.columnName, record.value)
                }
                val recordObj = JSONObject().put("data", dataObject).put("tableName", tableName)
                val upsertOpt = upsertByTable[tableName]
                if (upsertOpt != null) {
                    recordObj.put("upsert", JSONObject()
                        .put("updateType", upsertOpt.updateType.name)
                        .put("uniqueColumns", JSONArray(upsertOpt.uniqueColumns)))
                }
                recordsArray.put(recordObj)
            }
            return JSONObject().put("vaultID", vaultID).put("records", recordsArray)
        }

        internal fun buildUpdateRequestBody(
            vaultID: String,
            tableName: String,
            elements: MutableList<TextField>,
            skyflowID: String,
            logLevel: LogLevel
        ): JSONObject {
            val tableMap = groupByTable(elements, logLevel)
            val tableElements = tableMap[tableName] ?: mutableListOf()
            val dataObject = JSONObject()
            for (record in tableElements) {
                createJSONKey(dataObject, record.columnName, record.value)
            }
            return JSONObject()
                .put("vaultID", vaultID)
                .put("tableName", tableName)
                .put("records", JSONArray().put(JSONObject().put("skyflowID", skyflowID).put("data", dataObject)))
        }

        private fun groupByTable(
            elements: MutableList<TextField>,
            logLevel: LogLevel
        ): LinkedHashMap<String, MutableList<CollectRequestRecord>> {
            val tableMap = LinkedHashMap<String, MutableList<CollectRequestRecord>>()
            val tableWithColumn = HashSet<String>()
            for (element in elements) {
                val tableName = element.tableName
                if (tableMap[tableName] != null) {
                    if (tableWithColumn.contains(tableName + element.columnName)) {
                        var hasElementValueMatchRule = false
                        for (validation in element.collectInput.validations.rules) {
                            if (validation is ElementValueMatchRule) {
                                hasElementValueMatchRule = true
                                break
                            }
                        }
                        if (!hasElementValueMatchRule)
                            throw SkyflowError(
                                SkyflowErrorCode.DUPLICATE_COLUMN_FOUND, tag, logLevel,
                                arrayOf(tableName, element.columnName)
                            )
                        continue
                    }
                    tableWithColumn.add(tableName + element.columnName)
                    tableMap[tableName]!!.add(CollectRequestRecord(element.columnName, element.getValue()))
                } else {
                    tableWithColumn.add(tableName + element.columnName)
                    tableMap[tableName] = mutableListOf(CollectRequestRecord(element.columnName, element.getValue()))
                }
            }
            return tableMap
        }

        private fun createJSONKey(obj: JSONObject, columnName: String, value: Any) {
            val keys = columnName.split(".").toTypedArray()
            if (obj.has(keys[0])) {
                if (keys.size > 1) {
                    createJSONKey(obj.get(keys[0]) as JSONObject, keys.drop(1).joinToString("."), value)
                }
            } else {
                if (keys.size > 1) {
                    val tempObject = JSONObject()
                    obj.put(keys[0], tempObject)
                    createJSONKey(tempObject, keys.drop(1).joinToString("."), value)
                } else {
                    obj.put(keys[0], value)
                }
            }
        }
    }
}
