package com.erendogan6.seyahatasistanim.data.repository

import com.erendogan6.seyahatasistanim.data.local.dao.ChecklistDao
import com.erendogan6.seyahatasistanim.data.model.entity.ChecklistItemEntity

class ChecklistRepository(
    private val checklistDao: ChecklistDao,
) {
    suspend fun saveChecklistItems(items: List<ChecklistItemEntity>) {
        checklistDao.insertChecklistItems(items)
    }

    suspend fun getAllChecklistItems(): List<ChecklistItemEntity> = checklistDao.getAllChecklistItems()

    suspend fun deleteChecklistItem(id: Int) {
        checklistDao.deleteChecklistItem(id)
    }

    suspend fun toggleChecklistItemCompletion(id: Int) {
        val item = checklistDao.getChecklistItemById(id)
        val updatedItem = item.copy(isCompleted = !item.isCompleted)
        checklistDao.updateChecklistItem(updatedItem)
    }
}
