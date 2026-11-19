package com.example.myrecipes_luque.util

import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.view.updateLayoutParams
import com.google.android.material.snackbar.Snackbar

object AppSnackbar {

    fun show(
        host: View,
        @StringRes messageRes: Int,
        duration: Int = Snackbar.LENGTH_SHORT,
        /**
         * Si fourni, la notif repose juste au-dessus de cette vue et
         * l’animation de slide passe dessous (élévation plus basse).
         */
        slideUnder: View? = null,
        @StringRes actionRes: Int? = null,
        onAction: (() -> Unit)? = null,
    ): Snackbar {
        val snackbar = Snackbar.make(host, messageRes, duration)
        if (actionRes != null && onAction != null) {
            snackbar.setAction(actionRes) { onAction() }
        }
        snackbar.view.setOnClickListener { snackbar.dismiss() }

        if (slideUnder != null) {
            // Sous la barre pendant le slide ; position finale juste au-dessus
            snackbar.view.elevation = 0f
            val applyMargin = {
                val gap = slideUnder.height
                if (gap > 0) {
                    snackbar.view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        bottomMargin = gap
                    }
                }
            }
            if (slideUnder.height > 0) {
                applyMargin()
            } else {
                slideUnder.post(applyMargin)
            }
        }

        snackbar.show()
        return snackbar
    }
}
