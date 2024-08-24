package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.data.model.entity.ChecklistItemEntity
import com.erendogan6.seyahatasistanim.domain.repository.ChecklistRepository

class SaveChecklistItemsUseCase(
    private val checklistRepository: ChecklistRepository,
) {
    suspend operator fun invoke(items: List<ChecklistItemEntity>) {
        checklistRepository.saveChecklistItems(items)
    }
}
