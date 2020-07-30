package com.dididi.imageeditor.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt


/**
 * @author dididi(yechao)
 * @since 30/07/2020
 * @describe 带有轮廓的圆形view
 */

class ColorCircleView : View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    @ColorInt
    var color: Int = Color.BLACK
        set(value) {
            field = value
            paint.color = value
            invalidate()
        }

    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        color = Color.BLACK
    }

    private val borderPaint = Paint().apply {
        style = Paint.Style.STROKE
        isAntiAlias = true
        color = Color.DKGRAY
        strokeWidth = 2f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawCircle(width / 2f, height / 2f, (width / 2f - borderPaint.strokeWidth / 2f), paint)
            drawCircle(
                width / 2f,
                height / 2f,
                (width / 2f - borderPaint.strokeWidth / 2f),
                borderPaint
            )
        }
    }
}