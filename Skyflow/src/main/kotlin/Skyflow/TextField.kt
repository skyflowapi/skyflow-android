package Skyflow

import Skyflow.collect.elements.utils.*
import Skyflow.collect.elements.validations.SkyflowValidateYear
import Skyflow.composable.ComposableEvents
import Skyflow.core.Logger
import Skyflow.core.Messages
import Skyflow.core.elements.state.StateforText
import Skyflow.utils.EventName
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.*
import android.text.InputFilter.LengthFilter
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.isDigitsOnly
import com.Skyflow.collect.elements.validations.*
import com.skyflow_android.R
import org.json.JSONObject
import java.util.*
import kotlin.reflect.KClassifier

@Suppress("DEPRECATION")
@SuppressLint("ClickableViewAccessibility")
class TextField @JvmOverloads constructor(
    context: Context,
    val optionsForLogging: Options,
    val index: Int,
    val containerType: KClassifier? = ContainerType.COLLECT,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Element(context, attrs, defStyleAttr), BaseElement {

    internal var label = TextView(context)
    internal var inputField = EditText(context)
    internal var error = TextView(context)
    private var validationRules = ValidationSet()
    override lateinit var state: State
    private var border = GradientDrawable()
    private lateinit var padding: Padding
    private var mErrorAnimator: Animation? = null
    internal var actualValue: String = ""
    internal var userOnchangeListener: ((JSONObject) -> Unit)? = null
    internal var userOnFocusListener: ((JSONObject) -> Unit)? = null
    internal var userOnBlurListener: ((JSONObject) -> Unit)? = null
    internal var userOnReadyListener: ((JSONObject) -> Unit)? = null
    internal var containerOnSubmitListener: (() -> Unit)? = null
    internal var onFocusIsTrue: (() -> Unit)? = null
    internal var onBeginEditing: (() -> Unit)? = null
    internal var onEndEditing: (() -> Unit)? = null
    internal var expiryDateFormat = "mm/yy"
    internal var yearFormat = "yy"
    private var userError: String = ""
    private val tag = TextField::class.qualifiedName

    private var isFormatting = false

    private lateinit var inputFieldLP: LayoutParams
    private lateinit var labelLP: LayoutParams
    private lateinit var errorLP: LayoutParams

    internal fun applyCallback(eventName: ComposableEvents, handler: (() -> Unit)) {
        when (eventName) {
            ComposableEvents.ON_FOCUS_IS_TRUE -> this.onFocusIsTrue = handler
            ComposableEvents.ON_END_EDITING -> this.onEndEditing = handler
            ComposableEvents.ON_BEGIN_EDITING -> this.onBeginEditing = handler
        }
    }

    private var drawableRight = 0
    private var drawableLeft = 0

    override var uuid = ""
    override fun getValue(): String {
        return actualValue
    }

    override fun validate(): SkyflowValidationError {
        val str = getValue()
        if (userError.isNotEmpty()) {
            return userError
        }
        var builtinValidationError = ""
        if (isRequired && str.isEmpty()) {
            builtinValidationError = "value is empty"
            setErrorText("value is required")
            return builtinValidationError
        }
        builtinValidationError += SkyflowValidator.validate(str, validationRules)
        if (builtinValidationError == "") {
            return if (collectInput.validations.rules.isEmpty()) {
                setErrorText("")
                ""
            } else {
                val customValidationError = SkyflowValidator.validate(str, collectInput.validations)
                if (customValidationError == "") {
                    setErrorText("")
                    ""
                } else {
                    setErrorText(customValidationError)
                    customValidationError
                }
            }
        } else {
            if (collectInput.label.isEmpty())
                setErrorText("invalid field")
            else
                setErrorText("invalid " + collectInput.label)
            return builtinValidationError
        }
    }

    private fun appendIcon(iconName: String) {
        var drawableIcon = 0
        val copyIcon = R.drawable.ic_copy
        val copiedIcon = R.drawable.ic_copied

        when (iconName) {
            "COPY" -> {
                drawableIcon = copyIcon
            }
            "COPIED" -> {
                drawableIcon = copiedIcon
                Handler(Looper.getMainLooper()).postDelayed({
                    inputField.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        drawableLeft,
                        0,
                        copyIcon,
                        0
                    )
                    drawableIcon = copyIcon
                }, 2000) // 2000 milliseconds = 2 seconds
            }
        }
        inputField.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, 0, drawableIcon, 0)
        inputField.compoundDrawablePadding = 8
        drawableRight = drawableIcon
    }

    private fun removeIcon() {
        inputField.setCompoundDrawablesRelativeWithIntrinsicBounds(drawableLeft, 0, 0, 0)
    }

    private fun performCopyAction(event: MotionEvent): Boolean {
        val actionX = event.rawX
        if (drawableRight != 0) {
            val wBound = inputField.right - inputField.compoundDrawables[2].bounds.width()
            if (actionX >= wBound && actionX <= inputField.right) {
                handleTap()
                return true
            }
        }
        return true
    }

    private fun handleTap() {
        val textToCopy = this.actualValue
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("CustomCopyTextView", textToCopy)
        clipboard.setPrimaryClip(clip)
        VibrationHelper.vibrate(context, 10)
        appendIcon("COPIED")
    }

    override fun setupField(collectInput: CollectElementInput, options: CollectElementOptions) {
        super.setupField(collectInput, options)
        this.state = StateforText(this)
        validationRules = fieldType.getType().validation
        padding = collectInput.inputStyles.base.padding
        state = StateforText(this)
        this.collectInput = collectInput
        buildLabel()
        buildTextField()
        buildError()
    }

    private fun buildLabel() {
        val spannableString = SpannableStringBuilder(collectInput.label)
        if (isRequired && spannableString.isNotEmpty()) {
            spannableString.append(" *")
            val colorSpan = ForegroundColorSpan(collectInput.labelStyles.requiredAsterisk.textColor)
            val start = spannableString.length - 1
            val end = spannableString.length
            spannableString.setSpan(colorSpan, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        }
        label.text = spannableString
        applyLabelStyle(collectInput.labelStyles.base)
    }

    private fun buildTextField() {
        applyInputStyle(collectInput.inputStyles.base)
        inputField.hint = collectInput.placeholder
        inputField.inputType = fieldType.getType().keyboardType
        state = StateforText(this)
        when (fieldType) {
            SkyflowElementType.EXPIRATION_DATE -> {
                changeExpireDateValidations()
            }
            SkyflowElementType.EXPIRATION_YEAR -> {
                changeYearValidations()
            }
            else -> {}
        }
        formatPatternForField(inputField.editableText)
    }

    private fun buildError() {
        applyErrorTextStyle(collectInput.errorTextStyles.base)
        error.visibility = View.INVISIBLE
        mErrorAnimator = AnimationUtils.loadAnimation(context, R.anim.error_animation)
    }

    private fun changeExpireDateValidations() {
        validationRules.rules.clear()
        val expireDateList = mutableListOf("mm/yy", "mm/yyyy", "yy/mm", "yyyy/mm")
        if (expireDateList.contains(options.format.toLowerCase())) {
            expiryDateFormat = options.format.toLowerCase()
            validationRules.add(SkyflowValidateExpireDate(format = expiryDateFormat))
        } else {
            Logger.warn(tag, "invalid format for EXPIRATION_DATE", optionsForLogging.logLevel)
            Logger.warn(
                tag,
                "Using default format mm/yy for EXPIRATION_DATE",
                optionsForLogging.logLevel
            )
            validationRules.add(SkyflowValidateExpireDate(format = expiryDateFormat))
        }
    }

    private fun changeYearValidations() {
        validationRules.rules.clear()
        val yearList = mutableListOf("yy", "yyyy")
        if (yearList.contains(options.format.toLowerCase())) {
            yearFormat = options.format.toLowerCase()
            validationRules.add(SkyflowValidateYear(format = yearFormat))
        } else {
            Logger.warn(tag, "invalid format for EXPIRATION_YEAR", optionsForLogging.logLevel)
            Logger.warn(
                tag,
                "Using default format yy for EXPIRATION_YEAR",
                optionsForLogging.logLevel
            )
            validationRules.add(SkyflowValidateYear(format = yearFormat))
        }
    }

    fun on(eventName: EventName, handler: (state: JSONObject) -> Unit) {
        when (eventName) {
            EventName.CHANGE -> this.userOnchangeListener = handler
            EventName.READY -> this.userOnReadyListener = handler
            EventName.BLUR -> this.userOnBlurListener = handler
            EventName.FOCUS -> this.userOnFocusListener = handler
            EventName.SUBMIT -> {
                Logger.error(tag, Messages.INVALID_EVENT_TYPE.message, optionsForLogging.logLevel)
            }
        }
    }

    fun update(updateCollectInput: CollectElementInput) {
        this.collectInput.table = updateCollectInput.table
        this.collectInput.column = updateCollectInput.column
        this.collectInput.label = updateCollectInput.label
        this.collectInput.placeholder = updateCollectInput.placeholder
        this.collectInput.validations = updateCollectInput.validations
        this.collectInput.inputStyles = updateCollectInput.inputStyles
        this.collectInput.labelStyles = updateCollectInput.labelStyles
        this.collectInput.errorTextStyles = updateCollectInput.errorTextStyles
        this.setupField(this.collectInput, this.options)
    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        super.setOrientation(VERTICAL)
        setListenersForText()
        if (userOnReadyListener !== null)
            userOnReadyListener?.let { it((state as StateforText).getState(optionsForLogging.env)) }
        if (label.text.isNotEmpty() || containerType != ContainerType.COMPOSABLE) {
            addView(label)
        }
        addView(inputField)
        if (containerType != ContainerType.COMPOSABLE) {
            addView(error)
        }

//        error.visibility = INVISIBLE
        if (userError.isNotEmpty()) {
            invalidTextField()
        }
    }

    private fun setListenersForText() {
        //when text changes
        inputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) {
                    return
                }
                actualValue = inputField.text.toString()
                formatPatternForField(s)
                state = StateforText(this@TextField)
                if (state.getInternalState().getBoolean("isValid")) {
                    if (options.enableCopy) {
                        appendIcon("COPY")
                    }
                } else if (options.enableCopy) {
                    removeIcon()
                }

                if (userOnchangeListener !== null) {
                    userOnchangeListener?.let {
                        it((state as StateforText).getState(optionsForLogging.env))
                    }
                }
                onBeginEditing?.invoke()
            }

        })
        //when focus of text changes
        OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                onFocusTextField()
            } else {
                onBlurTextField()
                if (fieldType.equals(SkyflowElementType.EXPIRATION_MONTH)) {
                    val str = inputField.text.toString()
                    if (str.equals("1")) {
                        inputField.setText("0" + str.get(0).toString())
                    }
                }
            }
        }.also { inputField.onFocusChangeListener = it }

        // listens for enter key press if composable container
        inputField.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                // Handle the Enter key press
                this.containerOnSubmitListener?.invoke()
            }
            false
        }

        inputField.setOnTouchListener { _, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    performCopyAction(event)
                }
            }
            false
        }
    }

    private fun changeCardIcon(cardType: CardType) {
        inputField.setCompoundDrawablesRelativeWithIntrinsicBounds(
            cardType.image,
            0,
            drawableRight,
            0
        )
        inputField.compoundDrawablePadding = 8
        drawableLeft = cardType.image
    }

    private fun applyLabelStyle(style: Style) {
        labelLP = LayoutParams(style.width, style.height)
        val labelMargin = style.margin
        labelLP.setMargins(labelMargin.left, labelMargin.top, labelMargin.right, labelMargin.bottom)
        val padding = style.padding
        label.setPadding(padding.left, padding.top, padding.right, padding.bottom)
        label.textSize = 16F
        label.setTextColor(style.textColor)
        if (style.font != Typeface.NORMAL)
            label.typeface = ResourcesCompat.getFont(context, style.font)
        label.gravity = style.textAlignment
        label.minWidth = style.minWidth
        label.maxWidth = style.maxWidth
        label.minHeight = style.minHeight
        if (style.maxHeight != null)
            label.maxHeight = style.maxHeight!!
        label.layoutParams = labelLP
        label.requestLayout()
    }

    private fun applyInputStyle(style: Style) {
        inputFieldLP = LayoutParams(style.width, style.height)
        val margin = style.margin
        inputFieldLP.setMargins(margin.left, margin.top, margin.right, margin.bottom)
        val padding = style.padding
        inputField.setPadding(padding.left, padding.top, padding.right, padding.bottom)
        inputField.setTextColor(style.textColor)
        inputField.setHintTextColor(style.placeholderColor)
        border.setColor(style.backgroundColor)
        border.setStroke(style.borderWidth, style.borderColor)
        border.cornerRadius = style.cornerRadius
        inputField.setBackgroundDrawable(border)
        if (style.font != Typeface.NORMAL)
            inputField.typeface = ResourcesCompat.getFont(context, style.font)
        inputField.gravity = style.textAlignment
        inputField.minWidth = style.minWidth
        inputField.maxWidth = style.maxWidth
        inputField.minHeight = style.minHeight
        if (style.maxHeight != null)
            inputField.maxHeight = style.maxHeight!!
        inputField.layoutParams = inputFieldLP
        inputField.requestLayout()
    }

    private fun applyErrorTextStyle(style: Style) {
        errorLP = LayoutParams(style.width, style.height)
        val margin = style.margin
        errorLP.setMargins(margin.left, margin.top, margin.right, margin.bottom)
        val padding = style.padding
        error.setPadding(padding.left, padding.top, padding.right, padding.bottom)
        error.setTextColor(style.textColor)
        if (style.font != Typeface.NORMAL)
            error.typeface = ResourcesCompat.getFont(context, style.font)
        error.gravity = style.textAlignment
        error.minWidth = style.minWidth
        error.maxWidth = style.maxWidth
        error.minHeight = style.minHeight
        if (style.maxHeight != null)
            error.maxHeight = style.maxHeight!!
        error.layoutParams = errorLP
        error.requestLayout()
    }

    private fun onFocusTextField() {
        onFocusIsTrue?.invoke()
        applyLabelStyle(collectInput.labelStyles.focus)
        applyInputStyle(collectInput.inputStyles.focus)
        error.visibility = View.INVISIBLE
        if (userOnFocusListener !== null)
            userOnFocusListener?.let { it((state as StateforText).getState(optionsForLogging.env)) }
    }

    private fun onBlurTextField() {
        applyLabelStyle(collectInput.labelStyles.base)
        val internalState = this.state.getInternalState()
        if (internalState["isEmpty"] as Boolean && !isRequired) {
            emptyTextField()
        } else if (!(internalState["isValid"] as Boolean)) {
            invalidTextField()
        } else {
            error.visibility = INVISIBLE
            validTextField()
        }
        if (userOnBlurListener !== null)
            userOnBlurListener?.let { it((state as StateforText).getState(optionsForLogging.env)) }
    }

    private fun emptyTextField() {
        applyInputStyle(collectInput.inputStyles.empty)
    }

    internal fun invalidTextField() {
        onEndEditing?.invoke()
        applyInputStyle(collectInput.inputStyles.invalid)
        VibrationHelper.vibrate(context, 10)
        error.visibility = View.VISIBLE
        startAnimation(mErrorAnimator)
    }

    private fun validTextField() {
        applyInputStyle(collectInput.inputStyles.complete)
    }

    private var previousLength = 0
    private fun addSlashSpanToExpiryDate(editable: Editable?, expiryDateFormat: String) {

        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = LengthFilter(expiryDateFormat.length)
        inputField.setFilters(filterArray)
        val index = expiryDateFormat.indexOf("/")
        val length = editable!!.length
        if (index == length && previousLength < length) {
            if (inputField.text.isDigitsOnly()) {
                inputField.append("/")
                previousLength = length + 1
            }
        } else if (index == length && previousLength > length) {
            inputField.setText(inputField.text.toString().subSequence(0, inputField.length() - 1))
            previousLength = length - 1
            inputField.setSelection(inputField.length())
        }
    }

    private fun addSeparatorToCardNumber(
        editable: Editable?,
        spaceIndices: IntArray,
        separator: Char
    ) {
        val length = editable!!.length

        for (index in spaceIndices) {
            if (index <= length) {
                editable.setSpan(
                    Spacespan(separator.toString()), index - 1, index,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }

    private fun addSpanToExpiryMonth() {
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = LengthFilter(yearFormat.length)
        val str = inputField.text.toString()
        inputField.setFilters(filterArray)
        if (str.isNotEmpty() && str.toInt() > 1 && !str.get(0).toString()
                .equals("0") && str.length < 2
        ) {
            inputField.setText("0" + str)
        }
        inputField.setSelection(inputField.length())
    }

    private fun formatPatternForField(s: Editable?) {
        when (fieldType) {
            SkyflowElementType.CARD_NUMBER -> {
                val cardType = CardType.forCardNumber(inputField.text.toString())
                val separator = options.parseFormatForSeparator()

                if (options.enableCardIcon) {
                    changeCardIcon(cardType)
                }

                val filterArray = arrayOfNulls<InputFilter>(1)
                filterArray[0] = LengthFilter(cardType.cardLength[cardType.cardLength.size - 1])
                inputField.filters = filterArray
                addSeparatorToCardNumber(s, cardType.formatPattern, separator)
            }

            SkyflowElementType.CVV -> {
                inputField.filters = arrayOf<InputFilter>(LengthFilter(4))
            }

            SkyflowElementType.EXPIRATION_DATE -> {
                addSlashSpanToExpiryDate(s, expiryDateFormat)
            }

            SkyflowElementType.EXPIRATION_MONTH -> {
                addSpanToExpiryMonth()
            }

            SkyflowElementType.EXPIRATION_YEAR -> {
                inputField.filters = arrayOf<InputFilter>(LengthFilter(yearFormat.length))
            }

            SkyflowElementType.INPUT_FIELD -> {
                if (options.format.isNotEmpty()) {
                    val formattedInput = formatInput()
                    isFormatting = true
                    inputField.setText(formattedInput)
                    inputField.setSelection(formattedInput.length)
                    actualValue = formattedInput
                    isFormatting = false
                }
            }

            else -> {}
        }
    }

    private fun formatInput(): String {
        var output = String()
        var index = 0 // maintains format index
        for (inputChar in inputField.text.toString()) {
            if (index < options.format.length) {
                var formatChar = options.format[index]
                if (options.inputFormat.containsKey(formatChar)) {
                    val regex = options.inputFormat[formatChar]
                    if (regex!!.matches(inputChar.toString())) {
                        output = output.plus(inputChar)
                        index++
                    }
                } else if (formatChar == inputChar) {
                    output = output.plus(inputChar)
                    index++
                } else {
                    for (k in index until options.format.length) {
                        formatChar = options.format[k]
                        if (options.inputFormat.containsKey(formatChar)) {
                            val regex = options.inputFormat[formatChar]
                            if (regex!!.matches(inputChar.toString())) {
                                output = output.plus(inputChar)
                                index++
                            }
                            break
                        } else {
                            output = output.plus(formatChar)
                            index++
                        }
                    }
                }
            } else break
        }

        return output
    }

    internal fun setErrorText(error: String) {
        this.error.text = error
    }

    fun unmount() {
        buildTextField()
        buildLabel()
        error.visibility = View.INVISIBLE
        actualValue = ""
        setText("")
        onFocusIsTrue?.invoke()
    }

    override fun setError(error: String) {
        this.userError = error
        setErrorText(userError)
        state = StateforText(this@TextField)
    }

    override fun resetError() {
        this.userError = ""
        state = StateforText(this)
        val internalState = state.getInternalState()
        setErrorText(internalState["validationError"].toString())
        validTextField()
        onEndEditing?.invoke()
    }

    internal fun setText(value: String) {
        actualValue = value
        this.inputField.setText(value)
    }

    internal fun getErrorText(): String {
        return error.text.toString()
    }

    fun setValue(value: String) {
        if (optionsForLogging.env == Env.DEV) {
            actualValue = value
            setText(value)
        } else {
            Logger.warn(tag, "setValue can be called only in dev mode", optionsForLogging.logLevel)
        }
    }

    fun clearValue() {
        if (optionsForLogging.env == Env.DEV) {
            actualValue = ""
            setText("")
        } else {
            Logger.warn(
                tag,
                "clearValue can be called only in dev mode",
                optionsForLogging.logLevel
            )
        }
    }

}