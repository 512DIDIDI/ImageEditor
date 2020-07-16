package com.dididi.lib_image_edit.view

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
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView


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

    var onImageChangeListener: ((Bitmap?) -> Unit)? = null

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

