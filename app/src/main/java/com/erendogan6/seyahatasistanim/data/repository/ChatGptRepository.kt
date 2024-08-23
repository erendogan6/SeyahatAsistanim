package com.erendogan6.seyahatasistanim.data.repository

import com.erendogan6.seyahatasistanim.data.local.dao.ChatMessageDao
import com.erendogan6.seyahatasistanim.data.model.dto.chatGPT.ChatGptRequest
import com.erendogan6.seyahatasistanim.data.model.dto.chatGPT.ChatGptResponse
import com.erendogan6.seyahatasistanim.data.model.entity.ChatMessageEntity
import com.erendogan6.seyahatasistanim.data.remote.ChatGptApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ChatGptRepository(
    private val chatGptApiService: ChatGptApiService,
    private val chatMessageDao: ChatMessageDao,
) {
    fun getSuggestions(request: ChatGptRequest): Flow<ChatGptResponse> =
        flow {
            try {
                val response = chatGptApiService.getSuggestions(request)
                emit(response)
            } catch (e: Exception) {
                throw e
            }
        }

    suspend fun saveChatMessage(message: ChatMessageEntity) {
        chatMessageDao.insertChatMessage(message)
    }

    suspend fun getAllChatMessages(): List<ChatMessageEntity> = chatMessageDao.getAllChatMessages()

    suspend fun deleteAllMessages() {
        chatMessageDao.deleteAllMessages()
    }
}
