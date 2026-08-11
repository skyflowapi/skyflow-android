package Skyflow

import org.json.JSONArray
import org.json.JSONObject

data class CollectRecord(
    val tableName: String? = null,
    val skyflowId: String? = null,
    val error: String? = null,
    val tokens: Map<String, Any?>? = null,
    val hashedData: Map<String, Any?>? = null,
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
                tokens.forEach { (col, v) ->
                    when (v) {
                        is List<*> -> {
                            val tokenArr = JSONArray()
                            v.filterIsInstance<Map<*, *>>().forEach { entry ->
                                val e = JSONObject()
                                entry.forEach { (k, ev) -> e.put(k.toString(), ev) }
                                tokenArr.put(e)
                            }
                            tokObj.put(col, tokenArr)
                        }
                        else -> tokObj.put(col, v)
                    }
                }
                obj.put("tokens", tokObj)
            }
            r.hashedData?.let { hd ->
                val hdObj = JSONObject()
                hd.forEach { (k, v) -> hdObj.put(k, v) }
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
                            val fieldsObj = r.optJSONObject("tokens")
                            val tokens: Map<String, Any?>? = fieldsObj?.let { obj ->
                                val map = mutableMapOf<String, Any?>()
                                val keys = obj.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    val arr2 = obj.optJSONArray(key)
                                    if (arr2 != null) {
                                        map[key] = (0 until arr2.length()).mapNotNull { j ->
                                            val e = arr2.optJSONObject(j) ?: return@mapNotNull null
                                            e.keys().asSequence().associateWith { k -> e.opt(k) }
                                        }
                                    } else {
                                        map[key] = obj.opt(key)
                                    }
                                }
                                map
                            }
                            val hashedDataObj = r.optJSONObject("hashedData")
                            val hashedData: Map<String, Any?>? = hashedDataObj?.let { obj ->
                                val map = mutableMapOf<String, Any?>()
                                val keys = obj.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    val arr2 = obj.optJSONArray(key)
                                    if (arr2 != null) {
                                        map[key] = (0 until arr2.length()).mapNotNull { j ->
                                            val e = arr2.optJSONObject(j) ?: return@mapNotNull null
                                            e.keys().asSequence().associateWith { k -> e.opt(k) }
                                        }
                                    } else {
                                        map[key] = obj.opt(key)
                                    }
                                }
                                map
                            }
                            CollectRecord(
                                tableName = r.optString("tableName").ifEmpty { null },
                                skyflowId = r.optString("skyflowId").ifEmpty { null },
                                error = null,
                                tokens = tokens,
                                hashedData = hashedData,
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
    }
}

interface CollectCallback {
    fun onSuccess(response: CollectResponse)
    fun onFailure(error: SkyflowError)
}
