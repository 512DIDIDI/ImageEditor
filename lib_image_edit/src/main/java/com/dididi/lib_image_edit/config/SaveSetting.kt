package com.dididi.lib_image_edit.config

import android.graphics.Bitmap
import androidx.annotation.IntRange


/**
 * @author dididi(yechao)
 * @since 10/08/2020
 * @describe 保存的文件配置信息
 */

class SaveSetting private constructor(private val builder:Builder){

    val compressFormat = builder.compressFormat
    val compressQuality = builder.compressQuality

    class Builder{
        /**文件压缩编码格式*/
        var compressFormat = Bitmap.CompressFormat.PNG
        /**图片质量 0(差)-100(优) */
        @IntRange(from = 0L,to = 100L)
        var compressQuality = 100

        fun build() = SaveSetting(this)
    }
}