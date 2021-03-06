package com.dididi.lib_image_edit.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.dididi.lib_image_edit.R


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
    ) {
        initView(attrs)
    }

    var fillColor = Color.TRANSPARENT
        set(value) {
            field = value
            fillPaint.color = value
        }

    private fun initView(attrs: AttributeSet?) {
        attrs?.apply {
            //获取属性的背景色
            val typedArray = context.obtainStyledAttributes(this, R.styleable.OutlineTextView)
            fillColor =
                typedArray.getColor(R.styleable.OutlineTextView_text_background, Color.TRANSPARENT)
            typedArray.recycle()
        }
    }

    private val pathPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.WHITE
        setARGB(120, 255, 255, 255)
    }

    private val fillPaint = Paint().apply {
        style = Paint.Style.FILL
        color = fillColor
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.also {
            if (isFocusable){
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
            //画背景色
            it.drawRoundRect(
                5f,
                5f,
                width - 5f,
                height - 5f,
                10f,
                10f,
                fillPaint
            )
        }
        super.onDraw(canvas)
    }
}

