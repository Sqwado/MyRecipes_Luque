package com.example.myrecipes_luque.util

import com.example.myrecipes_luque.data.model.MealDetail

/**
 * Heuristique locale (TheMealDB ne fournit pas de calories).
 * Score 0–100 : plus le plat est estimé « léger », plus le score est élevé (vert).
 *
 * - Pénalité selon le nombre d'ingrédients
 * - Pénalité / bonus selon mots-clés d'ingrédients
 * - Ajustement selon la catégorie
 */
object NutriScoreCalculator {

    private val heavyKeywords = listOf(
        "butter", "cream", "sugar", "oil", "cheese", "bacon", "lard",
        "mayonnaise", "chocolate", "syrup", "shortening", "ghee", "suet",
        "beurre", "crème", "sucre", "huile", "fromage",
    )

    private val lightKeywords = listOf(
        "lettuce", "spinach", "broccoli", "tomato", "cucumber", "carrot",
        "lemon", "lime", "herb", "garlic", "onion", "celery", "pepper",
        "fish", "salmon", "tuna", "cod", "shrimp", "tofu", "bean", "lentil",
        "salade", "épinard", "tomate", "citron",
    )

    private val categoryBonus = mapOf(
        "Vegan" to 18,
        "Vegetarian" to 12,
        "Seafood" to 10,
        "Chicken" to 6,
        "Starter" to 8,
        "Side" to 5,
        "Breakfast" to 4,
        "Goat" to 0,
        "Lamb" to -4,
        "Beef" to -6,
        "Pork" to -6,
        "Pasta" to -4,
        "Miscellaneous" to 0,
        "Dessert" to -20,
    )

    fun compute(meal: MealDetail): Int {
        val ingredients = meal.ingredientLines().map { it.name.lowercase() }
        val count = ingredients.size.coerceAtLeast(1)

        // Base : 100, moins 3 pts par ingrédient au-delà de 4 (plats chargés)
        var score = 100 - ((count - 4).coerceAtLeast(0) * 3)

        val joined = ingredients.joinToString(" ")
        heavyKeywords.forEach { keyword ->
            if (joined.contains(keyword)) score -= 6
        }
        lightKeywords.forEach { keyword ->
            if (joined.contains(keyword)) score += 3
        }

        val category = meal.category?.trim().orEmpty()
        score += categoryBonus[category] ?: 0

        return score.coerceIn(0, 100)
    }
}
