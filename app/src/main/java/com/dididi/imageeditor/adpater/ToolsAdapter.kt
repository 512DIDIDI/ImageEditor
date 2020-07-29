package com.dididi.imageeditor.adpater

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.dididi.imageeditor.R


/**
 * @author dididi(yechao)
 * @since 29/07/2020
 * @describe
 */

class ToolsAdapter() : RecyclerView.Adapter<ToolsAdapter.ViewHolder>() {

    private val mToolsList = mutableListOf(
        ToolsItem("Brush", R.drawable.ic_brush, ToolsType.BRUSH.ordinal),
        ToolsItem("Text", R.drawable.ic_text, ToolsType.TEXT.ordinal),
        ToolsItem("Eraser", R.drawable.ic_eraser, ToolsType.ERASER.ordinal),
        ToolsItem("Filter", R.drawable.ic_photo_filter, ToolsType.FILTER.ordinal),
        ToolsItem("Emoji", R.drawable.ic_insert_emoticon, ToolsType.EMOJI.ordinal)
    )

    var itemClickListener: ((ToolsType) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tools,parent,false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = mToolsList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mToolsList[position]
        holder.icon.setImageResource(item.iconResource)
        holder.name.text = item.name
    }

    data class ToolsItem(val name: String, @DrawableRes val iconResource: Int, val type: Int)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.itemToolsIcon)
        val name: TextView = itemView.findViewById(R.id.itemToolsTitle)

        init {
            itemView.setOnClickListener {
                itemClickListener?.invoke(ToolsType.values()[mToolsList[layoutPosition].type])
            }
        }
    }
}