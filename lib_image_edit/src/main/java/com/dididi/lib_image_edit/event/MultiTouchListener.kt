package com.dididi.lib_image_edit.event

import android.content.Context
import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.dididi.lib_image_edit.view.OutlineTextView
import kotlin.math.max
import kotlin.math.min


/**
 * @author dididi(yechao)
 * @since 28/07/2020
 * @describe 负责处理控件的touch事件
 */

class MultiTouchListener(val context:Context) :View.OnTouchListener{

    companion object{
        internal var minScale = 0.1f
        internal var maxScale = 5f
    }

    /**初始位置*/
    private var mLocationPoint: PointF? = null

    /**位移量*/
    private var mTranslatePoint = PointF()

    /**是否正在执行缩放操作*/
    private var isScaling = false

    /**多指操作缩放*/
    private val mScaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        /**缩放倍数*/
        private var mScaleFactor = 1f

        override fun onScale(view:View,scaleGestureDetector: ScaleGestureDetector): Boolean {
            mScaleFactor *= scaleGestureDetector.scaleFactor
            //限定缩放倍数的范围
            mScaleFactor = max(minScale, min(mScaleFactor, maxScale))
            when {
                mScaleFactor <= minScale -> {
                    Toast.makeText(context, "已经最小了", Toast.LENGTH_SHORT).show()
                }
                mScaleFactor >= maxScale -> {

                    Toast.makeText(context, "已经最大了", Toast.LENGTH_SHORT).show()
                }
            }
            view.apply {
                scaleX = mScaleFactor
                scaleY = mScaleFactor
            }
            isScaling = true
            return true
        }

    }
    private val mScaleDetector = ScaleGestureDetector(mScaleListener)

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        //缩放操作由ScaleDetector处理
        mScaleDetector.onTouchEvent(v,event)
        //移动背景图片
        translationBackground(v,event)
        return true
    }

    /**平移背景图片*/
    private fun translationBackground(v: View?,event: MotionEvent?) {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (event.pointerCount == 1 && !isScaling) {
                    //记录初始值
                    mLocationPoint = PointF(event.rawX, event.rawY)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                //控制单指移动 且不在放缩时 平移
                if (event.pointerCount == 1 && !isScaling) {
                    //计算位移量
                    mTranslatePoint.x += event.rawX - mLocationPoint!!.x
                    mTranslatePoint.y += event.rawY - mLocationPoint!!.y
                    //平移view
                    v?.apply {
                        translationX = mTranslatePoint.x
                        translationY = mTranslatePoint.y
                    }
                    mLocationPoint = PointF(event.rawX, event.rawY)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                mLocationPoint = null
                //因为手指抬起会重新计算focusView的真实位置，所以需要将位移记录清零
                mTranslatePoint = PointF()
                v?.apply {
                    //因为translation变换，实际view的位置并不会发生变化还是在原位置，因此，需重新计算view的left top等
                    left += translationX.toInt()
                    right += translationX.toInt()
                    top += translationY.toInt()
                    bottom += translationY.toInt()
                    //清零focusView的位移量
                    translationX = 0f
                    translationY = 0f
                }
                //手指抬起时，缩放才结束
                isScaling = false
            }
        }
    }
}