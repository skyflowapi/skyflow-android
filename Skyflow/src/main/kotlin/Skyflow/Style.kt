package  Skyflow

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup

class Style(
    borderColor: Int? = Color.BLACK,
    cornerRadius: Float? = 20f,
    padding: Padding? = Padding(10, 10, 10, 10),
    borderWidth: Int? = 2,
    font: Int? = Typeface.NORMAL,
    textAlignment: Int? = Gravity.LEFT,
    textColor: Int? = Color.BLACK,
    placeholderColor: Int? = Color.LTGRAY,
    width: Int? = ViewGroup.LayoutParams.MATCH_PARENT,
    height: Int? = ViewGroup.LayoutParams.WRAP_CONTENT,
    margin: Margin? = Margin(10, 10, 10, 10),
    backgroundColor: Int? = Color.WHITE,
    minWidth: Int? = ViewGroup.LayoutParams.MATCH_PARENT,
    maxWidth: Int? = ViewGroup.LayoutParams.MATCH_PARENT,
    minHeight: Int? = ViewGroup.LayoutParams.WRAP_CONTENT,
    maxHeight: Int? = null
) {
    var borderColor = borderColor ?: Color.BLACK
    var cornerRadius = cornerRadius ?: 20f
    var padding = padding ?: Padding(10, 10, 10, 10)
    var borderWidth = borderWidth ?: 2
    var font = font ?: Typeface.NORMAL
    var textAlignment = textAlignment ?: Gravity.LEFT
    var textColor = textColor ?: Color.BLACK
    var placeholderColor = placeholderColor ?: Color.LTGRAY
    var width = width ?: 100
    var height = height ?: ViewGroup.LayoutParams.WRAP_CONTENT
    var margin = margin ?: Margin(10, 10, 10, 10)
    var backgroundColor = backgroundColor ?: Color.WHITE
    var minWidth = minWidth ?: ViewGroup.LayoutParams.MATCH_PARENT
    var maxWidth = maxWidth ?: ViewGroup.LayoutParams.MATCH_PARENT
    var minHeight = minHeight ?: ViewGroup.LayoutParams.WRAP_CONTENT
    var maxHeight = maxHeight
}