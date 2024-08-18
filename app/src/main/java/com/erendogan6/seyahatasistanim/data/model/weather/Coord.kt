package com.erendogan6.seyahatasistanim.data.model.weather

import com.google.gson.annotations.SerializedName

data class Coord(
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double,
)
