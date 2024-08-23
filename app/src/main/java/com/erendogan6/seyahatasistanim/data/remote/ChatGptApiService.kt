package com.erendogan6.seyahatasistanim.data.remote

import com.erendogan6.seyahatasistanim.data.model.dto.chatGPT.ChatGptRequest
import com.erendogan6.seyahatasistanim.data.model.dto.chatGPT.ChatGptResponse
import retrofit2.http.Body
import retrofit2.http.POST

fun interface ChatGptApiService {
    @POST("v1/chat/completions")
    suspend fun getSuggestions(
        @Body request: ChatGptRequest,
    ): ChatGptResponse
}
