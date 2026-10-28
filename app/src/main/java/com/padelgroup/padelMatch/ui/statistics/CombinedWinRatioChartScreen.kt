package com.davidpv.padelmatch.ui.statistics

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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davidpv.padelmatch.data.model.PlayerStats
import com.davidpv.padelmatch.data.model.SeasonFilter
import com.davidpv.padelmatch.data.model.label
import com.davidpv.padelmatch.ui.theme.isAppInDarkTheme
import com.davidpv.padelmatch.ui.theme.playerColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombinedWinRatioChartScreen(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val seasonLabel = when (val state = uiState) {
        is StatisticsUiState.Success -> state.selectedSeason.label
        is StatisticsUiState.Empty -> state.selectedSeason.label
        StatisticsUiState.Loading -> null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        seasonLabel?.let { "Gráfico general · $it" } ?: "Gráfico general",
                        fontWeight = FontWeight.Bold
                    )
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
    ) { paddingValues ->
        when (val state = uiState) {
            StatisticsUiState.Loading -> Unit
            is StatisticsUiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (state.selectedSeason is SeasonFilter.All) "Sin datos todavía" else "Sin datos en esta temporada",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            is StatisticsUiState.Success -> {
                val playerStats = state.playerStats
                RotatedLayout(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        CombinedWinRatioChart(
                            playerStats = playerStats,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        Spacer(Modifier.height(12.dp))
                        PlayerLegend(playerStats = playerStats)
                    }
                }
            }
        }
    }
}

@Composable
private fun CombinedWinRatioChart(
    playerStats: List<PlayerStats>,
    modifier: Modifier = Modifier
) {
    val darkTheme = isAppInDarkTheme()
    val series = remember(playerStats, darkTheme) {
        val globalEpochRange = winRatioEpochRange(playerStats.flatMap { it.history })
        playerStats.filter { it.history.isNotEmpty() }.map { stats ->
            WinRatioSeries(
                label = stats.player.name,
                color = playerColors(stats.player.name, darkTheme).accent,
                entries = stats.history,
                xFractions = winRatioXFractions(stats.history, globalEpochRange)
            )
        }
    }
    if (series.isEmpty()) return

    WinRatioChart(series = series, modifier = modifier)
}

@Composable
private fun RotatedLayout(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(
        content = content,
        modifier = modifier.graphicsLayer { rotationZ = 90f }
    ) { measurables, constraints ->
        val placeable = measurables.first().measure(
            Constraints.fixed(constraints.maxHeight, constraints.maxWidth)
        )
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeable.place(
                x = (constraints.maxWidth - placeable.width) / 2,
                y = (constraints.maxHeight - placeable.height) / 2
            )
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
            val dotColor = playerColors(stats.player.name).accent
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
