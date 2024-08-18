package com.erendogan6.seyahatasistanim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.seyahatasistanim.data.model.weather.WeatherApiResponse
import com.erendogan6.seyahatasistanim.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WeatherViewModel :
    ViewModel(),
    KoinComponent {
    private val weatherRepository: WeatherRepository by inject()

    private val _weatherData = MutableStateFlow<WeatherApiResponse?>(null)

    val weatherData: StateFlow<WeatherApiResponse?> = _weatherData

    fun getWeatherForecast(
        lat: Double,
        lon: Double,
        apiKey: String,
    ) {
        viewModelScope.launch {
            weatherRepository
                .getWeatherForecast(lat, lon, apiKey)
                .catch { e ->
                    // Handle the error
                    _weatherData.value = null
                }.collect { data ->
                    _weatherData.value = data
                }
        }
    }
}
