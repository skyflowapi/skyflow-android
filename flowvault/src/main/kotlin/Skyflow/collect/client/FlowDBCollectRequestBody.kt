package Skyflow.collect.client

import Skyflow.*
import Skyflow.collect.elements.validations.ElementValueMatchRule
import org.json.JSONArray
import org.json.JSONObject

internal class FlowDBCollectRequestBody {
    companion object {
        private val tag = FlowDBCollectRequestBody::class.qualifiedName

        // Validate additionalFields the same way v1 does (empty records / table / fields / column),
        // reusing the existing shared error codes. flowvault previously left these unvalidated, so a
        // malformed record produced a bad request (or wrote to tableName="") instead of a clear error.
        internal fun validateAdditionalFields(additionalFields: AdditionalFields?, logLevel: LogLevel) {
            val records = additionalFields?.records ?: return
            if (records.isEmpty()) {
                throw SkyflowInternalError(SkyflowErrorCode.ADDITIONAL_FIELDS_EMPTY_RECORDS, tag, logLevel)
            }
            records.forEachIndexed { i, rec ->
                if (rec.tableName.isEmpty())
                    throw SkyflowInternalError(SkyflowErrorCode.ADDITIONAL_FIELDS_EMPTY_TABLE_KEY, tag, logLevel, arrayOf("$i"))
                if (rec.data.isEmpty())
                    throw SkyflowInternalError(SkyflowErrorCode.ADDITIONAL_FIELDS_EMPTY_FIELDS, tag, logLevel, arrayOf("$i"))
                if (rec.data.keys.any { it.isEmpty() })
                    throw SkyflowInternalError(SkyflowErrorCode.EMPTY_COLUMN_NAME, tag, logLevel, arrayOf(rec.tableName))
            }
        }

        internal fun buildRequestBody(
            vaultID: String,
            elements: MutableList<TextField>,
            options: CollectOptions,
            logLevel: LogLevel
        ): JSONObject {
            val tableMap = groupByTable(elements, logLevel)
            val upsertByTable = options.upsert?.associateBy { it.tableName } ?: emptyMap()

            // Merge additionalFields insert records into tableMap. Reject any column that collides
            // with an element column (or another additionalFields column) in the same table — same
            // fail-fast DUPLICATE_COLUMN_FOUND behavior as v1, instead of silently dropping the value.
            val seenColumns = HashSet<String>()
            tableMap.forEach { (table, records) -> records.forEach { seenColumns.add(table + it.columnName) } }
            options.additionalFields?.records?.forEach { rec ->
                val existing = tableMap.getOrPut(rec.tableName) { mutableListOf() }
                rec.data.forEach { (k, v) ->
                    if (!seenColumns.add(rec.tableName + k)) {
                        throw SkyflowInternalError(
                            SkyflowErrorCode.DUPLICATE_COLUMN_FOUND, tag, logLevel, arrayOf(rec.tableName, k)
                        )
                    }
                    existing.add(CollectRequestRecord(k, anyToJsonValue(v)))
                }
            }

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

        // Builds ONE combined update body from update elements + additionalFields update records,
        // merging BOTH sources by (tableName, skyflowId) into a single record per record id — matching
        // v1's "${table}_${skyflowID}" merge, so the vault gets one update op per record, not two.
        // additionalFields overwrite element columns on collision (v1 last-writer-wins).
        internal fun buildCombinedUpdateBody(
            vaultID: String,
            updateElements: List<TextField>,
            additionalUpdates: List<AdditionalFieldsRecord>,
            logLevel: LogLevel
        ): JSONObject {
            val recordByKey = LinkedHashMap<Pair<String, String>, JSONObject>()

            updateElements.groupBy { it.tableName to it.skyflowId!! }.forEach { (key, elements) ->
                val (tableName, skyflowID) = key
                val rec = buildUpdateRequestBody(
                    vaultID, tableName, elements.toMutableList(), skyflowID, logLevel
                ).getJSONArray("records").getJSONObject(0).put("tableName", tableName)
                recordByKey[key] = rec
            }

            additionalUpdates.groupBy { it.tableName to it.skyflowId!! }.forEach { (key, records) ->
                val (tableName, skyflowID) = key
                val rec = recordByKey.getOrPut(key) {
                    JSONObject().put("skyflowID", skyflowID).put("data", JSONObject()).put("tableName", tableName)
                }
                val dataObj = rec.getJSONObject("data")
                records.forEach { r -> r.data.forEach { (k, v) -> dataObj.put(k, v) } }
            }

            val updateRecordsArray = JSONArray()
            recordByKey.values.forEach { updateRecordsArray.put(it) }
            return JSONObject().put("vaultID", vaultID).put("records", updateRecordsArray)
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
                            throw SkyflowInternalError(
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

        private fun anyToJsonValue(v: Any?): Any {
            if (v is Map<*, *>) {
                val obj = JSONObject()
                v.forEach { (mk, mv) -> obj.put(mk.toString(), anyToJsonValue(mv)) }
                return obj
            }
            return v ?: JSONObject.NULL
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
