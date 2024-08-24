package com.erendogan6.seyahatasistanim.domain.repository

import com.erendogan6.seyahatasistanim.data.model.dto.chatGPT.ChatGptRequest
import com.erendogan6.seyahatasistanim.data.model.dto.chatGPT.ChatGptResponse
import com.erendogan6.seyahatasistanim.data.model.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

interface ChatGptRepository {
    fun getSuggestions(request: ChatGptRequest): Flow<ChatGptResponse>

    suspend fun saveChatMessage(message: ChatMessageEntity)

    suspend fun getAllChatMessages(): List<ChatMessageEntity>
}
