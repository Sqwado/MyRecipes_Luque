package com.example.myrecipes_luque.data.model

import com.google.gson.annotations.SerializedName

data class AreasResponse(
    @SerializedName("meals")
    val meals: List<Area>?,
)

data class Area(
    @SerializedName("strArea")
    val name: String,
)
