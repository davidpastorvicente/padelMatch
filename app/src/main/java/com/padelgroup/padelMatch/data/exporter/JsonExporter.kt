package com.padelgroup.padelMatch.data.exporter

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.padelgroup.padelMatch.data.db.dao.GameDao
import com.padelgroup.padelMatch.data.db.dao.PlayerDao
import com.padelgroup.padelMatch.data.db.dao.SessionDao
import com.padelgroup.padelMatch.data.format.JsonGame
import com.padelgroup.padelMatch.data.format.JsonPlayer
import com.padelgroup.padelMatch.data.format.JsonSession
import com.padelgroup.padelMatch.data.format.PadelMatchExport
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonExporter @Inject constructor(
    private val context: Context,
    private val playerDao: PlayerDao,
    private val sessionDao: SessionDao,
    private val gameDao: GameDao
) {
    private val json = Json { prettyPrint = true }

    suspend fun export(): Uri {
        val players = playerDao.getAllPlayersList()
        val sessions = sessionDao.getAllSessionsList()

        val jsonSessions = sessions.map { session ->
            val sessionPlayers = sessionDao.getSessionPlayersWithNames(session.id)
            val games = gameDao.getGamesForSession(session.id)
            JsonSession(
                date = session.date,
                playerIds = sessionPlayers.map { it.playerId },
                games = games.map { g ->
                    JsonGame(
                        gameNumber = g.gameNumber,
                        pair1 = listOf(g.pair1Player1Id, g.pair1Player2Id),
                        pair2 = listOf(g.pair2Player1Id, g.pair2Player2Id),
                        winner = g.winningPair
                    )
                }
            )
        }

        val export = PadelMatchExport(
            players = players.map { JsonPlayer(it.id, it.name) },
            sessions = jsonSessions
        )

        val file = File(context.cacheDir, "padelMatch_export.json")
        file.writeText(json.encodeToString(PadelMatchExport.serializer(), export))

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
