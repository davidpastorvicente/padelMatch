package com.padelgroup.padelMatch.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.padelgroup.padelMatch.data.model.PlayerStats
import com.padelgroup.padelMatch.ui.theme.playerColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private data class PlayerLine(
    val stats: PlayerStats,
    val darkerColor: Color,
    val xFractions: List<Float>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombinedWinRatioChartScreen(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val playerStats by viewModel.playerStats.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Comparativa de ratios", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        if (playerStats.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Sin datos todavía",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                CombinedWinRatioChart(
                    playerStats = playerStats,
                    modifier = Modifier.fillMaxWidth().height(280.dp)
                )
                Spacer(Modifier.height(16.dp))
                PlayerLegend(playerStats = playerStats)
            }
        }
    }
}

@Composable
private fun CombinedWinRatioChart(
    playerStats: List<PlayerStats>,
    modifier: Modifier = Modifier
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val isoFmt = DateTimeFormatter.ISO_LOCAL_DATE

    val globalEpochRange: Pair<Long, Long>? = remember(playerStats) {
        val allEpochs = playerStats.flatMap { it.history }.mapNotNull {
            try { LocalDate.parse(it.date, isoFmt).toEpochDay() } catch (_: Exception) { null }
        }
        if (allEpochs.isEmpty()) null else allEpochs.min() to allEpochs.max()
    }

    if (globalEpochRange == null) return

    val playerLines: List<PlayerLine> = remember(playerStats, globalEpochRange) {
        val (minEpoch, maxEpoch) = globalEpochRange
        val totalDays = (maxEpoch - minEpoch).coerceAtLeast(1).toFloat()
        playerStats.filter { it.history.isNotEmpty() }.map { stats ->
            val (bg, _) = playerColors(stats.player.name)
            val darker = Color(red = bg.red * 0.65f, green = bg.green * 0.65f, blue = bg.blue * 0.65f, alpha = 1f)
            val fractions = stats.history.map { entry ->
                try {
                    val epoch = LocalDate.parse(entry.date, isoFmt).toEpochDay()
                    (epoch - minEpoch) / totalDays
                } catch (_: Exception) { 0.5f }
            }
            PlayerLine(stats, darker, fractions)
        }
    }

    val gridColorNormal = remember(onSurfaceVariant) { onSurfaceVariant.copy(alpha = 0.15f) }
    val gridColorMid = remember(onSurfaceVariant) { onSurfaceVariant.copy(alpha = 0.3f) }
    val axisColor = remember(onSurfaceVariant) { onSurfaceVariant.copy(alpha = 0.7f).toArgb() }
    val axisPaint = remember(axisColor) {
        android.graphics.Paint().apply {
            textSize = 30f
            color = axisColor
            textAlign = android.graphics.Paint.Align.RIGHT
        }
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val paddingLeft = 88f
        val paddingRight = 16f
        val paddingTop = 8f
        val chartW = w - paddingLeft - paddingRight
        val chartH = h - paddingTop

        listOf(0, 25, 50, 75, 100).forEach { pct ->
            val ratio = pct / 100f
            val y = paddingTop + chartH - ratio * chartH
            drawLine(
                color = if (pct == 50) gridColorMid else gridColorNormal,
                start = Offset(paddingLeft, y),
                end = Offset(w - paddingRight, y),
                strokeWidth = if (pct == 50) 1.5f else 1f,
                pathEffect = if (pct == 50) PathEffect.dashPathEffect(floatArrayOf(8f, 8f)) else null
            )
            drawContext.canvas.nativeCanvas.drawText(
                "$pct%",
                paddingLeft - 28f,
                y + axisPaint.textSize / 3f,
                axisPaint
            )
        }

        playerLines.forEach { line ->
            val n = line.stats.history.size
            if (n == 0) return@forEach

            fun xPos(i: Int): Float = paddingLeft + line.xFractions[i] * chartW
            fun yPos(i: Int): Float = paddingTop + chartH - line.stats.history[i].winRatio * chartH

            for (i in 0 until n - 1) {
                drawLine(
                    color = line.darkerColor,
                    start = Offset(xPos(i), yPos(i)),
                    end = Offset(xPos(i + 1), yPos(i + 1)),
                    strokeWidth = 7f
                )
            }
            for (i in 0 until n) {
                drawCircle(color = line.darkerColor, radius = 7f, center = Offset(xPos(i), yPos(i)))
                drawCircle(color = Color.White, radius = 3f, center = Offset(xPos(i), yPos(i)))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerLegend(playerStats: List<PlayerStats>) {
    val sortedPlayers = remember(playerStats) { playerStats.sortedBy { it.player.name } }
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sortedPlayers.forEach { stats ->
            val (bg, _) = remember(stats.player.name) { playerColors(stats.player.name) }
            val dotColor = remember(bg) {
                Color(red = bg.red * 0.65f, green = bg.green * 0.65f, blue = bg.blue * 0.65f, alpha = 1f)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = dotColor,
                    modifier = Modifier.size(12.dp)
                ) {}
                Spacer(Modifier.width(6.dp))
                Text(
                    text = stats.player.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
