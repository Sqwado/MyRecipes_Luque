package com.example.myrecipes_luque.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MealDbClient {

    private const val BASE_URL = "https://www.themealdb.com/api/json/v1/1/"

    val api: MealDbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealDbApi::class.java)
    }
}
