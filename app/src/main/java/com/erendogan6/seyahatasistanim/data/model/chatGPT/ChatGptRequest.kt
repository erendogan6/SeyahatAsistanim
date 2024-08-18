package com.erendogan6.seyahatasistanim.data.model.chatGPT

data class ChatGptRequest(
    val model: String = "gpt-4o",
    val messages: List<Message>,
    val temperature: Double = 1.0,
    val max_tokens: Int = 4096,
    val top_p: Int = 1,
    val frequency_penalty: Int = 0,
    val presence_penalty: Int = 0,
)
