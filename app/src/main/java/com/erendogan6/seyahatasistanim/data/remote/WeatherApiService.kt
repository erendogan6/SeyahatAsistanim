package com.erendogan6.seyahatasistanim.data.remote

import com.erendogan6.seyahatasistanim.data.model.weather.WeatherApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/forecast/climate")
    suspend fun getWeatherForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("cnt") days: Int = 30,
        @Query("units") units: String = "metric",
        @Query("lang") language: String = "tr",
    ): WeatherApiResponse
}