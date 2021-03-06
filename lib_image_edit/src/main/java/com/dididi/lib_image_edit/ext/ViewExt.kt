package com.dididi.lib_image_edit.ext

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View


/**
 * @author dididi(yechao)
 * @since 10/08/2020
 * @describe view拓展函数
 */

/**
 * 获取view的bitmap，截取view的当前画面
 */
fun View.getBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.draw(canvas)
    return bitmap
}