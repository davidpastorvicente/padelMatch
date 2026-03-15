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

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object NewMatch : Screen("newMatch")
    object EditResults : Screen("editResults/{sessionId}") {
        fun createRoute(sessionId: Long) = "editResults/$sessionId"
    }
}

@Composable
fun AppNavigation(historyViewModel: MatchHistoryViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                historyViewModel = historyViewModel,
                onNewMatch = { navController.navigate(Screen.NewMatch.route) },
                onEditResults = { sessionId -> navController.navigate(Screen.EditResults.createRoute(sessionId)) }
            )
        }
        composable(Screen.NewMatch.route) {
            val newMatchViewModel = hiltViewModel<com.padelgroup.padelMatch.ui.newmatch.NewMatchViewModel>()
            NewMatchScreen(
                viewModel = newMatchViewModel,
                onSaved = { navController.popBackStack() },
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
    }
}
