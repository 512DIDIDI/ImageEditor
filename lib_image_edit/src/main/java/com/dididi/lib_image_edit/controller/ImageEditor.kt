package com.dididi.lib_image_edit.controller

import android.content.Context
import com.dididi.lib_image_edit.view.CustomView
import com.dididi.lib_image_edit.view.ImageEditView


/**
 * @author dididi(yechao)
 * @since 29/06/2020
 * @describe 图片编辑controller层 提供外部通用接口
 */

@Suppress("unused")
class ImageEditor private constructor() {

    companion object {
        class Builder(context: Context, imageEditView: ImageEditView) {

            private var parentView = imageEditView
            private var brushDrawingView = imageEditView.brushDrawingView
            private var backgroundImageView = imageEditView.backgroundImageView
            private var customViews = imageEditView.customViews

            /**
             * 增加自定义view的list
             */
            fun addCustomViews(views: MutableList<CustomView>): Builder {
                customViews.clear()
                customViews = views
                return this
            }

            /**
             * 增加单个自定义view
             */
            fun addCustomView(view:CustomView):Builder{
                customViews.add(view)
                return this
            }

            fun build() = ImageEditor()
        }
    }

    fun undo(){}

    fun redo(){}

}