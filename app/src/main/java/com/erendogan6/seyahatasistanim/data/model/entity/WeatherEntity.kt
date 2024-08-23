package com.erendogan6.seyahatasistanim.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "weather_data")
data class WeatherEntity(
    @PrimaryKey val id: String,
    val date: LocalDate,
    val temperatureDay: Double,
    val temperatureNight: Double,
    val description: String,
    val latitude: Double,
    val longitude: Double,
)
