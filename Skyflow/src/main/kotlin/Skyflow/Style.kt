package  Skyflow

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import com.skyflow_android.R


class Style(
    borderColor: Int? = Color.BLACK,
    cornerRadius: Float? = 20f,
    padding: Padding? = Padding(10,10,10,10),
    borderWidth: Int? = 2,
    font: Int? = Typeface.NORMAL,
    textAlignment: Int? = Gravity.LEFT,
    textColor: Int? = Color.BLACK)
{
    var borderColor = borderColor ?: Color.BLACK
    var cornerRadius = cornerRadius?:  20f
    var padding = padding?: Padding(10,10,10,10)
    var borderWidth = borderWidth ?: 2
    var font = font ?: Typeface.NORMAL
    var textAlignment = textAlignment ?: Gravity.LEFT
    var textColor = textColor ?: Color.BLACK
}