package Skyflow

import Skyflow.core.Logger
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
        private const val TAG = "RevealResponse"

        // Strict decode: throws if the body is not a JSON object, so the SDK's success path can
        // surface a real failure (onFailure) instead of a false empty onSuccess. Non-object entries
        // in `records` are logged and skipped rather than silently dropped.
        internal fun fromJsonOrThrow(json: String, logLevel: LogLevel): RevealResponse {
            val root = JSONObject(json)
            val records = root.optJSONArray("records")?.let { arr ->
                (0 until arr.length()).mapNotNull { i ->
                    val r = arr.optJSONObject(i)
                    if (r == null) {
                        Logger.warn(TAG, "Skipping non-object entry at records[$i] in reveal response", logLevel)
                        return@mapNotNull null
                    }
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
            return RevealResponse(records)
        }

        // Public, lenient (kept for backwards compatibility): logs and returns an empty response on
        // parse failure instead of throwing. The SDK's own success path uses fromJsonOrThrow so an
        // undecodable response surfaces as onFailure rather than a false empty onSuccess.
        fun fromJson(json: String): RevealResponse {
            return try {
                fromJsonOrThrow(json, LogLevel.ERROR)
            } catch (e: Exception) {
                Logger.error(TAG, "Failed to parse reveal response: ${e.message}", LogLevel.ERROR)
                RevealResponse()
            }
        }
    }
}

/**
 * Result callback for a `reveal()` call.
 *
 * Threading: both [onSuccess] and [onFailure] are invoked on the **main (UI) thread**, so it is safe
 * to update Views directly from them. Exactly one of the two is called per `reveal()` invocation.
 */
interface RevealCallback {
    fun onSuccess(response: RevealResponse)
    fun onFailure(error: SkyflowError)
}
