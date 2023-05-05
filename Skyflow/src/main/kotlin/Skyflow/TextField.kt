package Skyflow

import Skyflow.collect.elements.utils.*
import Skyflow.collect.elements.validations.SkyflowValidateYear
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import Skyflow.core.elements.state.StateforText
import Skyflow.utils.EventName
import android.graphics.Typeface
import android.text.Spanned
import androidx.core.text.isDigitsOnly
import com.skyflow_android.R
import org.json.JSONObject
import kotlin.String
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.util.Log
import com.Skyflow.collect.elements.validations.*
import com.Skyflow.collect.elements.validations.SkyflowValidator
import com.Skyflow.collect.elements.validations.SkyflowValidateExpireDate
import java.util.*


@Suppress("DEPRECATION")
class TextField @JvmOverloads constructor(
    context: Context,
    val optionsForLogging: Options,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
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
    internal var expiryDateFormat = "mm/yy"
    internal var yearFormat = "yy"
    private var userError: String = ""
    private val tag = TextField::class.qualifiedName

    private var isFormatting = false

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
            builtinValidationError = "value is empty\n"
            setErrorText("value is required")
            return builtinValidationError
        }
        builtinValidationError += SkyflowValidator.validate(str, validationRules)
        if (builtinValidationError == "") {
            return if (collectInput.validations.rules.isEmpty())
                ""
            else {
                val customValidationError = SkyflowValidator.validate(str, collectInput.validations)
                if (customValidationError == "")
                    ""
                else {
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

    override fun setupField(
        collectInput: CollectElementInput,
        options: CollectElementOptions
    ) {
        super.setupField(collectInput, options)
        this.state = StateforText(this)
        validationRules = fieldType.getType().validation
        padding = collectInput.inputStyles.base.padding
        state = StateforText(this)
        this.collectInput = collectInput

        buildTextField()
        buildError()
        buildLabel()
    }

    private fun buildLabel() {
        label.text = collectInput.label
        val labelPadding = collectInput.labelStyles.base.padding
        label.textSize = 16F
        label.setPadding(
            labelPadding.left,
            labelPadding.top,
            labelPadding.right,
            labelPadding.bottom
        )
        label.setTextColor(collectInput.labelStyles.base.textColor)
        if (collectInput.labelStyles.base.font != Typeface.NORMAL)
            label.typeface = ResourcesCompat.getFont(context, collectInput.labelStyles.base.font)
        label.gravity = collectInput.labelStyles.base.textAlignment

    }

    private fun buildTextField() {
        state = StateforText(this)
        border.setColor(Color.WHITE)
        border.setStroke(
            collectInput.inputStyles.base.borderWidth,
            collectInput.inputStyles.base.borderColor
        )
        border.cornerRadius = collectInput.inputStyles.base.cornerRadius
        inputField.setBackgroundDrawable(border)
        inputField.setPadding(padding.left, padding.top, padding.right, padding.bottom)
        inputField.gravity = collectInput.inputStyles.base.textAlignment
        inputField.hint = collectInput.placeholder
        inputField.setTextColor(collectInput.inputStyles.base.textColor)
        inputField.inputType = fieldType.getType().keyboardType

        if (collectInput.inputStyles.base.font != Typeface.NORMAL)
            inputField.typeface =
                ResourcesCompat.getFont(context, collectInput.inputStyles.base.font)

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

    private fun changeExpireDateValidations() {
        validationRules.rules.clear()
        val expireDateList = mutableListOf("mm/yy", "mm/yyyy", "yy/mm", "yyyy/mm")
        if (expireDateList.contains(options.format.toLowerCase())) {
            expiryDateFormat = options.format.toLowerCase()
            validationRules.add(SkyflowValidateExpireDate(format = expiryDateFormat))

        } else {
            Log.w(tag, "invalid format for EXPIRATION_DATE")
            Log.w(tag, "Using default format mm/yy for EXPIRATION_DATE")
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
            Log.w(tag, "invalid format for EXPIRATION_YEAR")
            Log.w(tag, "Using default format yy for EXPIRATION_YEAR")
            validationRules.add(SkyflowValidateYear(format = yearFormat))
        }
    }


    private fun buildError() {
        error.visibility = View.INVISIBLE
        val errorPadding = collectInput.errorTextStyles.base.padding
        error.setPadding(
            errorPadding.left,
            errorPadding.top,
            errorPadding.right,
            errorPadding.bottom
        )
        mErrorAnimator = AnimationUtils.loadAnimation(context, R.anim.error_animation)
        error.setTextColor(collectInput.errorTextStyles.base.textColor)
        if (collectInput.errorTextStyles.base.font != Typeface.NORMAL)
            error.typeface =
                ResourcesCompat.getFont(context, collectInput.errorTextStyles.base.font)
        error.gravity = collectInput.errorTextStyles.base.textAlignment
    }

    public fun on(eventName: EventName, handler: (state: JSONObject) -> Unit) {
        when (eventName) {
            EventName.CHANGE -> this.userOnchangeListener = handler
            EventName.READY -> this.userOnReadyListener = handler
            EventName.BLUR -> this.userOnBlurListener = handler
            EventName.FOCUS -> this.userOnFocusListener = handler
        }
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        super.setOrientation(VERTICAL)
        setListenersForText()
        if (userOnReadyListener !== null)
            userOnReadyListener?.let { it((state as StateforText).getState(optionsForLogging.env)) }
        addView(label)
        addView(inputField)
        addView(error)
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
                if (userOnchangeListener !== null)
                    userOnchangeListener?.let {
                        it(
                            (state as StateforText).getState(
                                optionsForLogging.env
                            )
                        )
                    }
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

    }

    private fun changeCardIcon(cardtype: CardType) {
        inputField.setCompoundDrawablesRelativeWithIntrinsicBounds(cardtype.image, 0, 0, 0)
        inputField.compoundDrawablePadding = 8
    }

    private fun onFocusTextField() {
        val labelPadding = collectInput.labelStyles.focus.padding
        label.setPadding(
            labelPadding.left,
            labelPadding.top,
            labelPadding.right,
            labelPadding.bottom
        )
        label.setTextColor(collectInput.labelStyles.focus.textColor)
        if (collectInput.labelStyles.focus.font != Typeface.NORMAL)
            label.typeface = ResourcesCompat.getFont(context, collectInput.labelStyles.focus.font)
        label.gravity = collectInput.labelStyles.focus.textAlignment

        val inputFieldPadding = collectInput.inputStyles.focus.padding
        inputField.setPadding(
            inputFieldPadding.left,
            inputFieldPadding.top,
            inputFieldPadding.right,
            inputFieldPadding.bottom
        )
        inputField.setTextColor(collectInput.inputStyles.focus.textColor)
        border.setStroke(
            collectInput.inputStyles.focus.borderWidth,
            collectInput.inputStyles.focus.borderColor
        )
        border.cornerRadius = collectInput.inputStyles.focus.cornerRadius
        inputField.setBackgroundDrawable(border)
        inputField.gravity = collectInput.inputStyles.focus.textAlignment
        if (collectInput.inputStyles.focus.font != Typeface.NORMAL)
            inputField.typeface =
                ResourcesCompat.getFont(context, collectInput.inputStyles.focus.font)
        error.visibility = View.INVISIBLE
        if (userOnFocusListener !== null)
            userOnFocusListener?.let { it((state as StateforText).getState(optionsForLogging.env)) }
    }

    private fun onBlurTextField() {

        val labelPadding = collectInput.labelStyles.base.padding
        label.textSize = 16F
        label.setPadding(
            labelPadding.left,
            labelPadding.top,
            labelPadding.right,
            labelPadding.bottom
        )
        label.setTextColor(collectInput.labelStyles.base.textColor)
        if (collectInput.labelStyles.base.font != Typeface.NORMAL)
            label.typeface = ResourcesCompat.getFont(context, collectInput.labelStyles.base.font)
        label.gravity = collectInput.labelStyles.base.textAlignment

        val internalState = this.state.getInternalState()
        if (internalState["isEmpty"] as Boolean && !isRequired) {
            emptyTextField()
        } else if (!(internalState["isValid"] as Boolean)) {
            invalidTextField()
        } else {
            validTextField()
        }
        if (userOnBlurListener !== null)
            userOnBlurListener?.let { it((state as StateforText).getState(optionsForLogging.env)) }
    }

    private fun validTextField() {
        error.visibility = INVISIBLE
        val inputFieldPadding = collectInput.inputStyles.complete.padding
        inputField.setPadding(
            inputFieldPadding.left,
            inputFieldPadding.top,
            inputFieldPadding.right,
            inputFieldPadding.bottom
        )
        inputField.setTextColor(collectInput.inputStyles.complete.textColor)
        border.setStroke(
            collectInput.inputStyles.complete.borderWidth,
            collectInput.inputStyles.complete.borderColor
        )
        border.cornerRadius = collectInput.inputStyles.complete.cornerRadius
        inputField.setBackgroundDrawable(border)
        inputField.gravity = collectInput.inputStyles.complete.textAlignment
        if (collectInput.inputStyles.complete.font != Typeface.NORMAL)
            inputField.typeface =
                ResourcesCompat.getFont(context, collectInput.inputStyles.complete.font)
    }

    internal fun invalidTextField() {
        val inputFieldPadding = collectInput.inputStyles.invalid.padding
        inputField.setPadding(
            inputFieldPadding.left,
            inputFieldPadding.top,
            inputFieldPadding.right,
            inputFieldPadding.bottom
        )
        inputField.setTextColor(collectInput.inputStyles.invalid.textColor)
        border.setStroke(
            collectInput.inputStyles.invalid.borderWidth,
            collectInput.inputStyles.invalid.borderColor
        )
        border.cornerRadius = collectInput.inputStyles.invalid.cornerRadius
        inputField.setBackgroundDrawable(border)
        inputField.gravity = collectInput.inputStyles.invalid.textAlignment
        if (collectInput.inputStyles.invalid.font != Typeface.NORMAL)
            inputField.typeface =
                ResourcesCompat.getFont(context, collectInput.inputStyles.invalid.font)
        VibrationHelper.vibrate(context, 10)
        error.visibility = View.VISIBLE
        startAnimation(mErrorAnimator)
    }

    private fun emptyTextField() {
        val inputFieldPadding = collectInput.inputStyles.empty.padding
        inputField.setPadding(
            inputFieldPadding.left,
            inputFieldPadding.top,
            inputFieldPadding.right,
            inputFieldPadding.bottom
        )
        inputField.setTextColor(collectInput.inputStyles.empty.textColor)
        border.setStroke(
            collectInput.inputStyles.empty.borderWidth,
            collectInput.inputStyles.empty.borderColor
        )
        border.cornerRadius = collectInput.inputStyles.empty.cornerRadius
        inputField.setBackgroundDrawable(border)
        inputField.gravity = collectInput.inputStyles.empty.textAlignment
        if (collectInput.inputStyles.empty.font != Typeface.NORMAL)
            inputField.typeface =
                ResourcesCompat.getFont(context, collectInput.inputStyles.empty.font)
    }

    var previousLength = 0
    private fun addSlashspanToExpiryDate(editable: Editable?, expiryDateFormat: String) {

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

//    private fun formatPatternForField(s: Editable?) {
//        if (fieldType.equals(SkyflowElementType.CARD_NUMBER)) {
//            val cardtype =
//                CardType.forCardNumber(inputField.text.toString().replace(" ", "").replace("-", ""))
//            if (options.enableCardIcon)
//                changeCardIcon(cardtype)
//            val filterArray = arrayOfNulls<InputFilter>(1)
//            filterArray[0] = LengthFilter(cardtype.cardLength.get(cardtype.cardLength.size - 1))
//            inputField.setFilters(filterArray)
//            addSeparatorToCardNumber(s, cardtype.formatPattern)
//        } else if (fieldType.equals(SkyflowElementType.EXPIRATION_DATE)) {
//            addSlashspanToExpiryDate(s, expiryDateFormat)
//        } else if (fieldType.equals(SkyflowElementType.CVV)) {
//            inputField.filters = arrayOf<InputFilter>(LengthFilter(4))
//        } else if (fieldType.equals(SkyflowElementType.EXPIRATION_MONTH)) {
//            addSpanToExpiryMonth()
//        } else if (fieldType.equals(SkyflowElementType.EXPIRATION_YEAR)) {
//            inputField.filters = arrayOf<InputFilter>(LengthFilter(yearFormat.length))
//        }
//    }

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
                addSlashspanToExpiryDate(s, expiryDateFormat)
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
                Log.d("ACTUAL VALUE", actualValue)
            }

            else -> {}
        }
    }

    private fun formatInput(): String {
        Log.d("INPUT FIELD TEXT", inputField.text.toString())
        var output = String()
        var index = 0 // maintains format index
        for (inputChar in inputField.text.toString()) {
            Log.d("INPUT CHAR", inputChar.toString())
            Log.d("FORMAT LENGTH", "${options.format.length} $index")
            if (index < options.format.length) {
                var formatChar = options.format[index]
                Log.d("FORMAT CHAR", formatChar.toString())
                if (options.inputFormat.containsKey(formatChar)) {
                    val regex = options.inputFormat[formatChar]
                    if (regex!!.matches(inputChar.toString())) {
                        output = output.plus(inputChar)
                        Log.d("OUTPUT REGEX MATCH", output)
                        index++
                    }
                } else if (formatChar == inputChar) {
                    output = output.plus(inputChar)
                    Log.d("FORMAT INPUT MATCH", output)
                    index++
                } else {
                    for (k in index until options.format.length) {
                        formatChar = options.format[k]
                        if (options.inputFormat.containsKey(formatChar)) {
                            val regex = options.inputFormat[formatChar]
                            if (regex!!.matches(inputChar.toString())) {
                                output = output.plus(inputChar)
                                Log.d("OUTPUT REGEX MATCH", output)
                                index++
                            }
                            break
                        } else {
                            output = output.plus(formatChar)
                            Log.d("ELSE ELSE ELSE", output)
                            index++
                        }
                    }
                }
            } else break
        }

        Log.d("OUTPUT", output)
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
            Log.w(tag, "setValue can be called only in dev mode")
        }
    }

    fun clearValue() {
        if (optionsForLogging.env == Env.DEV) {
            actualValue = ""
            setText("")
        } else {
            Log.w(tag, "clearValue can be called only in dev mode")
        }
    }

}