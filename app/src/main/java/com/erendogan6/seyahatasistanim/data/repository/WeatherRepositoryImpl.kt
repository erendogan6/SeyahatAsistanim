package com.erendogan6.seyahatasistanim.data.repository

import com.erendogan6.seyahatasistanim.data.local.dao.WeatherDao
import com.erendogan6.seyahatasistanim.data.model.dto.weather.City
import com.erendogan6.seyahatasistanim.data.model.dto.weather.WeatherApiResponse
import com.erendogan6.seyahatasistanim.data.model.entity.WeatherEntity
import com.erendogan6.seyahatasistanim.data.remote.CityApiService
import com.erendogan6.seyahatasistanim.data.remote.WeatherApiService
import com.erendogan6.seyahatasistanim.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate

class WeatherRepositoryImpl(
    private val weatherApiService: WeatherApiService,
    private val cityApiService: CityApiService,
    private val weatherDao: WeatherDao,
) : WeatherRepository {
    override fun getWeatherForecast(
        lat: Double,
        lon: Double,
    ): Flow<WeatherApiResponse> =
        flow {
            val response = weatherApiService.getWeatherForecast(lat, lon)
            emit(response)
        }

    override suspend fun saveWeatherData(weatherEntities: List<WeatherEntity>) {
        weatherDao.insertWeatherData(weatherEntities)
    }

    override suspend fun getWeatherData(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<WeatherEntity> = weatherDao.getWeatherDataForRange(startDate, endDate)

    override fun getCitySuggestions(query: String): Flow<List<City>> =
        flow {
            if (query.isNotBlank()) {
                val cities = cityApiService.getCities(query)
                emit(cities)
            } else {
                emit(emptyList())
            }
        }
}
