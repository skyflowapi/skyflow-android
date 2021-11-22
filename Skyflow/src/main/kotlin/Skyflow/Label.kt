package Skyflow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat

@Suppress("DEPRECATION")
class Label @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr), BaseElement{

    internal  var actualValue: String =""
    internal var label = TextView(context)
    internal var placeholder = TextView(context)
    internal var error = TextView(context)
    internal lateinit var revealInput: RevealElementInput
    internal lateinit var options: RevealElementOptions
    private lateinit var padding: Padding
    internal var border = GradientDrawable()
    internal var isTokenNull = false

    @SuppressLint("NewApi", "WrongConstant")
    internal fun setupField(revealInput: RevealElementInput, options: RevealElementOptions)
    {
        this.revealInput = revealInput
        this.options = options
        padding = revealInput.inputStyles.base.padding
        if(this.revealInput.token.equals("null") || this.revealInput.token.equals(null))
        {
            isTokenNull = true
            this.revealInput.token = ""
        }
        buildLabel()
        buildPlaceholder()
//        buildError()
    }

    internal fun setError(error: String){
        this.error.text = error
    }

    internal fun showError(){
        setErrorStyles()
        setInvalidStyles()
        this.error.visibility = VISIBLE
    }


    private fun setErrorStyles(){
        Log.d("setting", "setErrorStyles: ")
        error.textSize = 16F
        val errorPadding = revealInput.errorTextStyles.base.padding
        error.setPadding(errorPadding.left,errorPadding.top,errorPadding.right,errorPadding.bottom)
        error.setTextColor(revealInput.errorTextStyles.base.textColor)
        if(revealInput.errorTextStyles.base.font != Typeface.NORMAL)
            error.typeface = ResourcesCompat.getFont(context,revealInput.errorTextStyles.base.font)
        error.gravity = revealInput.errorTextStyles.base.textAlignment
    }

    private fun setInvalidStyles(){
        if(this.revealInput.inputStyles.invalid.font != Typeface.NORMAL)
            this.placeholder.typeface =
                ResourcesCompat.getFont(this.context,
                    this.revealInput.inputStyles.invalid.font)
        this.placeholder.gravity =
            this.revealInput.inputStyles.invalid.textAlignment
        val padding = this.revealInput.inputStyles.invalid.padding
        this.placeholder.setPadding(padding.left,
            padding.top,
            padding.right,
            padding.bottom)
        this.placeholder.setTextColor(this.revealInput.inputStyles.invalid.textColor)
        this.border.setStroke(this.revealInput.inputStyles.invalid.borderWidth,
            this.revealInput.inputStyles.invalid.borderColor)
        this.border.cornerRadius =
            this.revealInput.inputStyles.invalid.cornerRadius
        this.placeholder.setBackgroundDrawable(this.border)
    }

    private fun hideError() {
        setError("")
        this.error.visibility = INVISIBLE
        buildPlaceholder()
    }

    private fun buildPlaceholder() {
        if(revealInput.altText.isEmpty() || revealInput.altText == "")
        {
            placeholder.text = revealInput.token
        }
        else
            placeholder.text = revealInput.altText

        if(revealInput.inputStyles.base.font != Typeface.NORMAL)
            placeholder.typeface = ResourcesCompat.getFont(context,revealInput.inputStyles.base.font)
        placeholder.textSize = 20f
        placeholder.gravity = revealInput.inputStyles.base.textAlignment
        placeholder.setPadding(padding.left,padding.top,padding.right,padding.bottom)
        placeholder.setTextColor(revealInput.inputStyles.base.textColor)
        border.setStroke(revealInput.inputStyles.base.borderWidth,revealInput.inputStyles.base.borderColor)
        border.cornerRadius = revealInput.inputStyles.base.cornerRadius
        placeholder.setBackgroundDrawable(border)
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun buildLabel() {
        val labelPadding = revealInput.labelStyles.base.padding
        label.text = revealInput.label
        label.textSize = 16F
        label.setPadding(labelPadding.left,labelPadding.top,labelPadding.right,labelPadding.bottom)
        label.setTextColor(revealInput.labelStyles.base.textColor)
        label.gravity = revealInput.labelStyles.base.textAlignment
        if(revealInput.labelStyles.base.font != Typeface.NORMAL)
            label.typeface = ResourcesCompat.getFont(context,revealInput.labelStyles.base.font)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        super.setOrientation(LinearLayout.VERTICAL)
        addView(label)
        addView(placeholder)
        addView(error)
        Log.d("att", "onAttachedToWindow: ")
    }



    @JvmName("getValue1")
    internal fun getValue(): String {
        return actualValue
    }

    override fun triggerError(error: String) {
        setError(error)
        showError()
    }

    override fun resetError() {
        hideError()
    }

}