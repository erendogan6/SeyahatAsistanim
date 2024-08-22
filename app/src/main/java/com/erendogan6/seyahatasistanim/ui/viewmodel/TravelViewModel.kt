package com.erendogan6.seyahatasistanim.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.seyahatasistanim.data.model.travel.TravelEntity
import com.erendogan6.seyahatasistanim.data.model.weather.City
import com.erendogan6.seyahatasistanim.data.model.weather.WeatherEntity
import com.erendogan6.seyahatasistanim.data.repository.TravelRepository
import com.erendogan6.seyahatasistanim.data.repository.WeatherRepository
import com.erendogan6.seyahatasistanim.extension.toEntityList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> get() = _errorState

    private var debounceJob: Job? = null

    init {
        loadLastTravelInfo()
    }

    private val _isTravelInfoLoading = MutableStateFlow(false)
    private val _isWeatherLoading = MutableStateFlow(false)
    private val _isLocalInfoLoading = MutableStateFlow(false)
    private val _isChecklistLoading = MutableStateFlow(false)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    fun saveTravelInfo(
        travelEntity: TravelEntity,
        chatGptViewModel: ChatGptViewModel,
        weatherViewModel: WeatherViewModel,
        onTravelInfoSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                Log.d("TravelViewModel", "saveTravelInfo: Başladı")

                saveTravelInfoToDatabase(travelEntity)
                initiateWeatherAndLocalInfoLoading(travelEntity, chatGptViewModel, weatherViewModel)
                monitorLoadingStates(onTravelInfoSaved)
            } catch (e: Exception) {
                handleLoadingError(e)
            }
        }
    }

    private suspend fun saveTravelInfoToDatabase(travelEntity: TravelEntity) {
        _isTravelInfoLoading.value = true
        travelRepository.saveTravelInfo(travelEntity)
        _travelInfo.value = travelEntity
        Log.d("TravelViewModel", "saveTravelInfo: Seyahat bilgisi veritabanına kaydedildi")
        _isTravelInfoLoading.value = false
    }

    private fun initiateWeatherAndLocalInfoLoading(
        travelEntity: TravelEntity,
        chatGptViewModel: ChatGptViewModel,
        weatherViewModel: WeatherViewModel,
    ) {
        val destination = travelEntity.arrivalPlace
        val arrivalDate: LocalDate =
            LocalDate.parse(
                travelEntity.arrivalDate,
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr")),
            )

        _isWeatherLoading.value = true
        _isLocalInfoLoading.value = true

        chatGptViewModel.getLocalInfoForDestination(destination)
        weatherViewModel.getWeatherForecast(
            travelEntity.arrivalLatitude,
            travelEntity.arrivalLongitude,
            arrivalDate,
        )

        viewModelScope.launch {
            weatherViewModel.weatherData.collect { weatherData ->
                weatherData?.let {
                    Log.d("TravelViewModel", "Hava durumu verisi alındı: $weatherData")
                    _isWeatherLoading.value = false
                    initiateChecklistGeneration(chatGptViewModel, travelEntity, it.toEntityList())
                }
            }
        }

        viewModelScope.launch {
            chatGptViewModel.localInfo.collect { localInfo ->
                localInfo?.let {
                    Log.d("TravelViewModel", "Yerel bilgi alındı: $localInfo")
                    _isLocalInfoLoading.value = false
                }
            }
        }
    }

    private fun initiateChecklistGeneration(
        chatGptViewModel: ChatGptViewModel,
        travelEntity: TravelEntity,
        weatherData: List<WeatherEntity>,
    ) {
        _isChecklistLoading.value = true
        chatGptViewModel.generateChecklist(
            departureLocation = travelEntity.departurePlace,
            departureDate = travelEntity.departureDate,
            destination = travelEntity.arrivalPlace,
            arrivalDate = travelEntity.arrivalDate,
            travelMethod = travelEntity.travelMethod,
            weatherData = weatherData,
        )
    }

    private fun monitorLoadingStates(onTravelInfoSaved: () -> Unit) {
        viewModelScope.launch {
            combine(
                _isTravelInfoLoading,
                _isWeatherLoading,
                _isLocalInfoLoading,
                _isChecklistLoading,
            ) { travelLoading, weatherLoading, localInfoLoading, checklistLoading ->
                travelLoading || weatherLoading || localInfoLoading || checklistLoading
            }.collect { combinedLoading ->
                _isLoading.value = combinedLoading
                Log.d("TravelViewModel", "Birleşik isLoading durumu: $combinedLoading")
                if (!combinedLoading) {
                    onTravelInfoSaved()
                    Log.d("TravelViewModel", "Tüm işlemler tamamlandı, onTravelInfoSaved çağırıldı.")
                }
            }
        }
    }

    private fun handleLoadingError(e: Exception) {
        val errorMessage = e.message ?: "Bilinmeyen bir hata oluştu."
        _errorState.value = errorMessage
        handleTravelError(e, errorMessage)
        _isTravelInfoLoading.value = false
        _isWeatherLoading.value = false
        _isLocalInfoLoading.value = false
        _isChecklistLoading.value = false
        _isLoading.value = false
    }

    fun loadLastTravelInfo() {
        viewModelScope.launch {
            Log.d("TravelViewModel", "loadLastTravelInfo: Loading last travel info from DB")
            val lastTravelInfo = travelRepository.getLastTravelInfo()
            _travelInfo.value = lastTravelInfo
            Log.d("TravelViewModel", "loadLastTravelInfo: Last travel info loaded: ")
        }
    }

    fun deleteTravelInfo() {
        viewModelScope.launch {
            Log.d("TravelViewModel", "deleteTravelInfo: Deleting all travel info from DB")
            travelRepository.deleteAllTravelInfo()
            _travelInfo.value = null
            Log.d("TravelViewModel", "deleteTravelInfo: Travel info deleted")
        }
    }

    fun fetchDepartureCitySuggestions(query: String) {
        debounceJob?.cancel()
        debounceJob =
            viewModelScope.launch {
                delay(300) // 300ms debounce
                if (query.isNotBlank()) {
                    _departureCityLoadingState.value = LoadingState.Loading
                    Log.d("TravelViewModel", "fetchDepartureCitySuggestions: Fetching suggestions for $query")
                    weatherRepository
                        .getCitySuggestions(query)
                        .catch { error ->
                            handleTravelError(error, "Error fetching suggestions")
                            _departureCityLoadingState.value = LoadingState.Error("Failed to load suggestions")
                        }.collect { cities ->
                            Log.d("TravelViewModel", "fetchDepartureCitySuggestions: Suggestions received: ${cities.size} cities")
                            _departureCityLoadingState.value = LoadingState.Loaded(cities)
                        }
                } else {
                    _departureCityLoadingState.value = LoadingState.Loaded(emptyList())
                    Log.d("TravelViewModel", "fetchDepartureCitySuggestions: Query is blank, no suggestions fetched")
                }
            }
    }

    fun fetchArrivalCitySuggestions(query: String) {
        if (query.isNotBlank()) {
            _arrivalCityLoadingState.value = LoadingState.Loading
            Log.d("TravelViewModel", "fetchArrivalCitySuggestions: Fetching suggestions for $query")
            viewModelScope.launch {
                weatherRepository
                    .getCitySuggestions(query)
                    .catch { error ->
                        handleTravelError(error, "Error fetching suggestions")
                        _arrivalCityLoadingState.value = LoadingState.Error("Failed to load suggestions")
                    }.collect { cities ->
                        Log.d("TravelViewModel", "fetchArrivalCitySuggestions: Suggestions received: ${cities.size} cities")
                        _arrivalCityLoadingState.value = LoadingState.Loaded(cities)
                    }
            }
        } else {
            _arrivalCityLoadingState.value = LoadingState.Loaded(emptyList())
            Log.d("TravelViewModel", "fetchArrivalCitySuggestions: Query is blank, no suggestions fetched")
        }
    }

    fun handleTravelError(
        error: Throwable,
        customMessage: String,
    ) {
        Log.e("TravelViewModel", "$customMessage - ${error.message}")
    }
}
