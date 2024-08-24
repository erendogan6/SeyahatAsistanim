package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.data.model.entity.LocalInfoEntity
import com.erendogan6.seyahatasistanim.domain.repository.LocalInfoRepository

class SaveLocalInfoUseCase(
    private val localInfoRepository: LocalInfoRepository,
) {
    suspend operator fun invoke(localInfo: LocalInfoEntity) {
        localInfoRepository.saveLocalInfo(localInfo)
    }
}
