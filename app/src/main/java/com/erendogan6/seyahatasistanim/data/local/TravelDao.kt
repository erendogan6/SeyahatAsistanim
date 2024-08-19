package com.erendogan6.seyahatasistanim.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.erendogan6.seyahatasistanim.data.model.travel.TravelEntity

@Dao
interface TravelDao {
    @Insert
    suspend fun insertTravelInfo(travelEntity: TravelEntity)

    @Query("SELECT * FROM travel_info ORDER BY id DESC LIMIT 1")
    suspend fun getLastTravelInfo(): TravelEntity?
}
