package com.erendogan6.seyahatasistanim.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.erendogan6.seyahatasistanim.data.model.chatGPT.LocalInfoEntity
import com.erendogan6.seyahatasistanim.data.model.travel.TravelEntity
import com.erendogan6.seyahatasistanim.data.model.weather.WeatherEntity
import com.erendogan6.seyahatasistanim.utils.Converters

@Database(entities = [TravelEntity::class, WeatherEntity::class, LocalInfoEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class TravelDatabase : RoomDatabase() {
    abstract fun travelDao(): TravelDao

    abstract fun weatherDao(): WeatherDao

    abstract fun localInfoDao(): LocalInfoDao
}
