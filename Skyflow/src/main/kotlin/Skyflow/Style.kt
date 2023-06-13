package  Skyflow

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity

@Description("This is the description for Style class")
class Style(
    @Description("Description for borderColor param")
    borderColor: Int? = Color.BLACK,
    @Description("Description for cornerRadius param")
    cornerRadius: Float? = 20f,
    @Description("Description for padding param")
    padding: Padding? = Padding(10,10,10,10),
    @Description("Description for borderWidth param")
    borderWidth: Int? = 2,
    @Description("Description for font param")
    font: Int? = Typeface.NORMAL,
    @Description("Description for textAlignment param")
    textAlignment: Int? = Gravity.LEFT,
    @Description("Description for textColor param")
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