package Skyflow

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

fun Container<CollectContainer>.collect(callback: Callback, options: CollectOptions? = CollectOptions()) {
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
        throw SkyflowError(SkyflowErrorCode.EMPTY_VAULT_ID, tag, configuration.options.logLevel)
    }
    if (configuration.vaultURL.isEmpty()) {
        throw SkyflowError(SkyflowErrorCode.EMPTY_VAULT_URL, tag, configuration.options.logLevel)
    }
    if (!Utils.checkUrl(configuration.vaultURL)) {
        throw SkyflowError(SkyflowErrorCode.INVALID_VAULT_URL, tag, configuration.options.logLevel)
    }
}

internal fun Container<CollectContainer>.validateElements() {
    var errors = ""
    for (element in this.collectElements) {
        errors = validateElement(element, errors)
    }
    if (errors != "") {
        throw SkyflowError(SkyflowErrorCode.INVALID_INPUT, tag, configuration.options.logLevel, arrayOf(errors))
    }
}

internal fun Container<CollectContainer>.validateElement(element: TextField, err: String): String {
    var errorOnElement = err
    if (!element.isAttachedToWindow()) {
        throw SkyflowError(SkyflowErrorCode.ELEMENT_NOT_MOUNTED, tag, configuration.options.logLevel, arrayOf(element.columnName))
    }
    when {
        element.collectInput.table.equals(null) -> {
            throw SkyflowError(SkyflowErrorCode.MISSING_TABLE_IN_ELEMENT, tag, configuration.options.logLevel, arrayOf(element.fieldType.toString()))
        }
        element.collectInput.column.equals(null) -> {
            throw SkyflowError(SkyflowErrorCode.MISSING_COLUMN, tag, configuration.options.logLevel, arrayOf(element.fieldType.toString()))
        }
        element.collectInput.table!!.isEmpty() -> {
            throw SkyflowError(SkyflowErrorCode.ELEMENT_EMPTY_TABLE_NAME, tag, configuration.options.logLevel, arrayOf(element.fieldType.toString()))
        }
        element.collectInput.column!!.isEmpty() -> {
            throw SkyflowError(SkyflowErrorCode.EMPTY_COLUMN_NAME, tag, configuration.options.logLevel, arrayOf(element.fieldType.toString()))
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
    val skyflowIds = collectOptions.skyflowIds

    if (!skyflowIds.isNullOrEmpty()) {
        val updateElements = collectElements.filter { skyflowIds.containsKey(it.tableName) }
        val insertElements = collectElements.filter { !skyflowIds.containsKey(it.tableName) }

        // One update request body per table (API has tableName at root)
        val updateBodies = updateElements.groupBy { it.tableName }.map { (tableName, elements) ->
            FlowDBCollectRequestBody.buildUpdateRequestBody(
                configuration.vaultID, tableName, elements.toMutableList(),
                skyflowIds[tableName]!!, configuration.options.logLevel
            )
        }

        // One insert/upsert body for the remaining elements (if any)
        val insertBody: JSONObject? = if (insertElements.isNotEmpty()) {
            val insertOptions = CollectOptions(
                tokens = collectOptions.tokens,
                upsert = collectOptions.upsert?.filter { !skyflowIds.containsKey(it.tableName) }
            )
            FlowDBCollectRequestBody.buildRequestBody(
                configuration.vaultID, insertElements.toMutableList(),
                insertOptions, configuration.options.logLevel
            )
        } else null

        val mixedCallback = FlowDBMixedAPICallback(
            client.apiClient, updateBodies, insertBody, callback, collectOptions,
            configuration.options.logLevel
        )
        client.apiClient.getAccessToken(mixedCallback)
        return
    }

    val requestBody = FlowDBCollectRequestBody.buildRequestBody(
        configuration.vaultID,
        this.collectElements,
        collectOptions,
        configuration.options.logLevel
    )
    this.client.apiClient.post(requestBody, callback, collectOptions)
}

fun Container<CollectContainer>.update(tableName: String, skyflowID: String, callback: Callback, options: CollectOptions = CollectOptions()) {
    try {
        validateVaultConfig()
        val requestBody = FlowDBCollectRequestBody.buildUpdateRequestBody(
            configuration.vaultID,
            tableName,
            this.collectElements,
            skyflowID,
            configuration.options.logLevel
        )
        this.client.apiClient.post(requestBody, callback, options, "update")
    } catch (e: Exception) {
        callback.onFailure(Utils.constructErrorResponse(e))
    }
}
