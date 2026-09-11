package com.davidpv.padelmatch.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.davidpv.padelmatch.data.db.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players ORDER BY name ASC")
    fun getAllPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players ORDER BY name ASC")
    suspend fun getAllPlayersList(): List<PlayerEntity>

    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getById(id: Long): PlayerEntity?

    @Query("SELECT * FROM players WHERE lower(name) = lower(:name) LIMIT 1")
    suspend fun findByName(name: String): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(player: PlayerEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(players: List<PlayerEntity>): List<Long>

    @Query("DELETE FROM players WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("""
        SELECT COUNT(*) FROM games g
        JOIN sessions s ON s.id = g.sessionId
        WHERE g.winningPair IS NOT NULL
          AND s.date BETWEEN :from AND :to
          AND (g.pair1Player1Id = :playerId OR g.pair1Player2Id = :playerId
            OR g.pair2Player1Id = :playerId OR g.pair2Player2Id = :playerId)
    """)
    suspend fun countGamesForPlayer(playerId: Long, from: String, to: String): Int

    @Query("SELECT COUNT(*) FROM games WHERE winningPair IS NOT NULL AND (pair1Player1Id = :playerId OR pair1Player2Id = :playerId OR pair2Player1Id = :playerId OR pair2Player2Id = :playerId)")
    suspend fun countGamesForPlayer(playerId: Long): Int
}
