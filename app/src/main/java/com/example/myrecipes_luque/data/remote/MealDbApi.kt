package com.example.myrecipes_luque.data.remote

import com.example.myrecipes_luque.data.model.CategoriesResponse
import com.example.myrecipes_luque.data.model.MealsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MealDbApi {

    @GET("categories.php")
    suspend fun getCategories(): CategoriesResponse

    @GET("filter.php")
    suspend fun getMealsByCategory(@Query("c") category: String): MealsResponse
}
