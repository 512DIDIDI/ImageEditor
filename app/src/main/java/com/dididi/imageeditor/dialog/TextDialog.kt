package com.dididi.imageeditor.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dididi.imageeditor.R
import com.dididi.imageeditor.adpater.ColorPickAdapter
import com.dididi.lib_image_edit.view.OutlineTextView


/**
 * @author dididi(yechao)
 * @since 31/07/2020
 * @describe 填写文字弹出框
 */

typealias finish = (CharSequence, Int) -> Unit

class TextDialog private constructor() : DialogFragment(), View.OnClickListener {

    companion object {

        private const val EXTRA_CONTENT = "content"
        private const val EXTRA_COLOR = "color"

        fun show(
            activity: AppCompatActivity,
            content: String = "",
            @ColorInt color: Int = Color.WHITE
        ): TextDialog {
            val bundle = Bundle()
            bundle.putString(EXTRA_CONTENT, content)
            bundle.putInt(EXTRA_COLOR, color)
            val dialog = TextDialog()
            dialog.arguments = bundle
            dialog.show(activity.supportFragmentManager, "textDialog")
            return dialog
        }
    }

    @ColorInt
    private var mColor: Int = Color.WHITE

    var textFinishListener: finish? = null

    private lateinit var mContentEt: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.custom_dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView = inflater.inflate(R.layout.dialog_text, container, false)
        val cancel = itemView.findViewById<OutlineTextView>(R.id.dialogTextCancel)
        val done = itemView.findViewById<OutlineTextView>(R.id.dialogTextDone)
        val colorRv = itemView.findViewById<RecyclerView>(R.id.dialogTextColorPickRv)
        mContentEt = itemView.findViewById(R.id.dialogTextEdit)
        arguments?.let {
            mContentEt.setText(it.getString(EXTRA_CONTENT))
            mContentEt.setTextColor(it.getInt(EXTRA_COLOR))
        }
        val colorAdapter = ColorPickAdapter(activity!!)
        colorAdapter.colorChangeListener = {
            this.mColor = it
            mContentEt.setTextColor(it)
        }
        colorRv.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        colorRv.adapter = colorAdapter
        setDialogSize()
        cancel.setOnClickListener(this)
        done.setOnClickListener(this)
        return itemView
    }

    private fun setDialogSize() {
        dialog?.window?.let {
            it.decorView.setPadding(0, 0, 0, 0)
            val lp = it.attributes
            lp.gravity = Gravity.CENTER
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT
            it.attributes = lp
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.dialogTextCancel -> dismiss()
            R.id.dialogTextDone -> {
                textFinishListener?.invoke(mContentEt.text, mColor)
                dismiss()
            }
        }
    }
}