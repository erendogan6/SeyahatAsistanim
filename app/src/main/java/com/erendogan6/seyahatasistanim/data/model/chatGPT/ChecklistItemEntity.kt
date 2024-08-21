package com.erendogan6.seyahatasistanim.data.model.chatGPT

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "checklist_items")
data class ChecklistItemEntity(
    val item: String,
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
)
