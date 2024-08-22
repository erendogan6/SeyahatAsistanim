package com.erendogan6.seyahatasistanim.ui.viewmodel

import android.util.Log
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

    private val _checklistItems = MutableStateFlow<List<ChecklistItemEntity>>(emptyList())
    val checklistItems: StateFlow<List<ChecklistItemEntity>> = _checklistItems

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun getSuggestions(prompt: String) {
        viewModelScope.launch {
            try {
                Log.i("ChatGptViewModel", "Fetching suggestions for prompt: $prompt")
                _isLoading.value = true
                val request = ChatGptRequest(messages = listOf(Message(role = "user", content = prompt)))
                chatGptRepository.getSuggestions(request).collect { response ->
                    _chatGptResponse.value = response
                    Log.i("ChatGptViewModel", "Suggestions successfully fetched.")
                }
            } catch (e: Exception) {
                val errorMsg = "Error fetching suggestions: ${e.message ?: "Unknown error"}"
                _error.value = errorMsg
                handleChatGPTError(e, errorMsg)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getLocalInfoForDestination(destination: String) {
        viewModelScope.launch {
            Log.i("ChatGptViewModel", "Fetching local info for destination: $destination")
            _isLoading.value = true
            try {
                val localInfoFromDb = localInfoRepository.getLocalInfo(destination)
                if (localInfoFromDb != null) {
                    Log.i("ChatGptViewModel", "Local info found in DB for destination: $destination")
                    _localInfo.value = localInfoFromDb
                } else {
                    Log.i("ChatGptViewModel", "No local info found in DB for $destination, making API request.")
                    val prompt = createLocalInfoPrompt(destination)
                    val request = ChatGptRequest(messages = listOf(Message(role = "user", content = prompt)))
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
                            Log.i("ChatGptViewModel", "Local info for $destination successfully saved to DB.")
                        } else {
                            Log.w("ChatGptViewModel", "Received empty response for $destination.")
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Error fetching local info for $destination: ${e.message ?: "Unknown error"}"
                _error.value = errorMsg
                handleChatGPTError(e, errorMsg)
            } finally {
                _isLoading.value = false
                Log.i("ChatGptViewModel", "Finished processing local info for $destination, loading set to false.")
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
            Log.i("ChatGptViewModel", "Generating checklist for destination: $destination")
            val prompt = createChecklistPrompt(departureLocation, departureDate, destination, arrivalDate, weatherData, travelMethod)
            val request = ChatGptRequest(messages = listOf(Message(role = "user", content = prompt)))
            _isLoading.value = true
            try {
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

                    _checklistItems.value = checklistItems.map { ChecklistItemEntity(item = it) }
                    saveChecklistToDatabase(checklistItems)
                    Log.i("ChatGptViewModel", "Checklist generated and saved to DB for $destination.")
                }
            } catch (e: Exception) {
                val errorMsg = "Error generating checklist for $destination: ${e.message ?: "Unknown error"}"
                _error.value = errorMsg
                handleChatGPTError(e, errorMsg)
            } finally {
                _isLoading.value = false
                Log.i("ChatGptViewModel", "Finished generating checklist for $destination, loading set to false.")
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
        Log.i("ChatGptViewModel", "Checklist items saved to local database.")
    }

    private suspend fun saveLocalInfoToDatabase(localInfo: LocalInfoEntity) {
        localInfoRepository.saveLocalInfo(localInfo)
        Log.i("ChatGptViewModel", "Local info saved to local database.")
    }

    fun loadChecklistItems() {
        viewModelScope.launch {
            try {
                Log.i("ChatGptViewModel", "Loading checklist items from database.")
                val checklistItemsFromDb = checklistRepository.getAllChecklistItems()
                _checklistItems.value = checklistItemsFromDb.map { it }
                Log.i("ChatGptViewModel", "Checklist items loaded from database.")
            } catch (e: Exception) {
                val errorMsg = "Error loading checklist items from database: ${e.message ?: "Unknown error"}"
                _error.value = errorMsg
                handleChatGPTError(e, errorMsg)
            }
        }
    }

    fun addChecklistItem(item: String) {
        viewModelScope.launch {
            Log.i("ChatGptViewModel", "Adding new checklist item: $item")
            try {
                val newItem = ChecklistItemEntity(item = item)
                checklistRepository.saveChecklistItems(listOf(newItem))
                loadChecklistItems()
                Log.i("ChatGptViewModel", "New checklist item added and checklist reloaded.")
            } catch (e: Exception) {
                val errorMsg = "Error adding checklist item: ${e.message ?: "Unknown error"}"
                _error.value = errorMsg
                handleChatGPTError(e, errorMsg)
            }
        }
    }

    fun deleteChecklistItem(id: Int) {
        viewModelScope.launch {
            Log.i("ChatGptViewModel", "Deleting checklist item with ID: $id")
            try {
                checklistRepository.deleteChecklistItem(id)
                loadChecklistItems()
                Log.i("ChatGptViewModel", "Checklist item deleted and checklist reloaded.")
            } catch (e: Exception) {
                val errorMsg = "Error deleting checklist item with ID $id: ${e.message ?: "Unknown error"}"
                _error.value = errorMsg
                handleChatGPTError(e, errorMsg)
            }
        }
    }

    fun toggleItemCompletion(id: Int) {
        viewModelScope.launch {
            Log.i("ChatGptViewModel", "Toggling completion status for checklist item with ID: $id")
            try {
                checklistRepository.toggleChecklistItemCompletion(id)
                loadChecklistItems()
                Log.i("ChatGptViewModel", "Completion status toggled and checklist reloaded.")
            } catch (e: Exception) {
                val errorMsg = "Error toggling completion status for checklist item with ID $id: ${e.message ?: "Unknown error"}"
                _error.value = errorMsg
                handleChatGPTError(e, errorMsg)
            }
        }
    }

    private fun createLocalInfoPrompt(destination: String): String =
        """
        $destination'a bir seyahat planlıyorum. Ziyaretim sırasında bana faydalı olacak kapsamlı yerel bilgiler sağlayabilir misiniz? Lütfen kültürel öne çıkanlar, tarihi yerler, popüler cazibe merkezleri, yerel mutfak, ulaşım seçenekleri ve $destination'daki deneyimimi geliştirebilecek diğer önemli ipuçlarını içeren bilgileri ekleyin. Ayrıca, konaklamam süresince dikkate almam gereken mevsimsel veya hava durumuyla ilgili tavsiyeleri de belirtin.
        """.trimIndent()

    private fun handleChatGPTError(
        error: Throwable,
        customMessage: String,
    ) {
        Log.e("ChatGptViewModel", "$customMessage - ${error.message}")
    }
}
