package com.erendogan6.seyahatasistanim.data.model.weather

import com.google.gson.annotations.SerializedName

data class Weather(
    @SerializedName("description") val description: String,
)
