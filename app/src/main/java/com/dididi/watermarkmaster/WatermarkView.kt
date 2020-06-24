package com.dididi.watermarkmaster

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.renderscript.Float2
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.max
import kotlin.math.min


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

    /**水印文字*/
    var watermarkText = ""
        set(value) {
            isReady = true
            field = value
            invalidate()
        }

    /**背景图片*/
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

    /**文字颜色*/
    var textColor = Color.BLACK

    /**文字尺寸*/
    var textSize = 30f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 20f
    }

    /**初始位置*/
    private var locationPoint = Float2()

    /**位移量*/
    private var translationPoint = Float2()

    /**缩放倍数*/
    private var mScaleFactor = 1f

    /**
     * 多指操作缩放
     */
    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            mScaleFactor *= scaleGestureDetector.scaleFactor
            //限定缩放倍数的范围
            mScaleFactor = max(0.1f, min(mScaleFactor, 5f))
            scaleX = mScaleFactor
            scaleY = mScaleFactor
            return true
        }
    }
    private val mScaleDetector = ScaleGestureDetector(context, scaleListener)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color = textColor
        paint.textSize = textSize
        if (isReady)
            canvas?.drawBitmap(addWatermark(), 0f, 0f, paint)
    }

    /**添加水印文字*/
    private fun addWatermark(): Bitmap {
        val canvas = Canvas(finalBitmap)
        canvas.drawText(watermarkText, 50f, 50f, paint)
        return finalBitmap
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //缩放操作由ScaleDetector处理
        mScaleDetector.onTouchEvent(event)
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.pointerCount == 1){
                    //记录初始值
                    locationPoint = Float2(event.rawX, event.rawY)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount == 1){
                    //计算位移量
                    translationPoint.x += event.rawX - locationPoint.x
                    translationPoint.y += event.rawY - locationPoint.y
                    translationX = translationPoint.x
                    translationY = translationPoint.y
                    locationPoint = Float2(event.rawX, event.rawY)
                }
            }
        }
        return true
    }

}