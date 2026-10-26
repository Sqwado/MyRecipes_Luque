package com.example.myrecipes_luque.data.remote

import com.example.myrecipes_luque.data.model.CategoriesResponse
import retrofit2.http.GET

interface MealDbApi {

    @GET("categories.php")
    suspend fun getCategories(): CategoriesResponse
}
