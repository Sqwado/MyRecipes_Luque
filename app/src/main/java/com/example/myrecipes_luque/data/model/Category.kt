package com.example.myrecipes_luque.data.model

import com.google.gson.annotations.SerializedName

data class CategoriesResponse(
    @SerializedName("categories")
    val categories: List<Category>?,
)

data class Category(
    @SerializedName("idCategory")
    val id: String,
    @SerializedName("strCategory")
    val name: String,
    @SerializedName("strCategoryThumb")
    val thumbUrl: String?,
    @SerializedName("strCategoryDescription")
    val description: String?,
)
