package com.erendogan6.seyahatasistanim.data.repository

import com.erendogan6.seyahatasistanim.data.local.dao.WeatherDao
import com.erendogan6.seyahatasistanim.data.model.dto.weather.City
import com.erendogan6.seyahatasistanim.data.model.dto.weather.WeatherApiResponse
import com.erendogan6.seyahatasistanim.data.model.entity.WeatherEntity
import com.erendogan6.seyahatasistanim.data.remote.CityApiService
import com.erendogan6.seyahatasistanim.data.remote.WeatherApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class WeatherRepository(
    private val weatherApiService: WeatherApiService,
    private val cityApiService: CityApiService,
    private val weatherDao: WeatherDao,
) {
    fun getWeatherForecast(
        lat: Double,
        lon: Double,
    ): Flow<WeatherApiResponse> =
        flow {
            val response = weatherApiService.getWeatherForecast(lat, lon)
            emit(response)
        }

    suspend fun saveWeatherData(weatherEntities: List<WeatherEntity>) {
        weatherDao.insertWeatherData(weatherEntities)
    }

    suspend fun getWeatherData(travelDate: LocalDate): List<WeatherEntity> {
        val startDate = travelDate.minusDays(1)
        return weatherDao.getWeatherDataForRange(startDate, travelDate)
    }

    fun getCitySuggestions(query: String): Flow<List<City>> =
        flow {
            if (query.isNotBlank()) {
                val cities = cityApiService.getCities(query)
                emit(cities)
            } else {
                emit(emptyList())
            }
        }
}
