package com.erendogan6.seyahatasistanim.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.seyahatasistanim.R
import com.erendogan6.seyahatasistanim.data.local.database.TravelDatabase
import com.erendogan6.seyahatasistanim.data.model.dto.weather.City
import com.erendogan6.seyahatasistanim.data.model.entity.TravelEntity
import com.erendogan6.seyahatasistanim.data.model.entity.WeatherEntity
import com.erendogan6.seyahatasistanim.data.repository.TravelRepository
import com.erendogan6.seyahatasistanim.data.repository.WeatherRepository
import com.erendogan6.seyahatasistanim.extension.toEntityList
import kotlinx.coroutines.Dispatchers
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
    private val context: Context,
    private val database: TravelDatabase,
) : ViewModel() {
    // Loading states definition
    sealed class LoadingState<out T> {
        object Loading : LoadingState<Nothing>()

        data class Loaded<T>(
            val data: T,
        ) : LoadingState<T>()

        data class Error(
            val message: String,
        ) : LoadingState<Nothing>()
    }

    // State variables
    private val _travelInfo = MutableStateFlow<TravelEntity?>(null)
    val travelInfo: StateFlow<TravelEntity?> get() = _travelInfo

    private val _departureCityLoadingState = MutableStateFlow<LoadingState<List<City>>>(LoadingState.Loaded(emptyList()))
    val departureCityLoadingState: StateFlow<LoadingState<List<City>>> get() = _departureCityLoadingState

    private val _arrivalCityLoadingState = MutableStateFlow<LoadingState<List<City>>>(LoadingState.Loaded(emptyList()))
    val arrivalCityLoadingState: StateFlow<LoadingState<List<City>>> get() = _arrivalCityLoadingState

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> get() = _errorState

    private val _isTravelInfoLoading = MutableStateFlow(false)
    private val _isWeatherLoading = MutableStateFlow(false)
    private val _isLocalInfoLoading = MutableStateFlow(false)
    private val _isChecklistLoading = MutableStateFlow(false)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private var debounceJob: Job? = null

    init {
        loadLastTravelInfo()
    }

    // Saving travel information and initiating loading processes
    fun saveTravelInfo(
        travelEntity: TravelEntity,
        chatGptViewModel: ChatGptViewModel,
        weatherViewModel: WeatherViewModel,
        onTravelInfoSaved: () -> Unit,
    ) {
        viewModelScope.launch {
            try {
                Log.d("TravelViewModel", "saveTravelInfo: ${context.getString(R.string.started)}")

                saveTravelInfoToDatabase(travelEntity)
                initiateWeatherAndLocalInfoLoading(travelEntity, chatGptViewModel, weatherViewModel)
                monitorLoadingStates(onTravelInfoSaved)
            } catch (e: Exception) {
                handleLoadingError(e)
            }
        }
    }

    // Save travel information to the database
    private suspend fun saveTravelInfoToDatabase(travelEntity: TravelEntity) {
        _isTravelInfoLoading.value = true
        travelRepository.saveTravelInfo(travelEntity)
        _travelInfo.value = travelEntity
        Log.d("TravelViewModel", context.getString(R.string.travel_info_saved_to_db))
        _isTravelInfoLoading.value = false
    }

    // Initiate loading weather and local info data
    private fun initiateWeatherAndLocalInfoLoading(
        travelEntity: TravelEntity,
        chatGptViewModel: ChatGptViewModel,
        weatherViewModel: WeatherViewModel,
    ) {
        val destination = travelEntity.arrivalPlace
        val arrivalDate: LocalDate =
            LocalDate.parse(
                travelEntity.arrivalDate,
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()),
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
                    Log.d("TravelViewModel", context.getString(R.string.weather_data_received, weatherData))
                    _isWeatherLoading.value = false
                    initiateChecklistGeneration(chatGptViewModel, travelEntity, it.toEntityList())
                }
            }
        }

        viewModelScope.launch {
            chatGptViewModel.localInfo.collect { localInfo ->
                localInfo?.let {
                    Log.d("TravelViewModel", context.getString(R.string.local_info_received, localInfo))
                    _isLocalInfoLoading.value = false
                }
            }
        }

        viewModelScope.launch {
            chatGptViewModel.checklistItems.collect { checklistItems ->
                checklistItems.let {
                    Log.d("TravelViewModel", context.getString(R.string.checklist_created, checklistItems))
                    _isChecklistLoading.value = false
                }
            }
        }
    }

    // Start generating a checklist based on weather and travel info
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

    // Monitor loading states and trigger the final callback
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
                Log.d("TravelViewModel", context.getString(R.string.combined_loading_state, combinedLoading))
                if (!combinedLoading) {
                    onTravelInfoSaved()
                    Log.d("TravelViewModel", context.getString(R.string.all_processes_completed))
                }
            }
        }
    }

    // Handle loading errors and reset states
    private fun handleLoadingError(e: Exception) {
        val errorMessage = e.message ?: context.getString(R.string.unknown_error_occurred)
        _errorState.value = errorMessage
        handleTravelError(e, errorMessage)
        _isTravelInfoLoading.value = false
        _isWeatherLoading.value = false
        _isLocalInfoLoading.value = false
        _isChecklistLoading.value = false
        _isLoading.value = false
    }

    // Load the last travel information from the database
    fun loadLastTravelInfo() {
        viewModelScope.launch {
            Log.d("TravelViewModel", context.getString(R.string.load_last_travel_info))
            val lastTravelInfo = travelRepository.getLastTravelInfo()
            _travelInfo.value = lastTravelInfo
            Log.d("TravelViewModel", context.getString(R.string.last_travel_info_loaded))
        }
    }

    // Delete all travel information from the database
    fun deleteTravelInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("TravelViewModel", context.getString(R.string.delete_travel_info))
            database.clearAllTables()
            _travelInfo.value = null
            Log.d("TravelViewModel", context.getString(R.string.travel_info_deleted))
        }
    }

    // Fetch suggestions for departure cities with a debounce
    fun fetchDepartureCitySuggestions(query: String) {
        debounceJob?.cancel()
        debounceJob =
            viewModelScope.launch {
                delay(300) // 300ms debounce
                if (query.isNotBlank()) {
                    _departureCityLoadingState.value =
                        LoadingState.Loading
                    Log.d("TravelViewModel", context.getString(R.string.fetch_departure_city_suggestions, query))
                    weatherRepository
                        .getCitySuggestions(query)
                        .catch { error ->
                            handleTravelError(error, context.getString(R.string.error_fetching_suggestions))
                            _departureCityLoadingState.value =
                                LoadingState.Error(context.getString(R.string.failed_to_load_suggestions))
                        }.collect { cities ->
                            Log.d("TravelViewModel", context.getString(R.string.suggestions_received, cities.size))
                            _departureCityLoadingState.value =
                                LoadingState.Loaded(cities)
                        }
                } else {
                    _departureCityLoadingState.value =
                        LoadingState.Loaded(emptyList())
                    Log.d("TravelViewModel", context.getString(R.string.query_is_blank))
                }
            }
    }

    // Fetch suggestions for arrival cities
    fun fetchArrivalCitySuggestions(query: String) {
        if (query.isNotBlank()) {
            _arrivalCityLoadingState.value =
                LoadingState.Loading
            Log.d("TravelViewModel", context.getString(R.string.fetch_arrival_city_suggestions, query))
            viewModelScope.launch {
                weatherRepository
                    .getCitySuggestions(query)
                    .catch { error ->
                        handleTravelError(error, context.getString(R.string.error_fetching_suggestions))
                        _arrivalCityLoadingState.value =
                            LoadingState.Error(context.getString(R.string.failed_to_load_suggestions))
                    }.collect { cities ->
                        Log.d("TravelViewModel", context.getString(R.string.suggestions_received, cities.size))
                        _arrivalCityLoadingState.value =
                            LoadingState.Loaded(cities)
                    }
            }
        } else {
            _arrivalCityLoadingState.value =
                LoadingState.Loaded(emptyList())
            Log.d("TravelViewModel", context.getString(R.string.query_is_blank))
        }
    }

    // Handle travel-related errors
    private fun handleTravelError(
        error: Throwable,
        customMessage: String,
    ) {
        Log.e("TravelViewModel", "$customMessage - ${error.message}")
    }
}