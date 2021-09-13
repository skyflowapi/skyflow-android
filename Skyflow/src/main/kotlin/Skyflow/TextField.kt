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

    override fun getOutput() : String {
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
        padding = collectInput.styles.base.padding!!
        //textField.keyboardType = fieldType.instance.keyboardType
        state = StateforText(this)
        this.collectInput = collectInput
        setError("Invalid Field")
        buildTextField()
        buildError()
        buildLabel()
    }

    private fun buildLabel() {
        label.text = collectInput.label
       /* label.textSize = 16F
        label.setPadding(15,0,0,5)
        label.setTextColor(collectInput.styles?.base?.textColor!!)*/
        val labelPadding = collectInput.labelStyles.base?.padding!!
        label.textSize = 16F
        label.setPadding(labelPadding.left,labelPadding.top,labelPadding.right,labelPadding.bottom)
        label.setTextColor(collectInput.labelStyles.base?.textColor!!)
        label.typeface = ResourcesCompat.getFont(context,collectInput.labelStyles.base?.font!!)
        label.gravity = collectInput.labelStyles.base?.textAlignment!!

    }

    private fun buildTextField()
    {
        border.setColor(Color.WHITE)
        border.setStroke(collectInput.styles.base!!.borderWidth,collectInput.styles.base!!.borderColor)
        border.cornerRadius = collectInput.styles.base!!.cornerRadius
        inputField.setBackgroundDrawable(border)
        inputField.setPadding(padding.left,padding.top,padding.right,padding.bottom)
        inputField.gravity = collectInput.styles.base?.textAlignment!!
        inputField.hint = collectInput.placeholder
        inputField.setTextColor(collectInput.styles.base!!.textColor)
        inputField.typeface = ResourcesCompat.getFont(context,collectInput.styles.base?.font!!)

    }

    private fun buildError()
    {
        error.visibility = View.INVISIBLE
        val errorPadding = collectInput.errorTextStyles.base!!.padding
        error.setPadding(errorPadding.left,errorPadding.top,errorPadding.right,errorPadding.bottom)
        mErrorAnimator = AnimationUtils.loadAnimation(context, R.anim.error_animation)
        error.setTextColor(collectInput.errorTextStyles.base!!.textColor)
        error.typeface = ResourcesCompat.getFont(context,collectInput.errorTextStyles.base?.font!!)
        error.gravity = collectInput.errorTextStyles.base?.textAlignment!!
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
                val labelPadding = collectInput.labelStyles.focus?.padding!!
                label.setPadding(labelPadding.left,labelPadding.top,labelPadding.right,labelPadding.bottom)
                label.setTextColor(collectInput.labelStyles.focus?.textColor!!)
                label.typeface = ResourcesCompat.getFont(context,collectInput.labelStyles.focus?.font!!)
                label.gravity = collectInput.labelStyles.focus?.textAlignment!!

                val inputFieldPadding = collectInput.styles.focus!!.padding
                inputField.setPadding(inputFieldPadding.left,inputFieldPadding.top,inputFieldPadding.right,inputFieldPadding.bottom)
                inputField.setTextColor(collectInput.styles.focus!!.textColor)
                border.setStroke(collectInput.styles.focus!!.borderWidth,collectInput.styles.focus!!.borderColor)
                border.cornerRadius = collectInput.styles.focus!!.cornerRadius
                inputField.setBackgroundDrawable(border)
                inputField.gravity = collectInput.styles.focus?.textAlignment!!
                inputField.typeface = ResourcesCompat.getFont(context,collectInput.styles.focus?.font!!)
                error.visibility = View.INVISIBLE
            } else {

                val labelPadding = collectInput.labelStyles.base?.padding!!
                label.textSize = 16F
                label.setPadding(labelPadding.left,labelPadding.top,labelPadding.right,labelPadding.bottom)
                label.setTextColor(collectInput.labelStyles.base?.textColor!!)
                label.typeface = ResourcesCompat.getFont(context,collectInput.labelStyles.base?.font!!)
                label.gravity = collectInput.labelStyles.base?.textAlignment!!


                val state = this.state.getState()
                if(state["isEmpty"] as Boolean) {

                    val inputFieldPadding = collectInput.styles.empty!!.padding
                    inputField.setPadding(inputFieldPadding.left,inputFieldPadding.top,inputFieldPadding.right,inputFieldPadding.bottom)
                    inputField.setTextColor(collectInput.styles.empty!!.textColor)
                    border.setStroke(collectInput.styles.empty!!.borderWidth,collectInput.styles.empty!!.borderColor)
                    border.cornerRadius = collectInput.styles.empty!!.cornerRadius
                    inputField.setBackgroundDrawable(border)
                    inputField.gravity = collectInput.styles.empty?.textAlignment!!
                    inputField.typeface = ResourcesCompat.getFont(context,collectInput.styles.empty?.font!!)

                } else if(!(state["isValid"] as Boolean)) {

                    val inputFieldPadding = collectInput.styles.invalid!!.padding
                    inputField.setPadding(inputFieldPadding.left,inputFieldPadding.top,inputFieldPadding.right,inputFieldPadding.bottom)
                    inputField.setTextColor(collectInput.styles.invalid!!.textColor)
                    border.setStroke(collectInput.styles.invalid!!.borderWidth,collectInput.styles.invalid!!.borderColor)
                    border.cornerRadius = collectInput.styles.invalid!!.cornerRadius
                    inputField.setBackgroundDrawable(border)
                    inputField.gravity = collectInput.styles.invalid?.textAlignment!!
                    inputField.typeface = ResourcesCompat.getFont(context,collectInput.styles.invalid?.font!!)
                    VibrationHelper.vibrate(context, 10)
                    error.visibility = View.VISIBLE
                    startAnimation(mErrorAnimator)

                } else {

                    val inputFieldPadding = collectInput.styles.complete!!.padding
                    inputField.setPadding(inputFieldPadding.left,inputFieldPadding.top,inputFieldPadding.right,inputFieldPadding.bottom)
                    inputField.setTextColor(collectInput.styles.complete!!.textColor)
                    border.setStroke(collectInput.styles.complete!!.borderWidth,collectInput.styles.complete!!.borderColor)
                    border.cornerRadius = collectInput.styles.complete!!.cornerRadius
                    inputField.setBackgroundDrawable(border)
                    inputField.gravity = collectInput.styles.complete?.textAlignment!!
                    inputField.typeface = ResourcesCompat.getFont(context,collectInput.styles.complete?.font!!)
                }
            }
        }.also { inputField.onFocusChangeListener = it }
    }

    internal fun setError(error:String)
    {
        this.error.text = error
    }

}