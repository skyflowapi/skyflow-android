package Skyflow

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

abstract class BaseElement @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
)  : LinearLayout(context, attrs, defStyleAttr){

      internal abstract fun getValue():String
 }
   // internal fun BaseElement.getValue() = Unit
