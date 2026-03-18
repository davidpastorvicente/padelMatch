package com.padelgroup.padelMatch.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "games",
    foreignKeys = [
        ForeignKey(entity = SessionEntity::class, parentColumns = ["id"], childColumns = ["sessionId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("sessionId")]
)
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val gameNumber: Int,
    val pair1Player1Id: Long,
    val pair1Player2Id: Long,
    val pair2Player1Id: Long,
    val pair2Player2Id: Long,
    val pair1Score: Int? = null,
    val pair2Score: Int? = null,
    @ColumnInfo(name = "winningPair") val winningPair: Int? = null
)
