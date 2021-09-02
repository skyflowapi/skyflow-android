package  Skyflow

import android.graphics.Color
import android.view.Gravity
import com.skyflow_android.R


class Style(
     borderColor: Int? = Color.BLACK,
     cornerRadius: Float? = 20f,
     padding: Padding? = Padding(10,10,10,10),
     borderWidth: Int? = 2,
     font: Int? = R.font.roboto_light,
     textAlignment: Int? = Gravity.LEFT,
     textColor: Int? = Color.BLACK)
{
    var borderColor = borderColor ?: Color.BLACK
    var cornerRadius = cornerRadius?:  20f
    var padding = padding?: Padding(10,10,10,10)
    var borderWidth = borderWidth ?: 2
    var font = font ?: R.font.roboto_light
    var textAlignment = textAlignment ?: Gravity.LEFT
    var textColor = textColor ?: Color.BLACK
}