package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.data.model.dto.chatGPT.ChatGptRequest
import com.erendogan6.seyahatasistanim.data.model.dto.chatGPT.ChatGptResponse
import com.erendogan6.seyahatasistanim.domain.repository.ChatGptRepository
import kotlinx.coroutines.flow.Flow

class GetSuggestionsUseCase(
    private val chatGptRepository: ChatGptRepository,
) {
    operator fun invoke(request: ChatGptRequest): Flow<ChatGptResponse> = chatGptRepository.getSuggestions(request)
}
