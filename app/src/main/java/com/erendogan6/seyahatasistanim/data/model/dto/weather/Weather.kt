package com.erendogan6.seyahatasistanim.data.model.dto.weather

import com.google.gson.annotations.SerializedName

data class Weather(
    @SerializedName("description") val description: String,
)
