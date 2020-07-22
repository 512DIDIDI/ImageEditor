package com.dididi.lib_image_edit.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.renderscript.Float2
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import com.dididi.lib_image_edit.R
import kotlin.math.max
import kotlin.math.min


/**
 * @author dididi(yechao)
 * @since 29/06/2020
 * @describe 图片编辑控件，
 * 组合 [BackgroundImageView] [BrushDrawingView] [ImageFilterView] [CustomView]
 */

@Suppress("MemberVisibilityCanBePrivate")
class ImageEditView : RelativeLayout {

    companion object {
        //布局id
        const val BACKGROUND_IMAGE_ID = 1
        const val BRUSH_ID = 2
        const val TEXT_ID = 3
        const val IMAGE_FILTER_ID = 4
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(attrs)
    }

    /**背景图片*/
    internal lateinit var backgroundImageView: BackgroundImageView
        private set

    /**画笔*/
    internal lateinit var brushDrawingView: BrushDrawingView
        private set

    /**带有轮廓的水印文字*/
    internal lateinit var outlineTextView: OutlineTextView
        private set

    /**水印文字的宽度覆盖范围*/
    private var outlineTvWidthRange = 0f..0f

    /**水印文字的高度覆盖范围*/
    private var outlineTvHeightRange = 0f..0f

    /**自定义View*/
    internal var customViews = mutableListOf<CustomView>()

    /**图片滤镜*/
    internal lateinit var imageFilterView: ImageFilterView
        private set

    /**初始位置*/
    private var mLocationPoint: Float2? = null

    /**位移量*/
    private var mTranslatePoint: Float2 = Float2()

    /**是否正在执行缩放操作*/
    private var isScaling = false

    /**
     * 平移或缩放时获取到焦点的控件
     * [onTouchEvent]
     */
    private var focusView: View = this

