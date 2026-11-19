package com.example.myrecipes_luque.util

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.example.myrecipes_luque.R

private val ease = FastOutSlowInInterpolator()

/**
 * Apparition sans fade : l’élément reste opaque (évite le « depop » des images).
 * Un léger slide suffit pour le ressenti.
 */
fun View.revealIn(durationMs: Long? = null) {
    if (isVisible) {
        alpha = 1f
        translationY = 0f
        return
    }
    val duration = durationMs
        ?: resources.getInteger(R.integer.ui_fade_duration).toLong()
    animate().cancel()
    alpha = 1f
    translationY = dp(16f)
    isVisible = true
    animate()
        .translationY(0f)
        .setDuration(duration)
        .setInterpolator(ease)
        .start()
}

fun View.showFabAnimated() {
    if (isVisible && scaleX == 1f) {
        alpha = 1f
        return
    }
    animate().cancel()
    alpha = 1f
    if (!isVisible) {
        scaleX = 0.85f
        scaleY = 0.85f
        isVisible = true
    }
    val duration = resources.getInteger(R.integer.ui_fab_duration).toLong()
    animate()
        .scaleX(1f)
        .scaleY(1f)
        .setDuration(duration)
        .setInterpolator(ease)
        .start()
}

fun View.hideFabAnimated() {
    if (!isVisible) return
    animate().cancel()
    val duration = resources.getInteger(R.integer.ui_fab_duration).toLong()
    animate()
        .scaleX(0.85f)
        .scaleY(0.85f)
        .setDuration(duration)
        .setInterpolator(ease)
        .withEndAction {
            isVisible = false
            scaleX = 1f
            scaleY = 1f
            alpha = 1f
        }
        .start()
}

fun View.slideBannerIn() {
    animate().cancel()
    alpha = 1f
    if (!isVisible) {
        translationY = -dp(12f)
        isVisible = true
    }
    animate()
        .translationY(0f)
        .setDuration(resources.getInteger(R.integer.ui_fade_duration).toLong())
        .setInterpolator(ease)
        .start()
}

fun View.slideBannerOut() {
    if (!isVisible) return
    animate().cancel()
    animate()
        .translationY(-dp(12f))
        .setDuration(resources.getInteger(R.integer.ui_fade_out_duration).toLong())
        .setInterpolator(ease)
        .withEndAction {
            isVisible = false
            translationY = 0f
            alpha = 1f
        }
        .start()
}

fun View.slideUpIn() {
    if (isVisible) {
        alpha = 1f
        translationY = 0f
        return
    }
    animate().cancel()
    alpha = 1f
    translationY = dp(28f)
    isVisible = true
    animate()
        .translationY(0f)
        .setDuration(resources.getInteger(R.integer.ui_fade_duration).toLong())
        .setInterpolator(ease)
        .start()
}

/** Resize fluide uniquement — pas de Fade (sinon les enfants « depop »). */
fun ViewGroup.beginExpandTransition() {
    TransitionManager.beginDelayedTransition(
        this,
        ChangeBounds().apply {
            duration = resources.getInteger(R.integer.ui_expand_duration).toLong()
            interpolator = ease
        },
    )
}

fun RecyclerView.enableAppItemAnimations() {
    itemAnimator = DefaultItemAnimator().apply {
        // Pas d’anim d’ajout (évite flash images au submitList)
        addDuration = 0
        changeDuration = 0
        supportsChangeAnimations = false
        removeDuration = resources.getInteger(R.integer.list_item_anim_remove).toLong()
        moveDuration = resources.getInteger(R.integer.list_item_anim_move).toLong()
    }
}

private fun View.dp(value: Float): Float = value * resources.displayMetrics.density

