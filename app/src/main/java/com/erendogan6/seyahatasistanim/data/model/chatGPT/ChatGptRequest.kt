package com.erendogan6.seyahatasistanim.data.model.chatGPT

data class ChatGptRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double,
    val max_tokens: Int,
    val top_p: Int,
    val frequency_penalty: Int,
    val presence_penalty: Int,
)
