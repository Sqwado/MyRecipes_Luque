package com.example.myrecipes_luque.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.myrecipes_luque.R

object ThumbLoader {

    fun load(imageView: ImageView, url: String?) {
        val heightPx = imageView.resources.getDimensionPixelSize(R.dimen.item_image_height)
        val spanCount = imageView.resources.getInteger(R.integer.list_span_count).coerceAtLeast(1)
        // largeur réelle d’une card (écran / colonnes), × densité écran déjà en px
        val widthPx = (imageView.resources.displayMetrics.widthPixels / spanCount)
            .coerceAtLeast(heightPx)

        Glide.with(imageView)
            .load(ImageUrls.full(url))
            .placeholder(R.drawable.bg_placeholder_thumb)
            .error(R.drawable.bg_placeholder_thumb)
            .override(widthPx, heightPx)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            // Pas de crossfade : évite placeholder → image → flash / disparition
            .into(imageView)
    }

    fun loadHero(imageView: ImageView, url: String?) {
        val heightPx = imageView.resources.getDimensionPixelSize(R.dimen.detail_hero_height)
        val widthPx = imageView.resources.displayMetrics.widthPixels

        Glide.with(imageView)
            .load(ImageUrls.full(url))
            .placeholder(R.drawable.bg_placeholder_thumb)
            .error(R.drawable.bg_placeholder_thumb)
            .override(widthPx, heightPx)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(imageView)
    }

    fun loadIngredient(imageView: ImageView, ingredientName: String) {
        val baseUrl = imageView.resources.getString(R.string.mealdb_ingredient_image_base_url)
        val url = ImageUrls.ingredientThumbSmall(baseUrl, ingredientName)
        val sizePx = imageView.resources.getDimensionPixelSize(R.dimen.ingredient_thumb_size)

        Glide.with(imageView)
            .load(url)
            .placeholder(R.drawable.bg_placeholder_thumb)
            .error(R.drawable.bg_placeholder_thumb)
            .override(sizePx, sizePx)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .into(imageView)
    }

    fun clear(imageView: ImageView) {
        Glide.with(imageView).clear(imageView)
    }
}
