package com.padelgroup.padelMatch.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.padelgroup.padelMatch.data.model.PlayerStats
import com.padelgroup.padelMatch.ui.theme.playerColors

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel, onPlayerClick: (Long) -> Unit = {}) {
    val playerStats by viewModel.playerStats.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        if (playerStats.isEmpty()) {
            Text(
                "Sin datos todavía",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(playerStats, key = { it.player.id }) { stats ->
                    PlayerStatCard(stats = stats, onClick = { onPlayerClick(stats.player.id) })
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun PlayerStatCard(stats: PlayerStats, onClick: () -> Unit = {}) {
    val (badgeBg, badgeFg) = playerColors(stats.player.name)
    val winPct = (stats.winRatio * 100).toInt()

    ElevatedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Player badge
            Surface(
                shape = MaterialTheme.shapes.small,
                color = badgeBg,
                contentColor = badgeFg
            ) {
                Text(
                    text = stats.player.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Stats table: header row + value row, evenly distributed
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Partidos", "Sets", "Victorias", "Ratio").forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                listOf(
                    stats.sessionsAttended.toString(),
                    stats.totalGames.toString(),
                    stats.wins.toString(),
                    "$winPct%"
                ).forEach { value ->
                    Text(
                        text = value,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (stats.history.size >= 2) {
                Spacer(Modifier.height(12.dp))
                WinRatioSparkLine(history = stats.history, lineColor = badgeBg)
            }
        }
    }
}

@Composable
fun WinRatioSparkLine(history: List<Float>, lineColor: Color, modifier: Modifier = Modifier) {
    if (history.size < 2) return
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        val w = size.width
        val h = size.height
        val stepX = w / (history.size - 1)
        for (i in 0 until history.size - 1) {
            drawLine(
                color = lineColor,
                start = Offset(i * stepX, h - history[i] * h),
                end = Offset((i + 1) * stepX, h - history[i + 1] * h),
                strokeWidth = 4f
            )
        }
    }
}
