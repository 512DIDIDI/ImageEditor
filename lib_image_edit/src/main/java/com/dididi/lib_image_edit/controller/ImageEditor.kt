package com.dididi.lib_image_edit.controller

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.RelativeLayout
import androidx.annotation.ColorInt
import com.dididi.lib_image_edit.view.BrushDrawingView
import com.dididi.lib_image_edit.view.ImageEditView
import com.dididi.lib_image_edit.view.OutlineTextView


/**
 * @author dididi(yechao)
 * @since 29/06/2020
 * @describe 图片编辑controller层 提供外部通用接口
 */

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

    init {
        mBrushDrawingView.brushDrawingListener = this
    }

    //region brush api

    /**是否处于绘制模式*/
    var isPaintMode = mBrushDrawingView.paintMode

    fun setBrushThickness(thickness: Float) {
        mBrushDrawingView.paintWidth = thickness
    }

    fun setBrushOpacity(opacity: Float) {
        mBrushDrawingView.paintOpacity = (opacity * 2.56).toInt()
    }

    fun setBrushColor(@ColorInt color: Int) {
        mBrushDrawingView.paintColor = color
    }

    /**
     * 设置绘制模式
     * @param paintMode 绘制 or 橡皮擦
     */
    fun setPaintMode(paintMode: BrushDrawingView.PaintMode) {
        when (paintMode) {
            BrushDrawingView.PaintMode.PAINT -> mBrushDrawingView.paintMode = true
            BrushDrawingView.PaintMode.ERASER -> mBrushDrawingView.eraserMode = true
        }
        mBrushDrawingView.visibility = View.VISIBLE
    }

    /**退出绘制模式*/
    fun exitPaintMode() {
        /**改变橡皮擦模式也会同时修改paintMode*/
        mBrushDrawingView.eraserMode = false
    }
    //endregion

    //region add view api
    /**
     * 添加文本水印
     * @param text 文字
     */
    fun addText(text: CharSequence, textColor: Int = Color.WHITE) {
        //创建文本控件
        OutlineTextView(builder.context).apply {
            setPadding(20, 20, 20, 20)
            setText(text)

            setTextColor(textColor)
            addViewToParent(this)
        }
    }

    /**
     * 添加表情包
     */
    fun addEmoji(){

    }

    /**
     * 添加到parentView当中，也就是ImageEditView
     */
    private fun addViewToParent(view: View) {
        exitPaintMode()
        val param = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).also {
            it.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
        }
        mParentView.addView(view, param)
        mAddViews.add(view)
        //添加新view，前进栈清空
        mRedoViews.clear()
    }

    //endregion

    //region undo/redo api

    /**
     * 撤销上一步操作
     */
    fun undo(): Boolean {
        exitPaintMode()
        if (mAddViews.isNotEmpty()) {
            val removeView = mAddViews[mAddViews.size - 1]
            //判断当前要撤销的是否是画笔
            if (removeView is BrushDrawingView) {
                //用mBrushDrawingView，共享画笔内部的前进后退栈
                return mBrushDrawingView.undo()
            }
            //不是画笔，就正常从addView和parentView中移除，添加到redoViews中
            mAddViews.removeAt(mAddViews.size - 1)
            mParentView.removeView(removeView)
            mRedoViews.add(removeView)
        }
        return mAddViews.isNotEmpty()
    }

    /**
     * redo撤销的历史记录
     * 从[mRedoViews]中读取记录，再添加到[mAddViews]中保存记录
     */
    fun redo(): Boolean {
        exitPaintMode()
        if (mRedoViews.isNotEmpty()) {
            val redoView = mRedoViews[mRedoViews.size - 1]
            if (redoView is BrushDrawingView) {
                //用mBrushDrawingView，共享画笔内部的前进后退栈
                return mBrushDrawingView.redo()
            }
            mRedoViews.removeAt(mRedoViews.size - 1)
            mParentView.addView(redoView)
            mAddViews.add(redoView)
        }
        return mRedoViews.isNotEmpty()
    }

    /**
     * 移除[BrushDrawingView] 在调用[BrushDrawingView.undo]时调用
     */
    override fun removeView(brushDrawingView: BrushDrawingView) {
        if (mAddViews.isNotEmpty()) {
            val removeView = mAddViews.removeAt(mAddViews.size-1)
            //画笔是固定写死的，所以不需要从parentView中移除，只需要移除后续addView添加的内容就行
            if (removeView !is BrushDrawingView){
                mParentView.removeView(removeView)
            }
            mRedoViews.add(removeView)
        }
    }

    /**
     * 添加[BrushDrawingView]
     * 在两种情况下调用 1.绘画开始时 2.[BrushDrawingView.redo]
     */
    override fun addView(brushDrawingView: BrushDrawingView) {
        if (mRedoViews.isNotEmpty()) {
            mRedoViews.removeAt(mRedoViews.size - 1)
        }
        mAddViews.add(brushDrawingView)
    }

    override fun startDrawing() {
        //开始绘画，清空redo栈
        mRedoViews.clear()
    }

    override fun stopDrawing() {
    }

    //endregion
}