package com.erendogan6.seyahatasistanim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChatGptRequest
import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChatGptResponse
import com.erendogan6.seyahatasistanim.data.model.chatGPT.LocalInfoEntity
import com.erendogan6.seyahatasistanim.data.model.chatGPT.Message
import com.erendogan6.seyahatasistanim.data.repository.ChatGptRepository
import com.erendogan6.seyahatasistanim.data.repository.LocalInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatGptViewModel(
    private val chatGptRepository: ChatGptRepository,
    private val localInfoRepository: LocalInfoRepository,
) : ViewModel() {
    private val _chatGptResponse = MutableStateFlow<ChatGptResponse?>(null)
    val chatGptResponse: StateFlow<ChatGptResponse?> = _chatGptResponse

    private val _localInfo = MutableStateFlow<LocalInfoEntity?>(null)
    val localInfo: StateFlow<LocalInfoEntity?> = _localInfo

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

    private suspend fun saveLocalInfoToDatabase(localInfo: LocalInfoEntity) {
        localInfoRepository.saveLocalInfo(localInfo)
    }
}
