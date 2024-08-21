package com.erendogan6.seyahatasistanim.data.repository

import com.erendogan6.seyahatasistanim.data.local.ChecklistDao
import com.erendogan6.seyahatasistanim.data.model.chatGPT.ChecklistItemEntity

class ChecklistRepository(
    private val checklistDao: ChecklistDao,
) {
    suspend fun saveChecklistItems(items: List<ChecklistItemEntity>) {
        checklistDao.insertChecklistItems(items)
    }

    suspend fun getAllChecklistItems(): List<ChecklistItemEntity> = checklistDao.getAllChecklistItems()
}
