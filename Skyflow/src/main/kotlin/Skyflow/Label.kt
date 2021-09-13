package Skyflow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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
) : LinearLayout(context, attrs, defStyleAttr) {

    internal var label = TextView(context)
    internal var placeholder = TextView(context)
    internal lateinit var revealInput: RevealElementInput
    internal lateinit var options: RevealElementOptions
    internal lateinit var padding: Padding
    internal var border = GradientDrawable()

    @SuppressLint("NewApi", "WrongConstant")
    internal fun setupField(revealInput: RevealElementInput, options: RevealElementOptions)
    {
        this.revealInput = revealInput
        this.options = options
        padding = revealInput.styles.base?.padding!!
        buildLabel()
        buildPlaceholder()
    }

    private fun buildPlaceholder() {
        if(revealInput.defaultText.isEmpty() || revealInput.defaultText.equals(""))
        {
            placeholder.text = revealInput.token
        }
        else
            placeholder.text = revealInput.defaultText

        placeholder.typeface = ResourcesCompat.getFont(context,revealInput.styles.base?.font!!)
        placeholder.textSize = 20f
        placeholder.gravity = revealInput.styles.base?.textAlignment!!
        placeholder.setPadding(padding.left,padding.top,padding.right,padding.bottom)
        placeholder.setTextColor(revealInput.styles.base?.textColor!!)
        border.setColor(Color.WHITE)
        border.setStroke(revealInput.styles.base!!.borderWidth,revealInput.styles.base!!.borderColor)
        border.cornerRadius = revealInput.styles.base!!.cornerRadius
        placeholder.setBackgroundDrawable(border)
    }

    @SuppressLint("WrongConstant")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private fun buildLabel() {
        val labelPadding = revealInput.labelStyles.base!!.padding
        label.text = revealInput.label
        label.textSize = 16F
        label.setPadding(labelPadding.left,labelPadding.top,labelPadding.right,labelPadding.bottom)
        label.setTextColor(revealInput.labelStyles.base?.textColor!!)
        label.gravity = revealInput.labelStyles.base?.textAlignment!!
        label.typeface = ResourcesCompat.getFont(context,revealInput.labelStyles.base?.font!!)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        super.setOrientation(LinearLayout.VERTICAL)
        addView(label)
        addView(placeholder)
    }

    internal fun getOutput() : String
    {
        return placeholder.text.toString()
    }


}