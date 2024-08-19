package com.erendogan6.seyahatasistanim.data.repository

import com.erendogan6.seyahatasistanim.data.model.weather.WeatherApiResponse
import com.erendogan6.seyahatasistanim.data.remote.WeatherApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WeatherRepository(
    private val weatherApiService: WeatherApiService,
) {
    fun getWeatherForecast(
        lat: Double,
        lon: Double,
    ): Flow<WeatherApiResponse> =
        flow {
            val response = weatherApiService.getWeatherForecast(lat, lon)
            emit(response)
        }
}
