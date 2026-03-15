package com.padelgroup.padelMatch.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "session_players",
    primaryKeys = ["sessionId", "playerId"],
    foreignKeys = [
        ForeignKey(entity = SessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = PlayerEntity::class, parentColumns = ["id"], childColumns = ["playerId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class SessionPlayerEntity(
    val sessionId: Long,
    val playerId: Long,
    val winRatio: Float
)
