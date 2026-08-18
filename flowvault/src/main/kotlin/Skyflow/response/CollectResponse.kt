package Skyflow

import org.json.JSONArray
import org.json.JSONObject

// Typed token/hashedData shapes matching the JS SDK (CollectRecordToken / CollectRecordHashedData).
// `path` carries FlowDB's nested JSON-path for nested tokenization.
data class CollectRecordToken(
    val token: String,
    val tokenGroupName: String? = null,
    val path: String? = null
)

data class CollectRecordHashedData(
    val data: String,
    val hashName: String
)

data class CollectRecord(
    val tableName: String? = null,
    val skyflowId: String? = null,
    val error: String? = null,
    val tokens: Map<String, List<CollectRecordToken>>? = null,
    val hashedData: Map<String, List<CollectRecordHashedData>>? = null,
    val httpCode: Int = 0
)

data class CollectResponse(val records: List<CollectRecord> = emptyList()) {
    fun toJson(): JSONObject {
        val arr = JSONArray()
        records.forEach { r ->
            val obj = JSONObject().put("httpCode", r.httpCode)
            r.tableName?.let { obj.put("tableName", it) }
            r.skyflowId?.let { obj.put("skyflowId", it) }
            r.error?.let { obj.put("error", it) }
            r.tokens?.let { tokens ->
                val tokObj = JSONObject()
                tokens.forEach { (col, list) ->
                    val tokenArr = JSONArray()
                    list.forEach { t ->
                        val e = JSONObject().put("token", t.token)
                        t.tokenGroupName?.let { e.put("tokenGroupName", it) }
                        t.path?.let { e.put("path", it) }
                        tokenArr.put(e)
                    }
                    tokObj.put(col, tokenArr)
                }
                obj.put("tokens", tokObj)
            }
            r.hashedData?.let { hd ->
                val hdObj = JSONObject()
                hd.forEach { (col, list) ->
                    val hdArr = JSONArray()
                    list.forEach { h -> hdArr.put(JSONObject().put("data", h.data).put("hashName", h.hashName)) }
                    hdObj.put(col, hdArr)
                }
                obj.put("hashedData", hdObj)
            }
            arr.put(obj)
        }
        return JSONObject().put("records", arr)
    }

    companion object {
        fun fromJson(json: String): CollectResponse {
            return try {
                val root = JSONObject(json)
                val records = root.optJSONArray("records")?.let { arr ->
                    (0 until arr.length()).mapNotNull { i ->
                        val r = arr.optJSONObject(i) ?: return@mapNotNull null
                        val httpCode = r.optInt("httpCode", 200)
                        if (r.has("error") && !r.isNull("error")) {
                            CollectRecord(
                                tableName = r.optString("tableName").ifEmpty { null },
                                skyflowId = if (r.isNull("skyflowId")) null else r.optString("skyflowId").ifEmpty { null },
                                error = r.optString("error"),
                                tokens = null,
                                hashedData = null,
                                httpCode = httpCode
                            )
                        } else {
                            CollectRecord(
                                tableName = r.optString("tableName").ifEmpty { null },
                                skyflowId = r.optString("skyflowId").ifEmpty { null },
                                error = null,
                                tokens = parseTokens(r.optJSONObject("tokens")),
                                hashedData = parseHashedData(r.optJSONObject("hashedData")),
                                httpCode = httpCode
                            )
                        }
                    }
                } ?: emptyList()
                CollectResponse(records)
            } catch (e: Exception) {
                CollectResponse()
            }
        }

        private fun parseTokens(obj: JSONObject?): Map<String, List<CollectRecordToken>>? = obj?.let {
            val map = mutableMapOf<String, List<CollectRecordToken>>()
            val keys = it.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val tokenArr = it.optJSONArray(key) ?: continue
                map[key] = (0 until tokenArr.length()).mapNotNull { j ->
                    val e = tokenArr.optJSONObject(j) ?: return@mapNotNull null
                    CollectRecordToken(
                        token = e.optString("token"),
                        tokenGroupName = e.optString("tokenGroupName").ifEmpty { null },
                        path = e.optString("path").ifEmpty { null }
                    )
                }
            }
            map
        }

        private fun parseHashedData(obj: JSONObject?): Map<String, List<CollectRecordHashedData>>? = obj?.let {
            val map = mutableMapOf<String, List<CollectRecordHashedData>>()
            val keys = it.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val hdArr = it.optJSONArray(key) ?: continue
                map[key] = (0 until hdArr.length()).mapNotNull { j ->
                    val e = hdArr.optJSONObject(j) ?: return@mapNotNull null
                    CollectRecordHashedData(
                        data = e.optString("data"),
                        hashName = e.optString("hashName")
                    )
                }
            }
            map
        }
    }
}

interface CollectCallback {
    fun onSuccess(response: CollectResponse)
    fun onFailure(error: SkyflowError)
}
