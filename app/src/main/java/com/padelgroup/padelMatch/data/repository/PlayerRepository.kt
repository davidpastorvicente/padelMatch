package com.padelgroup.padelMatch.data.repository

import com.padelgroup.padelMatch.data.db.dao.PlayerDao
import com.padelgroup.padelMatch.data.db.entity.PlayerEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(private val playerDao: PlayerDao) {
    fun getAllPlayersFlow(): Flow<List<PlayerEntity>> = playerDao.getAllPlayers()
    suspend fun getAllPlayers(): List<PlayerEntity> = playerDao.getAllPlayersList()
    suspend fun addPlayer(name: String): Long = playerDao.insert(PlayerEntity(name = name.trim()))

    /** Returns false if the player has match history and cannot be deleted. */
    suspend fun deletePlayer(id: Long): Boolean {
        if (playerDao.countGamesForPlayer(id) > 0) return false
        playerDao.deleteById(id)
        return true
    }
}
