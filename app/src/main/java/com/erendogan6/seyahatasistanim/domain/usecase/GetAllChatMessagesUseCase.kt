package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.data.model.entity.ChatMessageEntity
import com.erendogan6.seyahatasistanim.domain.repository.ChatGptRepository

class GetAllChatMessagesUseCase(
    private val chatGptRepository: ChatGptRepository,
) {
    suspend operator fun invoke(): List<ChatMessageEntity> = chatGptRepository.getAllChatMessages()
}
