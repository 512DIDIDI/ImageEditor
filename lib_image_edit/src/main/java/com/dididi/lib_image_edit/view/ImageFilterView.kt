package com.dididi.lib_image_edit.view

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.dididi.lib_image_edit.const.ImageFilter
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


/**
 * @author dididi(yechao)
 * @since 29/06/2020
 * @describe 图片渲染视图 主要负责渲染图片以及增加滤镜
 */

@Suppress("UNUSED_PARAMETER")
class ImageFilterView(context: Context, attrs: AttributeSet?) : GLSurfaceView(context, attrs),
    GLSurfaceView.Renderer {
    constructor(context: Context) : this(context, null)

    /**当前滤镜*/
    private var mCurrentFilter = ImageFilter.ORIGIN
    /**接收到的[BackgroundImageView]中的bitmap*/
    private var mSourceBitmap:Bitmap? = null
    /***/
    private var isInitialized = false

    init {
        //使用openGL ES 2.0
        setEGLContextClientVersion(2)
        //设置渲染器，开启GL线程进行渲染工作
        setRenderer(this)
        //仅当创建surface或调用requestRender时渲染
        renderMode = RENDERMODE_WHEN_DIRTY
        //设置初始滤镜
        setFilterEffect(ImageFilter.ORIGIN)
    }

    internal fun setFilterEffect(imageFilter: ImageFilter) {
        mCurrentFilter = imageFilter
        requestRender()
    }

    internal fun setSourceBitmap(src: Bitmap?) {
        mSourceBitmap = src
        isInitialized = false
    }

    /**执行渲染工作*/
    override fun onDrawFrame(gl: GL10?) {
        TODO("Not yet implemented")
    }

    /**渲染窗口大小发生改变的处理*/
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        TODO("Not yet implemented")
    }

    /**surface被创建后需要做的处理*/
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        TODO("Not yet implemented")
    }
}