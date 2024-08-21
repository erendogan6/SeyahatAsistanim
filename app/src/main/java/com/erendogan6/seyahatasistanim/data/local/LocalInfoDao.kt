package com.erendogan6.seyahatasistanim.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erendogan6.seyahatasistanim.data.model.chatGPT.LocalInfoEntity

@Dao
interface LocalInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocalInfo(localInfo: LocalInfoEntity)

    @Query("SELECT * FROM local_info WHERE destination = :destination LIMIT 1")
    suspend fun getLocalInfo(destination: String): LocalInfoEntity?
}
