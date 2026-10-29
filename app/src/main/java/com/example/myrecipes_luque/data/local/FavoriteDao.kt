package com.example.myrecipes_luque.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY savedAtEpochMs DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mealId = :mealId)")
    fun observeIsFavorite(mealId: String): Flow<Boolean>

    @Query("SELECT * FROM favorites WHERE mealId = :mealId LIMIT 1")
    suspend fun getById(mealId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE mealId = :mealId")
    suspend fun deleteById(mealId: String)
}
