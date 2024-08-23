package com.erendogan6.seyahatasistanim.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.erendogan6.seyahatasistanim.data.model.entity.ChecklistItemEntity

@Dao
interface ChecklistDao {
    @Insert
    suspend fun insertChecklistItems(items: List<ChecklistItemEntity>)

    @Query("SELECT * FROM checklist_items")
    suspend fun getAllChecklistItems(): List<ChecklistItemEntity>

    @Query("DELETE FROM checklist_items WHERE id = :id")
    suspend fun deleteChecklistItem(id: Int)

    @Query("SELECT * FROM checklist_items WHERE id = :id")
    suspend fun getChecklistItemById(id: Int): ChecklistItemEntity

    @Update
    suspend fun updateChecklistItem(item: ChecklistItemEntity)
}
