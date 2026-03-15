package com.padelgroup.padelMatch.data.importer

import com.padelgroup.padelMatch.data.db.dao.GameDao
import com.padelgroup.padelMatch.data.db.dao.PlayerDao
import com.padelgroup.padelMatch.data.db.dao.SessionDao
import com.padelgroup.padelMatch.data.db.entity.GameEntity
import com.padelgroup.padelMatch.data.db.entity.PlayerEntity
import com.padelgroup.padelMatch.data.db.entity.SessionEntity
import com.padelgroup.padelMatch.data.db.entity.SessionPlayerEntity
import com.padelgroup.padelMatch.data.format.PadelMatchExport
import kotlinx.serialization.json.Json
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonImporter @Inject constructor(
    private val playerDao: PlayerDao,
    private val sessionDao: SessionDao,
    private val gameDao: GameDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun import(stream: InputStream, skipExisting: Boolean = true) {
        val text = stream.bufferedReader().readText()
        val export = json.decodeFromString<PadelMatchExport>(text)

        val idMap = mutableMapOf<Long, Long>()
        for (p in export.players) {
            val existing = playerDao.findByName(p.name)
            val localId = existing?.id ?: playerDao.insert(PlayerEntity(name = p.name))
            idMap[p.id] = localId
        }

        for (session in export.sessions) {
            if (skipExisting && sessionDao.getSessionByDate(session.date) != null) continue

            val sessionId = sessionDao.insertSession(SessionEntity(date = session.date))

            val sessionPlayers = session.playerIds.mapNotNull { exportId ->
                idMap[exportId]?.let { localId ->
                    SessionPlayerEntity(sessionId = sessionId, playerId = localId, winRatio = 0f)
                }
            }
            sessionDao.insertSessionPlayers(sessionPlayers)

            val playerWins = mutableMapOf<Long, Int>()
            val playerGames = mutableMapOf<Long, Int>()
            session.playerIds.forEach { id ->
                val localId = idMap[id] ?: return@forEach
                playerWins[localId] = 0; playerGames[localId] = 0
            }

            val gameEntities = session.games.map { g ->
                val p1p1 = idMap[g.pair1[0]] ?: 0L
                val p1p2 = idMap[g.pair1[1]] ?: 0L
                val p2p1 = idMap[g.pair2[0]] ?: 0L
                val p2p2 = idMap[g.pair2[1]] ?: 0L
                listOf(p1p1, p1p2, p2p1, p2p2).forEach { playerGames[it] = (playerGames[it] ?: 0) + 1 }
                when (g.winner) {
                    1 -> { playerWins[p1p1] = (playerWins[p1p1] ?: 0) + 1; playerWins[p1p2] = (playerWins[p1p2] ?: 0) + 1 }
                    2 -> { playerWins[p2p1] = (playerWins[p2p1] ?: 0) + 1; playerWins[p2p2] = (playerWins[p2p2] ?: 0) + 1 }
                }
                GameEntity(
                    sessionId = sessionId,
                    gameNumber = g.gameNumber,
                    pair1Player1Id = p1p1,
                    pair1Player2Id = p1p2,
                    pair2Player1Id = p2p1,
                    pair2Player2Id = p2p2,
                    winningPair = g.winner
                )
            }
            gameDao.insertAll(gameEntities)

            val updatedPlayers = sessionPlayers.map { sp ->
                val total = playerGames[sp.playerId] ?: 0
                val wins = playerWins[sp.playerId] ?: 0
                sp.copy(winRatio = if (total > 0) wins.toFloat() / total else 0f)
            }
            sessionDao.insertSessionPlayers(updatedPlayers)
        }
    }
}
