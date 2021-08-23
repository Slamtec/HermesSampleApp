package com.slamtec.fooddelivery.view

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.slamtec.fooddelivery.R

/**
 * File   : TableViewAdapter
 * Author : Qikun.Xiong
 * Date   : 2021/8/11 2:09 PM
 */
class TableViewAdapter(var context: Context, var tables: List<String>) : RecyclerView.Adapter<TableViewAdapter.TableViewHolder>() {

    private var listener: OnItemClickListener? = null
    private var selectedPosition: Int = -1

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun clearPosition() {
        selectedPosition = -1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_table, null, false)
        return TableViewHolder(view)
    }

    override fun onBindViewHolder(holder: TableViewHolder, position: Int) {
        holder.tvName.text = tables[position]
        holder.tvName.setBackgroundResource(R.drawable.item_table_normal_rec)
        if (selectedPosition == position) {
            holder.tvName.setBackgroundResource(R.drawable.item_table_selected_rec)
            holder.tvName.setTextColor(Color.WHITE)
        } else {
            holder.tvName.setBackgroundResource(R.drawable.item_table_normal_rec)
            holder.tvName.setTextColor(R.color.item_table_unselected_text_color)
        }
        holder.tvName.setOnClickListener {
            listener?.onItemClick(it, holder.adapterPosition, tables[position])
            updateSelectedPosition(position)
            notifyItemChanged(selectedPosition)
        }
    }

    fun updateSelectedPosition(position: Int) {
        val temp = selectedPosition
        selectedPosition = position
        notifyItemChanged(temp)
        notifyItemChanged(selectedPosition)
    }

    override fun getItemCount(): Int {
        return tables.size
    }

    class TableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName = itemView.findViewById(R.id.tv_name) as TextView
    }

    interface OnItemClickListener {
        fun onItemClick(v: View, position: Int, poiName: String)
    }
}