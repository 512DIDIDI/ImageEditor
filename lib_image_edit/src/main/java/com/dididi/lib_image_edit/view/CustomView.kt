package com.dididi.lib_image_edit.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout


/**
 * @author dididi(yechao)
 * @since 30/06/2020
 * @describe 自定义View ，可根据需求拓展图片编辑功能
 */

abstract class CustomView :View{
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /**
     * 设置视图id
     * 注意：viewId需要大于3，因为1，2，3的id被占用了
     */
    abstract fun setViewId():Int

    /**
     * 设置view的参数位置，
     * 注意：父布局是一个RelativeLayout [ImageEditView]
     */
    abstract fun setLayoutParams():RelativeLayout.LayoutParams
}