package com.davidpv.padelmatch.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.davidpv.padelmatch.data.db.entity.SessionEntity
import com.davidpv.padelmatch.data.db.entity.SessionPlayerEntity
import com.davidpv.padelmatch.data.model.PlayerSessionEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionPlayers(players: List<SessionPlayerEntity>)

    @Query("SELECT sp.*, p.name as playerName FROM session_players sp JOIN players p ON sp.playerId = p.id WHERE sp.sessionId = :sessionId ORDER BY sp.winRatio DESC")
    suspend fun getSessionPlayersWithNames(sessionId: Long): List<SessionPlayerWithName>

    @Query("SELECT * FROM sessions WHERE date = :date LIMIT 1")
    suspend fun getSessionByDate(date: String): SessionEntity?

    @Query("SELECT * FROM sessions ORDER BY date DESC")
    suspend fun getAllSessionsList(): List<SessionEntity>

    @Query("DELETE FROM sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    @Query("SELECT date FROM sessions WHERE date LIKE :pattern ORDER BY date ASC")
    suspend fun getSessionDatesForMonth(pattern: String): List<String>

    @Query("SELECT date FROM sessions ORDER BY date ASC")
    fun getAllSessionDates(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM session_players")
    fun getSessionPlayersCountFlow(): Flow<Int>

    @Query("""
        SELECT sp.winRatio FROM session_players sp
        JOIN sessions s ON s.id = sp.sessionId
        WHERE sp.playerId = :playerId
        ORDER BY s.date ASC
        LIMIT 10
    """)
    suspend fun getPlayerWinRatioHistory(playerId: Long): List<Float>

    @Query("""
        SELECT s.id AS sessionId, s.date AS date, sp.winRatio AS winRatio FROM session_players sp
        JOIN sessions s ON s.id = sp.sessionId
        WHERE sp.playerId = :playerId
        AND EXISTS (SELECT 1 FROM games g WHERE g.sessionId = s.id AND g.winningPair IS NOT NULL)
        ORDER BY s.date ASC
    """)
    suspend fun getPlayerSessionHistory(playerId: Long): List<PlayerSessionEntry>
}

data class SessionPlayerWithName(
    val sessionId: Long,
    val playerId: Long,
    val winRatio: Float,
    val playerName: String
)
