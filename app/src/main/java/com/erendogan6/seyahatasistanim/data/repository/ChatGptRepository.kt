package com.erendogan6.seyahatasistanim.data.repository

import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChatGptRequest
import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChatGptResponse
import com.erendogan6.seyahatasistanim.data.remote.ChatGptApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ChatGptRepository : KoinComponent {
    private val chatGptApiService: ChatGptApiService by inject()

    fun getSuggestions(request: ChatGptRequest): Flow<ChatGptResponse> =
        flow {
            val response = chatGptApiService.getSuggestions(request)
            emit(response)
        }
}
