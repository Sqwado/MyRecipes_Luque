package com.example.myrecipes_luque

import android.app.Application
import com.example.myrecipes_luque.data.remote.MealDbClient

class MyRecipesApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MealDbClient.init(this)
    }
}
