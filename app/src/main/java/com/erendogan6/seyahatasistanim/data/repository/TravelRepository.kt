package com.erendogan6.seyahatasistanim.data.repository

import com.erendogan6.seyahatasistanim.data.local.TravelDao
import com.erendogan6.seyahatasistanim.data.model.travel.TravelEntity

class TravelRepository(
    private val travelDao: TravelDao,
) {
    suspend fun saveTravelInfo(travelEntity: TravelEntity) {
        travelDao.insertTravelInfo(travelEntity)
    }

    suspend fun getLastTravelInfo(): TravelEntity? = travelDao.getLastTravelInfo()
}
