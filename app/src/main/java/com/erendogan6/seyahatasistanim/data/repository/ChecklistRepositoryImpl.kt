package com.erendogan6.seyahatasistanim.data.repository

import com.erendogan6.seyahatasistanim.data.local.dao.ChecklistDao
import com.erendogan6.seyahatasistanim.data.model.entity.ChecklistItemEntity
import com.erendogan6.seyahatasistanim.domain.repository.ChecklistRepository

class ChecklistRepositoryImpl(
    private val checklistDao: ChecklistDao,
) : ChecklistRepository {
    override suspend fun saveChecklistItems(items: List<ChecklistItemEntity>) {
        checklistDao.insertChecklistItems(items)
    }

    override suspend fun getAllChecklistItems(): List<ChecklistItemEntity> = checklistDao.getAllChecklistItems()

    override suspend fun deleteChecklistItem(id: Int) {
        checklistDao.deleteChecklistItem(id)
    }

    override suspend fun toggleChecklistItemCompletion(id: Int) {
        val item = checklistDao.getChecklistItemById(id)
        val updatedItem = item.copy(isCompleted = !item.isCompleted)
        checklistDao.updateChecklistItem(updatedItem)
    }
}
