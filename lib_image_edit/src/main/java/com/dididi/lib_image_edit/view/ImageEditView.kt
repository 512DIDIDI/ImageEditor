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

    @SuppressLint("ClickableViewAccessibility")
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
        //3.增加滤镜
        imageFilterView = ImageFilterView(context).apply {
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
        return if (onInterceptTouchEvent(event)) {
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

        /**缩放倍数*/
        private var mScaleFactor = 1f

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
            focusView.apply {
                scaleX = mScaleFactor
                scaleY = mScaleFactor
            }
            isScaling = true
            return true
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
                //手指抬起时，缩放才结束
                isScaling = false
            }
        }
    }
}