package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.data.model.entity.ChatMessageEntity
import com.erendogan6.seyahatasistanim.domain.repository.ChatGptRepository

class SaveChatMessageUseCase(
    private val chatGptRepository: ChatGptRepository,
) {
    suspend operator fun invoke(message: ChatMessageEntity) {
        chatGptRepository.saveChatMessage(message)
    }
}
