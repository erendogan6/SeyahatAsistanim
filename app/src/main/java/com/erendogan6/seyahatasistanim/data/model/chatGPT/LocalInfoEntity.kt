package com.erendogan6.seyahatasistanim.data.model.chatGPT
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_info")
data class LocalInfoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val destination: String,
    val info: String,
)
