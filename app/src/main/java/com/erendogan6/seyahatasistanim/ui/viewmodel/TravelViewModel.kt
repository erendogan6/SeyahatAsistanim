package com.erendogan6.seyahatasistanim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.seyahatasistanim.data.model.travel.TravelEntity
import com.erendogan6.seyahatasistanim.data.model.weather.City
import com.erendogan6.seyahatasistanim.data.repository.TravelRepository
import com.erendogan6.seyahatasistanim.data.repository.WeatherRepository
import com.erendogan6.seyahatasistanim.extension.toEntityList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class TravelViewModel(
    private val travelRepository: TravelRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {
    sealed class LoadingState<out T> {
        data object Loading : LoadingState<Nothing>()

        data class Loaded<T>(
            val data: T,
        ) : LoadingState<T>()

        data class Error(
            val message: String,
        ) : LoadingState<Nothing>()
    }

    private val _travelInfo = MutableStateFlow<TravelEntity?>(null)
    val travelInfo: StateFlow<TravelEntity?> get() = _travelInfo

    private val _departureCityLoadingState = MutableStateFlow<LoadingState<List<City>>>(LoadingState.Loaded(emptyList()))
    val departureCityLoadingState: StateFlow<LoadingState<List<City>>> get() = _departureCityLoadingState

    private val _arrivalCityLoadingState = MutableStateFlow<LoadingState<List<City>>>(LoadingState.Loaded(emptyList()))
    val arrivalCityLoadingState: StateFlow<LoadingState<List<City>>> get() = _arrivalCityLoadingState

    init {
        loadLastTravelInfo()
    }

    fun saveTravelInfo(
        travelEntity: TravelEntity,
        chatGptViewModel: ChatGptViewModel,
        weatherViewModel: WeatherViewModel,
    ) {
        viewModelScope.launch {
            travelRepository.saveTravelInfo(travelEntity)
            _travelInfo.value = travelEntity
            val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
            val arrivalDate: LocalDate = LocalDate.parse(travelEntity.arrivalDate, formatter)

            chatGptViewModel.getLocalInfoForDestination(travelEntity.arrivalPlace)
            weatherViewModel.getWeatherForecast(
                travelEntity.arrivalLatitude,
                travelEntity.arrivalLongitude,
                arrivalDate,
            )

            weatherViewModel.weatherData.collect { weatherData ->
                weatherData?.let {
                    chatGptViewModel.generateChecklist(
                        travelEntity.departurePlace,
                        travelEntity.departureDate,
                        travelEntity.arrivalPlace,
                        travelEntity.arrivalDate,
                        it.toEntityList(),
                        travelEntity.travelMethod,
                    )
                }
            }
        }
    }

    fun loadLastTravelInfo() {
        viewModelScope.launch {
            val lastTravelInfo = travelRepository.getLastTravelInfo()
            _travelInfo.value = lastTravelInfo
        }
    }

    fun deleteTravelInfo() {
        viewModelScope.launch {
            travelRepository.deleteAllTravelInfo()
            _travelInfo.value = null
        }
    }

    fun fetchDepartureCitySuggestions(query: String) {
        if (query.isNotBlank()) {
            _departureCityLoadingState.value = LoadingState.Loading
            viewModelScope.launch {
                weatherRepository
                    .getCitySuggestions(query)
                    .catch {
                        _departureCityLoadingState.value = LoadingState.Error("Failed to load suggestions")
                    }.collect { cities ->
                        _departureCityLoadingState.value = LoadingState.Loaded(cities)
                    }
            }
        } else {
            _departureCityLoadingState.value = LoadingState.Loaded(emptyList())
        }
    }

    fun fetchArrivalCitySuggestions(query: String) {
        if (query.isNotBlank()) {
            _arrivalCityLoadingState.value = LoadingState.Loading
            viewModelScope.launch {
                weatherRepository
                    .getCitySuggestions(query)
                    .catch {
                        _arrivalCityLoadingState.value = LoadingState.Error("Failed to load suggestions")
                    }.collect { cities ->
                        _arrivalCityLoadingState.value = LoadingState.Loaded(cities)
                    }
            }
        } else {
            _arrivalCityLoadingState.value = LoadingState.Loaded(emptyList())
        }
    }
}
