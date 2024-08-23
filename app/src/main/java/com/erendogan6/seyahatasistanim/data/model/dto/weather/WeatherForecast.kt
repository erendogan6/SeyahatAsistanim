package com.erendogan6.seyahatasistanim.data.model.dto.weather

import com.google.gson.annotations.SerializedName

data class WeatherForecast(
    @SerializedName("dt") val dateTime: Long,
    @SerializedName("temp") val temp: Temperature,
    @SerializedName("weather") val weather: List<Weather>,
)
