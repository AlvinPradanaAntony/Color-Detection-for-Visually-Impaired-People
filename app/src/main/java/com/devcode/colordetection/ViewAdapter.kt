package com.devcode.colordetection

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devcode.colordetection.databinding.ItemRowListBinding

class ViewAdapter(private val listResolution: ArrayList<ListRes>) : RecyclerView.Adapter<ViewAdapter.ViewHolder>() {
    private lateinit var onItemClickCallback: OnItemClickCallback

    fun setOnItemClickCallback(onItemClickCallback: OnItemClickCallback) {
        this.onItemClickCallback = onItemClickCallback
    }

    class ViewHolder(var binding: ItemRowListBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRowListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount() = listResolution.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.tvResolution.text = listResolution[position].resolution
        holder.itemView.setOnClickListener { onItemClickCallback.onItemClicked(listResolution[holder.adapterPosition]) }
    }

    interface OnItemClickCallback {
        fun onItemClicked(data: ListRes)
    }
}