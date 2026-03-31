package com.davidpv.padelmatch.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidpv.padelmatch.data.model.PlayerStats
import com.davidpv.padelmatch.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    object Empty : StatisticsUiState()
    data class Success(val playerStats: List<PlayerStats>) : StatisticsUiState()
}

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    statisticsRepository: StatisticsRepository
) : ViewModel() {
    val uiState: StateFlow<StatisticsUiState> = statisticsRepository.getPlayerStatsFlow()
        .map { stats ->
            if (stats.isEmpty()) StatisticsUiState.Empty else StatisticsUiState.Success(stats)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsUiState.Loading)
}
