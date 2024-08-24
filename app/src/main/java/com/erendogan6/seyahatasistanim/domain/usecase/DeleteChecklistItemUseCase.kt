package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.domain.repository.ChecklistRepository

class DeleteChecklistItemUseCase(
    private val checklistRepository: ChecklistRepository,
) {
    suspend operator fun invoke(id: Int) {
        checklistRepository.deleteChecklistItem(id)
    }
}
