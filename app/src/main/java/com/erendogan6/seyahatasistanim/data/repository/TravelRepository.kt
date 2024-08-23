package com.erendogan6.seyahatasistanim.data.repository

import com.erendogan6.seyahatasistanim.data.local.dao.TravelDao
import com.erendogan6.seyahatasistanim.data.model.entity.TravelEntity

class TravelRepository(
    private val travelDao: TravelDao,
) {
    suspend fun saveTravelInfo(travelEntity: TravelEntity) {
        travelDao.insertTravelInfo(travelEntity)
    }

    suspend fun getLastTravelInfo(): TravelEntity? {
        val lastTravelInfo = travelDao.getLastTravelInfo()
        return lastTravelInfo
    }

    suspend fun deleteAllTravelInfo() {
        travelDao.deleteAllTravelInfo()
    }
}
