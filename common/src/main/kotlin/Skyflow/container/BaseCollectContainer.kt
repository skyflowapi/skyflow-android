package Skyflow

import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import Skyflow.utils.Utils
import android.content.Context
import java.util.*

private val tag = CollectContainer::class.qualifiedName

internal fun Container<CollectContainer>.createElement(
    context: Context,
    input: BaseCollectElementInput,
    options: CollectElementOptions
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
