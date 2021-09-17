package Skyflow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat

@Suppress("DEPRECATION")
class Label @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    internal var label = TextView(context)
    internal var placeholder = TextView(context)
    internal var error = TextView(context)
    internal lateinit var revealInput: RevealElementInput
    internal lateinit var options: RevealElementOptions
    internal lateinit var padding: Padding
    internal var border = GradientDrawable()

    @SuppressLint("NewApi", "WrongConstant")
    internal fun setupField(revealInput: RevealElementInput, options: RevealElementOptions)
    {
        this.revealInput = revealInput
        this.options = options
        padding = revealInput.inputStyles.base.padding
        buildLabel()
        buildPlaceholder()
        buildError()
    }

    private fun buildError() {
        error.text = " "
        error.textSize = 16F
        val errorPadding = revealInput.errorTextStyles.base.padding
        error.setPadding(errorPadding.left,errorPadding.top,errorPadding.right,errorPadding.bottom)
        error.setTextColor(revealInput.errorTextStyles.base.textColor)
        error.typeface = ResourcesCompat.getFont(context,revealInput.errorTextStyles.base.font)
        error.gravity = revealInput.errorTextStyles.base.textAlignment
    }

    private fun buildPlaceholder() {
        if(revealInput.altText.isEmpty() || revealInput.altText.equals(""))
        {
            placeholder.text = revealInput.token
        }
        else
            placeholder.text = revealInput.altText

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
        label.typeface = ResourcesCompat.getFont(context,revealInput.labelStyles.base.font)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        super.setOrientation(LinearLayout.VERTICAL)
        addView(label)
        addView(placeholder)
        addView(error)
    }

    internal fun getOutput() : String
    {
        return placeholder.text.toString()
    }


}