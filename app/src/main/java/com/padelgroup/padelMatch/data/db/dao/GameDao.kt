package com.padelgroup.padelMatch.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.padelgroup.padelMatch.data.db.entity.GameEntity

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE sessionId = :sessionId ORDER BY gameNumber ASC")
    suspend fun getGamesForSession(sessionId: Long): List<GameEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<GameEntity>)

    @Update
    suspend fun update(game: GameEntity)

    @Query("UPDATE games SET winningPair = :winningPair WHERE id = :gameId")
    suspend fun updateGameWinner(gameId: Long, winningPair: Int?)

    @Query("UPDATE games SET gameNumber = :gameNumber WHERE id = :gameId")
    suspend fun updateGameNumber(gameId: Long, gameNumber: Int)

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGame(gameId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Query("""
        SELECT COUNT(*) FROM games 
        WHERE (winningPair = 1 AND (pair1Player1Id = :playerId OR pair1Player2Id = :playerId))
           OR (winningPair = 2 AND (pair2Player1Id = :playerId OR pair2Player2Id = :playerId))
    """)
    suspend fun countWinsForPlayer(playerId: Long): Int
}
