package com.dididi.lib_image_edit.view

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.dididi.lib_image_edit.R
import com.dididi.lib_image_edit.const.ImageFilter
import com.dididi.lib_image_edit.event.MultiTouchListener


/**
 * @author dididi(yechao)
 * @since 29/06/2020
 * @describe 图片编辑控件，
 * 组合 [BackgroundImageView] [BrushDrawingView] [FilterView]
 */

class ImageEditView(context: Context, private val attrs: AttributeSet?, defStyleAttr: Int) :
    RelativeLayout(
        context,
        attrs,
        defStyleAttr
    ) {

    companion object {
        //布局id
        const val BACKGROUND_IMAGE_ID = 1
        const val BRUSH_ID = 2
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    /**背景图片*/
    internal var backgroundImageView: BackgroundImageView
        private set

    /**画笔*/
    internal var brushDrawingView: BrushDrawingView
        private set

    /**图片滤镜*/
    internal var filterView: FilterView
        private set

    /**位置 提供给属性动画的*/
    private var position: PointF = PointF(0f, 0f)

    init {
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
        filterView = FilterView(context).apply {
            visibility = GONE
            //同步滤镜与背景图片的bitmap
            backgroundImageView.onImageChangeListener = fun(it) {
                setFilterEffect(ImageFilter.ORIGIN)
                setSourceBitmap(it)
            }
            addView(this, viewParams)
        }
        //4.touch事件
        setOnTouchListener(MultiTouchListener(context))
    }

    fun setPosition(position: PointF) {
        this.position = position
        val l = position.x.toInt()
        val t = position.y.toInt()
        layout(l, t, l + width, t + height)
    }

    fun getPosition() = position
}