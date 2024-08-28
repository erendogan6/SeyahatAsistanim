package com.erendogan6.seyahatasistanim.domain.repository

import com.erendogan6.seyahatasistanim.data.model.dto.weather.City
import com.erendogan6.seyahatasistanim.data.model.dto.weather.WeatherApiResponse
import com.erendogan6.seyahatasistanim.data.model.entity.WeatherEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface WeatherRepository {
    fun getWeatherForecast(
        lat: Double,
        lon: Double,
    ): Flow<WeatherApiResponse>

    suspend fun saveWeatherData(weatherEntities: List<WeatherEntity>)

    suspend fun getWeatherData(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<WeatherEntity>

    fun getCitySuggestions(query: String): Flow<List<City>>
}
