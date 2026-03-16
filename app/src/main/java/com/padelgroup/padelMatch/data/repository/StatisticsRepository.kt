package com.padelgroup.padelMatch.data.repository

import com.padelgroup.padelMatch.data.db.dao.GameDao
import com.padelgroup.padelMatch.data.db.dao.PlayerDao
import com.padelgroup.padelMatch.data.db.dao.SessionDao
import com.padelgroup.padelMatch.data.model.PlayerSessionEntry
import com.padelgroup.padelMatch.data.model.PlayerStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(
    private val playerDao: PlayerDao,
    private val sessionDao: SessionDao,
    private val gameDao: GameDao
) {
    fun getPlayerStatsFlow(): Flow<List<PlayerStats>> = flow {
        val players = playerDao.getAllPlayersList()
        val stats = players.mapNotNull { player ->
            val totalGames = playerDao.countGamesForPlayer(player.id)
            if (totalGames == 0) return@mapNotNull null
            val wins = gameDao.countWinsForPlayer(player.id)
            val losses = totalGames - wins
            val winRatio = wins.toFloat() / totalGames
            val history = sessionDao.getPlayerWinRatioHistory(player.id)
            val sessionsAttended = sessionDao.getPlayerSessionHistory(player.id).size
            PlayerStats(
                player = player,
                totalGames = totalGames,
                wins = wins,
                losses = losses,
                winRatio = winRatio,
                history = history,
                sessionsAttended = sessionsAttended
            )
        }.sortedByDescending { it.winRatio }
        emit(stats)
    }

    suspend fun getPlayerSessionHistory(playerId: Long): List<PlayerSessionEntry> =
        sessionDao.getPlayerSessionHistory(playerId)
}
