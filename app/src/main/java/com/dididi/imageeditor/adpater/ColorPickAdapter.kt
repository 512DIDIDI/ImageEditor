package com.dididi.imageeditor.adpater

import android.content.Context
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.dididi.imageeditor.R
import com.dididi.imageeditor.view.ColorCircleView


/**
 * @author dididi(yechao)
 * @since 30/07/2020
 * @describe
 */

typealias colorChange = (Int) -> Unit

class ColorPickAdapter(private val context:Context) :RecyclerView.Adapter<ColorPickAdapter.ViewHolder>(){

    private val colorList = arrayListOf(
        ContextCompat.getColor(context,R.color.black),
        ContextCompat.getColor(context,R.color.white),
        ContextCompat.getColor(context,R.color.blue_color_picker),
        ContextCompat.getColor(context,R.color.brown_color_picker),
        ContextCompat.getColor(context,R.color.green_color_picker),
        ContextCompat.getColor(context,R.color.orange_color_picker),
        ContextCompat.getColor(context,R.color.red_color_picker),
        ContextCompat.getColor(context,R.color.red_orange_color_picker),
        ContextCompat.getColor(context,R.color.sky_blue_color_picker),
        ContextCompat.getColor(context,R.color.violet_color_picker),
        ContextCompat.getColor(context,R.color.yellow_color_picker),
        ContextCompat.getColor(context,R.color.yellow_green_color_picker)
    )

    var colorChangeListener:colorChange? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_color_pick,parent,false)
        return ViewHolder(itemView)
    }

    override fun getItemCount() = colorList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.colorView.color = colorList[position]
    }

    inner class ViewHolder(itemView:View): RecyclerView.ViewHolder(itemView){
        val colorView: ColorCircleView = itemView.findViewById(R.id.itemColorPickColor)
        init {
            itemView.setOnClickListener {
                colorChangeListener?.invoke(colorList[layoutPosition])
            }
        }
    }
}