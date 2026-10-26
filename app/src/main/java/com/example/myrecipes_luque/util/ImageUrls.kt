package com.example.myrecipes_luque.util

/**
 * TheMealDB sert des images meal souvent lourdes.
 * Pour les listes, on privilégie la variante /preview (plus légère).
 */
object ImageUrls {

    fun listThumb(url: String?): String? {
        if (url.isNullOrBlank()) return null
        if (url.contains("/preview") || url.endsWith("-small.png")) return url
        // Meal thumbs: .../meals/xxx.jpg → .../meals/xxx.jpg/preview
        if (url.contains("/media/meals/")) {
            return if (url.endsWith("/preview")) url else "$url/preview"
        }
        return url
    }
}
