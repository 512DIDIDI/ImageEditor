package com.dididi.lib_image_edit.event

import android.graphics.Bitmap


/**
 * @author dididi(yechao)
 * @since 10/08/2020
 * @describe 保存图片的回调
 */

interface OnSaveListener {
    /**
     * 图片保存成功
     */
    fun onSuccess(bitmap: Bitmap)

    /**
     * 图片保存失败
     */
    fun failure()
}