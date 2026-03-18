package com.padelgroup.padelMatch.ui.navigation

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.padelgroup.padelMatch.ui.history.MatchHistoryScreen
import com.padelgroup.padelMatch.ui.history.MatchHistoryViewModel
import com.padelgroup.padelMatch.ui.history.OverflowMenu
import com.padelgroup.padelMatch.ui.statistics.StatisticsScreen
import com.padelgroup.padelMatch.ui.statistics.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    historyViewModel: MatchHistoryViewModel,
    onNewMatch: () -> Unit,
    onSessionClick: (Long) -> Unit,
    onPlayerClick: (Long) -> Unit = {},
    onCombinedChart: () -> Unit = {}
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isHistoryTab = currentRoute == HomeTab.HISTORY.route || currentRoute == null
    val isStatisticsTab = currentRoute == HomeTab.STATISTICS.route

    val calendarVisible by historyViewModel.calendarVisible.collectAsStateWithLifecycle()
    val currentMonth by historyViewModel.currentMonth.collectAsStateWithLifecycle()
    val selectedDate by historyViewModel.selectedDate.collectAsStateWithLifecycle()
    val sessionDates by historyViewModel.sessionDates.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { historyViewModel.importFromUri(it) }
    }

    LaunchedEffect(historyViewModel.dataEvents, lifecycleOwner) {
        historyViewModel.dataEvents
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                when (event) {
                    is MatchHistoryViewModel.DataEvent.Share -> {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, event.uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Exportar PadelMatch"))
                    }
                    is MatchHistoryViewModel.DataEvent.ToastMessage -> {
                        Toast.makeText(context, event.text, Toast.LENGTH_SHORT).show()
                    }
                    is MatchHistoryViewModel.DataEvent.ScrollToTop -> {
                        // Handled in MatchHistoryScreen
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PadelMatch", fontWeight = FontWeight.Bold) },
                actions = {
                    if (isStatisticsTab) {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                            tooltip = { PlainTooltip { Text("Gráfico general") } },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = onCombinedChart) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ShowChart,
                                    contentDescription = "Ver comparativa",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    if (isHistoryTab) {
                        TooltipBox(
                            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                            tooltip = { PlainTooltip { Text("Calendario") } },
                            state = rememberTooltipState()
                        ) {
                            IconButton(onClick = { historyViewModel.toggleCalendar() }) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = "Filtrar por fecha",
                                    tint = if (calendarVisible)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    OverflowMenu(
                        onImport = { filePicker.launch(arrayOf("application/json")) },
                        onExport = { historyViewModel.exportData() }
                    )                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (isHistoryTab) {
                ExtendedFloatingActionButton(
                    text = { Text("Nuevo partido") },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    onClick = onNewMatch
                )
            }
        },
        bottomBar = {
            NavigationBar {
                HomeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        },
        snackbarHost = { }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeTab.HISTORY.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(HomeTab.HISTORY.route) {
                MatchHistoryScreen(
                    viewModel = historyViewModel,
                    onNewMatch = onNewMatch,
                    onSessionClick = onSessionClick,
                    calendarVisible = calendarVisible,
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    sessionDates = sessionDates,
                    onPreviousMonth = { historyViewModel.previousMonth() },
                    onNextMonth = { historyViewModel.nextMonth() },
                    onSelectDate = { historyViewModel.selectDate(it) }
                )
            }
            composable(HomeTab.STATISTICS.route) {
                val statisticsViewModel = hiltViewModel<StatisticsViewModel>()
                StatisticsScreen(viewModel = statisticsViewModel, onPlayerClick = onPlayerClick)
            }
        }
    }
}


