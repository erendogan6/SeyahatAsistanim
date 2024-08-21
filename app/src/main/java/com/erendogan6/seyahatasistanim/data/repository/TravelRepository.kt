package com.erendogan6.seyahatasistanim.data.repository

import com.erendogan6.seyahatasistanim.data.local.TravelDao
import com.erendogan6.seyahatasistanim.data.model.travel.TravelEntity
import com.erendogan6.seyahatasistanim.data.model.weather.City
import com.erendogan6.seyahatasistanim.data.remote.CityApiService

class TravelRepository(
    private val travelDao: TravelDao,
    private val cityApiService: CityApiService,
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

    suspend fun getCitySuggestions(query: String): List<City> = cityApiService.getCities(query = query)
}
