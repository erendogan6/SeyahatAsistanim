package com.erendogan6.seyahatasistanim.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.erendogan6.seyahatasistanim.data.model.entity.ChatMessageEntity

@Dao
interface ChatMessageDao {
    @Insert
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_messages ORDER BY id ASC")
    suspend fun getAllChatMessages(): List<ChatMessageEntity>
}
