package com.davidpv.padelmatch.data.repository

import com.davidpv.padelmatch.data.db.dao.GameDao
import com.davidpv.padelmatch.data.db.dao.PlayerDao
import com.davidpv.padelmatch.data.db.dao.SessionDao
import com.davidpv.padelmatch.data.model.PlayerSessionEntry
import com.davidpv.padelmatch.data.model.PlayerStats
import com.davidpv.padelmatch.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class PlayerDetailSummary(
    val totalGames: Int,
    val wins: Int,
    val losses: Int,
    val winRatio: Float,
    val sessionsAttended: Int,
    val sessionHistory: List<PlayerSessionEntry>
)

@Singleton
class StatisticsRepository @Inject constructor(
    private val playerDao: PlayerDao,
    private val sessionDao: SessionDao,
    private val gameDao: GameDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    fun getPlayerStatsFlow(): Flow<List<PlayerStats>> = combine(
        playerDao.getAllPlayers(),
        sessionDao.getAllSessions(),
        gameDao.getGamesCountFlow()
    ) { players, _, _ ->
        players
    }.map { players ->
        players.mapNotNull { player ->
            val totalGames = playerDao.countGamesForPlayer(player.id)
            if (totalGames == 0) return@mapNotNull null
            val wins = gameDao.countWinsForPlayer(player.id)
            val losses = totalGames - wins
            val winRatio = wins.toFloat() / totalGames
            val sessionHistory = sessionDao.getPlayerSessionHistory(player.id)
            val sessionsAttended = sessionHistory.size
            PlayerStats(
                player = player,
                totalGames = totalGames,
                wins = wins,
                losses = losses,
                winRatio = winRatio,
                history = sessionHistory,
                sessionsAttended = sessionsAttended
            )
        }.sortedByDescending { it.winRatio }
    }.flowOn(ioDispatcher)


    suspend fun getPlayerDetailSummary(playerId: Long): PlayerDetailSummary = withContext(ioDispatcher) {
        val totalGames = playerDao.countGamesForPlayer(playerId)
        val wins = gameDao.countWinsForPlayer(playerId)
        val losses = totalGames - wins
        val sessionHistory = sessionDao.getPlayerSessionHistory(playerId)
        PlayerDetailSummary(
            totalGames = totalGames,
            wins = wins,
            losses = losses,
            winRatio = if (totalGames > 0) wins.toFloat() / totalGames else 0f,
            sessionsAttended = sessionHistory.size,
            sessionHistory = sessionHistory
        )
    }
}
