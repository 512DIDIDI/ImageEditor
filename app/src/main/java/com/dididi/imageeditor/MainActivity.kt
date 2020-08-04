package com.dididi.imageeditor

import android.Manifest
import android.app.Activity
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.dididi.imageeditor.adpater.ColorPickAdapter
import com.dididi.imageeditor.adpater.ToolsAdapter
import com.dididi.imageeditor.adpater.ToolsType
import com.dididi.imageeditor.dialog.TextDialog
import com.dididi.imageeditor.util.getPictureDegree
import com.dididi.imageeditor.util.rotateImage
import com.dididi.lib_image_edit.controller.ImageEditor
import com.dididi.lib_image_edit.view.BrushDrawingView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("SameParameterValue")
class MainActivity : AppCompatActivity() ,SeekBar.OnSeekBarChangeListener{

    companion object {
        const val OPEN_ALBUM = 1
        const val OPEN_CAMERA = 2
        const val PERMISSION_CODE = 3
    }

    private val imageEditor:ImageEditor by lazy {
        ImageEditor.Builder(this,findViewById(R.id.activityMainBackgroundImage))
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityMainOpenAlbum.setOnClickListener {
            applyWritePermission(OPEN_ALBUM) {
                openAlbum()
            }
        }
        activityMainCamera.setOnClickListener {
            applyCameraPermission(OPEN_CAMERA) {
                openCamera()
            }
        }
        val adapter = ToolsAdapter()
        adapter.itemClickListener = {
            activityMainExitMode.text = resources.getString(R.string.reset_position)
            when (it) {
                ToolsType.BRUSH -> {
                    activityMainExitMode.text = resources.getString(R.string.exit_paint_mode)
                    showBrushDialog()
                }
                ToolsType.TEXT -> showTextDialog()
                ToolsType.ERASER -> {
                    activityMainExitMode.text = resources.getString(R.string.exit_paint_mode)
                    imageEditor.setPaintMode(BrushDrawingView.PaintMode.ERASER)
                }
                ToolsType.FILTER -> {
                    Toast.makeText(this, "not implementation", Toast.LENGTH_SHORT).show()
                }
                ToolsType.EMOJI -> {
                    showEmojiDialog()
                }
            }
        }
        activityMainToolsRv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        activityMainToolsRv.adapter = adapter
        activityMainUndo.setOnClickListener {
            imageEditor.undo()
        }
        activityMainRedo.setOnClickListener {
            imageEditor.redo()
        }
        activityMainClose.setOnClickListener {
            finish()
        }
        activityMainExitMode.setOnClickListener {
            if (imageEditor.isPaintMode) {
                imageEditor.exitPaintMode()
                activityMainExitMode.text = resources.getString(R.string.reset_position)
            } else {
                Toast.makeText(this, "not implementation", Toast.LENGTH_SHORT).show()
            }
        }
        activityMainSave.setOnClickListener {
            Toast.makeText(this, "not implementation", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showBrushDialog() {
        val dialog = MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            customView(R.layout.dialog_brush_pick)
        }
        val view = dialog.getCustomView()
        val colorAdapter = ColorPickAdapter(this)
        colorAdapter.colorChangeListener = {
            imageEditor.setBrushColor(it)
            imageEditor.setPaintMode(BrushDrawingView.PaintMode.PAINT)
            dialog.dismiss()
        }
        val colorRv = view.findViewById<RecyclerView>(R.id.dialogBrushPickColorRv)
        colorRv.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        colorRv.adapter = colorAdapter
        val thicknessSb = view.findViewById<SeekBar>(R.id.dialogBrushPickThickness)
        val opacitySb = view.findViewById<SeekBar>(R.id.dialogBrushPickOpacity)
        thicknessSb.setOnSeekBarChangeListener(this)
        opacitySb.setOnSeekBarChangeListener(this)
    }

    private fun showTextDialog() {
        val dialog = TextDialog.show(this)
        dialog.textFinishListener = { content, color ->
            imageEditor.addText(content, color)
        }
    }

    private fun showEmojiDialog(){
        val dialog = MaterialDialog(this,BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            customView(R.layout.dialog_emoji)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        when (seekBar?.id) {
            R.id.dialogBrushPickThickness -> {
                imageEditor.setBrushThickness(progress.toFloat())
            }
            R.id.dialogBrushPickOpacity -> {
                imageEditor.setBrushOpacity(progress.toFloat())
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    //region 相机/相册业务逻辑

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            OPEN_CAMERA -> {
                permissionHint(grantResults, "没有读写权限") {
                    openCamera()
                }
            }
            OPEN_ALBUM -> {
                permissionHint(grantResults, "没有读写权限") {
                    openAlbum()
                }
            }
            PERMISSION_CODE -> {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "未全部授权，无法使用", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                Toast.makeText(this, "没有权限", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun permissionHint(grantResults: IntArray, msg: String, target: () -> Unit) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            target()
        } else {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            OPEN_CAMERA -> {
                if (resultCode == Activity.RESULT_OK) {
                    BitmapFactory.decodeFile(currentPhotoPath).also {
                        val bitmap = it.rotateImage(getPictureDegree(currentPhotoPath).toFloat())
                        imageEditor.backgroundImageView.setImageBitmap(bitmap)
                    }
                }
            }
            OPEN_ALBUM -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    imageEditor.backgroundImageView.setImageBitmap(getImagePath(data))
                }
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
     * 打开相机
     */
    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager)?.also {
                val photoFile = try {
                    createImageFile()
                } catch (e: IOException) {
                    null
                }
                photoFile?.also {
                    val photoUri = FileProvider.getUriForFile(
                        this,
                        "${BuildConfig.APPLICATION_ID}.provider",
                        it
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(intent, OPEN_CAMERA)
                }
            }
        }
    }

    private lateinit var currentPhotoPath: String

    /**
     * 创建相机临时图片
     */
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_$timeStamp",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
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

    private fun applyCameraPermission(requestCode: Int, target: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                target()
            } else {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), requestCode)
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
            FileProvider.getUriForFile(
                this,
                "${BuildConfig.APPLICATION_ID}.provider",
                File(imagePath)
            )
        } else {
            Uri.parse(imagePath)
        }
        return MediaStore.Images.Media.getBitmap(this.contentResolver, oriUri).rotateImage(
            getPictureDegree(imagePath).toFloat()
        )
    }



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

    //endregion
}