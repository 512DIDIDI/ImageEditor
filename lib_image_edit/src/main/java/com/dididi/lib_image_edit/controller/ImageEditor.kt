package com.dididi.lib_image_edit.controller

import android.content.Context
import android.view.View
import android.widget.RelativeLayout
import com.dididi.lib_image_edit.view.BrushDrawingView
import com.dididi.lib_image_edit.view.ImageEditView
import com.dididi.lib_image_edit.view.OutlineTextView


/**
 * @author dididi(yechao)
 * @since 29/06/2020
 * @describe 图片编辑controller层 提供外部通用接口
 */

@Suppress("unused")
class ImageEditor private constructor(private val builder: Builder) :
    BrushDrawingView.OnBrushDrawingListener {

    class Builder(val context: Context, imageEditView: ImageEditView) {

        internal var parentView = imageEditView
        internal var brushDrawingView = imageEditView.brushDrawingView
        internal var backgroundImageView = imageEditView.backgroundImageView

        fun build() = ImageEditor(this)
    }

    private val mParentView = builder.parentView
    private val mBrushDrawingView = builder.brushDrawingView
    val backgroundImageView = builder.backgroundImageView
    private val mAddViews = mutableListOf<View>()
    private val mRedoViews = mutableListOf<View>()


    fun changePaintMode() {
        mBrushDrawingView.paintMode = !mBrushDrawingView.paintMode

        mBrushDrawingView.visibility = View.VISIBLE
    }

    fun changeEraserMode() {
        mBrushDrawingView.eraserMode = !mBrushDrawingView.eraserMode
        mAddViews.add(mBrushDrawingView)
        mBrushDrawingView.visibility = View.VISIBLE
    }

    fun addText(text: CharSequence) {
        mBrushDrawingView.eraserMode = false
        //创建文本控件
        OutlineTextView(builder.context).apply {
            val textParams =
                RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).also {
                    it.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
                }
            setPadding(20, 20, 20, 20)
            setText(text)
            //添加到parentView当中，也就是ImageEditView
            mParentView.addView(this, textParams)
            mAddViews.add(this)
        }
    }

    /**
     * 撤销上一步操作
     */
    fun undo(): Boolean {
        if (mAddViews.isNotEmpty()) {
            val removeView = mAddViews[mAddViews.size - 1]
            //判断当前要撤销的是否是画笔
            if (removeView is BrushDrawingView) {
                return removeView.undo()
            }
            mAddViews.remove(removeView)
            mParentView.removeView(removeView)
            mRedoViews.add(removeView)
            return true
        }
        return false
    }

    /**
     * redo撤销的历史记录
     */
    fun redo(): Boolean {
        if (mRedoViews.isNotEmpty()) {
            val redoView = mRedoViews[mRedoViews.size - 1]
            if (redoView is BrushDrawingView) {
                return redoView.redo()
            }
            mParentView.addView(redoView)
            mAddViews.add(redoView)
            mRedoViews.remove(redoView)
            return true
        }
        return false
    }

    /**
     * 添加[BrushDrawingView]，在调用绘画[touchUp] [BrushDrawingView.redo]时调用
     */
    override fun addView(brushDrawingView: BrushDrawingView) {
        if (mRedoViews.isNotEmpty()) {
            mRedoViews.removeAt(mRedoViews.size - 1)
        }
        mAddViews.add(brushDrawingView)
    }

    /**
     * 移除[BrushDrawingView] 在调用[BrushDrawingView.undo]时调用
     */
    override fun removeView(brushDrawingView: BrushDrawingView) {
        if (mAddViews.isNotEmpty()) {
            val removeView = mAddViews.removeAt(mAddViews.size-1)
            //发生removeView不是BrushDrawingView的情况，说明此时BrushDrawingView中已经没有内容了
            if (removeView !is BrushDrawingView){
                mParentView.removeView(removeView)
            }
            mRedoViews.add(brushDrawingView)
        }
    }

    override fun startDrawing() {
    }

    override fun stopDrawing() {
    }
}