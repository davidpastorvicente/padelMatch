package com.davidpv.padelmatch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.davidpv.padelmatch.ui.history.MatchHistoryViewModel
import com.davidpv.padelmatch.ui.navigation.AppNavigation
import com.davidpv.padelmatch.ui.theme.PadelMatchTheme
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
