package Skyflow.composable

import Skyflow.*
import Skyflow.collect.client.CVVMap
import Skyflow.collect.client.FlowDBCollectRequestBody
import Skyflow.collect.client.FlowDBMixedAPICallback
import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.getMessage
import Skyflow.utils.EventName
import Skyflow.utils.Utils
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import org.json.JSONObject
import java.util.*

// NOTE: `class ComposableContainer : ContainerProtocol` (the container marker) lives in common
// (composable/ComposableContainerClass.kt) so both products share it. Only the v2 extension
// functions live here.

val tag = ComposableContainer::class.qualifiedName

fun Container<ComposableContainer>.create(
    secondContext: Context,
    input: CollectElementInput,
    options: CollectElementOptions = CollectElementOptions()
): TextField {
    Utils.checkInputFormatOptions(input.type, options, configuration.options.logLevel)
    Logger.info(
        tag,
        Messages.VALIDATE_INPUT_FORMAT_OPTIONS.getMessage(input.label),
        configuration.options.logLevel
    )
    Logger.info(
        tag,
        Messages.CREATED_COLLECT_ELEMENT.getMessage(input.label),
        configuration.options.logLevel
    )

    val collectElement = TextField(
        context = context,
        optionsForLogging = configuration.options,
        index = collectElements.size,
        containerType = ContainerType.COMPOSABLE,
    )
    collectElement.setupField(input, options)
    collectElements.add(collectElement)
    val uuid = UUID.randomUUID().toString()
    client.elementMap[uuid] = collectElement
    collectElement.uuid = uuid
    return collectElement
}

fun Container<ComposableContainer>.on(eventName: EventName, handler: (() -> Unit)) {
    when (eventName) {
        EventName.SUBMIT -> {
            for (element in collectElements) {
                element.containerOnSubmitListener = handler
            }
        }
        else -> {
            Logger.error(
                tag,
                SkyflowErrorCode.INVALID_EVENT_TYPE.message,
                configuration.options.logLevel
            )
        }
    }
}

fun Container<ComposableContainer>.getComposableLayout(): LinearLayout {
    if (collectElements.size != totalComposableElements) {
        throw SkyflowInternalError(
            SkyflowErrorCode.MISMATCH_ELEMENT_COUNT_LAYOUT_SUM,
            tag,
            configuration.options.logLevel
        )
    }
    addViewsToComposableLayout()
    return composableLayout
}

internal fun Container<ComposableContainer>.collect(
    callback: Callback,
    options: CollectOptions? = CollectOptions()
) {
    try {
        validateVaultConfig()
        Logger.info(
            tag,
            Messages.VALIDATE_COLLECT_RECORDS.getMessage(),
            configuration.options.logLevel
        )
        validateElements()
        post(callback, options)
    } catch (e: Exception) {
        callback.onFailure(Utils.constructErrorResponse(e))
    }
}

