package com.erendogan6.seyahatasistanim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChatGptRequest
import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChatGptResponse
import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChecklistItemEntity
import com.erendogan6.seyahatasistanim.data.model.chatGPT.LocalInfoEntity
import com.erendogan6.seyahatasistanim.data.model.chatGPT.Message
import com.erendogan6.seyahatasistanim.data.model.weather.WeatherEntity
import com.erendogan6.seyahatasistanim.data.repository.ChatGptRepository
import com.erendogan6.seyahatasistanim.data.repository.ChecklistRepository
import com.erendogan6.seyahatasistanim.data.repository.LocalInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatGptViewModel(
    private val chatGptRepository: ChatGptRepository,
    private val localInfoRepository: LocalInfoRepository,
    private val checklistRepository: ChecklistRepository,
) : ViewModel() {
    private val _chatGptResponse = MutableStateFlow<ChatGptResponse?>(null)
    val chatGptResponse: StateFlow<ChatGptResponse?> = _chatGptResponse

    private val _localInfo = MutableStateFlow<LocalInfoEntity?>(null)
    val localInfo: StateFlow<LocalInfoEntity?> = _localInfo

    private val _checklistItems = MutableStateFlow<List<String>>(emptyList())
    val checklistItems: StateFlow<List<String>> = _checklistItems

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun getSuggestions(prompt: String) {
        viewModelScope.launch {
            try {
                val request =
                    ChatGptRequest(
                        messages = listOf(Message(role = "user", content = prompt)),
                    )
                _isLoading.value = true
                chatGptRepository.getSuggestions(request).collect { response ->
                    _chatGptResponse.value = response
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            }
        }
    }

    fun getLocalInfoForDestination(destination: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val localInfoFromDb = localInfoRepository.getLocalInfo(destination)
                if (localInfoFromDb != null) {
                    _localInfo.value = localInfoFromDb
                } else {
                    val prompt = "Provide detailed local information about $destination."
                    val request =
                        ChatGptRequest(
                            messages = listOf(Message(role = "user", content = prompt)),
                        )
                    chatGptRepository.getSuggestions(request).collect { response ->
                        val localInfoContent =
                            response.choices
                                .firstOrNull()
                                ?.message
                                ?.content
                        if (!localInfoContent.isNullOrEmpty()) {
                            val localInfo = LocalInfoEntity(destination = destination, info = localInfoContent)
                            saveLocalInfoToDatabase(localInfo)
                            _localInfo.value = localInfo
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateChecklist(
        departureLocation: String,
        departureDate: String,
        destination: String,
        arrivalDate: String,
        weatherData: List<WeatherEntity>,
        travelMethod: String,
    ) {
        viewModelScope.launch {
            try {
                val prompt =
                    createChecklistPrompt(
                        departureLocation,
                        departureDate,
                        destination,
                        arrivalDate,
                        weatherData,
                        travelMethod,
                    )
                val request =
                    ChatGptRequest(
                        messages = listOf(Message(role = "user", content = prompt)),
                    )
                _isLoading.value = true
                chatGptRepository.getSuggestions(request).collect { response ->
                    val checklistItems =
                        response.choices
                            .firstOrNull()
                            ?.message
                            ?.content
                            ?.lines()
                            ?.filter { it.isNotBlank() }
                            ?.map { it.trimStart('-').trim() }
                            ?: emptyList()

                    _checklistItems.value = checklistItems
                    saveChecklistToDatabase(checklistItems)
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            }
        }
    }

    private fun createChecklistPrompt(
        departureLocation: String,
        departureDate: String,
        destination: String,
        arrivalDate: String,
        weatherData: List<WeatherEntity>,
        travelMethod: String,
    ): String {
        val weatherSummary =
            weatherData.joinToString(separator = "\n") { weather ->
                "Tarih: ${weather.date}, Sıcaklık: Gündüz ${weather.temperatureDay}°C, Gece ${weather.temperatureNight}°C, Hava Durumu: ${weather.description.firstOrNull()}"
            }

        return """
            Aşağıdaki seyahat bilgileri doğrultusunda, yanımda götürmem gereken öğelerin bir kontrol listesini oluşturun:
            - Kalkış Yeri: $departureLocation
            - Kalkış Tarihi: $departureDate
            - Varış Noktası: $destination
            - Varış Tarihi: $arrivalDate
            - Seyahat Yöntemi: $travelMethod
            - Varıştan sonraki 7 gün için hava durumu tahmini:
            $weatherSummary

            Lütfen aşağıdaki unsurlara dikkat ederek yanımda götürmem gereken öğelerin bir kontrol listesini oluşturun:
            - Seyahat yöntemi (örneğin, uçak, tren, araba vb.) ile ilgili gerekli eşyalar
            - Varış noktasındaki yerel koşullara uygun eşyalar
            - Varış noktasındaki hava durumu koşullarına uygun eşyalar

            Yanıtınızda yalnızca kontrol listesi öğelerini listeleyin ve başka açıklama veya bilgi vermeyin. 
            """.trimIndent()
    }

    private suspend fun saveChecklistToDatabase(items: List<String>) {
        val checklistItems = items.map { ChecklistItemEntity(item = it) }
        checklistRepository.saveChecklistItems(checklistItems)
    }

    private suspend fun saveLocalInfoToDatabase(localInfo: LocalInfoEntity) {
        localInfoRepository.saveLocalInfo(localInfo)
    }

    fun loadChecklistItems() {
        viewModelScope.launch {
            try {
                val checklistItemsFromDb = checklistRepository.getAllChecklistItems()
                _checklistItems.value = checklistItemsFromDb.map { it.item }
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred while loading checklist items."
            }
        }
    }
}
