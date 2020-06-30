package com.dididi.lib_image_edit.view

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.util.AttributeSet


/**
 * @author dididi(yechao)
 * @since 29/06/2020
 * @describe 图片渲染视图 主要负责渲染图片以及增加滤镜
 */

@Suppress("UNUSED_PARAMETER")
class ImageFilterView :GLSurfaceView {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : super(context,attrs)

    fun setFilterEffect(){}

    fun setSourceBitmap(src:Bitmap?){}
}