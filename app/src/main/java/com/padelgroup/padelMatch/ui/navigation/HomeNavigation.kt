package com.padelgroup.padelMatch.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.graphics.vector.ImageVector

enum class HomeTab(val route: String, val label: String, val icon: ImageVector) {
    HISTORY("home_history", "Historial", Icons.Default.History),
    CALENDAR("home_calendar", "Calendario", Icons.Default.CalendarMonth),
    STATISTICS("home_statistics", "Estadísticas", Icons.Default.BarChart)
}
