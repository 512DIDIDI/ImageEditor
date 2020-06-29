package com.dididi.lib_image_edit.view

import android.content.Context
import android.util.AttributeSet
import android.view.View


/**
 * @author dididi(yechao)
 * @since 29/06/2020
 * @describe 绘画view
 */

class BrushDrawingView :View{
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )
}