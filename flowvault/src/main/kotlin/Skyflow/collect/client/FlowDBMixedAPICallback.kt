package Skyflow.collect.client

import Skyflow.Callback
import Skyflow.CollectOptions
import Skyflow.LogLevel
import Skyflow.SkyflowError
import Skyflow.core.FlowDBAPIClient
import Skyflow.utils.Utils
import org.json.JSONArray
import org.json.JSONObject

/**
 * Fans a mixed insert+update collect() into two independent HTTP calls (one per endpoint) and
 * reconciles them into EXACTLY ONE terminal callback.
 *
 * BOTH success and failure count toward completion. Once every sub-call has finished:
 *  - if any sub-call succeeded, the app gets ONE onSuccess with a consolidated {records:[...]} —
 *    the succeeded records (with their tokens) plus one error record per record of any failed call
 *    (matching FlowDB's per-record {error, httpCode, tableName?, skyflowId?} shape). A half that was
 *    already committed server-side is therefore never dropped.
 *  - if every sub-call failed, the app gets ONE onFailure carrying the first error.
 *
 * The `dispatched` guard ensures the app's callback fires exactly once. (Previously each sub-call's
 * onFailure was forwarded straight to the app with no bookkeeping: a both-fail case fired onFailure
 * twice, and a partial failure never reached completedCalls == totalCalls, silently dropping the
 * committed half.)
 */
internal class FlowDBMixedAPICallback(
    private val apiClient: FlowDBAPIClient,
    private val updateBody: JSONObject?,
    private val insertBody: JSONObject?,
    private val finalCallback: Callback,
    private val options: CollectOptions,
    val logLevel: LogLevel,
    private val cvvMap: CVVMap = CVVMap.EMPTY
) : Callback {

    private val totalCalls = (if (updateBody != null) 1 else 0) + (if (insertBody != null) 1 else 0)
    private val lock = Any()
    private var completedCalls = 0
    private var anySuccess = false
    private var firstFailure: Any? = null
    private var dispatched = false
    private val allRecords = JSONArray()

    override fun onSuccess(responseBody: Any) {
        val token = responseBody.toString()
        updateBody?.let { body ->
            FlowDBCollectAPICallback(apiClient, body, subCallbackFor(body), options, logLevel, "update", cvvMap)
                .onSuccess(token)
        }
        insertBody?.let { body ->
            FlowDBCollectAPICallback(apiClient, body, subCallbackFor(body), options, logLevel, "insert", cvvMap)
                .onSuccess(token)
        }
    }

    // Token acquisition itself failed (before any sub-call is fired) — a single terminal failure.
    override fun onFailure(exception: Any) {
        val deliver = synchronized(lock) {
            if (dispatched) false else { dispatched = true; true }
        }
        if (deliver) finalCallback.onFailure(exception)
    }

    // internal (not private) so unit tests can drive the reconciliation directly without HTTP.
    internal fun subCallbackFor(body: JSONObject): Callback = object : Callback {
        override fun onSuccess(responseBody: Any) = complete(body, responseBody, null)
        override fun onFailure(exception: Any) = complete(body, null, exception)
    }

    private fun complete(body: JSONObject, success: Any?, failure: Any?) {
        val dispatch: (() -> Unit)? = synchronized(lock) {
            if (success != null) {
                mergeInto(allRecords, JSONObject(success.toString()).optJSONArray("records"))
                anySuccess = true
            } else {
                val ex = failure ?: Utils.constructErrorResponse(500, "Unknown error")
                if (firstFailure == null) firstFailure = ex
                val error = SkyflowError.fromJson(ex.toString())
                mergeInto(allRecords, errorRecordsForBody(body, error.httpCode ?: 500, error.message ?: "Unknown error"))
            }
            completedCalls++
            when {
                completedCalls < totalCalls || dispatched -> null
                anySuccess -> {
                    dispatched = true
                    val payload = JSONObject().put("records", allRecords)
                    val action: () -> Unit = { finalCallback.onSuccess(payload) }
                    action
                }
                else -> {
                    dispatched = true
                    val err = firstFailure ?: Utils.constructErrorResponse(500, "Unknown error")
                    val action: () -> Unit = { finalCallback.onFailure(err) }
                    action
                }
            }
        }
        dispatch?.invoke()
    }

    // Represent a failed call as one error record per record it carried, so the consolidated
    // response tells the app exactly which (table[, id]) writes failed and why.
    private fun errorRecordsForBody(body: JSONObject, httpCode: Int, message: String): JSONArray {
        val out = JSONArray()
        val topTable = body.optString("tableName", "")
        val records: JSONArray? = body.optJSONArray("records")
        if (records == null || records.length() == 0) {
            val rec = JSONObject().put("error", message).put("httpCode", httpCode)
            if (topTable.isNotEmpty()) rec.put("tableName", topTable)
            out.put(rec)
            return out
        }
        for (i in 0 until records.length()) {
            val r = records.optJSONObject(i)
            val table = (r?.optString("tableName", "") ?: "").ifEmpty { topTable }
            val rec = JSONObject().put("error", message).put("httpCode", httpCode)
            if (table.isNotEmpty()) rec.put("tableName", table)
            val sid = r?.opt("skyflowID") ?: r?.opt("skyflowId")
            if (sid != null) rec.put("skyflowId", sid)
            out.put(rec)
        }
        return out
    }

    private fun mergeInto(target: JSONArray, source: JSONArray?) {
        source ?: return
        for (i in 0 until source.length()) target.put(source[i])
    }
}
