package com.example.myrecipes_luque.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myrecipes_luque.MyRecipesApp
import com.example.myrecipes_luque.data.FavoritesRepository
import com.example.myrecipes_luque.data.local.FavoriteEntity
import com.example.myrecipes_luque.data.model.MealDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FavoritesRepository =
        (application as MyRecipesApp).favoritesRepository

    val favorites = repository.observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun observeIsFavorite(mealId: String): Flow<Boolean> = repository.observeIsFavorite(mealId)

    fun add(meal: MealDetail) {
        viewModelScope.launch { repository.add(meal) }
    }

    fun remove(mealId: String) {
        viewModelScope.launch { repository.remove(mealId) }
    }

    fun restore(favorite: FavoriteEntity) {
        viewModelScope.launch { repository.restore(favorite) }
    }
}
