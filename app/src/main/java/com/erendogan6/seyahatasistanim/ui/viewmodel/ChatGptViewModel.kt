package com.erendogan6.seyahatasistanim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChatGptRequest
import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChatGptResponse
import com.erendogan6.seyahatasistanim.data.model.chatGPT.Message
import com.erendogan6.seyahatasistanim.data.repository.ChatGptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatGptViewModel(
    private val chatGptRepository: ChatGptRepository,
) : ViewModel() {
    private val _chatGptResponse = MutableStateFlow<ChatGptResponse?>(null)
    val chatGptResponse: StateFlow<ChatGptResponse?> = _chatGptResponse

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
}
