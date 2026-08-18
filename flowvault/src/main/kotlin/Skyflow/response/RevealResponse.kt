package Skyflow

import org.json.JSONArray
import org.json.JSONObject

data class RevealRecordMetadata(
    val tableName: String? = null,
    val skyflowId: String? = null
)

data class RevealRecord(
    val token: String,
    val error: String? = null,
    val tokenGroupName: String? = null,
    val metadata: RevealRecordMetadata? = null,
    val httpCode: Int = 0
)

data class RevealResponse(val records: List<RevealRecord> = emptyList()) {
    fun toJson(): JSONObject {
        val arr = JSONArray()
        records.forEach { r ->
            val obj = JSONObject().put("token", r.token).put("httpCode", r.httpCode)
            r.tokenGroupName?.let { obj.put("tokenGroupName", it) }
            r.error?.let { obj.put("error", it) }
            r.metadata?.let { m ->
                val meta = JSONObject()
                m.tableName?.let { meta.put("tableName", it) }
                m.skyflowId?.let { meta.put("skyflowId", it) }
                obj.put("metadata", meta)
            }
            arr.put(obj)
        }
        return JSONObject().put("records", arr)
    }

    companion object {
        fun fromJson(json: String): RevealResponse {
            return try {
                val root = JSONObject(json)
                val records = root.optJSONArray("records")?.let { arr ->
                    (0 until arr.length()).mapNotNull { i ->
                        val r = arr.optJSONObject(i) ?: return@mapNotNull null
                        val httpCode = r.optInt("httpCode", 200)
                        if (r.has("error") && !r.isNull("error")) {
                            RevealRecord(
                                token = r.optString("token"),
                                error = r.optString("error"),
                                tokenGroupName = null,
                                metadata = null,
                                httpCode = httpCode
                            )
                        } else {
                            val metaObj = r.optJSONObject("metadata")
                            val metadata: RevealRecordMetadata? = metaObj?.let { obj ->
                                RevealRecordMetadata(
                                    tableName = obj.optString("tableName").ifEmpty { null },
                                    skyflowId = obj.optString("skyflowId").ifEmpty { null }
                                        ?: obj.optString("skyflowID").ifEmpty { null }
                                )
                            }
                            RevealRecord(
                                token = r.optString("token"),
                                error = null,
                                tokenGroupName = r.optString("tokenGroupName").ifEmpty { null },
                                metadata = metadata,
                                httpCode = httpCode
                            )
                        }
                    }
                } ?: emptyList()
                RevealResponse(records)
            } catch (e: Exception) {
                RevealResponse()
            }
        }
    }
}

interface RevealCallback {
    fun onSuccess(response: RevealResponse)
    fun onFailure(error: SkyflowError)
}
