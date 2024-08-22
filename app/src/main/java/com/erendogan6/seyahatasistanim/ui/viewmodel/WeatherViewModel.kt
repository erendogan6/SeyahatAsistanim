package com.erendogan6.seyahatasistanim.ui.viewmodel

import android.util.Log
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
            Log.w("WeatherViewModel", "Skipping API call. Travel date ($travelDate) is more than 30 days in the future.")
            return
        }

        viewModelScope.launch {
            Log.i("WeatherViewModel", "Fetching weather data for lat: $lat, lon: $lon for travel date: $travelDate.")
            weatherRepository
                .getWeatherForecast(lat, lon)
                .catch { error ->
                    Log.e("WeatherViewModel", "Error fetching weather data from API: ${error.message}. Loading data from DB.")
                    loadWeatherFromDb(travelDate)
                    handleWeatherError(error, "Error fetching weather data for lat: $lat, lon: $lon.")
                }.collect { data ->
                    saveWeatherDataToDb(data)
                    _weatherData.value = data
                    Log.i("WeatherViewModel", "Weather data fetched successfully and saved to StateFlow.")
                }
        }
    }

    private suspend fun saveWeatherDataToDb(weatherData: WeatherApiResponse) {
        val weatherEntities = weatherData.toEntityList()
        weatherRepository.saveWeatherData(weatherEntities)
        Log.i("WeatherViewModel", "Weather data saved to local database successfully.")
    }

    fun loadWeatherFromDb(travelDate: LocalDate) {
        viewModelScope.launch {
            Log.i("WeatherViewModel", "Loading weather data for $travelDate from local database.")
            try {
                _weatherFromDb.value = weatherRepository.getWeatherData(travelDate)
                Log.i("WeatherViewModel", "Weather data for $travelDate loaded from database.")
            } catch (e: Exception) {
                handleWeatherError(e, "Error loading weather data from database.")
            }
        }
    }

    private fun handleWeatherError(
        error: Throwable,
        customMessage: String,
    ) {
        Log.e("WeatherViewModel", "$customMessage - ${error.message}")
    }
}
