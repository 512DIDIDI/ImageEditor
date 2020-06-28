package com.dididi.watermarkmaster

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.renderscript.Float2
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Toast
import java.util.*
import kotlin.math.max
import kotlin.math.min


/**
 * @author dididi(yechao)
 * @since 19/06/2020
 * @describe
 */

@Suppress("unused", "MemberVisibilityCanBePrivate")
class WatermarkView : View {

    companion object {
        const val TAG = "WatermarkView"
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    enum class Operation {
        /**水印文字*/
        WATERMARK,

        /**背景图片*/
        IMAGE,

        /**绘制路径*/
        PATH
    }

    private var mScreenWidth = 0
    private var mScreenHeight = 0
    private var mFinalBitmap: Bitmap? = null

    /**操作栈*/
    private val operationStack = Stack<Operation>()

    /**历史栈*/
    private val historyStack = Stack<Operation>()

    /**是否有背景图片*/
    private var isBackgroundExit = false

    /**水印文字*/
    var watermarkText: String? = ""
        set(value) {
            field = value
            invalidate()
        }

    /**水印文字颜色*/
    var textColor = Color.BLACK

    /**水印文字尺寸*/
    var textSize = 30f

    /**背景图片*/
    var backgroundImage: Bitmap? = null
        set(value) {
            if (value == null) return
            field = value
            val dm = context.resources.displayMetrics
            mScreenWidth = dm.widthPixels
            mScreenHeight = dm.heightPixels
            val widthScale = value.width.toFloat() / mScreenWidth.toFloat()
            val heightScale = value.height.toFloat() / mScreenHeight.toFloat()
            val finalSize = if (widthScale >= heightScale) {
                Pair(mScreenWidth, (value.height / widthScale).toInt())
            } else {
                Pair((value.width / heightScale).toInt(), mScreenHeight)
            }
            mFinalBitmap = Bitmap.createScaledBitmap(value, finalSize.first, finalSize.second, true)
            watermarkText = ""
            isBackgroundExit = true
            isPainting = false
        }

    /**是否正在绘制图案*/
    var isPainting = false
        set(value) {
            field = value
            invalidate()
        }

    /**绘制画笔的颜色*/
    var paintColor = Color.BLACK

    /**画笔粗细*/
    var paintWidth = 20f

    /**绘制图形路径*/
    private val mPaintPath = Path()

    /**绘制画笔*/
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    /**水印文字画笔*/
    private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 20f
    }

    /**初始位置*/
    private var mLocationPoint: Float2? = null

    /**位移量*/
    private var mTranslatePoint: Float2 = Float2()

    /**缩放倍数*/
    private var mScaleFactor = 1f

    /**是否正在执行缩放操作*/
    private var isScaling = false

    /**
     * 多指操作缩放
     */
    private val mScaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            mScaleFactor *= scaleGestureDetector.scaleFactor
            //限定缩放倍数的范围
            mScaleFactor = max(0.1f, min(mScaleFactor, 5f))
            when {
                mScaleFactor <= 0.1f -> {
                    Toast.makeText(context, "已经最小了", Toast.LENGTH_SHORT).show()
                }
                mScaleFactor >= 5f -> {
                    Toast.makeText(context, "已经最大了", Toast.LENGTH_SHORT).show()
                }
            }
            scaleX = mScaleFactor
            scaleY = mScaleFactor
            isScaling = true
            return true
        }
    }
    private val mScaleDetector = ScaleGestureDetector(context, mScaleListener)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mTextPaint.color = textColor
        mTextPaint.textSize = textSize
        mPaint.strokeWidth = paintWidth
        mPaint.color = paintColor
        if (isBackgroundExit) {
            canvas?.apply {
                mFinalBitmap?.let {
                    //切割画布为背景图大小
                    clipRect(
                        (this@WatermarkView.width - it.width) / 2f,
                        (this@WatermarkView.height - it.height) / 2f,
                        (this@WatermarkView.width + it.width) / 2f,
                        (this@WatermarkView.height + it.height) / 2f
                    )
                    drawBitmap(
                        it,
                        (this@WatermarkView.width - it.width) / 2f,
                        (this@WatermarkView.height - it.height) / 2f,
                        mPaint
                    )
                }
                watermarkText?.let {
                    drawText(it, 50f, 50f, mTextPaint)
                }
                drawPath(mPaintPath, mPaint)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mFinalBitmap?.recycle()
        backgroundImage?.recycle()
        backgroundImage = null
        mFinalBitmap = null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //如果正在绘制，需要直接拦截消费触摸事件
        if (isPainting) {
            initPaintPath(event)
            return true
        }
        //缩放操作由ScaleDetector处理
        mScaleDetector.onTouchEvent(event)
        //移动背景图片
        translationBackground(event)
        return true
    }

    /**
     * 初始化绘制路径 [mPaintPath]
     */
    private fun initPaintPath(event: MotionEvent?) {
//        val path = Path()
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                //起点
                mPaintPath.moveTo(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                //lineTo连接移动中的点
                mPaintPath.lineTo(event.x, event.y)
            }
        }
//        mPathList.add(path)
        //绘制路径推入栈
        operationStack.push(Operation.PATH)
        //重绘
        invalidate()
    }

    /**
     * 平移背景图片 [backgroundImage]
     */
    private fun translationBackground(event: MotionEvent?) {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.pointerCount == 1 && !isScaling) {
                    //记录初始值
                    mLocationPoint = Float2(event.rawX, event.rawY)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                //控制单指移动 且不在放缩时 平移
                if (event.pointerCount == 1 && !isScaling) {
                    //计算位移量
                    mTranslatePoint.x += event.rawX - mLocationPoint!!.x
                    mTranslatePoint.y += event.rawY - mLocationPoint!!.y
                    translationX = mTranslatePoint.x
                    translationY = mTranslatePoint.y
                    mLocationPoint = Float2(event.rawX, event.rawY)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mLocationPoint = null
                //手指松开时，重新初始化
                isScaling = false
            }
        }
    }

}