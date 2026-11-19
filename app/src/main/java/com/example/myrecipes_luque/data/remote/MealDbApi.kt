package com.example.myrecipes_luque.data.remote

import com.example.myrecipes_luque.data.model.AreasResponse
import com.example.myrecipes_luque.data.model.CategoriesResponse
import com.example.myrecipes_luque.data.model.MealDetailResponse
import com.example.myrecipes_luque.data.model.MealsResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface MealDbApi {

    @GET("categories.php")
    suspend fun getCategories(): CategoriesResponse

    @GET("filter.php")
    suspend fun getMealsByCategory(@Query("c") category: String): MealsResponse

    @GET("filter.php")
    suspend fun getMealsByArea(@Query("a") area: String): MealsResponse

    @GET("list.php")
    suspend fun getAreas(@Query("a") list: String = "list"): AreasResponse

    @GET("lookup.php")
    suspend fun getMealById(@Query("i") mealId: String): MealDetailResponse

    @GET("search.php")
    suspend fun searchMealsByName(@Query("s") name: String): MealDetailResponse

    @Headers("Cache-Control: no-cache")
    @GET("random.php")
    suspend fun getRandomMeal(): MealDetailResponse
}
