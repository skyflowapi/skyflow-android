package Skyflow

import org.json.JSONArray
import org.json.JSONObject

data class SkyflowError(
    val grpcCode: Int?,
    val httpCode: Int?,
    override val message: String?,
    val httpStatus: String?,
    val details: List<Any>?
) : Error(message) {
    companion object {
        fun fromJson(json: String): SkyflowError {
            return try {
                val root = JSONObject(json)

                // Priority 1: flat format { "grpcCode": 13, "httpCode": 500, "message": "...", ... }
                if (root.has("grpcCode") || (root.has("httpCode") && !root.has("error") && !root.has("errors"))) {
                    return SkyflowError(
                        grpcCode = if (root.has("grpcCode")) root.optInt("grpcCode") else null,
                        httpCode = if (root.has("httpCode")) root.optInt("httpCode") else null,
                        message = root.optString("message").ifEmpty { null },
                        httpStatus = root.optString("httpStatus").ifEmpty { null },
                        details = parseDetails(root.optJSONArray("details"))
                    )
                }

                // Priority 2: wrapped format { "error": { "grpcCode", "httpCode", "message", ... } }
                if (root.has("error") && root.optJSONObject("error") != null) {
                    val err = root.getJSONObject("error")
                    return SkyflowError(
                        grpcCode = if (err.has("grpcCode")) err.optInt("grpcCode") else null,
                        httpCode = err.optInt("httpCode", err.optInt("code", 500)),
                        message = err.optString("message", err.optString("description", "Unknown error")).ifEmpty { null },
                        httpStatus = err.optString("httpStatus").ifEmpty { null },
                        details = parseDetails(err.optJSONArray("details"))
                    )
                }

                // Priority 3: legacy format { "errors": [{ "error": { "code", "description" } }] }
                val errorsArr = root.optJSONArray("errors")
                if (errorsArr != null && errorsArr.length() > 0) {
                    val first = errorsArr.optJSONObject(0)
                    val err = first?.optJSONObject("error") ?: first
                    if (err != null) {
                        return SkyflowError(
                            grpcCode = null,
                            httpCode = err.optInt("code", err.optInt("httpCode", 500)),
                            message = err.optString("description", err.optString("message", "Unknown error")).ifEmpty { null },
                            httpStatus = "${err.optInt("code", 500)}",
                            details = emptyList()
                        )
                    }
                }

                SkyflowError(null, 500, json, null, emptyList())
            } catch (e: Exception) {
                SkyflowError(null, 500, e.message ?: "Unknown error", null, emptyList())
            }
        }

        private fun parseDetails(arr: JSONArray?): List<Any> {
            if (arr == null) return emptyList()
            return (0 until arr.length()).mapNotNull { arr.opt(it) }
        }
    }
}
