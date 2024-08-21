package com.erendogan6.seyahatasistanim.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChecklistItemEntity

@Dao
interface ChecklistDao {
    @Insert
    suspend fun insertChecklistItems(items: List<ChecklistItemEntity>)

    @Query("SELECT * FROM checklist_items")
    suspend fun getAllChecklistItems(): List<ChecklistItemEntity>
}
