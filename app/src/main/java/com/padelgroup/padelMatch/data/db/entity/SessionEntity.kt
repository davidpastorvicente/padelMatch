package com.davidpv.padelmatch.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,          // ISO format: YYYY-MM-DD
    val createdAt: Long = System.currentTimeMillis()
)
