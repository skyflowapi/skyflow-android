package Skyflow.collect.elements.utils

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan

class Spacespan(internal val separator: String) : ReplacementSpan() {
    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        val padding = paint.measureText(" ", 0, 1)
        val textSize = paint.measureText(text, start, end)
        return (padding + textSize).toInt()
    }

    override fun draw(
        canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int,
        bottom: Int, paint: Paint
    ) {
        canvas.drawText(text.subSequence(start, end).toString() + separator, x, y.toFloat(), paint)
    }
}