package com.padelgroup.padelMatch.data.repository

import com.padelgroup.padelMatch.data.db.dao.PlayerDao
import com.padelgroup.padelMatch.data.db.entity.PlayerEntity
import com.padelgroup.padelMatch.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(
    private val playerDao: PlayerDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    suspend fun getAllPlayers(): List<PlayerEntity> = withContext(ioDispatcher) {
        playerDao.getAllPlayersList()
    }
    
    suspend fun addPlayer(name: String): Long = withContext(ioDispatcher) {
        playerDao.insert(PlayerEntity(name = name.trim()))
    }

    /** Returns false if the player has match history and cannot be deleted. */
    suspend fun deletePlayer(id: Long): Boolean = withContext(ioDispatcher) {
        if (playerDao.countGamesForPlayer(id) > 0) return@withContext false
        playerDao.deleteById(id)
        true
    }
}