    private fun initView(attrs: AttributeSet?) {
        //1.初始化背景图片并添加
        backgroundImageView = BackgroundImageView(context).apply {
            id = BACKGROUND_IMAGE_ID
            adjustViewBounds = true
            //设置view的参数，与RelativeLayout的位置
            val backgroundParam = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ).apply {
                addRule(CENTER_IN_PARENT, TRUE)
            }
            //读取xml中的image_src
            attrs?.also {
                val typedArray = context.obtainStyledAttributes(it, R.styleable.ImageEditView)
                val imgSrcDrawable = typedArray.getDrawable(R.styleable.ImageEditView_image_src)
                imgSrcDrawable?.let { drawable ->
                    setImageDrawable(drawable)
                }
                typedArray.recycle()
            }
            addView(this, backgroundParam)
        }
        val viewParams =
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).also {
                it.addRule(CENTER_IN_PARENT, TRUE)
                it.addRule(ALIGN_TOP, BACKGROUND_IMAGE_ID)
                it.addRule(ALIGN_BOTTOM, BACKGROUND_IMAGE_ID)
            }
        //2.添加画笔
        brushDrawingView = BrushDrawingView(context).apply {
            id = BRUSH_ID
            visibility = GONE
            addView(this, viewParams)
        }
        //3.添加文本
        outlineTextView = OutlineTextView(context).apply {
            id = TEXT_ID
            visibility = GONE
            val textParams =
                LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).also {
                    it.addRule(CENTER_IN_PARENT, TRUE)
                }
            setPadding(20, 20, 20, 20)
            addView(this, textParams)
        }
        //4.添加自定义view
        customViews.apply {
            if (isNotEmpty()) {
                forEach {
                    it.id = it.setViewId()
                    it.visibility = GONE
                    addView(it, it.setLayoutParams())
                }
            }
        }
        //5.增加滤镜
        imageFilterView = ImageFilterView(context).apply {
            id = IMAGE_FILTER_ID
            visibility = GONE
            //同步滤镜与背景图片的bitmap
            backgroundImageView.onImageChangeListener = fun(it) {
                imageFilterView.setFilterEffect()
                imageFilterView.setSourceBitmap(it)
            }
            addView(this, viewParams)
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        //裁剪画布到背景图大小，可以避免涂鸦时画出背景外的bug
        backgroundImageView.getBitmap()?.let {
            canvas?.clipRect(
                (width - it.width) / 2f,
                (height - it.height) / 2f,
                (width + it.width) / 2f,
                (height + it.height) / 2f
            )
        }
        super.dispatchDraw(canvas)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        //如果处于绘制模式或者触摸到文字水印等区域内时，分发事件让子类处理
        if (brushDrawingView.paintMode) {
            return super.onInterceptTouchEvent(ev)
        }
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        outlineTvWidthRange = outlineTextView.left.toFloat()..outlineTextView.right.toFloat()
        outlineTvHeightRange = outlineTextView.top.toFloat()..outlineTextView.bottom.toFloat()
        return if (onInterceptTouchEvent(event)) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                //只有在每次手指按下时，才会重新赋值focusView。
                focusView = when {
                    //水印框覆盖范围内，焦点控件为水印控件
                    event.x in outlineTvWidthRange && event.y in outlineTvHeightRange -> {
                        outlineTextView
                    }
                    else -> this
                }
            }
            //缩放操作由ScaleDetector处理
            mScaleDetector.onTouchEvent(event)
            //移动背景图片
            translationBackground(event)
            true
        } else {
            false
        }
    }


    /**
     * 多指操作缩放
     */
    private val mScaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        /**多指之间的x距离*/
        private var lastSpanX = 0f

        /**多指之间的y距离*/
        private var lastSpanY = 0f
        private var newWidth = 0f
        private var newHeight = 0f
        private var focusX = 0f
        private var focusY = 0f

        /**缩放倍数*/
        private var mScaleFactor = 1f

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            //获取多指之间的初始距离
            lastSpanX = detector.currentSpanX
            lastSpanY = detector.currentSpanY
            return true
        }

        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            val spanX = scaleGestureDetector.currentSpanX
            val spanY = scaleGestureDetector.currentSpanY
            //缩放后的新宽高
            newWidth = lastSpanX / spanX * focusView.width
            newHeight = lastSpanY / spanY * focusView.height
            //获取多指焦点的平均x y坐标
            focusX = scaleGestureDetector.focusX
            focusY = scaleGestureDetector.focusY
            Log.d(
                "ImageEditView",
                "spanX:$spanX spanY:$spanY lastSpanX:$lastSpanX lastSpanY:$lastSpanY newWidth:$newWidth newHeight:$newHeight focusX:$focusX focusY:$focusY"
            )
            mScaleFactor *= (lastSpanX / spanX)
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
            focusView.scaleX = mScaleFactor
            focusView.scaleY = mScaleFactor
            isScaling = true
            lastSpanX = spanX
            lastSpanY = spanY
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            focusView.apply {

            }
            isScaling =false
        }
    }
    private val mScaleDetector = ScaleGestureDetector(context, mScaleListener)

    /**
     * 平移背景图片
     */
    private fun translationBackground(event: MotionEvent?) {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.pointerCount == 1 && !isScaling) {
                    //记录初始值
                    mLocationPoint = Float2(event.rawX, event.rawY)
                }
                Log.d("ImageEditView", "ACTION_DOWN x:${event.x} y:${event.y}")
            }
            MotionEvent.ACTION_MOVE -> {
                //控制单指移动 且不在放缩时 平移
                if (event.pointerCount == 1 && !isScaling) {
                    //计算位移量
                    mTranslatePoint.x += event.rawX - mLocationPoint!!.x
                    mTranslatePoint.y += event.rawY - mLocationPoint!!.y
                    focusView.apply {
                        //平移动画
                        translationX = mTranslatePoint.x
                        translationY = mTranslatePoint.y
                    }
                    mLocationPoint = Float2(event.rawX, event.rawY)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mLocationPoint = null
                //因为手指抬起会重新计算focusView的真实位置，所以需要将位移记录清零
                mTranslatePoint = Float2()
                focusView.apply {
                    //手指抬起时，因为focusView有平移，所以需要重新计算focusView的位置
                    left += translationX.toInt()
                    right += translationX.toInt()
                    top += translationY.toInt()
                    bottom += translationY.toInt()
                    //清零focusView的位移量
                    translationX = 0f
                    translationY = 0f
                }

            }
        }
    }
}