package com.dididi.lib_image_edit.view

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout


/**
 * @author dididi(yechao)
 * @since 29/06/2020
 * @describe 图片编辑控件，组合 [BackgroundImageView] [BrushDrawingView] [ImageFilterView]
 */

class ImageEditView :RelativeLayout{
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ){
        initView()
    }

    private fun initView(){
        val background = BackgroundImageView(context)

    }
}