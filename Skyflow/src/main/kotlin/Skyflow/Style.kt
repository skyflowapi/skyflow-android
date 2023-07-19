package  Skyflow

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity

@Description("Contains various styles for skyflow elements.")
class Style(
    @Description("Color of the border.")
    borderColor: Int? = Color.BLACK,
    @Description("Radius applied to the corners.")
    cornerRadius: Float? = 20f,
    @Description("Padding for the element.")
    padding: Padding? = Padding(10,10,10,10),
    @Description("Width of the border.")
    borderWidth: Int? = 2,
    @Description("Type of font used.")
    font: Int? = Typeface.NORMAL,
    @Description("Alignment of the text.")
    textAlignment: Int? = Gravity.LEFT,
    @Description("Color of the text.")
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