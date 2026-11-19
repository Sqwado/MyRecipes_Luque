package com.example.myrecipes_luque.ui.detail

import androidx.lifecycle.ViewModel
import com.example.myrecipes_luque.data.model.MealDetail

/** Cache léger pour éviter le flash / reload au retour du mode cuisine. */
class RecipeSessionViewModel : ViewModel() {
    var mealId: String? = null
        private set
    var meal: MealDetail? = null
        private set

    fun put(meal: MealDetail) {
        mealId = meal.id
        this.meal = meal
    }

    fun getIfMatch(id: String): MealDetail? =
        meal?.takeIf { mealId == id }

    fun clear() {
        mealId = null
        meal = null
    }
}
