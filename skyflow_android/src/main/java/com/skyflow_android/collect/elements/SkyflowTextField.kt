package com.skyflow_android.collect.elements

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.skyflow_android.R
import com.skyflow_android.collect.elements.validations.SkyflowValidationError
import com.skyflow_android.collect.elements.validations.SkyflowValidationSet
import com.skyflow_android.collect.elements.validations.SkyflowValidator
import com.skyflow_android.core.elements.Padding
import com.skyflow_android.core.elements.state.State
import com.skyflow_android.core.elements.state.StateforText
import com.skyflow_android.collect.elements.utils.VibrationHelper

@Suppress("DEPRECATION")
class SkyflowTextField @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : SkyflowElement(context, attrs, defStyleAttr) {

    internal var inputField = EditText(context)
    internal var error = TextView(context)
    private var validationRules = SkyflowValidationSet()
    override var state: State = StateforText(this)
    private var border = GradientDrawable()
    private lateinit var padding: Padding
    private var mErrorAnimator: Animation? = null

    override fun getOutput() : String {
        if(inputField.text.toString().isEmpty())
            return ""
        return inputField.text.toString()
    }

    override fun validate() : MutableList<SkyflowValidationError> {
        val str = inputField.text.toString()
        return SkyflowValidator.validate(str,validationRules)
    }

    override fun setupField(collectInput: CollectElementInput, options: CollectElementOptions) {
        super.setupField(collectInput,options)
        validationRules = fieldType.getType().validation
        padding = collectInput.styles?.base?.padding!!
        //textField.keyboardType = fieldType.instance.keyboardType
        state = StateforText(this)
        setError("Invalid Field")
        buildTextField()
        buildError()
    }

    private fun buildTextField()
    {
        border.setColor(Color.WHITE)
        border.setStroke(collectInput.styles!!.base!!.borderWidth!!,collectInput.styles!!.base!!.borderColor!!)
        border.cornerRadius = collectInput.styles!!.base!!.cornerRadius!!
        inputField.setBackgroundDrawable(border)
        inputField.setPadding(padding.left,padding.top,padding.right,padding.bottom)
        inputField.gravity = collectInput.styles?.base?.textAlignment!!
        inputField.hint = collectInput.placeholder
        inputField.setTextColor(collectInput.styles!!.base!!.textColor!!)
        inputField.typeface = ResourcesCompat.getFont(context,collectInput.styles?.base?.font!!)

    }

    private fun buildError()
    {
        error.visibility = View.INVISIBLE
        error.setPadding(20,5,0,0)
        mErrorAnimator = AnimationUtils.loadAnimation(context, R.anim.error_animation)
    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        super.setOrientation(VERTICAL)
        getListenersForText()
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
                state = StateforText(this@SkyflowTextField)
            }

        })
        //when focus of text changes
        OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                inputField.setTextColor(collectInput.styles!!.focus!!.textColor!!)
                border.setStroke(collectInput.styles!!.focus!!.borderWidth!!,collectInput.styles!!.focus!!.borderColor!!)
                border.cornerRadius = collectInput.styles!!.focus!!.cornerRadius!!
                inputField.setBackgroundDrawable(border)
                inputField.gravity = collectInput.styles?.focus?.textAlignment!!
                inputField.typeface = ResourcesCompat.getFont(context,collectInput.styles?.focus?.font!!)
                error.visibility = View.INVISIBLE
            } else {
                val state = this.state.getState()
                if(state["isEmpty"] as Boolean) {
                    inputField.setTextColor(collectInput.styles!!.empty!!.textColor!!)
                    border.setStroke(collectInput.styles!!.empty!!.borderWidth!!,collectInput.styles!!.empty!!.borderColor!!)
                    border.cornerRadius = collectInput.styles!!.empty!!.cornerRadius!!
                    inputField.setBackgroundDrawable(border)
                    inputField.gravity = collectInput.styles?.empty?.textAlignment!!
                    inputField.typeface = ResourcesCompat.getFont(context,collectInput.styles?.empty?.font!!)

                } else if(!(state["isValid"] as Boolean)) {
                    inputField.setTextColor(collectInput.styles!!.invalid!!.textColor!!)
                    border.setStroke(collectInput.styles!!.invalid!!.borderWidth!!,collectInput.styles!!.invalid!!.borderColor!!)
                    border.cornerRadius = collectInput.styles!!.invalid!!.cornerRadius!!
                    inputField.setBackgroundDrawable(border)
                    inputField.gravity = collectInput.styles?.invalid?.textAlignment!!
                    inputField.typeface = ResourcesCompat.getFont(context,collectInput.styles?.invalid?.font!!)
                    VibrationHelper.vibrate(context, 10)
                    error.visibility = View.VISIBLE
                    error.setTextColor(collectInput.styles!!.invalid!!.textColor!!)
                    startAnimation(mErrorAnimator)

                } else {
                    inputField.setTextColor(collectInput.styles!!.completed!!.textColor!!)
                    border.setStroke(collectInput.styles!!.completed!!.borderWidth!!,collectInput.styles!!.completed!!.borderColor!!)
                    border.cornerRadius = collectInput.styles!!.completed!!.cornerRadius!!
                    inputField.setBackgroundDrawable(border)
                    inputField.gravity = collectInput.styles?.completed?.textAlignment!!
                    inputField.typeface = ResourcesCompat.getFont(context,collectInput.styles?.completed?.font!!)
                }
            }
        }.also { inputField.onFocusChangeListener = it }
    }

    internal fun setError(error:String)
    {
        this.error.text = error
    }

}