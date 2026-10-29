package com.example.myrecipes_luque

import android.app.Application
import com.example.myrecipes_luque.data.FavoritesRepository
import com.example.myrecipes_luque.data.local.AppDatabase
import com.example.myrecipes_luque.data.remote.MealDbClient

class MyRecipesApp : Application() {

    lateinit var favoritesRepository: FavoritesRepository
        private set

    override fun onCreate() {
        super.onCreate()
        MealDbClient.init(this)
        favoritesRepository = FavoritesRepository(
            AppDatabase.getInstance(this).favoriteDao(),
        )
    }
}
