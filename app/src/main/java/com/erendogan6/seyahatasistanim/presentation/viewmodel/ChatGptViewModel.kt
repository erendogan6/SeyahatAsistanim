package com.erendogan6.seyahatasistanim.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.seyahatasistanim.R
import com.erendogan6.seyahatasistanim.data.model.dto.chatGPT.ChatGptRequest
import com.erendogan6.seyahatasistanim.data.model.dto.chatGPT.Message
import com.erendogan6.seyahatasistanim.data.model.entity.ChatMessageEntity
import com.erendogan6.seyahatasistanim.data.model.entity.ChecklistItemEntity
import com.erendogan6.seyahatasistanim.data.model.entity.LocalInfoEntity
import com.erendogan6.seyahatasistanim.data.model.entity.WeatherEntity
import com.erendogan6.seyahatasistanim.domain.usecase.AddChecklistItemUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.DeleteChecklistItemUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetAllChatMessagesUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetLocalInfoUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.GetSuggestionsUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.LoadChecklistItemsUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.SaveChatMessageUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.SaveChecklistItemsUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.SaveLocalInfoUseCase
import com.erendogan6.seyahatasistanim.domain.usecase.ToggleItemCompletionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatGptViewModel(
    private val getLocalInfoUseCase: GetLocalInfoUseCase,
    private val saveLocalInfoUseCase: SaveLocalInfoUseCase,
    private val loadChecklistItemsUseCase: LoadChecklistItemsUseCase,
    private val addChecklistItemUseCase: AddChecklistItemUseCase,
    private val deleteChecklistItemUseCase: DeleteChecklistItemUseCase,
    private val toggleItemCompletionUseCase: ToggleItemCompletionUseCase,
    private val saveChecklistItemsUseCase: SaveChecklistItemsUseCase,
    private val getSuggestionsUseCase: GetSuggestionsUseCase,
    private val saveChatMessageUseCase: SaveChatMessageUseCase,
    private val getAllChatMessagesUseCase: GetAllChatMessagesUseCase,
    private val context: Context,
) : ViewModel() {
    private val _localInfo = MutableStateFlow<LocalInfoEntity?>(null)
    val localInfo: StateFlow<LocalInfoEntity?> = _localInfo

    private val _checklistItems = MutableStateFlow<List<ChecklistItemEntity>>(emptyList())
    val checklistItems: StateFlow<List<ChecklistItemEntity>> = _checklistItems

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _conversation = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val conversation: StateFlow<List<ChatMessageEntity>> = _conversation

    init {
        loadConversation()
    }

    private fun loadConversation() {
        viewModelScope.launch {
            try {
                val messages = getAllChatMessagesUseCase()
                _conversation.value = messages
            } catch (e: Exception) {
                _error.value = context.getString(R.string.failed_to_load_conversation, e.message ?: "")
            }
        }
    }

    fun sendMessage(
        userMessage: String,
        departureLocation: String,
        departureDate: String,
        arrivalLocation: String,
        arrivalDate: String,
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                val userMessageEntity = ChatMessageEntity(content = userMessage, role = "user")
                saveChatMessageUseCase(userMessageEntity)
                _conversation.value += userMessageEntity

                val prompt = createPrompt(userMessage, departureLocation, departureDate, arrivalLocation, arrivalDate)
                val request = ChatGptRequest(messages = listOf(Message(role = "user", content = prompt)))

                getSuggestionsUseCase(request).collect { response ->
                    val assistantMessage =
                        response.choices
                            .firstOrNull()
                            ?.message
                            ?.content
                            .orEmpty()
                    val assistantMessageEntity = ChatMessageEntity(content = assistantMessage, role = "assistant")

                    saveChatMessageUseCase(assistantMessageEntity)
                    _conversation.value += assistantMessageEntity
                }
            } catch (e: Exception) {
                _error.value = context.getString(R.string.failed_to_send_message, e.message ?: "")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun createPrompt(
        userMessage: String,
        departureLocation: String,
        departureDate: String,
        arrivalLocation: String,
        arrivalDate: String,
    ): String =
        """
        ${context.getString(R.string.travel_details)}:
        - ${context.getString(R.string.departure_location)}: $departureLocation
        - ${context.getString(R.string.departure_date)}: $departureDate
        - ${context.getString(R.string.arrival_location)}: $arrivalLocation
        - ${context.getString(R.string.arrival_date)}: $arrivalDate

        ${context.getString(R.string.user_message)}:
        $userMessage

        ${context.getString(R.string.provide_helpful_information)}
        """.trimIndent()

    fun getLocalInfoForDestination(destination: String) {
        viewModelScope.launch {
            Log.i("ChatGptViewModel", context.getString(R.string.fetching_local_info))
            _isLoading.value = true
            try {
                val localInfoFromDb = getLocalInfoUseCase()
                if (localInfoFromDb != null) {
                    Log.i("ChatGptViewModel", context.getString(R.string.local_info_found_in_db))
                    _localInfo.value = localInfoFromDb
                } else {
                    Log.i("ChatGptViewModel", context.getString(R.string.no_local_info_found))
                    val prompt = createLocalInfoPrompt(destination)
                    val request = ChatGptRequest(messages = listOf(Message(role = "user", content = prompt)))
                    getSuggestionsUseCase(request).collect { response ->
                        val localInfoContent =
                            response.choices
                                .firstOrNull()
                                ?.message
                                ?.content
                        if (!localInfoContent.isNullOrEmpty()) {
                            val localInfo = LocalInfoEntity(destination = destination, info = localInfoContent)
                            saveLocalInfoToDatabase(localInfo)
                            _localInfo.value = localInfo
                            Log.i("ChatGptViewModel", context.getString(R.string.local_info_saved_to_db, destination))
                        } else {
                            Log.w("ChatGptViewModel", context.getString(R.string.received_empty_response, destination))
                        }
                    }
                }
            } catch (e: Exception) {
                val errorMsg = context.getString(R.string.error_fetching_local_info, destination, e.message ?: "")
                _error.value = errorMsg
                handleChatGPTError(e, errorMsg)
            } finally {
                _isLoading.value = false
                Log.i("ChatGptViewModel", context.getString(R.string.finished_processing_local_info, destination))
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
        daysToStay: Int,
    ) {
        viewModelScope.launch {
            Log.i("ChatGptViewModel", context.getString(R.string.generating_checklist, destination))
            val prompt =
                createChecklistPrompt(departureLocation, departureDate, destination, arrivalDate, weatherData, travelMethod, daysToStay)
            val request = ChatGptRequest(messages = listOf(Message(role = "user", content = prompt)))
            _isLoading.value = true
            try {
                getSuggestionsUseCase(request).collect { response ->
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
                    Log.i("ChatGptViewModel", context.getString(R.string.checklist_generated_and_saved, destination))
                }
            } catch (e: Exception) {
                val errorMsg = context.getString(R.string.error_generating_checklist, destination, e.message ?: "")
                _error.value = errorMsg
                handleChatGPTError(e, errorMsg)
            } finally {
                _isLoading.value = false
                Log.i("ChatGptViewModel", context.getString(R.string.finished_generating_checklist, destination))
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
        daysToStay: Int,
    ): String {
        val weatherSummary =
            weatherData.joinToString(separator = "\n") { weather ->
                context.getString(
                    R.string.weather_summary_format,
                    weather.date,
                    weather.temperatureDay,
                    weather.temperatureNight,
                    weather.description.firstOrNull(),
                )
            }

        return context.getString(
            R.string.checklist_prompt_format,
            departureLocation,
            departureDate,
            destination,
            arrivalDate,
            travelMethod,
            daysToStay,
            weatherSummary,
        )
    }

    private suspend fun saveChecklistToDatabase(items: List<String>) {
        val checklistItems = items.map { ChecklistItemEntity(item = it) }
        saveChecklistItemsUseCase(checklistItems)
        Log.i("ChatGptViewModel", context.getString(R.string.checklist_items_saved_to_db))
    }

    private suspend fun saveLocalInfoToDatabase(localInfo: LocalInfoEntity) {
        saveLocalInfoUseCase(localInfo)
        Log.i("ChatGptViewModel", context.getString(R.string.local_info_saved_to_db))
    }

    fun loadChecklistItems() {
        viewModelScope.launch {
            try {
                Log.i("ChatGptViewModel", context.getString(R.string.loading_checklist_items))
                val checklistItemsFromDb = loadChecklistItemsUseCase()
                _checklistItems.value = checklistItemsFromDb.map { it }
                Log.i("ChatGptViewModel", context.getString(R.string.checklist_items_loaded_from_db))
            } catch (e: Exception) {
                val errorMsg = context.getString(R.string.error_loading_checklist_items, e.message ?: "")
                _error.value = errorMsg
                handleChatGPTError(e, errorMsg)
            }
        }
    }

    fun addChecklistItem(item: String) {
        viewModelScope.launch {
            Log.i("ChatGptViewModel", context.getString(R.string.adding_new_checklist_item, item))
            try {
                addChecklistItemUseCase(item)
                loadChecklistItems()
                Log.i("ChatGptViewModel", context.getString(R.string.new_checklist_item_added))
            } catch (e: Exception) {
                val errorMsg = context.getString(R.string.error_adding_checklist_item, e.message ?: "")
                _error.value = errorMsg
                handleChatGPTError(e, errorMsg)
            }
        }
    }

    fun deleteChecklistItem(id: Int) {
        viewModelScope.launch {
            Log.i("ChatGptViewModel", context.getString(R.string.deleting_checklist_item, id))
            try {
                deleteChecklistItemUseCase(id)
                loadChecklistItems()
                Log.i("ChatGptViewModel", context.getString(R.string.checklist_item_deleted))
            } catch (e: Exception) {
                val errorMsg = context.getString(R.string.error_deleting_checklist_item, id, e.message ?: "")
                _error.value = errorMsg
                handleChatGPTError(e, errorMsg)
            }
        }
    }

    fun toggleItemCompletion(id: Int) {
        viewModelScope.launch {
            Log.i("ChatGptViewModel", context.getString(R.string.toggling_completion_status, id))
            try {
                toggleItemCompletionUseCase(id)
                loadChecklistItems()
                Log.i("ChatGptViewModel", context.getString(R.string.completion_status_toggled))
            } catch (e: Exception) {
                val errorMsg = context.getString(R.string.error_toggling_completion_status, id, e.message ?: "")
                _error.value = errorMsg
                handleChatGPTError(e, errorMsg)
            }
        }
    }

    private fun createLocalInfoPrompt(destination: String): String =
        """
        ${context.getString(R.string.planning_trip_to, destination)}. 
        ${context.getString(R.string.can_you_provide_local_info)}?
        ${context.getString(R.string.include_tips)}.
        """.trimIndent()

    private fun handleChatGPTError(
        error: Throwable,
        customMessage: String,
    ) {
        Log.e("ChatGptViewModel", "$customMessage - ${error.message}")
    }
}
