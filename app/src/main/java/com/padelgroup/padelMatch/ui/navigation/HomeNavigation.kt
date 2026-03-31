package com.davidpv.padelmatch.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.ui.graphics.vector.ImageVector

enum class HomeTab(val route: String, val label: String, val icon: ImageVector) {
    HISTORY("home_history", "Partidos", Icons.Default.SportsTennis),
    STATISTICS("home_statistics", "Estadísticas", Icons.Default.BarChart)
}
