package com.erendogan6.seyahatasistanim.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.seyahatasistanim.R
import com.erendogan6.seyahatasistanim.data.model.dto.weather.WeatherApiResponse
import com.erendogan6.seyahatasistanim.data.model.entity.WeatherEntity
import com.erendogan6.seyahatasistanim.domain.usecase.GetWeatherDataUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetWeatherForecastUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.SaveWeatherDataUseCase
import com.erendogan6.seyahatasistanim.extension.toEntityList
import com.erendogan6.seyahatasistanim.extension.toWeatherForecast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class WeatherViewModel(
    private val getWeatherForecastUseCase: GetWeatherForecastUseCase,
    private val saveWeatherDataUseCase: SaveWeatherDataUseCase,
    private val getWeatherDataUseCase: GetWeatherDataUseCase,
    private val context: Context,
) : ViewModel() {
    private val _weatherData = MutableStateFlow<WeatherApiResponse?>(null)
    val weatherData: StateFlow<WeatherApiResponse?> = _weatherData

    private val _weatherFromDb = MutableStateFlow<List<WeatherEntity>?>(null)
    val weatherFromDb: StateFlow<List<WeatherEntity>?> = _weatherFromDb

    fun fetchWeatherData(
        lat: Double,
        lon: Double,
        travelDate: LocalDate,
        daysToStay: Int,
    ) {
        val today = LocalDate.now()
        val startDate = travelDate.minusDays(1)
        val endDate = travelDate.plusDays(daysToStay.toLong())

        val daysDifference = ChronoUnit.DAYS.between(today, startDate).toInt()
        if (daysDifference > 30) {
            _weatherData.value = null
            Log.w("WeatherViewModel", context.getString(R.string.skipping_api_call, travelDate.toString()))
            return
        }

        viewModelScope.launch {
            Log.i(
                "WeatherViewModel",
                context.getString(R.string.fetching_weather_data, lat.toString(), lon.toString(), startDate.toString()),
            )

            getWeatherForecastUseCase(lat, lon)
                .catch { error ->
                    Log.e("WeatherViewModel", context.getString(R.string.error_fetching_weather_data_api, error.message ?: "Unknown Error"))
                    loadWeatherFromDb(travelDate, daysToStay)
                }.collect { data ->
                    val filteredForecasts =
                        data.forecastList
                            .filter { forecast ->
                                val forecastDate = LocalDate.ofEpochDay(forecast.dateTime / (24 * 60 * 60))
                                !forecastDate.isBefore(startDate) && !forecastDate.isAfter(endDate)
                            }.take(daysToStay + 1)

                    val limitedData = data.copy(forecastList = filteredForecasts)
                    saveWeatherDataToDb(limitedData)
                    _weatherData.value = limitedData
                    Log.i("WeatherViewModel", context.getString(R.string.weather_data_fetched))
                }
        }
    }

    private suspend fun saveWeatherDataToDb(weatherData: WeatherApiResponse) {
        val weatherEntities = weatherData.toEntityList()
        saveWeatherDataUseCase(weatherEntities)
        Log.i("WeatherViewModel", context.getString(R.string.weather_data_saved_to_db))
    }

    fun loadWeatherFromDb(
        travelDate: LocalDate,
        daysToStay: Int,
    ) {
        viewModelScope.launch {
            val startDate = travelDate.minusDays(1)
            val endDate = travelDate.plusDays(daysToStay.toLong())

            Log.i("WeatherViewModel", context.getString(R.string.loading_weather_data_from_db, travelDate.toString()))
            try {
                _weatherFromDb.value = getWeatherDataUseCase(startDate, endDate)
                Log.i("WeatherViewModel", context.getString(R.string.weather_data_loaded_from_db, travelDate.toString()))

                if (!_weatherFromDb.value.isNullOrEmpty()) {
                    _weatherData.value =
                        WeatherApiResponse(
                            forecastList = _weatherFromDb.value!!.map { it.toWeatherForecast() },
                        )
                }
            } catch (e: Exception) {
                handleWeatherError(e, context.getString(R.string.error_loading_weather_data))
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
