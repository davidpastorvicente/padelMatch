package com.padelgroup.padelMatch.data.repository

import com.padelgroup.padelMatch.data.db.dao.GameDao
import com.padelgroup.padelMatch.data.db.dao.PlayerDao
import com.padelgroup.padelMatch.data.db.dao.SessionDao
import com.padelgroup.padelMatch.data.model.PlayerSessionEntry
import com.padelgroup.padelMatch.data.model.PlayerStats
import com.padelgroup.padelMatch.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(
    private val playerDao: PlayerDao,
    private val sessionDao: SessionDao,
    private val gameDao: GameDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    fun getPlayerStatsFlow(): Flow<List<PlayerStats>> = flow {
        val players = playerDao.getAllPlayersList()
        val stats = players.mapNotNull { player ->
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
        emit(stats)
    }.flowOn(ioDispatcher)

    suspend fun getPlayerSessionHistory(playerId: Long): List<PlayerSessionEntry> = withContext(ioDispatcher) {
        sessionDao.getPlayerSessionHistory(playerId)
    }
}
