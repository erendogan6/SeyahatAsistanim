package com.erendogan6.seyahatasistanim.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erendogan6.seyahatasistanim.data.model.entity.LocalInfoEntity

@Dao
interface LocalInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLocalInfo(localInfo: LocalInfoEntity)

    @Query("SELECT * FROM local_info")
    suspend fun getLocalInfo(): LocalInfoEntity?
}
