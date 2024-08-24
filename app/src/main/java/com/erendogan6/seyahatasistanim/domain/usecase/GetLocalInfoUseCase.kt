package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.data.model.entity.LocalInfoEntity
import com.erendogan6.seyahatasistanim.domain.repository.LocalInfoRepository

class GetLocalInfoUseCase(
    private val localInfoRepository: LocalInfoRepository,
) {
    suspend operator fun invoke(): LocalInfoEntity? = localInfoRepository.getLocalInfo()
}
