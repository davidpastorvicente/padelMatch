package com.padelgroup.padelMatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import com.padelgroup.padelMatch.ui.navigation.AppNavigation
import com.padelgroup.padelMatch.ui.theme.PadelMatchTheme
import com.padelgroup.padelMatch.ui.history.MatchHistoryViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PadelMatchTheme {
                val historyViewModel: MatchHistoryViewModel = hiltViewModel()
                LaunchedEffect(Unit) {
                    historyViewModel.triggerImportIfNeeded()
                }
                AppNavigation(historyViewModel = historyViewModel)
            }
        }
    }
}
