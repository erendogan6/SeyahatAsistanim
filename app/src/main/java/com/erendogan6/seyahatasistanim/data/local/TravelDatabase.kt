package com.erendogan6.seyahatasistanim.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.erendogan6.seyahatasistanim.data.model.travel.TravelEntity

@Database(entities = [TravelEntity::class], version = 1, exportSchema = false)
abstract class TravelDatabase : RoomDatabase() {
    abstract fun travelDao(): TravelDao
}
