package com.example.myrecipes_luque.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myrecipes_luque.data.local.FavoriteEntity
import com.example.myrecipes_luque.databinding.ItemFavoriteBinding
import com.example.myrecipes_luque.util.ThumbLoader
import java.text.DateFormat
import java.util.Date

class FavoritesAdapter(
    private val onFavoriteClick: (FavoriteEntity) -> Unit,
    private val onRemoveClick: (FavoriteEntity) -> Unit,
) : ListAdapter<FavoriteEntity, FavoritesAdapter.FavoriteViewHolder>(DiffCallback) {

    private val dateFormat: DateFormat =
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: FavoriteViewHolder) {
        ThumbLoader.clear(holder.binding.imageThumb)
        super.onViewRecycled(holder)
    }

    inner class FavoriteViewHolder(
        val binding: ItemFavoriteBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(favorite: FavoriteEntity) {
            binding.textName.text = favorite.name
            binding.textSavedAt.text = dateFormat.format(Date(favorite.savedAtEpochMs))
            ThumbLoader.load(binding.imageThumb, favorite.thumbUrl)
            binding.root.setOnClickListener { onFavoriteClick(favorite) }
            binding.buttonRemove.setOnClickListener { onRemoveClick(favorite) }
        }
    }

    private companion object DiffCallback : DiffUtil.ItemCallback<FavoriteEntity>() {
        override fun areItemsTheSame(oldItem: FavoriteEntity, newItem: FavoriteEntity): Boolean =
            oldItem.mealId == newItem.mealId

        override fun areContentsTheSame(oldItem: FavoriteEntity, newItem: FavoriteEntity): Boolean =
            oldItem == newItem
    }
}