fun Container<ComposableContainer>.collect(callback: CollectCallback, options: CollectOptions? = CollectOptions()) {
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

private fun Container<ComposableContainer>.validateVaultConfig() {
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

private fun Container<ComposableContainer>.validateElements() {
    var errors = ""
    for (element in this.collectElements) {
        errors = validateElement(element, errors)
    }
    if (errors != "") {
        throw SkyflowInternalError(
            SkyflowErrorCode.INVALID_INPUT,
            tag, configuration.options.logLevel, arrayOf(errors)
        )
    }
}

private fun Container<ComposableContainer>.validateElement(
    element: TextField,
    err: String
): String {
    var errorOnElement = err
    if (!element.isAttachedToWindow) {
        throw SkyflowInternalError(
            SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
            tag,
            configuration.options.logLevel,
            arrayOf(element.columnName)
        )
    }
    when {
        element.collectInput.tableName.equals(null) -> {
            throw SkyflowInternalError(
                SkyflowErrorCode.MISSING_TABLE_IN_ELEMENT,
                tag,
                configuration.options.logLevel,
                arrayOf(element.fieldType.toString())
            )
        }
        element.collectInput.column.equals(null) -> {
            throw SkyflowInternalError(
                SkyflowErrorCode.MISSING_COLUMN,
                tag,
                configuration.options.logLevel,
                arrayOf(element.fieldType.toString())
            )
        }
        element.collectInput.tableName!!.isEmpty() -> {
            throw SkyflowInternalError(
                SkyflowErrorCode.ELEMENT_EMPTY_TABLE_NAME,
                tag,
                configuration.options.logLevel,
                arrayOf(element.fieldType.toString())
            )
        }
        element.collectInput.column!!.isEmpty() -> {
            throw SkyflowInternalError(
                SkyflowErrorCode.EMPTY_COLUMN_NAME,
                tag,
                configuration.options.logLevel,
                arrayOf(element.fieldType.toString())
            )
        }
        else -> {
            val state = element.getState()
            val error = state["validationError"]
            if (!(state["isValid"] as Boolean)) {
                element.invalidTextField()
                errorOnElement += "for ${element.columnName} ${(error as String)}\n"
            }
        }
    }
    return errorOnElement
}

private fun Container<ComposableContainer>.post(callback: Callback, options: CollectOptions?) {
    val collectOptions = options ?: CollectOptions()
    val updateElements = collectElements.filter { !it.skyflowId.isNullOrEmpty() }
    val insertElements = collectElements.filter { it.skyflowId.isNullOrEmpty() }

    val additionalUpdates = collectOptions.additionalFields?.records
        ?.filter { !it.skyflowId.isNullOrEmpty() } ?: emptyList()
    val additionalInserts = collectOptions.additionalFields?.records
        ?.filter { it.skyflowId.isNullOrEmpty() } ?: emptyList()

    if (updateElements.isNotEmpty() || additionalUpdates.isNotEmpty()) {
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

internal fun Container<ComposableContainer>.update(tableName: String, skyflowID: String, callback: Callback, options: CollectOptions = CollectOptions()) {
    try {
        validateVaultConfig()
        val requestBody = FlowDBCollectRequestBody.buildUpdateRequestBody(
            configuration.vaultID, tableName, this.collectElements, skyflowID,
            configuration.options.logLevel
        )
        (this.client as Client).apiClient.post(requestBody, callback, options, "update",
            CVVMap.captureForUpdate(this.collectElements, skyflowID))
    } catch (e: Exception) {
        callback.onFailure(Utils.constructErrorResponse(e))
    }
}

private fun Container<ComposableContainer>.addViewsToComposableLayout() {

    val lp = LinearLayout.LayoutParams(
        options.styles!!.base.width,
        options.styles!!.base.height
    )
    var k = 0
    for (i in options.layout.indices) {
        val padding = options.styles!!.base.padding
        val margin = options.styles!!.base.margin
        val composableRow = LinearLayout(context, null, 0)
        composableRow.orientation = LinearLayout.HORIZONTAL
        composableRow.layoutParams = lp
        lp.setMargins(margin.left, margin.top, margin.right, margin.bottom)
        composableRow.background = getBackgroundDrawable(true)
        composableRow.setPadding(padding.left, padding.top, padding.right, padding.bottom)

        val errorList = ComposableErrorsList(options.layout[i])
        val commonErrorText = TextView(context)
        applyStylesToErrorText(commonErrorText)

        for (j in 0 until options.layout[i]) {
            val element = collectElements[k++]

            element.applyCallback(ComposableEvents.ON_FOCUS_IS_TRUE) {
                errorList.setError(j, String())
                commonErrorText.text = errorList.getErrors()
                commonErrorText.visibility = if (errorList.isEmpty()) View.INVISIBLE
                else View.VISIBLE
            }

            element.applyCallback(ComposableEvents.ON_BEGIN_EDITING) {
                if (element.index + 1 < this.totalComposableElements) {
                    val state = element.getState()
                    if (state.getBoolean("isValid") &&
                        !state.getBoolean("isEmpty") &&
                        SkyflowElementType.getAutoFocusSupportedElements()
                            .contains(state.get("elementType")) &&
                        (!state.get("elementType").equals(SkyflowElementType.EXPIRATION_MONTH)
                                || element.inputField.text.toString() != "1")
                    ) collectElements[element.index + 1].requestFocus()
                }
            }

            element.applyCallback(ComposableEvents.ON_END_EDITING) {
                errorList.setError(j, element.error.text.toString())
                commonErrorText.text = errorList.getErrors()
                commonErrorText.visibility = if (errorList.isEmpty()) View.INVISIBLE
                else View.VISIBLE
            }

            val elementWidth = element.collectInput.inputStyles.base.width
            val elementHeight = element.collectInput.inputStyles.base.height
            val elementLP = LinearLayout.LayoutParams(elementWidth, elementHeight)
            element.layoutParams = elementLP
            composableRow.addView(element)
        }

        composableLayout.addView(composableRow)
        composableLayout.addView(commonErrorText)
    }
}

private fun Container<ComposableContainer>.applyStylesToErrorText(errorText: TextView) {
    errorText.visibility = View.INVISIBLE
    val baseErrorTextStyles = options.errorTextStyles!!.base

    val errorMargin = baseErrorTextStyles.margin
    val lp = LinearLayout.LayoutParams(baseErrorTextStyles.width, baseErrorTextStyles.height)
    lp.setMargins(errorMargin.left, errorMargin.top, errorMargin.right, errorMargin.bottom)
    errorText.layoutParams = lp

    errorText.background = getBackgroundDrawable(false)

    val errorPadding = baseErrorTextStyles.padding
    errorText.setPadding(
        errorPadding.left,
        errorPadding.top,
        errorPadding.right,
        errorPadding.bottom
    )

    errorText.setTextColor(baseErrorTextStyles.textColor)
    if (baseErrorTextStyles.font != Typeface.NORMAL) {
        errorText.typeface = ResourcesCompat.getFont(context, baseErrorTextStyles.font)
    }
    errorText.gravity = baseErrorTextStyles.textAlignment
}

private fun Container<ComposableContainer>.getBackgroundDrawable(row: Boolean): Drawable {
    val border = GradientDrawable()
    border.setColor(Color.WHITE)
    val borderStyles = if (row) options.styles!!.base else options.errorTextStyles!!.base
    border.setStroke(borderStyles.borderWidth, borderStyles.borderColor)
    border.cornerRadius = borderStyles.cornerRadius
    return border
}
