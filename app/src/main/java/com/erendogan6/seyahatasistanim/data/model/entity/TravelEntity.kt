package com.erendogan6.seyahatasistanim.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "travel_info")
data class TravelEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val departureDate: String,
    val arrivalDate: String,
    val departurePlace: String,
    val arrivalPlace: String,
    val travelMethod: String,
    val departureLatitude: Double,
    val departureLongitude: Double,
    val arrivalLatitude: Double,
    val arrivalLongitude: Double,
)
