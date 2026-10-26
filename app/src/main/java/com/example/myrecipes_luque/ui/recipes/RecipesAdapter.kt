package com.example.myrecipes_luque.ui.recipes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myrecipes_luque.data.model.MealSummary
import com.example.myrecipes_luque.databinding.ItemRecipeBinding

class RecipesAdapter(
    private val onRecipeClick: (MealSummary) -> Unit,
) : ListAdapter<MealSummary, RecipesAdapter.RecipeViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(
        private val binding: ItemRecipeBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(meal: MealSummary) {
            binding.textName.text = meal.name
            Glide.with(binding.imageThumb)
                .load(meal.thumbUrl)
                .centerCrop()
                .into(binding.imageThumb)

            binding.root.setOnClickListener {
                onRecipeClick(meal)
            }
        }
    }

    private companion object DiffCallback : DiffUtil.ItemCallback<MealSummary>() {
        override fun areItemsTheSame(oldItem: MealSummary, newItem: MealSummary): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MealSummary, newItem: MealSummary): Boolean =
            oldItem == newItem
    }
}
