package com.example.myrecipes_luque.data.model

import com.google.gson.annotations.SerializedName

data class MealsResponse(
    @SerializedName("meals")
    val meals: List<MealSummary>?,
)

data class MealSummary(
    @SerializedName("idMeal")
    val id: String,
    @SerializedName("strMeal")
    val name: String,
    @SerializedName("strMealThumb")
    val thumbUrl: String?,
)
