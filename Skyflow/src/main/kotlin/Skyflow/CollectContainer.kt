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
import com.Skyflow.core.container.ContainerProtocol
import java.util.*

open class CollectContainer : ContainerProtocol {
}

private val tag = CollectContainer::class.qualifiedName

fun Container<CollectContainer>.create(
    context: Context,
    input: CollectElementInput,
    options: CollectElementOptions = CollectElementOptions()
): TextField {
    Utils.checkInputFormatOptions(input.type, options, configuration.options.logLevel)
    Logger.info(tag, Messages.VALIDATE_INPUT_FORMAT_OPTIONS.getMessage(input.label), configuration.options.logLevel)
    Logger.info(tag, Messages.CREATED_COLLECT_ELEMENT.getMessage(input.label), configuration.options.logLevel)
    val collectElement = TextField(context, configuration.options, collectElements.size)
    collectElement.setupField(input, options)
    collectElements.add(collectElement)
    val uuid = UUID.randomUUID().toString()
    client.elementMap[uuid] = collectElement
    collectElement.uuid = uuid
    return collectElement
}

internal fun Container<CollectContainer>.collect(callback: Callback, options: CollectOptions? = CollectOptions()) {
    try {
        validateVaultConfig()
        Logger.info(tag, Messages.VALIDATE_COLLECT_RECORDS.getMessage(), configuration.options.logLevel)
        validateElements()
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

internal fun Container<CollectContainer>.validateElements() {
    var errors = ""
    for (element in this.collectElements) {
        errors = validateElement(element, errors)
    }
    if (errors != "") {
        throw SkyflowInternalError(SkyflowErrorCode.INVALID_INPUT, tag, configuration.options.logLevel, arrayOf(errors))
    }
}

internal fun Container<CollectContainer>.validateElement(element: TextField, err: String): String {
    var errorOnElement = err
    if (!element.isAttachedToWindow()) {
        throw SkyflowInternalError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, configuration.options.logLevel, arrayOf(element.columnName))
    }
    when {
        element.collectInput.tableName.equals(null) -> {
            throw SkyflowInternalError(SkyflowErrorCode.MISSING_TABLE_IN_ELEMENT, tag, configuration.options.logLevel, arrayOf(element.fieldType.toString()))
        }
        element.collectInput.column.equals(null) -> {
            throw SkyflowInternalError(SkyflowErrorCode.MISSING_COLUMN, tag, configuration.options.logLevel, arrayOf(element.fieldType.toString()))
        }
        element.collectInput.tableName!!.isEmpty() -> {
            throw SkyflowInternalError(SkyflowErrorCode.ELEMENT_EMPTY_TABLE_NAME, tag, configuration.options.logLevel, arrayOf(element.fieldType.toString()))
        }
        element.collectInput.column!!.isEmpty() -> {
            throw SkyflowInternalError(SkyflowErrorCode.EMPTY_COLUMN_NAME, tag, configuration.options.logLevel, arrayOf(element.fieldType.toString()))
        }
        else -> {
            val state = element.getState()
            val error = state["validationError"]
            if (!(state["isValid"] as Boolean)) {
                element.invalidTextField()
                errorOnElement += "for " + element.columnName + " " + (error as String) + "\n"
            }
        }
    }
    return errorOnElement
}

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
            client.apiClient, combinedUpdateBody, insertBody, callback, collectOptions,
            configuration.options.logLevel, CVVMap.capture(collectElements)
        )
        client.apiClient.getAccessToken(mixedCallback)
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
    this.client.apiClient.post(requestBody, callback, collectOptions, cvvMap = CVVMap.capture(this.collectElements))
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

internal fun Container<CollectContainer>.update(tableName: String, skyflowID: String, callback: Callback, options: CollectOptions = CollectOptions()) {
    try {
        validateVaultConfig()
        val requestBody = FlowDBCollectRequestBody.buildUpdateRequestBody(
            configuration.vaultID,
            tableName,
            this.collectElements,
            skyflowID,
            configuration.options.logLevel
        )
        this.client.apiClient.post(requestBody, callback, options, "update",
            CVVMap.captureForUpdate(this.collectElements, skyflowID))
    } catch (e: Exception) {
        callback.onFailure(Utils.constructErrorResponse(e))
    }
}
