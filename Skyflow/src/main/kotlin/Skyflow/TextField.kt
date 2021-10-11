package Skyflow

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
import Skyflow.collect.elements.utils.VibrationHelper
import com.Skyflow.collect.elements.validations.SkyflowValidationError
import com.Skyflow.collect.elements.validations.SkyflowValidationSet
import com.Skyflow.collect.elements.validations.SkyflowValidator
import Skyflow.core.elements.state.StateforText
import com.skyflow_android.R
import kotlin.String

@Suppress("DEPRECATION")
class TextField @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : Skyflow.Element(context, attrs, defStyleAttr) {

    internal var label = TextView(context)
    internal var inputField = EditText(context)
    internal var error = TextView(context)
    private var validationRules = SkyflowValidationSet()
    override var state: State = StateforText(this)
    private var border = GradientDrawable()
    private lateinit var padding: Padding
    private var mErrorAnimator: Animation? = null

    override fun getValue() : String {
        if(inputField.text.toString().isEmpty())
            return ""
        return inputField.text.toString()
    }

    override fun validate() : MutableList<SkyflowValidationError> {
        val str = inputField.text.toString()
        return SkyflowValidator.validate(str,validationRules)
    }

    override fun setupField(collectInput: CollectElementInput, options: Skyflow.CollectElementOptions) {
        super.setupField(collectInput,options)
        validationRules = fieldType.getType().validation
        padding = collectInput.inputStyles.base.padding
        state = StateforText(this)
        this.collectInput = collectInput
        if(collectInput.label.isEmpty())
            setError("Invalid field")
        else
            setError("Invalid "+collectInput.label)
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
        label.typeface = ResourcesCompat.getFont(context,collectInput.labelStyles.base.font)
        label.gravity = collectInput.labelStyles.base.textAlignment

    }

    private fun buildTextField()
    {
        if(collectInput.altText.isNotEmpty() || collectInput.altText != "")
        {
            inputField.setText(collectInput.altText)
            state = StateforText(this)
        }
        border.setColor(Color.WHITE)
        border.setStroke(collectInput.inputStyles.base.borderWidth,collectInput.inputStyles.base.borderColor)
        border.cornerRadius = collectInput.inputStyles.base.cornerRadius
        inputField.setBackgroundDrawable(border)
        inputField.setPadding(padding.left,padding.top,padding.right,padding.bottom)
        inputField.gravity = collectInput.inputStyles.base.textAlignment
        inputField.hint = collectInput.placeholder
        inputField.setTextColor(collectInput.inputStyles.base.textColor)
        inputField.typeface = ResourcesCompat.getFont(context,collectInput.inputStyles.base.font)

    }

    private fun buildError()
    {
        error.visibility = View.INVISIBLE
        val errorPadding = collectInput.errorTextStyles.base.padding
        error.setPadding(errorPadding.left,errorPadding.top,errorPadding.right,errorPadding.bottom)
        mErrorAnimator = AnimationUtils.loadAnimation(context, R.anim.error_animation)
        error.setTextColor(collectInput.errorTextStyles.base.textColor)
        error.typeface = ResourcesCompat.getFont(context,collectInput.errorTextStyles.base.font)
        error.gravity = collectInput.errorTextStyles.base.textAlignment
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        super.setOrientation(VERTICAL)
        getListenersForText()
        addView(label)
        addView(inputField)
        addView(error)
    }

    private fun getListenersForText() {
        //when text changes
        inputField.addTextChangedListener( object : TextWatcher
        {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                state = StateforText(this@TextField)

            }

        })
        //when focus of text changes
        OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val labelPadding = collectInput.labelStyles.focus.padding
                label.setPadding(labelPadding.left,labelPadding.top,labelPadding.right,labelPadding.bottom)
                label.setTextColor(collectInput.labelStyles.focus.textColor)
                label.typeface = ResourcesCompat.getFont(context,collectInput.labelStyles.focus.font)
                label.gravity = collectInput.labelStyles.focus.textAlignment

                val inputFieldPadding = collectInput.inputStyles.focus.padding
                inputField.setPadding(inputFieldPadding.left,inputFieldPadding.top,inputFieldPadding.right,inputFieldPadding.bottom)
                inputField.setTextColor(collectInput.inputStyles.focus.textColor)
                border.setStroke(collectInput.inputStyles.focus.borderWidth,collectInput.inputStyles.focus.borderColor)
                border.cornerRadius = collectInput.inputStyles.focus.cornerRadius
                inputField.setBackgroundDrawable(border)
                inputField.gravity = collectInput.inputStyles.focus.textAlignment
                inputField.typeface = ResourcesCompat.getFont(context,collectInput.inputStyles.focus.font)
                error.visibility = View.INVISIBLE
            } else {

                val labelPadding = collectInput.labelStyles.base.padding
                label.textSize = 16F
                label.setPadding(labelPadding.left,labelPadding.top,labelPadding.right,labelPadding.bottom)
                label.setTextColor(collectInput.labelStyles.base.textColor)
                label.typeface = ResourcesCompat.getFont(context,collectInput.labelStyles.base.font)
                label.gravity = collectInput.labelStyles.base.textAlignment


                val state = this.state.getState()
                if(state["isEmpty"] as Boolean) {

                    val inputFieldPadding = collectInput.inputStyles.empty.padding
                    inputField.setPadding(inputFieldPadding.left,inputFieldPadding.top,inputFieldPadding.right,inputFieldPadding.bottom)
                    inputField.setTextColor(collectInput.inputStyles.empty.textColor)
                    border.setStroke(collectInput.inputStyles.empty.borderWidth,collectInput.inputStyles.empty.borderColor)
                    border.cornerRadius = collectInput.inputStyles.empty.cornerRadius
                    inputField.setBackgroundDrawable(border)
                    inputField.gravity = collectInput.inputStyles.empty.textAlignment
                    inputField.typeface = ResourcesCompat.getFont(context,collectInput.inputStyles.empty.font)

                } else if(!(state["isValid"] as Boolean)) {

                    val inputFieldPadding = collectInput.inputStyles.invalid.padding
                    inputField.setPadding(inputFieldPadding.left,inputFieldPadding.top,inputFieldPadding.right,inputFieldPadding.bottom)
                    inputField.setTextColor(collectInput.inputStyles.invalid.textColor)
                    border.setStroke(collectInput.inputStyles.invalid.borderWidth,collectInput.inputStyles.invalid.borderColor)
                    border.cornerRadius = collectInput.inputStyles.invalid.cornerRadius
                    inputField.setBackgroundDrawable(border)
                    inputField.gravity = collectInput.inputStyles.invalid.textAlignment
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
                    inputField.typeface = ResourcesCompat.getFont(context,collectInput.inputStyles.complete.font)
                }
            }
        }.also { inputField.onFocusChangeListener = it }
    }

    internal fun setError(error: String)
    {
        this.error.text = error
    }

}