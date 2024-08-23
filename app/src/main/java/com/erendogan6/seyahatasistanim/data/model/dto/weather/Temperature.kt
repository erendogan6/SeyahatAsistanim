package com.erendogan6.seyahatasistanim.data.model.dto.weather

import com.google.gson.annotations.SerializedName

data class Temperature(
    @SerializedName("day") val day: Double,
    @SerializedName("night") val night: Double,
)
