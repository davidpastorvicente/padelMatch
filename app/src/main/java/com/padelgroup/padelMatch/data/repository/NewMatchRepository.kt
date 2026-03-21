package com.padelgroup.padelMatch.data.repository

import com.padelgroup.padelMatch.data.db.dao.GameDao
import com.padelgroup.padelMatch.data.db.dao.SessionDao
import com.padelgroup.padelMatch.data.db.entity.GameEntity
import com.padelgroup.padelMatch.data.db.entity.SessionEntity
import com.padelgroup.padelMatch.data.db.entity.SessionPlayerEntity
import com.padelgroup.padelMatch.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

data class NewMatchGame(
    val gameNumber: Int,
    val pair1Player1Id: Long,
    val pair1Player2Id: Long,
    val pair2Player1Id: Long,
    val pair2Player2Id: Long,
    val pair1Score: Int? = null,
    val pair2Score: Int? = null,
    val winningPair: Int? = null
)

@Singleton
class NewMatchRepository @Inject constructor(
    private val sessionDao: SessionDao,
    private val gameDao: GameDao,
    private val templateRepository: TemplateRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    fun generateGames(playerIds: List<Long>): List<NewMatchGame> {
        require(playerIds.size in 4..7) { "El número de jugadores debe estar entre 4 y 7" }
        val shuffled = playerIds.shuffled()
        val template = templateRepository.getTemplate(shuffled.size)
            ?: error("No existe plantilla para ${shuffled.size} jugadores")
        return template.mapIndexed { index, slots ->
            NewMatchGame(
                gameNumber = index + 1,
                pair1Player1Id = shuffled[slots.pair1Slot1],
                pair1Player2Id = shuffled[slots.pair1Slot2],
                pair2Player1Id = shuffled[slots.pair2Slot1],
                pair2Player2Id = shuffled[slots.pair2Slot2]
            )
        }
    }

    suspend fun saveSession(
        playerIds: List<Long>,
        games: List<NewMatchGame>,
        date: String = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    ): Long = withContext(ioDispatcher) {
        val sessionId = sessionDao.insertSession(SessionEntity(date = date))

        // Insert session players with 0 win ratio (results entered separately)
        val sessionPlayers = playerIds.map { pid ->
            SessionPlayerEntity(sessionId = sessionId, playerId = pid, winRatio = 0f)
        }
        sessionDao.insertSessionPlayers(sessionPlayers)
        gameDao.insertAll(games.map { g ->
            GameEntity(
                sessionId = sessionId,
                gameNumber = g.gameNumber,
                pair1Player1Id = g.pair1Player1Id,
                pair1Player2Id = g.pair1Player2Id,
                pair2Player1Id = g.pair2Player1Id,
                pair2Player2Id = g.pair2Player2Id,
                pair1Score = null,
                pair2Score = null,
                winningPair = null
            )
        })
        sessionId
    }
}
