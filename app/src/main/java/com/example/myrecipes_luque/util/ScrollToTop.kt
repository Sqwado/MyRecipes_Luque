package com.example.myrecipes_luque.util

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

/** FAB « retour en haut » + scroll fluide à durée fixe (pattern M2Tmdb). */
fun RecyclerView.setupScrollToTop(
    fab: View,
    threshold: Int,
    onScrolledExtra: ((dy: Int) -> Unit)? = null,
) {
    val layoutManager = layoutManager as? LinearLayoutManager ?: return

    fab.setOnClickListener { scrollToTopSmooth() }

    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            fab.isVisible = layoutManager.findFirstVisibleItemPosition() > threshold
            onScrolledExtra?.invoke(dy)
        }
    })
}

fun RecyclerView.scrollToTopSmooth() {
    val layoutManager = layoutManager as? LinearLayoutManager ?: return
    val firstVisible = layoutManager.findFirstVisibleItemPosition()
    if (firstVisible <= 0) return

    if (firstVisible > SCROLL_TOP_NEAR_ITEMS) {
        layoutManager.scrollToPositionWithOffset(SCROLL_TOP_NEAR_ITEMS, 0)
    }
    post {
        val scroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int = SNAP_TO_START
            override fun calculateTimeForScrolling(dx: Int): Int = SCROLL_TOP_DURATION_MS
            override fun calculateTimeForDeceleration(dx: Int): Int = SCROLL_TOP_DURATION_MS
        }
        scroller.targetPosition = 0
        layoutManager.startSmoothScroll(scroller)
    }
}

private const val SCROLL_TOP_NEAR_ITEMS = 12
private const val SCROLL_TOP_DURATION_MS = 400
