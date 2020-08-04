package com.dididi.lib_image_edit.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Toast
import com.dididi.lib_image_edit.R
import com.dididi.lib_image_edit.event.MultiTouchListener
import kotlin.math.max
import kotlin.math.min


/**
 * @author dididi(yechao)
 * @since 29/06/2020
 * @describe 图片编辑控件，
 * 组合 [BackgroundImageView] [BrushDrawingView] [ImageFilterView]
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
    private var mLocationPoint: PointF? = null

    /**位移量*/
    private var mTranslatePoint = PointF()

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
        setOnTouchListener(MultiTouchListener(context))
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
}