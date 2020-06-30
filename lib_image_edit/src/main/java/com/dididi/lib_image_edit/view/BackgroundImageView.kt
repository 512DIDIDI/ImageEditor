package com.dididi.lib_image_edit.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BlendMode
import android.graphics.Matrix
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.renderscript.Float2
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.max
import kotlin.math.min


/**
 * @author dididi(yechao)
 * @since 29/06/2020
 * @describe 背景图片，实现了拖拽移动，和多指缩放等操作
 */

@Suppress("MemberVisibilityCanBePrivate")
class BackgroundImageView : AppCompatImageView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //缩放操作由ScaleDetector处理
        mScaleDetector.onTouchEvent(event)
        //移动背景图片
        translationBackground(event)
        return true
    }

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

    var onImageChangeListener:Function1<Bitmap?,Unit>? = null

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        onImageChangeListener?.invoke(getBitmap())
    }

    override fun setImageIcon(icon: Icon?) {
        super.setImageIcon(icon)
        onImageChangeListener?.invoke(getBitmap())
    }

    override fun setImageMatrix(matrix: Matrix?) {
        super.setImageMatrix(matrix)
        onImageChangeListener?.invoke(getBitmap())
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        onImageChangeListener?.invoke(getBitmap())
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        onImageChangeListener?.invoke(getBitmap())
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        onImageChangeListener?.invoke(getBitmap())
    }

    override fun setImageTintList(tint: ColorStateList?) {
        super.setImageTintList(tint)
        onImageChangeListener?.invoke(getBitmap())
    }

    override fun setImageTintMode(tintMode: PorterDuff.Mode?) {
        super.setImageTintMode(tintMode)
        onImageChangeListener?.invoke(getBitmap())
    }

    override fun setImageAlpha(alpha: Int) {
        super.setImageAlpha(alpha)
        onImageChangeListener?.invoke(getBitmap())
    }

    override fun setImageTintBlendMode(blendMode: BlendMode?) {
        super.setImageTintBlendMode(blendMode)
        onImageChangeListener?.invoke(getBitmap())
    }

    override fun setImageLevel(level: Int) {
        super.setImageLevel(level)
        onImageChangeListener?.invoke(getBitmap())
    }

    override fun setImageState(state: IntArray?, merge: Boolean) {
        super.setImageState(state, merge)
        onImageChangeListener?.invoke(getBitmap())
    }

    fun getBitmap() = if (drawable != null) {
        (drawable as BitmapDrawable).bitmap
    } else {
        null
    }
}

