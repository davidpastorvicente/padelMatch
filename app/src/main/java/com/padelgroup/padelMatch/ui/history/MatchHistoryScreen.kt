package com.davidpv.padelmatch.ui.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchHistoryScreen(
    viewModel: MatchHistoryViewModel,
    onNewMatch: () -> Unit,
    onSessionClick: (sessionId: Long) -> Unit = {},
    calendarVisible: Boolean = false,
    currentMonth: YearMonth = YearMonth.now(),
    selectedDate: String? = null,
    sessionDates: Set<String> = emptySet(),
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onSelectDate: (String?) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel.dataEvents) {
        viewModel.dataEvents.collect { event ->
            when (event) {
                is MatchHistoryViewModel.DataEvent.ScrollToTop -> {
                    listState.animateScrollToItem(0)
                }
                else -> {} // Share and ToastMessage handled in HomeScreen
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Inline calendar panel
        AnimatedVisibility(
            visible = calendarVisible,
            enter = expandVertically(animationSpec = tween(300), expandFrom = Alignment.Top),
            exit = shrinkVertically(animationSpec = tween(300), shrinkTowards = Alignment.Top)
        ) {
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                InlineCalendarPanel(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    sessionDates = sessionDates,
                    onPreviousMonth = onPreviousMonth,
                    onNextMonth = onNextMonth,
                    onSelectDate = onSelectDate
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = uiState) {
                is MatchHistoryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is MatchHistoryUiState.Empty -> {
                    EmptyState(onNewMatch = onNewMatch, modifier = Modifier.align(Alignment.Center))
                }
                is MatchHistoryUiState.Success -> {
                    val filteredSessions = if (selectedDate != null) {
                        state.sessions.filter { it.date == selectedDate }
                    } else {
                        state.sessions
                    }

                    if (filteredSessions.isEmpty() && selectedDate != null) {
                        Text(
                            "Sin partido ese día",
                            modifier = Modifier.align(Alignment.Center).padding(16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredSessions, key = { it.id }) { session ->
                                val onSessionItemClick = remember(session.id) { { onSessionClick(session.id) } }
                                SessionCard(
                                    session = session,
                                    onClick = onSessionItemClick
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
                is MatchHistoryUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (importState is MatchHistoryViewModel.ImportState.Importing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }
}

@Composable
private fun InlineCalendarPanel(
    currentMonth: YearMonth,
    selectedDate: String?,
    sessionDates: Set<String>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
            }
            val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es"))
                .replaceFirstChar { it.uppercase() }
            Text(
                "$monthName ${currentMonth.year}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("L", "M", "X", "J", "V", "S", "D").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        val firstDayOffset = currentMonth.atDay(1).dayOfWeek.value - 1
        val daysInMonth = currentMonth.lengthOfMonth()
        val totalCells = firstDayOffset + daysInMonth
        val gridRows = (totalCells + 6) / 7
        val cellSize = 48.dp

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().height(cellSize * gridRows),
            userScrollEnabled = false
        ) {
            items(firstDayOffset) { Box(modifier = Modifier.size(cellSize)) }
            items(daysInMonth) { index ->
                val dayNum = index + 1
                val isoDate = "%04d-%02d-%02d".format(currentMonth.year, currentMonth.monthValue, dayNum)
                val hasSession = isoDate in sessionDates
                val isSelected = isoDate == selectedDate
                DayCell(
                    day = dayNum,
                    hasSession = hasSession,
                    isSelected = isSelected,
                    onClick = { onSelectDate(if (isSelected) null else isoDate) },
                    modifier = Modifier.size(cellSize)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun EmptyState(onNewMatch: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("🎾", style = MaterialTheme.typography.displayLarge)
        Text(
            "No hay partidos todavía",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "Crea tu primer partido para empezar a registrar tus resultados.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onNewMatch) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Nuevo partido")
        }
    }
}


