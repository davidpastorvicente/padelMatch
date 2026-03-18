package com.padelgroup.padelMatch.ui.session

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.padelgroup.padelMatch.data.repository.SessionWithDetails
import com.padelgroup.padelMatch.ui.history.BracketGameCard
import com.padelgroup.padelMatch.ui.history.ClassificationChart
import com.padelgroup.padelMatch.ui.theme.playerColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SessionDetailScreen(
    viewModel: SessionDetailViewModel,
    onBack: () -> Unit,
    onEditResults: (sessionId: Long) -> Unit
) {
    val session by viewModel.session.collectAsState()
    val context = LocalContext.current
    val showDeleteDialog = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navBack.collect { onBack() }
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text("Eliminar partido") },
            text = { Text("¿Eliminar este partido? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog.value = false; viewModel.deleteSession() }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog.value = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = session?.date?.toDisplayDate() ?: "",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    session?.let { s ->
                        IconButton(onClick = {
                            val shareText = formatMatchForSharing(s)
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Compartir")
                        }
                        IconButton(onClick = { onEditResults(s.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                        }
                        IconButton(onClick = { showDeleteDialog.value = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val s = session
        if (s != null) {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 16.dp,
                    bottom = innerPadding.calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        s.players.sortedBy { it.playerName }.forEach { player ->
                            val (bg, fg) = playerColors(player.playerName)
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = bg,
                                contentColor = fg
                            ) {
                                Text(
                                    text = player.playerName,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                if (s.players.isNotEmpty()) {
                    item {
                        Text(
                            "Clasificación",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        ClassificationChart(players = s.players)
                    }
                }

                if (s.games.isNotEmpty()) {
                    item {
                        Text(
                            "Sets",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(s.games, key = { it.id }) { game ->
                        BracketGameCard(
                            game = game,
                            onTeamClick = null,
                            onEdit = null,
                            onDelete = null
                        )
                    }
                }
            }
        }
    }
}

private fun String.toDisplayDate(): String = try {
    val date = LocalDate.parse(this)
    date.format(DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale("es")))
} catch (_: Exception) { this }

private fun formatMatchForSharing(session: SessionWithDetails): String {
    val dateStr = session.date.toDisplayDate()
    val header = "Partido de pádel - $dateStr\n"
    val players = session.players.sortedBy { it.playerName }.joinToString(", ") { it.playerName }
    val playersStr = "Jugadores: $players\n\n"
    
    val allGamesHaveResults = session.games.isNotEmpty() && session.games.all { it.winningPair != null }
    
    val setsStr = if (session.games.isEmpty()) {
        "Sin sets registrados"
    } else {
        "Sets:\n" + session.games.mapIndexed { index, game ->
            val setNum = index + 1
            val pair1 = "${game.pair1Player1} y ${game.pair1Player2}"
            val pair2 = "${game.pair2Player1} y ${game.pair2Player2}"

            when (game.winningPair) {
                null -> "$setNum. $pair1 vs $pair2"
                1 -> "$setNum. *$pair1* vs $pair2"
                2 -> "$setNum. $pair1 vs *$pair2*"
                else -> "$setNum. $pair1 vs $pair2"
            }
        }.joinToString("\n")
    }
    
    val classificationStr = if (allGamesHaveResults && session.players.isNotEmpty()) {
        val sortedPlayers = session.players.sortedByDescending { it.winRatio }
        "\n\nClasificación:\n" + sortedPlayers.mapIndexed { index, player ->
            val percentage = (player.winRatio * 100).toInt()
            "${index + 1}. ${player.playerName}: $percentage%"
        }.joinToString("\n")
    } else {
        ""
    }
    
    return header + playersStr + setsStr + classificationStr
}
