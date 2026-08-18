package Skyflow

import Skyflow.collect.client.CVVMap
import Skyflow.collect.client.FlowDBCollectRequestBody
import Skyflow.collect.client.FlowDBMixedAPICallback
import org.json.JSONObject
import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import Skyflow.utils.Utils
import android.content.Context
import java.util.*

// NOTE: `open class CollectContainer : ContainerProtocol` (the container marker) lives in common
// (ContainerClasses.kt) so both products share it. Only the v2 extension functions live here.

private val tag = CollectContainer::class.qualifiedName

// Public signature unchanged; the shared body lives in common (createElement).
fun Container<CollectContainer>.create(
    context: Context,
    input: CollectElementInput,
    options: CollectElementOptions = CollectElementOptions()
): TextField = createElement(context, input, options)

internal fun Container<CollectContainer>.collect(callback: Callback, options: CollectOptions? = CollectOptions()) {
    try {
        validateVaultConfig()
        Logger.info(tag, Messages.VALIDATE_COLLECT_RECORDS.getMessage(), configuration.options.logLevel)
        validateElements()
        FlowDBCollectRequestBody.validateAdditionalFields(options?.additionalFields, configuration.options.logLevel)
        post(callback, options)
    } catch (e: Exception) {
        callback.onFailure(Utils.constructErrorResponse(e))
    }
}

internal fun Container<CollectContainer>.validateVaultConfig() {
    if (configuration.vaultID.isEmpty()) {
        throw SkyflowInternalError(SkyflowErrorCode.EMPTY_VAULT_ID, tag, configuration.options.logLevel)
    }
    if (configuration.vaultURL.isEmpty()) {
        throw SkyflowInternalError(SkyflowErrorCode.EMPTY_VAULT_URL, tag, configuration.options.logLevel)
    }
    if (!Utils.checkUrl(configuration.vaultURL)) {
        throw SkyflowInternalError(SkyflowErrorCode.INVALID_VAULT_URL, tag, configuration.options.logLevel)
    }
}

// validateElements / validateElement moved to common (BaseCollectContainer.kt) — shared with v1.

internal fun Container<CollectContainer>.post(callback: Callback, options: CollectOptions?) {
    val collectOptions = options ?: CollectOptions()
    val updateElements = collectElements.filter { !it.skyflowId.isNullOrEmpty() }
    val insertElements = collectElements.filter { it.skyflowId.isNullOrEmpty() }

    val additionalUpdates = collectOptions.additionalFields?.records
        ?.filter { !it.skyflowId.isNullOrEmpty() } ?: emptyList()
    val additionalInserts = collectOptions.additionalFields?.records
        ?.filter { it.skyflowId.isNullOrEmpty() } ?: emptyList()

    if (updateElements.isNotEmpty() || additionalUpdates.isNotEmpty()) {
        // Build ONE combined update body — tableName per record so all go in a single API call
        val updateRecordsArray = org.json.JSONArray()

        updateElements.groupBy { it.skyflowId }.forEach { (_, elements) ->
            val tableName = elements.first().tableName
            val skyflowID = elements.first().skyflowId!!
            val singleBody = FlowDBCollectRequestBody.buildUpdateRequestBody(
                configuration.vaultID, tableName, elements.toMutableList(),
                skyflowID, configuration.options.logLevel
            )
            val rec = singleBody.getJSONArray("records").getJSONObject(0)
            rec.put("tableName", tableName)
            updateRecordsArray.put(rec)
        }

        additionalUpdates.groupBy { it.skyflowId }.forEach { (_, records) ->
            val merged = records.fold(mutableMapOf<String, Any>()) { acc, r -> acc.also { it.putAll(r.data) } }
            val dataObj = org.json.JSONObject().apply { merged.forEach { (k, v) -> put(k, v) } }
            updateRecordsArray.put(
                org.json.JSONObject()
                    .put("skyflowID", records.first().skyflowId!!)
                    .put("data", dataObj)
                    .put("tableName", records.first().tableName)
            )
        }

        val combinedUpdateBody = JSONObject()
            .put("vaultID", configuration.vaultID)
            .put("records", updateRecordsArray)

        val insertBody: JSONObject? = if (insertElements.isNotEmpty() || additionalInserts.isNotEmpty()) {
            val insertOptions = CollectOptions(
                upsert = collectOptions.upsert?.filter { opt -> insertElements.any { it.tableName == opt.tableName } },
                additionalFields = if (additionalInserts.isEmpty()) null else AdditionalFields(additionalInserts)
            )
            FlowDBCollectRequestBody.buildRequestBody(
                configuration.vaultID, insertElements.toMutableList(),
                insertOptions, configuration.options.logLevel
            )
        } else null

        val mixedCallback = FlowDBMixedAPICallback(
            (client as Client).apiClient, combinedUpdateBody, insertBody, callback, collectOptions,
            configuration.options.logLevel, CVVMap.capture(collectElements)
        )
        (client as Client).apiClient.getAccessToken(mixedCallback)
        return
    }

    val insertOptions = CollectOptions(
        upsert = collectOptions.upsert,
        additionalFields = if (additionalInserts.isEmpty()) null else AdditionalFields(additionalInserts)
    )
    val requestBody = FlowDBCollectRequestBody.buildRequestBody(
        configuration.vaultID,
        this.collectElements,
        insertOptions,
        configuration.options.logLevel
    )
    (this.client as Client).apiClient.post(requestBody, callback, collectOptions, cvvMap = CVVMap.capture(this.collectElements))
}

fun Container<CollectContainer>.collect(callback: CollectCallback, options: CollectOptions? = CollectOptions()) {
    val adapter = object : Callback {
        override fun onSuccess(responseBody: Any) {
            callback.onSuccess(CollectResponse.fromJson(responseBody.toString()))
        }
        override fun onFailure(exception: Any) {
            callback.onFailure(SkyflowError.fromJson(exception.toString()))
        }
    }
    collect(adapter, options)
}
