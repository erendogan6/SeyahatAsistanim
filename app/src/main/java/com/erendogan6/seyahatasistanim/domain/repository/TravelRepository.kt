package com.erendogan6.seyahatasistanim.domain.repository

import com.erendogan6.seyahatasistanim.data.model.entity.TravelEntity

interface TravelRepository {
    suspend fun saveTravelInfo(travelEntity: TravelEntity)

    suspend fun getLastTravelInfo(): TravelEntity?
}
