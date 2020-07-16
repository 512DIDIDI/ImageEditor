package com.dididi.lib_image_edit.controller

import android.content.Context
import android.view.View
import com.dididi.lib_image_edit.view.CustomView
import com.dididi.lib_image_edit.view.ImageEditView


/**
 * @author dididi(yechao)
 * @since 29/06/2020
 * @describe 图片编辑controller层 提供外部通用接口
 */

@Suppress("unused")
class ImageEditor private constructor(builder: Builder) {

    class Builder(context: Context, private val imageEditView: ImageEditView) {

        internal var parentView = imageEditView
        internal var brushDrawingView = imageEditView.brushDrawingView
        internal var backgroundImageView = imageEditView.backgroundImageView
        internal var addTextView = imageEditView.outlineTextView
        internal var customViews = imageEditView.customViews

        /**
         * 增加自定义view的list
         */
        fun addCustomViews(views: MutableList<CustomView>): Builder {
            customViews.clear()
            imageEditView.customViews.clear()
            imageEditView.customViews = views
            customViews = views
            return this
        }

        /**
         * 增加单个自定义view
         */
        fun addCustomView(view: CustomView): Builder {
            customViews.add(view)
            imageEditView.customViews.add(view)
            return this
        }

        fun build() = ImageEditor(this)
    }

    private val mParentView = builder.parentView
    private val mBrushDrawingView = builder.brushDrawingView
    val backgroundImageView = builder.backgroundImageView
    private val mCustomViews = builder.customViews
    private val mAddViews = mutableListOf<View>()
    private val mRedoViews = mutableListOf<View>()
    private val mTextView = builder.addTextView


    fun changePaintMode() {
        mBrushDrawingView.paintMode = !mBrushDrawingView.paintMode
        mBrushDrawingView.visibility = View.VISIBLE
    }

    fun changeEraserMode(){
        mBrushDrawingView.eraserMode = !mBrushDrawingView.eraserMode
        mBrushDrawingView.visibility = View.VISIBLE
    }

    fun setText(text:CharSequence){
        mTextView.text = text
    }

    fun undo() {
        mBrushDrawingView.undo()
    }

    fun redo() {
        mBrushDrawingView.redo()
    }

}