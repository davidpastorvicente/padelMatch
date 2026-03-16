package com.padelgroup.padelMatch.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padelgroup.padelMatch.data.model.PlayerStats
import com.padelgroup.padelMatch.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {
    val playerStats: StateFlow<List<PlayerStats>> = statisticsRepository.getPlayerStatsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
