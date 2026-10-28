package com.davidpv.padelmatch.ui.statistics

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidpv.padelmatch.data.model.PlayerSessionEntry
import com.davidpv.padelmatch.ui.theme.playerColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerDetailScreen(
    viewModel: PlayerDetailViewModel,
    onBack: () -> Unit,
    onSessionClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
    val lineColor = playerColors(data.player.name).accent
    val chartSeries = remember(data.sessionHistory, lineColor) {
        listOf(WinRatioSeries(null, lineColor, data.sessionHistory, winRatioXFractions(data.sessionHistory)))
    }
    val winPct = "%.1f%%".format(data.winRatio * 100)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            data.seasonLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Stats table
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                StatsRow(
                    labels = listOf("Partidos", "Sets", "Victorias", "Ratio"),
                    values = listOf(
                        data.sessionsAttended.toString(),
                        data.totalGames.toString(),
                        data.wins.toString(),
                        winPct
                    )
                )
            }
        }

        // Chart + session list
        if (data.sessionHistory.isNotEmpty()) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Historial de ratio",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    WinRatioChart(
                        series = chartSeries,
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(thickness = 0.5.dp)
                    val reversedHistory = remember(data.sessionHistory) { data.sessionHistory.reversed() }
                    reversedHistory.forEach { entry ->
                        val onRowClick = remember(entry.sessionId) { { onSessionClick(entry.sessionId) } }
                        SessionHistoryRow(entry = entry, onClick = onRowClick)
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
    val dateStr = remember(entry.date) {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"))
        try {
            LocalDate.parse(entry.date, DateTimeFormatter.ISO_LOCAL_DATE).format(formatter)
                .replaceFirstChar { it.uppercase() }
        } catch (_: Exception) { entry.date }
    }
    val ratioPct = remember(entry.winRatio) { (entry.winRatio * 100).roundToInt() }
    val (badgeColor, textColor) = remember(entry.winRatio) { winRatioBadgeColor(entry.winRatio) }

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
    val red = Color(0xFFEF5350)        // Material Red 400 — 0%
    val orange = Color(0xFFFFA726)     // Material Orange 400 — 40%
    val lightGreen = Color(0xFFA5D6A7) // Material Green 200 — 50%
    val darkGreen = Color(0xFF2E7D32)  // Material Green 800 — 100%
    val bg = when {
        ratio < 0.3f -> lerp(red, orange, ratio / 0.3f)
        ratio < 0.5f -> lerp(orange, lightGreen, (ratio - 0.3f) / 0.2f)
        else -> lerp(lightGreen, darkGreen, (ratio - 0.5f) * 2f)
    }
    return Pair(bg, Color.White)
}
