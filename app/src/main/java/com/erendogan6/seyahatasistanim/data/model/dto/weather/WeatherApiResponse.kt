package com.erendogan6.seyahatasistanim.data.model.dto.weather

import com.google.gson.annotations.SerializedName

data class WeatherApiResponse(
    @SerializedName("cod") val cod: String,
    @SerializedName("city") val city: City,
    @SerializedName("list") val forecastList: List<WeatherForecast>,
)
