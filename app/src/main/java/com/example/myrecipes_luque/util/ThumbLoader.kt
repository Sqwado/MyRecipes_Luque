package com.example.myrecipes_luque.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.myrecipes_luque.R

object ThumbLoader {

    fun load(imageView: ImageView, url: String?) {
        val thumbUrl = ImageUrls.listThumb(url)
        val sizePx = imageView.resources.getDimensionPixelSize(R.dimen.item_thumb_size)

        Glide.with(imageView)
            .load(thumbUrl)
            .placeholder(R.drawable.bg_placeholder_thumb)
            .error(R.drawable.bg_placeholder_thumb)
            .override(sizePx, sizePx)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .transition(DrawableTransitionOptions.withCrossFade(150))
            .into(imageView)
    }

    fun loadHero(imageView: ImageView, url: String?) {
        val heightPx = imageView.resources.getDimensionPixelSize(R.dimen.detail_hero_height)

        Glide.with(imageView)
            .load(url)
            .placeholder(R.drawable.bg_placeholder_thumb)
            .error(R.drawable.bg_placeholder_thumb)
            .override(heightPx * 2, heightPx)
            .centerCrop()
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .transition(DrawableTransitionOptions.withCrossFade(200))
            .into(imageView)
    }
}
