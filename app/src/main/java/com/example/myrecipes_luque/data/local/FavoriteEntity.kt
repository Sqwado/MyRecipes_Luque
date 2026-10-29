package com.example.myrecipes_luque.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val mealId: String,
    val name: String,
    val thumbUrl: String?,
    val savedAtEpochMs: Long,
)
