package com.dididi.lib_image_edit.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView


/**
 * @author dididi(yechao)
 * @since 16/07/2020
 * @describe 带有轮廓的文本框
 */

class OutlineTextView : AppCompatTextView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private val pathPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.WHITE
        setARGB(120, 255, 255, 255)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.also {
            //画轮廓
            it.drawRoundRect(
                5f,
                5f,
                width - 5f,
                height - 5f,
                10f,
                10f,
                pathPaint
            )
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        visibility = View.VISIBLE
        invalidate()
    }

    override fun setTextSize(size: Float) {
        super.setTextSize(size)
        invalidate()
    }
}

