package Skyflow.collect.client

import Skyflow.Callback
import Skyflow.CollectOptions
import Skyflow.LogLevel
import Skyflow.core.FlowDBAPIClient
import org.json.JSONArray
import org.json.JSONObject

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
    private var completedCalls = 0
    private val allRecords = JSONArray()

    override fun onSuccess(responseBody: Any) {
        val token = responseBody.toString()
        updateBody?.let { body ->
            FlowDBCollectAPICallback(apiClient, body, makeSubCallback(), options, logLevel, "update", cvvMap)
                .onSuccess(token)
        }
        insertBody?.let { body ->
            FlowDBCollectAPICallback(apiClient, body, makeSubCallback(), options, logLevel, "insert", cvvMap)
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
            finalCallback.onFailure(exception)
        }
    }

    private fun mergeInto(target: JSONArray, source: JSONArray?) {
        source ?: return
        for (i in 0 until source.length()) target.put(source[i])
    }

    private fun dispatch() {
        finalCallback.onSuccess(JSONObject().put("records", allRecords))
    }
}
