package com.davidpv.padelmatch.ui.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.davidpv.padelmatch.data.db.entity.PlayerEntity
import com.davidpv.padelmatch.data.model.PlayerSessionEntry
import com.davidpv.padelmatch.data.model.label
import com.davidpv.padelmatch.data.repository.PlayerRepository
import com.davidpv.padelmatch.data.repository.SeasonFilterStore
import com.davidpv.padelmatch.data.repository.StatisticsRepository
import com.davidpv.padelmatch.ui.navigation.PlayerDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class PlayerDetailData(
    val player: PlayerEntity,
    val totalGames: Int,
    val wins: Int,
    val losses: Int,
    val winRatio: Float,
    val sessionsAttended: Int,
    val sessionHistory: List<PlayerSessionEntry>,
    val seasonLabel: String
)

sealed class PlayerDetailUiState {
    object Loading : PlayerDetailUiState()
    data class Success(val data: PlayerDetailData) : PlayerDetailUiState()
    data class Error(val message: String) : PlayerDetailUiState()
}

@HiltViewModel
class PlayerDetailViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val statisticsRepository: StatisticsRepository,
    seasonFilterStore: SeasonFilterStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val playerId: Long = savedStateHandle.toRoute<PlayerDetailRoute>().playerId

    val uiState: StateFlow<PlayerDetailUiState> = seasonFilterStore.filter.filterNotNull()
        .map { filter ->
            val player = playerRepository.getPlayerById(playerId)
                ?: return@map PlayerDetailUiState.Error("Jugador no encontrado")
            val summary = statisticsRepository.getPlayerDetailSummary(playerId, filter)
            PlayerDetailUiState.Success(
                PlayerDetailData(
                    player = player,
                    totalGames = summary.totalGames,
                    wins = summary.wins,
                    losses = summary.losses,
                    winRatio = summary.winRatio,
                    sessionsAttended = summary.sessionsAttended,
                    sessionHistory = summary.sessionHistory,
                    seasonLabel = filter.label
                )
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerDetailUiState.Loading)
}
