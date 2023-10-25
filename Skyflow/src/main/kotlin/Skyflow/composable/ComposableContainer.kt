package Skyflow.composable

import Skyflow.*
import Skyflow.collect.client.CollectRequestBody
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
import com.Skyflow.core.container.ContainerProtocol
import org.json.JSONObject
import java.util.*

class ComposableContainer : ContainerProtocol {
}

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
        throw SkyflowError(
            SkyflowErrorCode.MISMATCH_ELEMENT_COUNT_LAYOUT_SUM,
            tag,
            configuration.options.logLevel
        )
    }
    addViewsToComposableLayout()
    return composableLayout
}

fun Container<ComposableContainer>.collect(
    callback: Callback,
    options: CollectOptions? = CollectOptions()
) {
    try {
        Utils.checkVaultDetails(client.configuration)
        Logger.info(
            tag,
            Messages.VALIDATE_COLLECT_RECORDS.getMessage(),
            configuration.options.logLevel
        )
        validateElements()
        post(callback, options)
    } catch (e: Exception) {
        callback.onFailure(e)
    }
}

private fun Container<ComposableContainer>.validateElements() {
    var errors = ""
    for (element in this.collectElements) {
        errors = validateElement(element, errors)
    }
    if (errors != "") {
        throw SkyflowError(
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
        throw SkyflowError(
            SkyflowErrorCode.ELEMENT_NOT_MOUNTED,
            tag,
            configuration.options.logLevel,
            arrayOf(element.columnName)
        )
    }
    when {
        element.collectInput.table.equals(null) -> {
            throw SkyflowError(
                SkyflowErrorCode.MISSING_TABLE_IN_ELEMENT,
                tag,
                configuration.options.logLevel,
                arrayOf(element.fieldType.toString())
            )
        }
        element.collectInput.column.equals(null) -> {
            throw SkyflowError(
                SkyflowErrorCode.MISSING_COLUMN,
                tag,
                configuration.options.logLevel,
                arrayOf(element.fieldType.toString())
            )
        }
        element.collectInput.table!!.isEmpty() -> {
            throw SkyflowError(
                SkyflowErrorCode.ELEMENT_EMPTY_TABLE_NAME,
                tag,
                configuration.options.logLevel,
                arrayOf(element.fieldType.toString())
            )
        }
        element.collectInput.column!!.isEmpty() -> {
            throw SkyflowError(
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
    val records = CollectRequestBody.createRequestBody(
        this.collectElements,
        options!!.additionalFields,
        configuration.options.logLevel
    )
    val insertOptions = InsertOptions(options.token, options.upsert)
    this.client.apiClient.post(JSONObject(records), callback, insertOptions)
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