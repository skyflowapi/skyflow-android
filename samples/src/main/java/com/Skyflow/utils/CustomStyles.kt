package com.Skyflow.utils

import Skyflow.Padding
import Skyflow.Style
import Skyflow.Styles
import android.graphics.Color
import android.view.Gravity
import com.Skyflow.R

class CustomStyles {
    companion object {
        private val padding = Padding(8, 8, 8, 8)
        private val baseStyles = Style(
            Color.parseColor("#403E6B"),
            10f,
            padding,
            null,
            R.font.roboto_light,
            Gravity.START,
            Color.parseColor("#403E6B")
        )
        private val completeStyles = Style(
            Color.GREEN,
            10f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.END,
            Color.GREEN
        )
        private val focusStyles = Style(
            Color.parseColor("#403E6B"),
            10f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.START,
            Color.GREEN
        )
        private val emptyStyles = Style(
            Color.YELLOW,
            10f,
            padding,
            4,
            R.font.roboto_light,
            Gravity.CENTER,
            Color.YELLOW
        )
        private val invalidStyles = Style(
            Color.RED,
            15f,
            padding,
            6,
            R.font.roboto_light,
            Gravity.START,
            Color.RED
        )

        private val baseErrorStyles = Style(
            null,
            null,
            padding,
            null,
            R.font.roboto_light,
            Gravity.START,
            Color.RED
        )

        fun getInputStyles(): Styles {
            return Styles(baseStyles, completeStyles, emptyStyles, focusStyles, invalidStyles)
        }

        fun getLabelStyles(): Styles {
            return getInputStyles()
        }

        fun getErrorStyles(): Styles {
            return Styles(baseErrorStyles)
        }
    }
}