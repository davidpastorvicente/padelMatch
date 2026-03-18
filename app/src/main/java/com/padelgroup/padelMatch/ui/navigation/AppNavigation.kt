package com.padelgroup.padelMatch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.padelgroup.padelMatch.ui.history.MatchHistoryViewModel
import com.padelgroup.padelMatch.ui.newmatch.NewMatchScreen
import com.padelgroup.padelMatch.ui.results.EditResultsScreen
import com.padelgroup.padelMatch.ui.results.EditResultsViewModel
import com.padelgroup.padelMatch.ui.session.SessionDetailScreen
import com.padelgroup.padelMatch.ui.session.SessionDetailViewModel
import com.padelgroup.padelMatch.ui.statistics.CombinedWinRatioChartScreen
import com.padelgroup.padelMatch.ui.statistics.PlayerDetailScreen
import com.padelgroup.padelMatch.ui.statistics.PlayerDetailViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object NewMatch : Screen("newMatch")
    object EditResults : Screen("editResults/{sessionId}") {
        fun createRoute(sessionId: Long) = "editResults/$sessionId"
    }
    object SessionDetail : Screen("sessionDetail/{sessionId}") {
        fun createRoute(sessionId: Long) = "sessionDetail/$sessionId"
    }
    object PlayerDetail : Screen("playerDetail/{playerId}") {
        fun createRoute(playerId: Long) = "playerDetail/$playerId"
    }
    object CombinedChart : Screen("combinedChart")
}

@Composable
fun AppNavigation(historyViewModel: MatchHistoryViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                historyViewModel = historyViewModel,
                onNewMatch = { navController.navigate(Screen.NewMatch.route) },
                onSessionClick = { sessionId -> navController.navigate(Screen.SessionDetail.createRoute(sessionId)) },
                onPlayerClick = { playerId -> navController.navigate(Screen.PlayerDetail.createRoute(playerId)) },
                onCombinedChart = { navController.navigate(Screen.CombinedChart.route) }
            )
        }
        composable(Screen.NewMatch.route) {
            val newMatchViewModel = hiltViewModel<com.padelgroup.padelMatch.ui.newmatch.NewMatchViewModel>()
            NewMatchScreen(
                viewModel = newMatchViewModel,
                onSaved = { sessionId ->
                    navController.popBackStack()
                    navController.navigate(Screen.SessionDetail.createRoute(sessionId))
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.EditResults.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            val editResultsViewModel = hiltViewModel<EditResultsViewModel>()
            EditResultsScreen(
                viewModel = editResultsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.SessionDetail.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            val sessionDetailViewModel = hiltViewModel<SessionDetailViewModel>()
            SessionDetailScreen(
                viewModel = sessionDetailViewModel,
                onBack = { navController.popBackStack() },
                onEditResults = { id -> navController.navigate(Screen.EditResults.createRoute(id)) }
            )
        }
        composable(
            route = Screen.PlayerDetail.route,
            arguments = listOf(navArgument("playerId") { type = NavType.LongType })
        ) {
            val playerDetailViewModel = hiltViewModel<PlayerDetailViewModel>()
            PlayerDetailScreen(
                viewModel = playerDetailViewModel,
                onBack = { navController.popBackStack() },
                onSessionClick = { id -> navController.navigate(Screen.SessionDetail.createRoute(id)) }
            )
        }
        composable(Screen.CombinedChart.route) {
            val statisticsViewModel = hiltViewModel<com.padelgroup.padelMatch.ui.statistics.StatisticsViewModel>()
            CombinedWinRatioChartScreen(
                viewModel = statisticsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
