package com.erendogan6.seyahatasistanim.domain.usecase

import com.erendogan6.seyahatasistanim.data.model.entity.TravelEntity
import com.erendogan6.seyahatasistanim.domain.repository.TravelRepository

class GetLastTravelInfoUseCase(
    private val travelRepository: TravelRepository,
) {
    suspend operator fun invoke(): TravelEntity? = travelRepository.getLastTravelInfo()
}
