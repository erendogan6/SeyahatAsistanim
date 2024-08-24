package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.data.model.entity.ChecklistItemEntity
import com.erendogan6.seyahatasistanim.domain.repository.ChecklistRepository

class AddChecklistItemUseCase(
    private val checklistRepository: ChecklistRepository,
) {
    suspend operator fun invoke(item: String) {
        checklistRepository.saveChecklistItems(listOf(ChecklistItemEntity(item = item)))
    }
}
