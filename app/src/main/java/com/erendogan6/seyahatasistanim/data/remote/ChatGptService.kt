package com.erendogan6.seyahatasistanim.data.remote

import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChatGptRequest
import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChatGptResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ChatGptService {
    @POST("v1/chat/completions")
    suspend fun getSuggestions(
        @Header("Authorization") token: String,
        @Body request: ChatGptRequest,
    ): ChatGptResponse
}
