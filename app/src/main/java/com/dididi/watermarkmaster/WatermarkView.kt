package com.dididi.watermarkmaster

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


/**
 * @author dididi(yechao)
 * @since 19/06/2020
 * @describe
 */

class WatermarkView : View {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var mScreenWidth = 0
    private var mScreenHeight = 0
    private lateinit var finalBitmap: Bitmap
    private var isReady = false

    /**
     * 水印文字
     */
    var watermarkText = ""
        set(value) {
            isReady = true
            field = value
            invalidate()
        }

    /**
     * 背景图片
     */
    var backgroundImage: Bitmap? = null
        set(value) {
            if (value == null) return
            field = value
            val dm = context.resources.displayMetrics
            mScreenWidth = dm.widthPixels
            mScreenHeight = dm.heightPixels
            finalBitmap = Bitmap.createScaledBitmap(value, mScreenWidth, mScreenHeight, true)
            isReady = true
            invalidate()
        }

    /**
     * 文字颜色
     */
    var textColor = Color.BLACK

    /**
     * 文字尺寸
     */
    var textSize = 30f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 20f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = textColor
        paint.textSize = textSize
        if (isReady)
            canvas?.drawBitmap(addWatermark(), 0f, 0f, paint)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        return super.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }


    private fun addWatermark(): Bitmap {
        val canvas = Canvas(finalBitmap)
        canvas.drawText(watermarkText, 50f, 50f, paint)
        return finalBitmap
    }
}