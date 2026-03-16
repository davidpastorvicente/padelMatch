package com.padelgroup.padelMatch.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.padelgroup.padelMatch.ui.history.SessionCard
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val sessionDates by viewModel.sessionDates.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedSession by viewModel.selectedSession.collectAsState()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 8.dp)
    ) {
        // Month navigation row
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.previousMonth() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
            }
            val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es"))
                .replaceFirstChar { it.uppercase() }
            Text(
                "$monthName ${currentMonth.year}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.nextMonth() }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
            }
        }

        // Day-of-week headers (Mon–Sun in Spanish)
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

            // Build grid cells
            val firstDayOffset = currentMonth.atDay(1).dayOfWeek.value - 1 // Mon=0, Sun=6
            val daysInMonth = currentMonth.lengthOfMonth()
            val totalCells = firstDayOffset + daysInMonth
            val gridRows = (totalCells + 6) / 7

            val cellSize = 48.dp
            val gridHeight = cellSize * gridRows

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight),
                userScrollEnabled = false
            ) {
                // Empty cells before first day
                items(firstDayOffset) {
                    Box(modifier = Modifier.size(cellSize))
                }
                // Day cells
                items(daysInMonth) { index ->
                    val dayNum = index + 1
                    val isoDate = "%04d-%02d-%02d".format(currentMonth.year, currentMonth.monthValue, dayNum)
                    val hasSession = isoDate in sessionDates
                    val isSelected = isoDate == selectedDate

                    DayCell(
                        day = dayNum,
                        hasSession = hasSession,
                        isSelected = isSelected,
                        onClick = {
                            if (hasSession) {
                                viewModel.selectDate(if (isSelected) null else isoDate)
                            }
                        },
                        modifier = Modifier.size(cellSize)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Session detail below the grid
            if (selectedDate != null && selectedSession != null) {
                Text(
                    "Partida seleccionada",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
                SessionCard(
                    session = selectedSession!!,
                    onClick = {}
                )
                Spacer(Modifier.height(80.dp))
            }
        }
}

@Composable
fun DayCell(
    day: Int,
    hasSession: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val onSurfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) Modifier.background(primaryColor)
                else if (hasSession) Modifier.background(primaryContainerColor)
                else Modifier
            )
            .clickable(enabled = hasSession || true) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (hasSession) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> onPrimaryColor
                    hasSession -> primaryColor
                    else -> onSurfaceVariantColor
                }
            )
            if (hasSession && !isSelected) {
                Spacer(Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                )
            }
        }
    }
}
