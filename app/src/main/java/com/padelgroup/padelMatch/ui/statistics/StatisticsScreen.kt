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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.padelgroup.padelMatch.data.model.PlayerSessionEntry
import com.padelgroup.padelMatch.data.model.PlayerStats
import com.padelgroup.padelMatch.ui.theme.playerColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
            val isoFmt = DateTimeFormatter.ISO_LOCAL_DATE
            val globalEpochRange = remember(playerStats) {
                val allDates = playerStats.flatMap { it.history }.mapNotNull {
                    try { LocalDate.parse(it.date, isoFmt).toEpochDay() } catch (_: Exception) { null }
                }
                if (allDates.isEmpty()) null
                else Pair(allDates.min(), allDates.max())
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(playerStats, key = { it.player.id }) { stats ->
                    val onClick = remember(stats.player.id) { { onPlayerClick(stats.player.id) } }
                    PlayerStatCard(stats = stats, onClick = onClick, globalEpochRange = globalEpochRange)
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
fun PlayerStatCard(stats: PlayerStats, onClick: () -> Unit = {}, globalEpochRange: Pair<Long, Long>? = null) {
    val (badgeBg, badgeFg) = remember(stats.player.name) { playerColors(stats.player.name) }
    val winPct = remember(stats.winRatio) { (stats.winRatio * 100).toInt() }

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

            if (stats.history.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                WinRatioSparkLine(history = stats.history, lineColor = badgeBg, globalEpochRange = globalEpochRange)
            }
        }
    }
}

@Composable
fun WinRatioSparkLine(history: List<PlayerSessionEntry>, lineColor: Color, globalEpochRange: Pair<Long, Long>? = null, modifier: Modifier = Modifier) {
    if (history.isEmpty()) return

    val darkerLineColor = remember(lineColor) {
        Color(
            red = lineColor.red * 0.65f,
            green = lineColor.green * 0.65f,
            blue = lineColor.blue * 0.65f,
            alpha = 1f
        )
    }

    val xFractions = remember(history, globalEpochRange) {
        val isoFmt = DateTimeFormatter.ISO_LOCAL_DATE
        val dates = history.map {
            try { LocalDate.parse(it.date, isoFmt) } catch (_: Exception) { LocalDate.now() }
        }
        val minEpoch = globalEpochRange?.first ?: dates.first().toEpochDay()
        val maxEpoch = globalEpochRange?.second ?: dates.last().toEpochDay()
        val totalDays = (maxEpoch - minEpoch).coerceAtLeast(1).toFloat()
        dates.map { (it.toEpochDay() - minEpoch) / totalDays }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        val w = size.width
        val h = size.height
        val n = history.size

        fun xPos(i: Int) = xFractions[i] * w
        fun yPos(i: Int) = h - history[i].winRatio * h

        for (i in 0 until n - 1) {
            drawLine(
                color = darkerLineColor,
                start = Offset(xPos(i), yPos(i)),
                end = Offset(xPos(i + 1), yPos(i + 1)),
                strokeWidth = 7f
            )
        }
        for (i in 0 until n) {
            drawCircle(color = darkerLineColor, radius = 7f, center = Offset(xPos(i), yPos(i)))
            drawCircle(color = Color.White, radius = 3f, center = Offset(xPos(i), yPos(i)))
        }
    }
}
