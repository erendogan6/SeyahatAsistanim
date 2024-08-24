package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.domain.repository.ChecklistRepository

class LoadChecklistItemsUseCase(
    private val checklistRepository: ChecklistRepository,
) {
    suspend operator fun invoke() = checklistRepository.getAllChecklistItems()
}
