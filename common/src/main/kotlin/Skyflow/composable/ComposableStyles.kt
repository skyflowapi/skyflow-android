package Skyflow.composable

import Skyflow.Margin
import Skyflow.Style
import Skyflow.Styles
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

class ComposableStyles {
    companion object {
        private val baseStyle = Style(
            borderWidth = 5,
            cornerRadius = 5f,
            margin = Margin(0, 20, 0, 0)
        )
        private val errorTextBaseStyles = Style(
            borderColor = Color.TRANSPARENT,
            borderWidth = 0,
            cornerRadius = 0f,
            textColor = Color.RED,
            margin = Margin(0, 10, 0, 0)
        )

        fun getStyles(): Styles {
            return Styles(base = baseStyle)
        }

        fun getErrorTextStyles(): Styles {
            return Styles(base = errorTextBaseStyles)
        }
    }
}