package com.erendogan6.seyahatasistanim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChatGptRequest
import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChatGptResponse
import com.erendogan6.seyahatasistanim.data.model.chatGPT.Message
import com.erendogan6.seyahatasistanim.data.repository.ChatGptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ChatGptViewModel(
    private val chatGptRepository: ChatGptRepository,
) : ViewModel() {
    private val _chatGptResponse = MutableStateFlow<ChatGptResponse?>(null)
    val chatGptResponse: StateFlow<ChatGptResponse?> = _chatGptResponse

    fun getSuggestions(prompt: String) {
        viewModelScope.launch {
            val request =
                ChatGptRequest(
                    messages = listOf(Message(role = "user", content = prompt)),
                )

            chatGptRepository
                .getSuggestions(request)
                .catch { e ->
                    _chatGptResponse.value = null
                }.collect { response ->
                    _chatGptResponse.value = response
                }
        }
    }
}
