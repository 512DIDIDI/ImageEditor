package com.dididi.lib_image_edit.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.math.abs


/**
 * @author dididi(yechao)
 * @since 29/06/2020
 * @describe 画笔/橡皮擦 view 在[BackgroundImageView]之上，其他[CustomView]之下
 */

@Suppress("unused", "MemberVisibilityCanBePrivate")
class BrushDrawingView : View {

    companion object {
        /**绘制触发阈值*/
        const val TOUCH_TOLERANCE = 4f
    }

    /**绘制模式*/
    enum class PaintMode {
        /**绘画模式*/
        PAINT,
        /**橡皮擦模式*/
        ERASER
    }

    /**
     * 绘画动作监听
     */
    interface OnBrushDrawingListener {
        /**添加绘画路径*/
        fun addView(brushDrawingView: BrushDrawingView)

        /**移除绘画路径*/
        fun removeView(brushDrawingView: BrushDrawingView)

        /**开始绘画*/
        fun startDrawing()
        /**停止绘画*/
        fun stopDrawing()
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var mPaintPath = Path()
    private val mPaint = Paint()
    private val paintXfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_OVER)
    private val eraserXfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    init {
        resetPaint()
    }

    internal var brushDrawingListener:OnBrushDrawingListener? = null
    /**绘制画笔的颜色*/
    internal var paintColor = Color.BLACK
        set(value) {
            field = value
            mPaint.color = value
        }

    /**画笔粗细*/
    internal var paintWidth = 20f
        set(value) {
            field = value
            mPaint.strokeWidth = value
        }

    /**画笔透明度*/
    internal var paintOpacity = 255
        set(value) {
            field = value
            mPaint.alpha = value
        }

    /**是否是绘制模式*/
    internal var paintMode = false
        set(value) {
            field = value
            mPaint.xfermode = if (value) paintXfermode else null
        }

    /**是否是橡皮擦模式*/
    internal var eraserMode = false
        set(value) {
            field = value
            paintMode = value
            mPaint.xfermode = if (value) eraserXfermode else null
        }

    /**当前触摸位置x坐标*/
    private var mCurrentX = 0f

    /**当前触摸位置y坐标*/
    private var mCurrentY = 0f

    /**历史路径，后退栈*/
    private val mDrawnPaths = Stack<PathPaint>()

    /**后退栈push出来的路径，前进栈*/
    private val mRedoPaths = Stack<PathPaint>()

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            //从历史路径中画，但会延迟，手指抬起才能画出来，因此需要drawPath(mPaintPath,mPaint)
            mDrawnPaths.forEach {
                //这里实际调用的是新的path和paint实例，并不是 mPaintPath和mPaint引用
                //否则橡皮擦模式时，会全局修改mPaint的模式，改变历史路径
                drawPath(it.path, it.paint)
            }
            //画出跟随手指移动的路径
            drawPath(mPaintPath, mPaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //如果处于painting模式，则直接拦截触摸事件
        if (paintMode) {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    //获取起点
                    touchStart(event)
                }
                MotionEvent.ACTION_MOVE -> {
                    //连接移动中的点
                    touchMove(event)
                }
                MotionEvent.ACTION_UP -> {
                    //触摸事件结束
                    touchUp()
                }
            }
            invalidate()
            return true
        } else {
            return false
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mDrawnPaths.clear()
        mRedoPaths.clear()
        paintMode = false
        eraserMode = false
    }

    /**
     * 获取触摸起点
     */
    private fun touchStart(event: MotionEvent) {
        //一旦开始画新的，清空前进栈
        mRedoPaths.clear()
        mPaintPath.reset()
        mPaintPath.moveTo(event.x, event.y)
        mCurrentX = event.x
        mCurrentY = event.y
        brushDrawingListener?.startDrawing()
    }

    /**
     * 获取移动中的点
     */
    private fun touchMove(event: MotionEvent) {
        val dx = abs(event.x - mCurrentX)
        val dy = abs(event.y - mCurrentY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            //如果超过阈值，则用曲线连接移动中的点
            mPaintPath.quadTo(
                mCurrentX,
                mCurrentY,
                (event.x + mCurrentX) / 2,
                (event.y + mCurrentY) / 2
            )
            mCurrentX = event.x
            mCurrentY = event.y
        }
    }

    /**
     * 手指抬起，压入栈中
     */
    private fun touchUp() {
        mDrawnPaths.push(PathPaint(mPaintPath, mPaint))
        mPaintPath = Path()
        brushDrawingListener?.stopDrawing()
        brushDrawingListener?.addView(this)
    }

    /**重置画笔*/
    internal fun resetPaint(){
        setLayerType(LAYER_TYPE_SOFTWARE,null)
        mPaint.apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            color = Color.BLACK
            strokeWidth = 20f
        }
    }

    /**
     * 清除画布
     */
    internal fun clearAll() {
        mDrawnPaths.clear()
        mRedoPaths.clear()
        invalidate()
    }

    /**
     * 撤销上一步画笔
     * @return 撤销成功返回true，否则false
     */
    internal fun undo(): Boolean {
        brushDrawingListener?.removeView(this)
        return if (mDrawnPaths.isNotEmpty()) {
            mRedoPaths.push(mDrawnPaths.pop())
            invalidate()
            true
        } else {
            false
        }
    }

    /**
     * redo撤销的历史记录
     */
    internal fun redo(): Boolean {
        brushDrawingListener?.addView(this)
        return if (mRedoPaths.isNotEmpty()) {
            mDrawnPaths.push(mRedoPaths.pop())
            invalidate()
            true
        } else {
            false
        }
    }

    /**
     * 获取path和paint的实例，避免配置污染历史记录
     */
    private class PathPaint(path: Path, paint: Paint) {
        //注意必须创建新的path与paint实例，不能直接使用引用
        var path = Path(path)
            private set
        var paint = Paint(paint)
            private set
    }
}