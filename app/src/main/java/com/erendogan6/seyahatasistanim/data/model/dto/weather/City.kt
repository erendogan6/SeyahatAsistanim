package com.erendogan6.seyahatasistanim.data.model.dto.weather

import com.google.gson.annotations.SerializedName

data class City(
    @SerializedName("name") val name: String,
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double,
    @SerializedName("country") val country: String,
    @SerializedName("local_names") val localNames: LocalNames? = null,
)
