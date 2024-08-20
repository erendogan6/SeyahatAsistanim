package com.erendogan6.seyahatasistanim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.seyahatasistanim.data.model.weather.WeatherApiResponse
import com.erendogan6.seyahatasistanim.data.model.weather.WeatherEntity
import com.erendogan6.seyahatasistanim.data.repository.WeatherRepository
import com.erendogan6.seyahatasistanim.extension.toEntityList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class WeatherViewModel(
    private val weatherRepository: WeatherRepository,
) : ViewModel() {
    private val _weatherData = MutableStateFlow<WeatherApiResponse?>(null)
    val weatherData: StateFlow<WeatherApiResponse?> = _weatherData

    private val _weatherFromDb = MutableStateFlow<List<WeatherEntity>?>(null)
    val weatherFromDb: StateFlow<List<WeatherEntity>?> = _weatherFromDb

    fun getWeatherForecast(
        lat: Double,
        lon: Double,
        travelDate: LocalDate,
    ) {
        val today = LocalDate.now()
        val startDate = travelDate.minusDays(1)
        val daysDifference = ChronoUnit.DAYS.between(today, startDate).toInt()

        if (daysDifference > 30) {
            _weatherData.value = null
            return
        }

        viewModelScope.launch {
            weatherRepository
                .getWeatherForecast(lat, lon)
                .catch { _ -> _weatherData.value = null }
                .collect { data ->
                    saveWeatherDataToDb(data)
                    _weatherData.value = data
                }
        }
    }

    private suspend fun saveWeatherDataToDb(weatherData: WeatherApiResponse) {
        val weatherEntities = weatherData.toEntityList()
        weatherRepository.saveWeatherData(weatherEntities)
    }

    fun loadWeatherFromDb(travelDate: LocalDate) {
        viewModelScope.launch {
            _weatherFromDb.value = weatherRepository.getWeatherData(travelDate)
        }
    }
}
