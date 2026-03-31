package com.davidpv.padelmatch.ui.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidpv.padelmatch.data.db.entity.PlayerEntity
import com.davidpv.padelmatch.data.model.PlayerSessionEntry
import com.davidpv.padelmatch.data.repository.PlayerRepository
import com.davidpv.padelmatch.data.repository.StatisticsRepository
import com.davidpv.padelmatch.di.MainDispatcher
import com.davidpv.padelmatch.ui.navigation.PlayerDetailRoute
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerDetailData(
    val player: PlayerEntity,
    val totalGames: Int,
    val wins: Int,
    val losses: Int,
    val winRatio: Float,
    val sessionsAttended: Int,
    val sessionHistory: List<PlayerSessionEntry>
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
    savedStateHandle: SavedStateHandle,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val playerId: Long = savedStateHandle.toRoute<PlayerDetailRoute>().playerId

    private val _uiState = MutableStateFlow<PlayerDetailUiState>(PlayerDetailUiState.Loading)
    val uiState: StateFlow<PlayerDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(mainDispatcher) {
            val player = playerRepository.getPlayerById(playerId)
            if (player == null) {
                _uiState.value = PlayerDetailUiState.Error("Jugador no encontrado")
                return@launch
            }
            val summary = statisticsRepository.getPlayerDetailSummary(playerId)
            _uiState.value = PlayerDetailUiState.Success(
                PlayerDetailData(
                    player = player,
                    totalGames = summary.totalGames,
                    wins = summary.wins,
                    losses = summary.losses,
                    winRatio = summary.winRatio,
                    sessionsAttended = summary.sessionsAttended,
                    sessionHistory = summary.sessionHistory
                )
            )
        }
    }

}
