package Skyflow

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
                            val fieldsObj = r.optJSONObject("fields")
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
