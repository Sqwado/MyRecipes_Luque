package com.example.myrecipes_luque.data

import com.example.myrecipes_luque.data.local.FavoriteDao
import com.example.myrecipes_luque.data.local.FavoriteEntity
import com.example.myrecipes_luque.data.model.MealDetail
import kotlinx.coroutines.flow.Flow

class FavoritesRepository(
    private val favoriteDao: FavoriteDao,
) {

    fun observeAll(): Flow<List<FavoriteEntity>> = favoriteDao.observeAll()

    fun observeIsFavorite(mealId: String): Flow<Boolean> = favoriteDao.observeIsFavorite(mealId)

    suspend fun add(meal: MealDetail) {
        favoriteDao.upsert(
            FavoriteEntity(
                mealId = meal.id,
                name = meal.name,
                thumbUrl = meal.thumbUrl,
                savedAtEpochMs = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun remove(mealId: String) {
        favoriteDao.deleteById(mealId)
    }

    suspend fun restore(favorite: FavoriteEntity) {
        favoriteDao.upsert(favorite)
    }
}
