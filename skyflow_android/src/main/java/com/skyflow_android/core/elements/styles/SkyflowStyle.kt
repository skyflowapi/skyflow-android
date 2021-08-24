package  com.skyflow_android.core.elements.styles

import android.graphics.Color
import android.view.Gravity
import com.skyflow_android.R
import com.skyflow_android.core.elements.Padding


class SkyflowStyle(
    var borderColor: Int? = Color.BLACK,
    var cornerRadius: Float? = 20f,
    var padding: Padding = Padding(10,10,10,10),
    var borderWidth: Int? = 2,
    var font: Int? = R.font.roboto_light,
    var textAlignment: Int = Gravity.LEFT,
    var textColor: Int? = Color.BLACK)
{

}