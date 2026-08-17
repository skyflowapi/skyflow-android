package Skyflow.utils

import Skyflow.*
import Skyflow.get.GetOptions
import Skyflow.get.GetRecord
import org.json.JSONArray
import org.json.JSONObject
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Legacy (v1) request/validation helpers extracted from Utils so that core/Utils stays
 * contract-neutral (these reference v1-only InsertOptions / GetOptions / GetRecord).
 */
object LegacyUtils {
        fun constructBatchRequestBody(
            records: JSONObject,
            options: InsertOptions,
            logLevel: LogLevel
        ): JSONObject {
            val postPayload: MutableList<Any> = mutableListOf()
            val insertTokenPayload: MutableList<Any> = mutableListOf()
            if (records == {}) {
                throw SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND, Utils.tag, logLevel)
            } else if (!records.has("records")) {
                throw SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND, Utils.tag, logLevel)
            } else if (records.get("records").toString().isEmpty()) {
                throw SkyflowError(SkyflowErrorCode.EMPTY_RECORDS, Utils.tag, logLevel)
            } else if (records.get("records") !is JSONArray) {
                throw SkyflowError(SkyflowErrorCode.INVALID_RECORDS, Utils.tag, logLevel)
            } else {
                val obj1 = records.getJSONArray("records")
                var i = 0
                while (i < obj1.length()) {
                    val jsonObj = obj1.getJSONObject(i)
                    if (!jsonObj.has("table")) {
                        throw SkyflowError(
                            SkyflowErrorCode.TABLE_KEY_NOY_FOUND, Utils.tag, logLevel, arrayOf("$i")
                        )
                    } else if (jsonObj.get("table") !is String) {
                        throw SkyflowError(
                            SkyflowErrorCode.INVALID_TABLE_NAME, Utils.tag, logLevel, arrayOf("$i")
                        )
                    } else if (jsonObj.get("table").toString().isEmpty()) {
                        throw SkyflowError(
                            SkyflowErrorCode.EMPTY_TABLE_KEY, Utils.tag, logLevel, arrayOf("$i")
                        )
                    } else if (!jsonObj.has("fields")) {
                        throw SkyflowError(
                            SkyflowErrorCode.FIELDS_KEY_NOT_FOUND, Utils.tag, logLevel, arrayOf("$i")
                        )
                    } else if (jsonObj.getJSONObject("fields").toString().equals("{}")) {
                        throw SkyflowError(
                            SkyflowErrorCode.EMPTY_FIELDS, Utils.tag, logLevel, arrayOf("$i")
                        )
                    }

                    val map = HashMap<String, Any>()
                    map["tableName"] = jsonObj["table"]
                    map["fields"] = jsonObj["fields"]
                    map["method"] = "POST"
                    map["quorum"] = true
                    map["upsert"] =
                        Utils.getUpsertColumn(jsonObj.getString("table"), options.upsert, logLevel)
                    val jsonObject = jsonObj["fields"] as JSONObject
                    val keys: Iterator<String> = jsonObject.keys()

                    while (keys.hasNext()) {
                        val key = keys.next()
                        if (key.isEmpty()) {
                            throw SkyflowError(
                                SkyflowErrorCode.EMPTY_FIELD_IN_FIELDS, Utils.tag, logLevel,
                                params = arrayOf("$i")
                            )
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

        internal fun validateGetInputAndOptions(
            records: JSONObject,
            options: GetOptions?,
            logLevel: LogLevel
        ) {
            if (!records.has("records")) {
                throw SkyflowError(SkyflowErrorCode.RECORDS_KEY_NOT_FOUND, Utils.tag, logLevel)
            } else if (records.get("records").toString().isEmpty()) {
                throw SkyflowError(SkyflowErrorCode.EMPTY_RECORDS, Utils.tag, logLevel)
            } else if (records.get("records") !is JSONArray) {
                throw SkyflowError(SkyflowErrorCode.INVALID_RECORDS, Utils.tag, logLevel)
            }

            val recordsArray = records.getJSONArray("records")

            (0 until recordsArray.length()).forEach {
                val recordObject = recordsArray.getJSONObject(it)
                var hasIds = false
                var hasRedaction = false

                if (!recordObject.keys().hasNext()) {
                    throw SkyflowError(
                        SkyflowErrorCode.EMPTY_RECORD_OBJECT, Utils.tag, logLevel, arrayOf(it.toString())
                    )
                }

                // checking for table
                if (!recordObject.has("table")) {
                    throw SkyflowError(
                        SkyflowErrorCode.TABLE_KEY_NOY_FOUND, Utils.tag, logLevel, arrayOf(it.toString())
                    )
                } else if (recordObject.get("table") !is String) {
                    throw SkyflowError(
                        SkyflowErrorCode.INVALID_TABLE_NAME, Utils.tag, logLevel, arrayOf(it.toString())
                    )
                } else if (recordObject.get("table").toString().isEmpty()) {
                    throw SkyflowError(
                        SkyflowErrorCode.EMPTY_TABLE_KEY, Utils.tag, logLevel, arrayOf(it.toString())
                    )
                }

                // checking for ids
                if (recordObject.has("ids")) {
                    val ids = recordObject.get("ids")
                    if (ids !is JSONArray) {
                        throw SkyflowError(
                            SkyflowErrorCode.INVALID_IDS, Utils.tag, logLevel, arrayOf(it.toString())
                        )
                    } else if (ids.length() == 0) {
                        throw SkyflowError(
                            SkyflowErrorCode.EMPTY_RECORD_IDS, Utils.tag, logLevel, arrayOf(it.toString())
                        )
                    } else {
                        hasIds = true
                        for (i in 0 until ids.length()) {
                            if (ids[i] !is String) {
                                throw SkyflowError(
                                    SkyflowErrorCode.INVALID_ID_IN_RECORD_IDS, Utils.tag, logLevel,
                                    arrayOf(it.toString())
                                )
                            } else if (ids[i].toString().isEmpty()) {
                                throw SkyflowError(
                                    SkyflowErrorCode.EMPTY_ID_IN_RECORD_IDS, Utils.tag, logLevel,
                                    arrayOf(it.toString())
                                )
                            }
                        }
                    }
                }

                // checking for redaction
                if (recordObject.has("redaction")) hasRedaction = true

                val hasColumnName = recordObject.has("columnName")
                val hasColumnValues = recordObject.has("columnValues")

                if (options?.tokens == true && hasRedaction) {
                    throw SkyflowError(
                        SkyflowErrorCode.REDACTION_WITH_TOKENS_NOT_SUPPORTED, Utils.tag, logLevel
                    )
                } else if (options?.tokens == true && hasColumnName && hasColumnValues) {
                    throw SkyflowError(
                        SkyflowErrorCode.TOKENS_NOT_SUPPORTED_WITH_COLUMN_DETAILS, Utils.tag, logLevel
                    )
                } else if (options?.tokens == false) {
                    if (!hasRedaction) {
                        throw SkyflowError(
                            SkyflowErrorCode.REDACTION_KEY_NOT_FOUND, Utils.tag, logLevel,
                            arrayOf(it.toString())
                        )
                    } else if (recordObject.get("redaction").toString().isEmpty()) {
                        throw SkyflowError(
                            SkyflowErrorCode.EMPTY_REDACTION_VALUE, Utils.tag, logLevel,
                            arrayOf(it.toString())
                        )
                    } else if (recordObject.get("redaction") !is RedactionType) {
                        throw SkyflowError(
                            SkyflowErrorCode.INVALID_REDACTION_TYPE, Utils.tag, logLevel,
                            arrayOf(it.toString())
                        )
                    }
                }

                // checking for column name and column values
                if (!hasColumnName && hasColumnValues) {
                    throw SkyflowError(
                        SkyflowErrorCode.MISSING_RECORD_COLUMN_NAME, Utils.tag, logLevel,
                        arrayOf(it.toString())
                    )
                } else if (hasColumnName && !hasColumnValues) {
                    throw SkyflowError(
                        SkyflowErrorCode.MISSING_RECORD_COLUMN_VALUES, Utils.tag, logLevel,
                        arrayOf(it.toString())
                    )
                } else if (hasColumnName && hasColumnValues) {
                    if (hasIds) {
                        throw SkyflowError(
                            SkyflowErrorCode.BOTH_IDS_AND_COLUMN_DETAILS_SPECIFIED, Utils.tag, logLevel,
                            arrayOf(it.toString())
                        )
                    }

                    val columnName = recordObject.get("columnName")
                    val columnValues = recordObject.get("columnValues")

                    if (columnName !is String) {
                        throw SkyflowError(
                            SkyflowErrorCode.INVALID_RECORD_COLUMN_NAME_TYPE, Utils.tag, logLevel,
                            arrayOf(it.toString())
                        )
                    } else if (columnName.toString().isEmpty()) {
                        throw SkyflowError(
                            SkyflowErrorCode.EMPTY_RECORD_COLUMN_NAME, Utils.tag, logLevel,
                            arrayOf(it.toString())
                        )
                    } else if (columnValues !is JSONArray) {
                        throw SkyflowError(
                            SkyflowErrorCode.INVALID_RECORD_COLUMN_VALUES_TYPE,
                            Utils.tag, logLevel,
                            arrayOf(it.toString())
                        )
                    } else if (columnValues.length() == 0) {
                        throw SkyflowError(
                            SkyflowErrorCode.EMPTY_RECORD_COLUMN_VALUES, Utils.tag, logLevel,
                            arrayOf(it.toString())
                        )
                    } else {
                        for (i in 0 until columnValues.length()) {
                            if (columnValues[i] !is String) {
                                throw SkyflowError(
                                    SkyflowErrorCode.INVALID_COLUMN_VALUE_TYPE, Utils.tag, logLevel,
                                    arrayOf(it.toString())
                                )
                            } else if (columnValues[i].toString().isEmpty()) {
                                throw SkyflowError(
                                    SkyflowErrorCode.EMPTY_COLUMN_VALUE, Utils.tag, logLevel,
                                    arrayOf(it.toString())
                                )
                            }
                        }
                    }
                } else {
                    if (!hasIds) {
                        throw SkyflowError(
                            SkyflowErrorCode.NEITHER_IDS_NOR_COLUMN_DETAILS_SPECIFIED,
                            Utils.tag, logLevel, arrayOf(it.toString())
                        )
                    }
                }
            }
        }

        internal fun constructRequestBodyForGet(records: JSONObject): MutableList<GetRecord> {
            val requestBody = mutableListOf<GetRecord>()
            val recordsArray = records.getJSONArray("records")
            for (it in 0 until recordsArray.length()) {
                val record = recordsArray.getJSONObject(it)

                val table = record.getString("table")
                val ids = arrayListOf<String>()
                val columnValues = arrayListOf<String>()

                val redaction = if (record.has("redaction")) {
                    record.getString("redaction")
                } else null

                if (record.has("ids")) {
                    val skyflowIds = record.getJSONArray("ids")
                    for (i in 0 until skyflowIds.length()) {
                        ids.add(skyflowIds[i].toString())
                    }

                    requestBody.add(
                        GetRecord(skyflowIds = ids, table = table, redaction = redaction)
                    )
                    continue
                } else if (record.has("columnValues")) {
                    val skyflowColumnValues = record.getJSONArray("columnValues")
                    for (i in 0 until skyflowColumnValues.length()) {
                        columnValues.add(skyflowColumnValues[i].toString())
                    }
                }

                val columnName = record.getString("columnName")

                val requestRecord = GetRecord(
                    table = table,
                    redaction = redaction,
                    columnName = columnName,
                    columnValues = columnValues
                )

                requestBody.add(requestRecord)
            }
            return requestBody
        }

        fun getRequestbodyForConnection(requestBody: JSONObject, contentType: String): RequestBody {
            val mediaType = contentType.toMediaTypeOrNull()
            if (contentType.equals(ContentType.FORMURLENCODED.type)) {
                return Utils.convertJSONToQueryString(requestBody).toRequestBody(mediaType)
            } else if (contentType.equals(ContentType.FORMDATA.type)) {
                val map = Utils.r_urlencode(mutableListOf(), HashMap(), requestBody)
                val mutlipartBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                map.forEach { (key, value) ->
                    mutlipartBody.addPart(
                        Headers.headersOf("Content-Disposition", "form-data; name=\"$key\""),
                        "$value".toRequestBody(null)
                    )
                }
                return mutlipartBody.build()
            } else {
                return requestBody.toString().toRequestBody(mediaType)
            }
        }
}
