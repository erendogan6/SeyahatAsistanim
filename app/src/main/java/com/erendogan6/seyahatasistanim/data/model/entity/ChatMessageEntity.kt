package com.erendogan6.seyahatasistanim.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    val content: String,
    val role: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
)
