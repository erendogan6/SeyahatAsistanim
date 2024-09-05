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
import com.erendogan6.seyahatasistanim.domain.usecase.GetCitySuggestionsUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetLastTravelInfoUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.SaveTravelInfoUseCase
import com.erendogan6.seyahatasistanim.extension.toEntityList
import com.erendogan6.seyahatasistanim.utils.isNetworkAvailable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class TravelViewModel(
    private val saveTravelInfoUseCase: SaveTravelInfoUseCase,
    private val getLastTravelInfoUseCase: GetLastTravelInfoUseCase,
    private val getCitySuggestionsUseCase: GetCitySuggestionsUseCase,
    private val context: Context,
    private val database: TravelDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : ViewModel() {
    // Loading states definition
    sealed class LoadingState<out T> {
        data object Loading : LoadingState<Nothing>()

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

    val _isTravelInfoLoading = MutableStateFlow(false)
    val _isWeatherLoading = MutableStateFlow(false)
    val _isLocalInfoLoading = MutableStateFlow(false)
    val _isChecklistLoading = MutableStateFlow(false)

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
        viewModelScope.launch(dispatcher) {
            Log.d("TestLog", "Started saveTravelInfo")
            try {
                if (!isNetworkAvailable(context)) {
                    _errorState.value = context.getString(R.string.no_internet_connection)
                    Log.d("TestLog", "Network unavailable")
                    return@launch
                }

                Log.d("TestLog", "Network available, proceeding with save")

                _isTravelInfoLoading.value = true
                saveTravelInfoUseCase.invoke(travelEntity)
                Log.d("TestLog", "saveTravelInfoUseCase invoked successfully")

                _travelInfo.value = travelEntity
                _isTravelInfoLoading.value = false

                initiateWeatherAndLocalInfoLoading(travelEntity, chatGptViewModel, weatherViewModel)
                monitorLoadingStates(onTravelInfoSaved)
            } catch (e: Exception) {
                Log.d("TestLog", "Exception caught in saveTravelInfo: ${e.message}")
                handleLoadingError(e)
            }
        }
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
                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH),
            )

        _isWeatherLoading.value = true
        _isLocalInfoLoading.value = true

        Log.d("TestLog", "Initiating loading of weather and local info")

        chatGptViewModel.getLocalInfoForDestination(destination)
        weatherViewModel.fetchWeatherData(
            travelEntity.arrivalLatitude,
            travelEntity.arrivalLongitude,
            arrivalDate,
            travelEntity.daysToStay,
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
            daysToStay = travelEntity.daysToStay,
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
                Log.d("TestLog", "Combined loading state: $combinedLoading")
                Log.d(
                    "TestLog",
                    "States - Travel: ${_isTravelInfoLoading.value}, Weather: ${_isWeatherLoading.value}, LocalInfo: ${_isLocalInfoLoading.value}, Checklist: ${_isChecklistLoading.value}",
                )
                if (!combinedLoading) {
                    Log.d("TestLog", "Callback about to be triggered")
                    onTravelInfoSaved()
                    Log.d("TestLog", "Callback triggered")
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
            try {
                Log.d("TravelViewModel", context.getString(R.string.load_last_travel_info))
                _travelInfo.value = getLastTravelInfoUseCase()
                Log.d("TravelViewModel", context.getString(R.string.last_travel_info_loaded))
            } catch (e: Exception) {
                handleLoadingError(e)
            }
        }
    }

    // Delete all travel information from the database
    fun deleteTravelInfo(onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("TravelViewModel", context.getString(R.string.delete_travel_info))
            database.clearAllTables()

            withContext(Dispatchers.Main) {
                _travelInfo.value = null
                Log.d("TravelViewModel", context.getString(R.string.travel_info_deleted))
                onComplete()
            }
        }
    }

    // Unified function to fetch city suggestions
    fun fetchCitySuggestions(
        query: String,
        isDeparture: Boolean,
    ) {
        debounceJob?.cancel()
        debounceJob =
            viewModelScope.launch {
                delay(300) // 300ms debounce delay
                if (query.isNotBlank()) {
                    val loadingState = if (isDeparture) _departureCityLoadingState else _arrivalCityLoadingState
                    loadingState.value = LoadingState.Loading
                    Log.d(
                        "TravelViewModel",
                        context.getString(
                            if (isDeparture) R.string.fetch_departure_city_suggestions else R.string.fetch_arrival_city_suggestions,
                            query,
                        ),
                    )
                    try {
                        getCitySuggestionsUseCase(query)
                            .catch { error ->
                                handleTravelError(error, context.getString(R.string.error_fetching_suggestions))
                                loadingState.value = LoadingState.Error(context.getString(R.string.failed_to_load_suggestions))
                            }.collect { cities ->
                                Log.d("TravelViewModel", context.getString(R.string.suggestions_received, cities.size))
                                loadingState.value = LoadingState.Loaded(cities)
                            }
                    } catch (e: Exception) {
                        handleTravelError(e, context.getString(R.string.error_fetching_suggestions))
                        loadingState.value = LoadingState.Error(context.getString(R.string.failed_to_load_suggestions))
                    }
                } else {
                    val loadingState = if (isDeparture) _departureCityLoadingState else _arrivalCityLoadingState
                    loadingState.value = LoadingState.Loaded(emptyList())
                    Log.d("TravelViewModel", context.getString(R.string.query_is_blank))
                }
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
