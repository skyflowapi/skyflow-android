package Skyflow

import Skyflow.collect.elements.utils.*
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
import com.Skyflow.collect.elements.validations.SkyflowValidationError
import com.Skyflow.collect.elements.validations.SkyflowValidationSet
import com.Skyflow.collect.elements.validations.SkyflowValidator
import Skyflow.core.elements.state.StateforText
import Skyflow.utils.EventName
import android.graphics.Typeface
import com.skyflow_android.R
import org.json.JSONObject
import kotlin.String


@Suppress("DEPRECATION")
class TextField @JvmOverloads constructor(
    context: Context,
    val optionsForLogging: Options,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : Skyflow.Element(context, attrs, defStyleAttr) {

    internal var label = TextView(context)
    internal var inputField = EditText(context)
    internal var error = TextView(context)
    private var validationRules = SkyflowValidationSet()
    override lateinit var state: State
    private var border = GradientDrawable()
    private lateinit var padding: Padding
    private var mErrorAnimator: Animation? = null
    internal var actualValue: String = ""
    internal var userOnchangeListener: ((JSONObject) -> Unit)? = null
    private var userOnFocusListener: ((JSONObject) -> Unit)? = null
    private var userOnBlurListener: ((JSONObject) -> Unit)? = null
    private var userOnReadyListener: ((JSONObject) -> Unit)? = null
    private var userError : String = ""
    override fun getValue() : String {
        return actualValue
    }

    override fun validate() : MutableList<SkyflowValidationError> {
        val str = inputField.text.toString()
        if(userError.isNotEmpty()){
            return mutableListOf(userError)
        }
        val builtinValidations = SkyflowValidator.validate(str,validationRules)
        if(builtinValidations.isEmpty())
        {
            return if(collectInput.validations.rules.isEmpty())
                mutableListOf()
            else {
                val customValidations = SkyflowValidator.validate(str,collectInput.validations)
                if(customValidations.isEmpty())
                    mutableListOf()
                else {
                    setError(customValidations[0])
                    customValidations
                }
            }
        }
        else
        {
            if(collectInput.label.isEmpty())
                setError("invalid field")
            else
                setError("invalid "+ collectInput.label)
            return builtinValidations
        }
    }

    override fun setupField(collectInput: CollectElementInput, options: Skyflow.CollectElementOptions) {
        super.setupField(collectInput,options)
        this.state = StateforText(this)
        validationRules = fieldType.getType().validation
//        if(collectInput.validations.rules.isNotEmpty())
//            validationRules.add(collectInput.validations.rules)
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
        label.setPadding(labelPadding.left,labelPadding.top,labelPadding.right,labelPadding.bottom)
        label.setTextColor(collectInput.labelStyles.base.textColor)
        if(!collectInput.labelStyles.base.font.equals(Typeface.NORMAL))
            label.typeface = ResourcesCompat.getFont(context,collectInput.labelStyles.base.font)
        label.gravity = collectInput.labelStyles.base.textAlignment

    }

    private fun buildTextField()
    {
        if(collectInput.altText.isNotEmpty() || collectInput.altText != "")
        {
            inputField.setText(collectInput.altText)
        }
        else
            inputField.setText("")
        state = StateforText(this)
        border.setColor(Color.WHITE)
        border.setStroke(collectInput.inputStyles.base.borderWidth,collectInput.inputStyles.base.borderColor)
        border.cornerRadius = collectInput.inputStyles.base.cornerRadius
        inputField.setBackgroundDrawable(border)
        inputField.setPadding(padding.left,padding.top,padding.right,padding.bottom)
        inputField.gravity = collectInput.inputStyles.base.textAlignment
        inputField.hint = collectInput.placeholder
        inputField.setTextColor(collectInput.inputStyles.base.textColor)
        if(collectInput.inputStyles.base.font != Typeface.NORMAL)
            inputField.typeface = ResourcesCompat.getFont(context,collectInput.inputStyles.base.font)

        changeCardIcon()
        inputField.inputType = fieldType.getType().keyboardType
    }

    private fun buildError()
    {
        error.visibility = View.INVISIBLE
        val errorPadding = collectInput.errorTextStyles.base.padding
        error.setPadding(errorPadding.left,errorPadding.top,errorPadding.right,errorPadding.bottom)
        mErrorAnimator = AnimationUtils.loadAnimation(context, R.anim.error_animation)
        error.setTextColor(collectInput.errorTextStyles.base.textColor)
        if(collectInput.errorTextStyles.base.font != Typeface.NORMAL)
            error.typeface = ResourcesCompat.getFont(context,collectInput.errorTextStyles.base.font)
        error.gravity = collectInput.errorTextStyles.base.textAlignment
    }

    public fun on(eventName: EventName, handler: (state:JSONObject) -> Unit) {
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
        if(userOnReadyListener !== null)
            userOnReadyListener?.let{it((state as StateforText).getState(optionsForLogging.env))}
        addView(label)
        addView(inputField)
        addView(error)
    }

    private fun setListenersForText() {
        //when text changes
        inputField.addTextChangedListener( object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                actualValue = inputField.text.toString()
                changeCardIcon()
                state = StateforText(this@TextField)
                if(userOnchangeListener !== null)
                    userOnchangeListener?.let { it((state as StateforText).getState(optionsForLogging.env)) }
            }

        })
        //when focus of text changes
        OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val labelPadding = collectInput.labelStyles.focus.padding
                label.setPadding(labelPadding.left,labelPadding.top,labelPadding.right,labelPadding.bottom)
                label.setTextColor(collectInput.labelStyles.focus.textColor)
                if(collectInput.labelStyles.focus.font != Typeface.NORMAL)
                 label.typeface = ResourcesCompat.getFont(context,collectInput.labelStyles.focus.font)
                label.gravity = collectInput.labelStyles.focus.textAlignment

                val inputFieldPadding = collectInput.inputStyles.focus.padding
                inputField.setPadding(inputFieldPadding.left,inputFieldPadding.top,inputFieldPadding.right,inputFieldPadding.bottom)
                inputField.setTextColor(collectInput.inputStyles.focus.textColor)
                border.setStroke(collectInput.inputStyles.focus.borderWidth,collectInput.inputStyles.focus.borderColor)
                border.cornerRadius = collectInput.inputStyles.focus.cornerRadius
                inputField.setBackgroundDrawable(border)
                inputField.gravity = collectInput.inputStyles.focus.textAlignment
                if(collectInput.inputStyles.focus.font != Typeface.NORMAL)
                    inputField.typeface = ResourcesCompat.getFont(context,collectInput.inputStyles.focus.font)
                error.visibility = View.INVISIBLE
                if(userOnFocusListener !== null)
                    userOnFocusListener?.let { it((state as StateforText).getState(optionsForLogging.env)) }
            } else {

                val labelPadding = collectInput.labelStyles.base.padding
                label.textSize = 16F
                label.setPadding(labelPadding.left,labelPadding.top,labelPadding.right,labelPadding.bottom)
                label.setTextColor(collectInput.labelStyles.base.textColor)
                if(collectInput.labelStyles.base.font != Typeface.NORMAL)
                 label.typeface = ResourcesCompat.getFont(context,collectInput.labelStyles.base.font)
                label.gravity = collectInput.labelStyles.base.textAlignment

                val internalState = this.state.getInternalState()
                if(internalState["isEmpty"] as Boolean) {

                    val inputFieldPadding = collectInput.inputStyles.empty.padding
                    inputField.setPadding(inputFieldPadding.left,inputFieldPadding.top,inputFieldPadding.right,inputFieldPadding.bottom)
                    inputField.setTextColor(collectInput.inputStyles.empty.textColor)
                    border.setStroke(collectInput.inputStyles.empty.borderWidth,collectInput.inputStyles.empty.borderColor)
                    border.cornerRadius = collectInput.inputStyles.empty.cornerRadius
                    inputField.setBackgroundDrawable(border)
                    inputField.gravity = collectInput.inputStyles.empty.textAlignment
                    if(collectInput.inputStyles.empty.font != Typeface.NORMAL)
                        inputField.typeface = ResourcesCompat.getFont(context,collectInput.inputStyles.empty.font)

                } else if(!(internalState["isValid"] as Boolean)) {

                    val inputFieldPadding = collectInput.inputStyles.invalid.padding
                    inputField.setPadding(inputFieldPadding.left,inputFieldPadding.top,inputFieldPadding.right,inputFieldPadding.bottom)
                    inputField.setTextColor(collectInput.inputStyles.invalid.textColor)
                    border.setStroke(collectInput.inputStyles.invalid.borderWidth,collectInput.inputStyles.invalid.borderColor)
                    border.cornerRadius = collectInput.inputStyles.invalid.cornerRadius
                    inputField.setBackgroundDrawable(border)
                    inputField.gravity = collectInput.inputStyles.invalid.textAlignment
                    if(collectInput.inputStyles.invalid.font != Typeface.NORMAL)
                        inputField.typeface = ResourcesCompat.getFont(context,collectInput.inputStyles.invalid.font)
                    VibrationHelper.vibrate(context, 10)
                    error.visibility = View.VISIBLE
                    startAnimation(mErrorAnimator)

                } else {

                    val inputFieldPadding = collectInput.inputStyles.complete.padding
                    inputField.setPadding(inputFieldPadding.left,inputFieldPadding.top,inputFieldPadding.right,inputFieldPadding.bottom)
                    inputField.setTextColor(collectInput.inputStyles.complete.textColor)
                    border.setStroke(collectInput.inputStyles.complete.borderWidth,collectInput.inputStyles.complete.borderColor)
                    border.cornerRadius = collectInput.inputStyles.complete.cornerRadius
                    inputField.setBackgroundDrawable(border)
                    inputField.gravity = collectInput.inputStyles.complete.textAlignment
                    if(collectInput.inputStyles.complete.font != Typeface.NORMAL)
                        inputField.typeface = ResourcesCompat.getFont(context,collectInput.inputStyles.complete.font)
                }
                if(userOnBlurListener !== null)
                    userOnBlurListener?.let { it((state as StateforText).getState(optionsForLogging.env)) }
            }
        }.also { inputField.onFocusChangeListener = it }

    }
    internal fun changeCardIcon()
    {
        if(fieldType == SkyflowElementType.CARD_NUMBER && options.enableCardIcon)
        {
            val cardType = CardType.forCardNumber(inputField.text.toString())
            inputField.setCompoundDrawablesRelativeWithIntrinsicBounds(cardType.image, 0, 0, 0)
            inputField.compoundDrawablePadding = 8
        }
    }
    internal fun setError(error: String)
    {
        this.error.text = error
    }

    fun unmount()
    {
        buildTextField()
        error.visibility = View.INVISIBLE
        actualValue = ""
    }

    override fun triggerError(error: String) {
        this.userError = error
        setError(userError)
        state = StateforText(this@TextField)
    }

    override fun resetError() {
        this.userError = ""
        state = StateforText(this)
    }
}