package com.erendogan6.seyahatasistanim.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erendogan6.seyahatasistanim.data.model.entity.WeatherEntity
import java.time.LocalDate

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherData(weatherData: List<WeatherEntity>)

    @Query("SELECT * FROM weather_data WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getWeatherDataForRange(
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<WeatherEntity>
}
