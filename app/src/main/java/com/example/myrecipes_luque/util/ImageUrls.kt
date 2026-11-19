package com.example.myrecipes_luque.util

/**
 * TheMealDB — URLs images.
 * Les cards image-forward utilisent la photo pleine (pas /preview, trop pixelisée).
 */
object ImageUrls {

    /** URL pleine résolution (détail + listes cards). */
    fun full(url: String?): String? {
        if (url.isNullOrBlank()) return null
        return url.removeSuffix("/preview")
    }

    fun ingredientThumb(baseUrl: String, ingredientName: String): String {
        val normalized = ingredientName.trim().replace(' ', '_')
        return "${baseUrl.trimEnd('/')}/$normalized.png"
    }

    fun ingredientThumbSmall(baseUrl: String, ingredientName: String): String {
        val normalized = ingredientName.trim().replace(' ', '_')
        return "${baseUrl.trimEnd('/')}/$normalized-Small.png"
    }
}
