package com.padelgroup.padelMatch.ui.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padelgroup.padelMatch.data.db.dao.GameDao
import com.padelgroup.padelMatch.data.db.dao.PlayerDao
import com.padelgroup.padelMatch.data.db.entity.PlayerEntity
import com.padelgroup.padelMatch.data.model.PlayerSessionEntry
import com.padelgroup.padelMatch.data.repository.StatisticsRepository
import com.padelgroup.padelMatch.di.MainDispatcher
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
    private val playerDao: PlayerDao,
    private val gameDao: GameDao,
    private val statisticsRepository: StatisticsRepository,
    savedStateHandle: SavedStateHandle,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val playerId: Long = checkNotNull(savedStateHandle["playerId"])

    private val _uiState = MutableStateFlow<PlayerDetailUiState>(PlayerDetailUiState.Loading)
    val uiState: StateFlow<PlayerDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(mainDispatcher) {
            val player = playerDao.getById(playerId)
            if (player == null) {
                _uiState.value = PlayerDetailUiState.Error("Jugador no encontrado")
                return@launch
            }
            val totalGames = playerDao.countGamesForPlayer(playerId)
            val wins = gameDao.countWinsForPlayer(playerId)
            val losses = totalGames - wins
            val sessionHistory = statisticsRepository.getPlayerSessionHistory(playerId)
            val sessionsAttended = sessionHistory.size
            _uiState.value = PlayerDetailUiState.Success(
                PlayerDetailData(
                    player = player,
                    totalGames = totalGames,
                    wins = wins,
                    losses = losses,
                    winRatio = if (totalGames > 0) wins.toFloat() / totalGames else 0f,
                    sessionsAttended = sessionsAttended,
                    sessionHistory = sessionHistory
                )
            )
        }
    }

}
