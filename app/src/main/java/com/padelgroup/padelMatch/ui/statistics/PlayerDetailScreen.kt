package com.padelgroup.padelMatch.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.padelgroup.padelMatch.data.model.PlayerSessionEntry
import com.padelgroup.padelMatch.ui.theme.playerColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    viewModel: PlayerDetailViewModel,
    onBack: () -> Unit,
    onSessionClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val name = (uiState as? PlayerDetailUiState.Success)?.data?.player?.name ?: ""
                    Text(name, fontWeight = FontWeight.Bold)
                },
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
    ) { innerPadding ->
        when (val state = uiState) {
            is PlayerDetailUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is PlayerDetailUiState.Error -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is PlayerDetailUiState.Success -> {
                PlayerDetailContent(
                    data = state.data,
                    onSessionClick = onSessionClick,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun PlayerDetailContent(data: PlayerDetailData, onSessionClick: (Long) -> Unit, modifier: Modifier = Modifier) {
    val (badgeBg, _) = playerColors(data.player.name)
    val winPct = (data.winRatio * 100).toInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats table
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatsRow(
                    labels = listOf("Partidos", "Sets", "Victorias", "Ratio"),
                    values = listOf(
                        data.sessionsAttended.toString(),
                        data.totalGames.toString(),
                        data.wins.toString(),
                        "$winPct%"
                    )
                )
            }
        }

        // Chart + session list
        if (data.sessionHistory.size >= 2) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Historial de ratio",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    PlayerWinRatioChart(
                        history = data.sessionHistory,
                        lineColor = badgeBg,
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(thickness = 0.5.dp)
                    data.sessionHistory.reversed().forEach { entry ->
                        SessionHistoryRow(entry = entry, onClick = { onSessionClick(entry.sessionId) })
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsRow(labels: List<String>, values: List<String>) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEach { label ->
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
            values.forEach { value ->
                Text(
                    text = value,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SessionHistoryRow(entry: PlayerSessionEntry, onClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale("es"))
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val dateStr = try {
        LocalDate.parse(entry.date, isoFormatter).format(formatter)
            .replaceFirstChar { it.uppercase() }
    } catch (_: Exception) { entry.date }
    val ratioPct = (entry.winRatio * 100).toInt()
    val (badgeColor, textColor) = winRatioBadgeColor(entry.winRatio)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = dateStr,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Surface(shape = MaterialTheme.shapes.small, color = badgeColor) {
            Text(
                text = "$ratioPct%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

private fun winRatioBadgeColor(ratio: Float): Pair<Color, Color> {
    val red = Color(0xFFEF5350)    // Material Red 400
    val orange = Color(0xFFFFA726) // Material Orange 400
    val green = Color(0xFF66BB6A)  // Material Green 400
    val bg = if (ratio < 0.5f) lerp(red, orange, ratio * 2f) else lerp(orange, green, (ratio - 0.5f) * 2f)
    return Pair(bg, Color.White)
}

@Composable
private fun PlayerWinRatioChart(
    history: List<PlayerSessionEntry>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val darkerLineColor = Color(
        red = lineColor.red * 0.65f,
        green = lineColor.green * 0.65f,
        blue = lineColor.blue * 0.65f,
        alpha = 1f
    )

    // Compute time-proportional X positions
    val dates = history.map {
        try { LocalDate.parse(it.date, isoFormatter) } catch (_: Exception) { LocalDate.now() }
    }
    val minEpoch = dates.first().toEpochDay()
    val maxEpoch = dates.last().toEpochDay()
    val totalDays = (maxEpoch - minEpoch).coerceAtLeast(1).toFloat()
    val xFractions = dates.map { (it.toEpochDay() - minEpoch) / totalDays }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val paddingLeft = 88f
        val paddingRight = 16f
        val paddingTop = 8f
        val chartW = w - paddingLeft - paddingRight
        val chartH = h - paddingTop

        val n = history.size

        fun xPos(i: Int) = paddingLeft + xFractions[i] * chartW
        fun yPos(i: Int) = paddingTop + chartH - history[i].winRatio * chartH

        val axisPaint = android.graphics.Paint().apply {
            textSize = 30f
            color = onSurfaceVariant.copy(alpha = 0.7f).toArgb()
            textAlign = android.graphics.Paint.Align.RIGHT
        }

        // Y-axis grid lines and labels at 0, 25, 50, 75, 100%
        listOf(0, 25, 50, 75, 100).forEach { pct ->
            val ratio = pct / 100f
            val y = paddingTop + chartH - ratio * chartH
            drawLine(
                color = onSurfaceVariant.copy(alpha = if (pct == 50) 0.3f else 0.15f),
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

        // Data line
        for (i in 0 until n - 1) {
            drawLine(
                color = darkerLineColor,
                start = Offset(xPos(i), yPos(i)),
                end = Offset(xPos(i + 1), yPos(i + 1)),
                strokeWidth = 7f
            )
        }

        // Dots
        for (i in 0 until n) {
            drawCircle(color = darkerLineColor, radius = 7f, center = Offset(xPos(i), yPos(i)))
            drawCircle(color = Color.White, radius = 3f, center = Offset(xPos(i), yPos(i)))
        }
    }
}
