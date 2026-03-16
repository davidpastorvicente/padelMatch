package com.padelgroup.padelMatch.ui.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padelgroup.padelMatch.data.db.dao.GameDao
import com.padelgroup.padelMatch.data.db.dao.PlayerDao
import com.padelgroup.padelMatch.data.db.entity.PlayerEntity
import com.padelgroup.padelMatch.data.model.PlayerSessionEntry
import com.padelgroup.padelMatch.data.repository.StatisticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlayerDetailData(
    val player: PlayerEntity,
    val totalGames: Int,
    val wins: Int,
    val losses: Int,
    val winRatio: Float,
    val sessionsAttended: Int,
    val avgWinRatioPerSession: Float,
    val longestWinStreak: Int,
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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val playerId: Long = checkNotNull(savedStateHandle["playerId"])

    private val _uiState = MutableStateFlow<PlayerDetailUiState>(PlayerDetailUiState.Loading)
    val uiState: StateFlow<PlayerDetailUiState> = _uiState

    init {
        viewModelScope.launch {
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
            val avgWinRatioPerSession = if (sessionsAttended > 0)
                sessionHistory.map { it.winRatio }.average().toFloat() else 0f
            val longestWinStreak = longestStreak(sessionHistory)
            _uiState.value = PlayerDetailUiState.Success(
                PlayerDetailData(
                    player = player,
                    totalGames = totalGames,
                    wins = wins,
                    losses = losses,
                    winRatio = if (totalGames > 0) wins.toFloat() / totalGames else 0f,
                    sessionsAttended = sessionsAttended,
                    avgWinRatioPerSession = avgWinRatioPerSession,
                    longestWinStreak = longestWinStreak,
                    sessionHistory = sessionHistory
                )
            )
        }
    }

    private fun longestStreak(history: List<PlayerSessionEntry>): Int {
        var max = 0
        var current = 0
        for (entry in history) {
            if (entry.winRatio >= 0.5f) { current++; if (current > max) max = current }
            else current = 0
        }
        return max
    }
}
