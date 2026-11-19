package com.example.myrecipes_luque.ui.areas

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myrecipes_luque.data.model.Area
import com.example.myrecipes_luque.databinding.ItemAreaBinding

class AreasAdapter(
    private val onAreaClick: (Area) -> Unit,
) : ListAdapter<Area, AreasAdapter.AreaViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AreaViewHolder {
        val binding = ItemAreaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return AreaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AreaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AreaViewHolder(
        private val binding: ItemAreaBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(area: Area) {
            binding.textName.text = area.name
            binding.textInitial.text = area.name.firstOrNull()?.uppercaseChar()?.toString().orEmpty()
            binding.root.setOnClickListener { onAreaClick(area) }
        }
    }

    private companion object DiffCallback : DiffUtil.ItemCallback<Area>() {
        override fun areItemsTheSame(oldItem: Area, newItem: Area): Boolean =
            oldItem.name == newItem.name

        override fun areContentsTheSame(oldItem: Area, newItem: Area): Boolean =
            oldItem == newItem
    }
}
