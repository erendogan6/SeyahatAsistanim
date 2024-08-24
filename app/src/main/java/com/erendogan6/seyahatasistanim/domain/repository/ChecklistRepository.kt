package com.erendogan6.seyahatasistanim.domain.repository

import com.erendogan6.seyahatasistanim.data.model.entity.ChecklistItemEntity

interface ChecklistRepository {
    suspend fun saveChecklistItems(items: List<ChecklistItemEntity>)

    suspend fun getAllChecklistItems(): List<ChecklistItemEntity>

    suspend fun deleteChecklistItem(id: Int)

    suspend fun toggleChecklistItemCompletion(id: Int)
}
