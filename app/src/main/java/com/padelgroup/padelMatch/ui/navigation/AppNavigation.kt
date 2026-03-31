package com.davidpv.padelmatch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.davidpv.padelmatch.ui.history.MatchHistoryViewModel
import com.davidpv.padelmatch.ui.newmatch.NewMatchScreen
import com.davidpv.padelmatch.ui.results.EditResultsScreen
import com.davidpv.padelmatch.ui.results.EditResultsViewModel
import com.davidpv.padelmatch.ui.session.SessionDetailScreen
import com.davidpv.padelmatch.ui.session.SessionDetailViewModel
import com.davidpv.padelmatch.ui.statistics.CombinedWinRatioChartScreen
import com.davidpv.padelmatch.ui.statistics.PlayerDetailScreen
import com.davidpv.padelmatch.ui.statistics.PlayerDetailViewModel

@Composable
fun AppNavigation(historyViewModel: MatchHistoryViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> {
            HomeScreen(
                historyViewModel = historyViewModel,
                onNewMatch = { navController.navigate(NewMatchRoute) },
                onSessionClick = { sessionId -> navController.navigate(SessionDetailRoute(sessionId)) },
                onPlayerClick = { playerId -> navController.navigate(PlayerDetailRoute(playerId)) },
                onCombinedChart = { navController.navigate(CombinedChartRoute) }
            )
        }
        composable<NewMatchRoute> {
            val newMatchViewModel = hiltViewModel<com.davidpv.padelmatch.ui.newmatch.NewMatchViewModel>()
            NewMatchScreen(
                viewModel = newMatchViewModel,
                onSaved = { sessionId ->
                    navController.popBackStack()
                    navController.navigate(SessionDetailRoute(sessionId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable<EditResultsRoute> {
            val editResultsViewModel = hiltViewModel<EditResultsViewModel>()
            EditResultsScreen(
                viewModel = editResultsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable<SessionDetailRoute> {
            val sessionDetailViewModel = hiltViewModel<SessionDetailViewModel>()
            SessionDetailScreen(
                viewModel = sessionDetailViewModel,
                onBack = { navController.popBackStack() },
                onEditResults = { id -> navController.navigate(EditResultsRoute(id)) }
            )
        }
        composable<PlayerDetailRoute> {
            val playerDetailViewModel = hiltViewModel<PlayerDetailViewModel>()
            PlayerDetailScreen(
                viewModel = playerDetailViewModel,
                onBack = { navController.popBackStack() },
                onSessionClick = { id -> navController.navigate(SessionDetailRoute(id)) }
            )
        }
        composable<CombinedChartRoute> {
            val statisticsViewModel = hiltViewModel<com.davidpv.padelmatch.ui.statistics.StatisticsViewModel>()
            CombinedWinRatioChartScreen(
                viewModel = statisticsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
