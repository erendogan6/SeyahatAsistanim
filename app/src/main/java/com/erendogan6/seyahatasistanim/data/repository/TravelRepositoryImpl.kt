package com.erendogan6.seyahatasistanim.data.repository

import com.erendogan6.seyahatasistanim.data.local.dao.TravelDao
import com.erendogan6.seyahatasistanim.data.model.entity.TravelEntity
import com.erendogan6.seyahatasistanim.domain.repository.TravelRepository

class TravelRepositoryImpl(
    private val travelDao: TravelDao,
) : TravelRepository {
    override suspend fun saveTravelInfo(travelEntity: TravelEntity) {
        travelDao.insertTravelInfo(travelEntity)
    }

    override suspend fun getLastTravelInfo(): TravelEntity? = travelDao.getLastTravelInfo()
}
