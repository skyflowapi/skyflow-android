package Skyflow.collect.client

import Skyflow.*
import Skyflow.core.APIClient
import org.json.JSONArray
import org.json.JSONObject

internal class MixedAPICallback(
    private val apiClient: APIClient,
    private val insertRecords: JSONObject?,
    private val updateRecords: MutableList<UpdateRequestRecord>,
    private val finalCallback: Skyflow.Callback,
    private val options: InsertOptions,
    val logLevel: LogLevel
) : Skyflow.Callback {
    private var insertResponse: JSONObject? = null
    private var updateResponse: JSONObject? = null
    private var insertCompleted = false
    private var updateCompleted = false
    private var hasInsertRecords = insertRecords != null && 
                                    insertRecords.has("records") && 
                                    insertRecords.getJSONArray("records").length() > 0
    private var hasUpdateRecords = updateRecords.isNotEmpty()

    init {
        if (!hasInsertRecords) {
            insertCompleted = true
        }
        if (!hasUpdateRecords) {
            updateCompleted = true
        }
    }

    fun executeInsertAndUpdate(token: String) {
        // Execute insert if there are insert records
        if (hasInsertRecords) {
            val collectCallback = CollectAPICallback(
                apiClient,
                insertRecords!!,
                object : Skyflow.Callback {
                    override fun onSuccess(responseBody: Any) {
                        synchronized(this@MixedAPICallback) {
                            insertResponse = responseBody as JSONObject
                            insertCompleted = true
                            checkIfBothCompleted()
                        }
                    }

                    override fun onFailure(exception: Any) {
                        synchronized(this@MixedAPICallback) {
                            // exception is now a JSONObject with errors (and possibly records)
                            insertResponse = exception as JSONObject
                            insertCompleted = true
                            checkIfBothCompleted()
                        }
                    }
                },
                options,
                logLevel
            )
            collectCallback.onSuccess(token)
        }

        // Execute update if there are update records
        if (hasUpdateRecords) {
            val updateCallback = UpdateAPICallback(
                apiClient,
                updateRecords,
                object : Skyflow.Callback {
                    override fun onSuccess(responseBody: Any) {
                        synchronized(this@MixedAPICallback) {
                            updateResponse = responseBody as JSONObject
                            updateCompleted = true
                            checkIfBothCompleted()
                        }
                    }

                    override fun onFailure(exception: Any) {
                        synchronized(this@MixedAPICallback) {
                            // exception is now a JSONObject with errors (and possibly records)
                            updateResponse = exception as JSONObject
                            updateCompleted = true
                            checkIfBothCompleted()
                        }
                    }
                },
                options,
                logLevel
            )
            updateCallback.onSuccess(token)
        }
    }

    private fun checkIfBothCompleted() {
        if (insertCompleted && updateCompleted) {
            // Merge responses
            val mergedResponse = JSONObject()
            val recordsArray = JSONArray()
            val errorsArray = JSONArray()

            // Add insert records
            if (insertResponse != null && insertResponse!!.has("records")) {
                val insertRecordsArray = insertResponse!!.getJSONArray("records")
                for (i in 0 until insertRecordsArray.length()) {
                    recordsArray.put(insertRecordsArray.get(i))
                }
            }

            // Add insert errors
            if (insertResponse != null && insertResponse!!.has("errors")) {
                val insertErrorsArray = insertResponse!!.getJSONArray("errors")
                for (i in 0 until insertErrorsArray.length()) {
                    errorsArray.put(insertErrorsArray.get(i))
                }
            }

            // Add update records
            if (updateResponse != null && updateResponse!!.has("records")) {
                val updateRecordsArray = updateResponse!!.getJSONArray("records")
                for (i in 0 until updateRecordsArray.length()) {
                    recordsArray.put(updateRecordsArray.get(i))
                }
            }

            // Add update errors
            if (updateResponse != null && updateResponse!!.has("errors")) {
                val updateErrorsArray = updateResponse!!.getJSONArray("errors")
                for (i in 0 until updateErrorsArray.length()) {
                    errorsArray.put(updateErrorsArray.get(i))
                }
            }

            // Add successful records if any
            if (recordsArray.length() > 0) {
                mergedResponse.put("records", recordsArray)
            }
            
            // Add errors if any
            if (errorsArray.length() > 0) {
                mergedResponse.put("errors", errorsArray)
            }
            
            // If any operations failed, call onFailure with the JSON response (like JS SDK)
            if (errorsArray.length() > 0) {
                finalCallback.onFailure(mergedResponse)
                return
            }
            
            // Only return success if ALL operations succeeded (no errors)
            finalCallback.onSuccess(mergedResponse)
        }
    }

    override fun onSuccess(responseBody: Any) {
        // Not used directly
    }

    override fun onFailure(exception: Any) {
        finalCallback.onFailure(exception)
    }
}
