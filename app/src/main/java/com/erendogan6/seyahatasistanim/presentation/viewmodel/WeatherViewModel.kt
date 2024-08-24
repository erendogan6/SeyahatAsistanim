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
            Log.w("WeatherViewModel", context.getString(R.string.skipping_api_call, travelDate.toString()))
            return
        }

        viewModelScope.launch {
            Log.i(
                "WeatherViewModel",
                context.getString(R.string.fetching_weather_data, lat.toString(), lon.toString(), travelDate.toString()),
            )
            getWeatherForecastUseCase(lat, lon)
                .catch { error ->
                    Log.e(
                        "WeatherViewModel",
                        context.getString(R.string.error_fetching_weather_data_api, error.message ?: "Unknown Error"),
                    )
                    loadWeatherFromDb(travelDate)
                    handleWeatherError(error, context.getString(R.string.error_fetching_weather_data, lat.toString(), lon.toString()))
                }.collect { data ->
                    saveWeatherDataToDb(data)
                    _weatherData.value = data
                    Log.i("WeatherViewModel", context.getString(R.string.weather_data_fetched))
                }
        }
    }

    private suspend fun saveWeatherDataToDb(weatherData: WeatherApiResponse) {
        val weatherEntities = weatherData.toEntityList()
        saveWeatherDataUseCase(weatherEntities)
        Log.i("WeatherViewModel", context.getString(R.string.weather_data_saved_to_db))
    }

    fun loadWeatherFromDb(travelDate: LocalDate) {
        viewModelScope.launch {
            Log.i("WeatherViewModel", context.getString(R.string.loading_weather_data_from_db, travelDate.toString()))
            try {
                _weatherFromDb.value = getWeatherDataUseCase(travelDate)
                Log.i("WeatherViewModel", context.getString(R.string.weather_data_loaded_from_db, travelDate.toString()))
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
