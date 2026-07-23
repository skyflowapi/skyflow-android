package Skyflow.collect.client

import Skyflow.Callback
import Skyflow.CollectOptions
import Skyflow.LogLevel
import Skyflow.core.FlowDBAPIClient
import org.json.JSONArray
import org.json.JSONObject

internal class FlowDBMixedAPICallback(
    private val apiClient: FlowDBAPIClient,
    private val updateBodies: List<JSONObject>,
    private val insertBody: JSONObject?,
    private val finalCallback: Callback,
    private val options: CollectOptions,
    val logLevel: LogLevel
) : Callback {

    private val totalCalls = updateBodies.size + if (insertBody != null) 1 else 0
    private var completedCalls = 0
    private val allRecords = JSONArray()
    private val allErrors = JSONArray()

    override fun onSuccess(responseBody: Any) {
        val token = responseBody.toString()
        for (body in updateBodies) {
            FlowDBCollectAPICallback(apiClient, body, makeSubCallback(), options, logLevel, "update")
                .onSuccess(token)
        }
        insertBody?.let { body ->
            FlowDBCollectAPICallback(apiClient, body, makeSubCallback(), options, logLevel, "insert")
                .onSuccess(token)
        }
    }

    override fun onFailure(exception: Any) {
        finalCallback.onFailure(exception)
    }

    private fun makeSubCallback(): Callback = object : Callback {
        override fun onSuccess(responseBody: Any) {
            synchronized(this@FlowDBMixedAPICallback) {
                val json = JSONObject(responseBody.toString())
                mergeInto(allRecords, json.optJSONArray("records"))
                completedCalls++
                if (completedCalls == totalCalls) dispatch()
            }
        }

        override fun onFailure(exception: Any) {
            synchronized(this@FlowDBMixedAPICallback) {
                try {
                    val json = JSONObject(exception.toString())
                    mergeInto(allRecords, json.optJSONArray("records"))
                    mergeInto(allErrors, json.optJSONArray("errors"))
                } catch (e: Exception) {
                    allErrors.put(JSONObject().put("error", exception.toString()))
                }
                completedCalls++
                if (completedCalls == totalCalls) dispatch()
            }
        }
    }

    private fun mergeInto(target: JSONArray, source: JSONArray?) {
        source ?: return
        for (i in 0 until source.length()) target.put(source[i])
    }

    private fun dispatch() {
        val result = JSONObject()
        if (allRecords.length() > 0) result.put("records", allRecords)
        if (allErrors.length() > 0) result.put("errors", allErrors)
        if (allErrors.length() > 0) finalCallback.onFailure(result)
        else finalCallback.onSuccess(result)
    }
}
