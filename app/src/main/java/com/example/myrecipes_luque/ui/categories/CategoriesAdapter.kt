package com.example.myrecipes_luque.ui.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myrecipes_luque.data.model.Category
import com.example.myrecipes_luque.databinding.ItemCategoryBinding
import com.example.myrecipes_luque.util.ThumbLoader

class CategoriesAdapter(
    private val onCategoryClick: (Category) -> Unit,
) : ListAdapter<Category, CategoriesAdapter.CategoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: Category) {
            binding.textName.text = category.name
            ThumbLoader.load(binding.imageThumb, category.thumbUrl)
            binding.root.setOnClickListener {
                onCategoryClick(category)
            }
        }
    }

    private companion object DiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean =
            oldItem == newItem
    }
}
