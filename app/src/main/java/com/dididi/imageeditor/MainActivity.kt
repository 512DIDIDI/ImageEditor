package com.dididi.imageeditor

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

@Suppress("SameParameterValue")
class MainActivity : AppCompatActivity() {

    companion object {
        const val OPEN_ALBUM = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityMainChooseImageBtn.setOnClickListener {
            applyWritePermission(OPEN_ALBUM) {
                openAlbum()
            }
        }
        activityMainPaintBtn.setOnClickListener {
            activityMainBackgroundImage.brushDrawingView.paintMode =
                !activityMainBackgroundImage.brushDrawingView.paintMode
            activityMainBackgroundImage.brushDrawingView.visibility = View.VISIBLE
        }
        activityMainEraserBtn.setOnClickListener {
            activityMainBackgroundImage.brushDrawingView.eraserMode =
                !activityMainBackgroundImage.brushDrawingView.eraserMode
        }
        activityMainUndoBtn.setOnClickListener {
            activityMainBackgroundImage.brushDrawingView.undo()
        }
        activityMainRedoBtn.setOnClickListener {
            activityMainBackgroundImage.brushDrawingView.redo()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == OPEN_ALBUM) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openAlbum()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OPEN_ALBUM) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                activityMainBackgroundImage.backgroundImageView.setImageBitmap(getImagePath(data))
            }
        }
    }

    private fun openAlbum() {
        Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            startActivityForResult(this, OPEN_ALBUM)
        }
    }

    /**
     * 请求读写权限
     * @param requestCode 请求码
     * @param target 要做什么
     */
    private fun applyWritePermission(requestCode: Int, target: () -> Unit) {
        val permissions = listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        //android6.0之后，需要动态申请读写权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //读写是否已经授权
            val check = ContextCompat.checkSelfPermission(this, permissions[0])
            if (check == PackageManager.PERMISSION_GRANTED) {
                target()
            } else {
                //如果未发现授权，则请求权限
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    requestCode
                )
            }
        } else {
            target()
        }
    }

    private fun getImagePath(data: Intent?): Bitmap? {
        if (data == null) return null
        val imagePath = handleImageAfterKitKat(data)
        val oriUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //7.0适配(此处的authority为${applicationId}.provider)
            FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", File(imagePath))
        } else {
            Uri.parse(imagePath)
        }
        return MediaStore.Images.Media.getBitmap(this.contentResolver, oriUri)
    }


    /**
     * android4.4之后，需要解析获取图片真实路径
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun handleImageAfterKitKat(data: Intent): String {
        val uri = data.data
        var imagePath = ""
        //document类型的Uri
        when {
            DocumentsContract.isDocumentUri(this, uri) -> {
                //通过documentId处理
                val docId = DocumentsContract.getDocumentId(uri)
                when (uri?.authority) {
                    "com.android.externalstorage.documents" -> {
                        val type = docId.split(":")[0]
                        if ("primary".equals(type, ignoreCase = true)) {
                            imagePath = Environment.getExternalStorageDirectory()
                                .toString() + "/" + docId.split(":")[1]
                        }
                    }
                    //media类型解析
                    "com.android.providers.media.documents" -> {
                        val id = docId.split(":")[1]
                        val type = docId.split(":")[0]
                        val contentUri: Uri? = when (type) {
                            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                            else -> null
                        }
                        val selection = "_id=?"
                        val selectionArgs: Array<String> = arrayOf(id)
                        imagePath = getImagePath(contentUri!!, selection, selectionArgs)!!
                    }
                    //downloads文件解析
                    "com.android.providers.downloads.documents" -> {
                        ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"), docId.toLong()
                        ).apply {
                            imagePath = getImagePath(this, null, null)!!
                        }
                    }
                    else -> {
                    }
                }
            }
            "content".equals(uri?.scheme, ignoreCase = true) ->
                //content类型数据不需要解析，直接传入生成即可
                imagePath = getImagePath(uri!!, null, null)!!
            "file".equals(uri?.scheme, ignoreCase = true) ->
                //file类型的uri直接获取图片路径即可
                imagePath = uri!!.path!!
        }
        return imagePath
    }

    /**
     * android4.4之前可直接获取图片真实uri
     */
    private fun handleImageBeforeKitKat(data: Intent): String {
        val uri = data.data
        return getImagePath(uri!!, null, null)!!
    }

    /**
     * 解析uri及selection
     * 获取图片真实路径
     */
    private fun getImagePath(uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
        var cursor: Cursor? = null
        try {
            cursor = this.contentResolver.query(uri, null, selection, selectionArgs, null)
            if (cursor?.moveToFirst()!!) {
                return cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
        } finally {
            cursor?.close()
        }
        return null
    }
}