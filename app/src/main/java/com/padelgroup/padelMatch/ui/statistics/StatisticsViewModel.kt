package com.davidpv.padelmatch.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidpv.padelmatch.data.model.PlayerStats
import com.davidpv.padelmatch.data.model.Season
import com.davidpv.padelmatch.data.model.SeasonFilter
import com.davidpv.padelmatch.data.model.currentSeason
import com.davidpv.padelmatch.data.repository.SeasonFilterStore
import com.davidpv.padelmatch.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Empty(val seasons: List<Season>, val selectedSeason: SeasonFilter) : StatisticsUiState()
    data class Success(
        val playerStats: List<PlayerStats>,
        val seasons: List<Season>,
        val selectedSeason: SeasonFilter
    ) : StatisticsUiState()
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel @Inject constructor(
    private val statisticsRepository: StatisticsRepository,
    private val seasonFilterStore: SeasonFilterStore
) : ViewModel() {
    init {
        seasonFilterStore.initializeIfNeeded(SeasonFilter.Of(currentSeason()))
    }

    val uiState: StateFlow<StatisticsUiState> = combine(
        statisticsRepository.availableSeasons,
        seasonFilterStore.filter.filterNotNull()
    ) { seasons, selectedSeason -> seasons to selectedSeason }
        .flatMapLatest { (seasons, selectedSeason) ->
            statisticsRepository.getPlayerStatsFlow(selectedSeason).map { stats ->
                if (stats.isEmpty()) {
                    StatisticsUiState.Empty(seasons, selectedSeason)
                } else {
                    StatisticsUiState.Success(stats, seasons, selectedSeason)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsUiState.Loading)

    fun selectSeason(seasonFilter: SeasonFilter) {
        seasonFilterStore.select(seasonFilter)
    }
}
