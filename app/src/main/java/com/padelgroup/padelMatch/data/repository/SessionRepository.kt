package com.padelgroup.padelMatch.data.repository

import com.padelgroup.padelMatch.data.db.dao.GameDao
import com.padelgroup.padelMatch.data.db.dao.PlayerDao
import com.padelgroup.padelMatch.data.db.dao.SessionDao
import com.padelgroup.padelMatch.data.db.dao.SessionPlayerWithName
import com.padelgroup.padelMatch.data.db.entity.GameEntity
import com.padelgroup.padelMatch.data.db.entity.SessionPlayerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class SessionWithDetails(
    val id: Long,
    val date: String,
    val players: List<SessionPlayerWithName>,
    val games: List<GameWithPlayerNames>
)

data class GameWithPlayerNames(
    val id: Long,
    val gameNumber: Int,
    val pair1Player1: String,
    val pair1Player2: String,
    val pair2Player1: String,
    val pair2Player2: String,
    val pair1Player1Id: Long,
    val pair1Player2Id: Long,
    val pair2Player1Id: Long,
    val pair2Player2Id: Long,
    val pair1Score: Int?,
    val pair2Score: Int?,
    val winningPair: Int? = null
)

@Singleton
class SessionRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val gameDao: GameDao,
    private val playerDao: PlayerDao
) {
    fun getAllSessionsFlow(): Flow<List<SessionWithDetails>> =
        sessionDao.getAllSessions().map { sessions ->
            sessions.map { session ->
                val players = sessionDao.getSessionPlayersWithNames(session.id)
                val games = gameDao.getGamesForSession(session.id)
                val allPlayers = playerDao.getAllPlayersList()
                val playerMap = allPlayers.associate { it.id to it.name }
                SessionWithDetails(
                    id = session.id,
                    date = session.date,
                    players = players,
                    games = games.map { g -> g.toGameWithNames(playerMap) }
                )
            }
        }

    suspend fun sessionExistsForDate(date: String): Boolean =
        sessionDao.getSessionByDate(date) != null

    suspend fun deleteSession(sessionId: Long) =
        sessionDao.deleteSession(sessionId)

    suspend fun updateGameWinners(
        sessionId: Long,
        winners: Map<Long, Int?>,
        toDelete: Set<Long> = emptySet(),
        toInsert: List<GameEntity> = emptyList(),
        gameNumberUpdates: Map<Long, Int> = emptyMap()
    ) {
        toDelete.forEach { gameDao.deleteGame(it) }
        toInsert.forEach { gameDao.insertGame(it) }
        winners.forEach { (gameId, winningPair) ->
            if (gameId !in toDelete) gameDao.updateGameWinner(gameId, winningPair)
        }
        gameNumberUpdates.forEach { (gameId, number) ->
            gameDao.updateGameNumber(gameId, number)
        }
        // Recalculate win ratios based on final game list
        val games = gameDao.getGamesForSession(sessionId)
        val updatedGames = games.map { g -> g.copy(winningPair = winners[g.id] ?: g.winningPair) }
        val players = sessionDao.getSessionPlayersWithNames(sessionId)
        val playerIds = players.map { it.playerId }
        val wins = mutableMapOf<Long, Int>().apply { playerIds.forEach { put(it, 0) } }
        val totals = mutableMapOf<Long, Int>().apply { playerIds.forEach { put(it, 0) } }
        updatedGames.forEach { g ->
            listOf(g.pair1Player1Id, g.pair1Player2Id, g.pair2Player1Id, g.pair2Player2Id)
                .forEach { totals[it] = (totals[it] ?: 0) + 1 }
            when (g.winningPair) {
                1 -> listOf(g.pair1Player1Id, g.pair1Player2Id).forEach { wins[it] = (wins[it] ?: 0) + 1 }
                2 -> listOf(g.pair2Player1Id, g.pair2Player2Id).forEach { wins[it] = (wins[it] ?: 0) + 1 }
            }
        }
        val updatedSessionPlayers = playerIds.map { pid ->
            val total = totals[pid] ?: 0
            val win = wins[pid] ?: 0
            SessionPlayerEntity(
                sessionId = sessionId,
                playerId = pid,
                winRatio = if (total > 0) win.toFloat() / total else 0f
            )
        }
        sessionDao.insertSessionPlayers(updatedSessionPlayers)
    }

    private fun GameEntity.toGameWithNames(playerMap: Map<Long, String>) = GameWithPlayerNames(
        id = id,
        gameNumber = gameNumber,
        pair1Player1 = playerMap[pair1Player1Id] ?: "?",
        pair1Player2 = playerMap[pair1Player2Id] ?: "?",
        pair2Player1 = playerMap[pair2Player1Id] ?: "?",
        pair2Player2 = playerMap[pair2Player2Id] ?: "?",
        pair1Player1Id = pair1Player1Id,
        pair1Player2Id = pair1Player2Id,
        pair2Player1Id = pair2Player1Id,
        pair2Player2Id = pair2Player2Id,
        pair1Score = pair1Score,
        pair2Score = pair2Score,
        winningPair = winningPair
    )
}
