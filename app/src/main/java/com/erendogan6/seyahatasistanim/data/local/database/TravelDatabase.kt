package com.erendogan6.seyahatasistanim.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.erendogan6.seyahatasistanim.data.local.dao.ChatMessageDao
import com.erendogan6.seyahatasistanim.data.local.dao.ChecklistDao
import com.erendogan6.seyahatasistanim.data.local.dao.LocalInfoDao
import com.erendogan6.seyahatasistanim.data.local.dao.TravelDao
import com.erendogan6.seyahatasistanim.data.local.dao.WeatherDao
import com.erendogan6.seyahatasistanim.data.model.entity.ChatMessageEntity
import com.erendogan6.seyahatasistanim.data.model.entity.ChecklistItemEntity
import com.erendogan6.seyahatasistanim.data.model.entity.LocalInfoEntity
import com.erendogan6.seyahatasistanim.data.model.entity.TravelEntity
import com.erendogan6.seyahatasistanim.data.model.entity.WeatherEntity
import com.erendogan6.seyahatasistanim.utils.Converters

@Database(
    entities = [TravelEntity::class, WeatherEntity::class, LocalInfoEntity::class, ChecklistItemEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class TravelDatabase : RoomDatabase() {
    abstract fun travelDao(): TravelDao

    abstract fun weatherDao(): WeatherDao

    abstract fun localInfoDao(): LocalInfoDao

    abstract fun checklistDao(): ChecklistDao

    abstract fun chatMessageDao(): ChatMessageDao

    override fun clearAllTables() {
        clearAllTables()
    }
}
