package com.padelgroup.padelMatch.ui.newmatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padelgroup.padelMatch.data.db.dao.PlayerDao
import com.padelgroup.padelMatch.data.db.entity.PlayerEntity
import com.padelgroup.padelMatch.data.repository.NewMatchRepository
import com.padelgroup.padelMatch.data.repository.PlayerRepository
import com.padelgroup.padelMatch.data.repository.SessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class NewMatchUiState(
    val players: List<PlayerEntity> = emptyList(),
    val deletablePlayerIds: Set<Long> = emptySet(),
    val selectedPlayerIds: Set<Long> = emptySet(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val newPlayerName: String = "",
    val selectedDate: LocalDate = LocalDate.now(),
    val isDateConflict: Boolean = false
)

@HiltViewModel
class NewMatchViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val newMatchRepository: NewMatchRepository,
    private val sessionRepository: SessionRepository,
    private val playerDao: PlayerDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewMatchUiState())
    val uiState: StateFlow<NewMatchUiState> = _uiState

    private val _todaySessionExists = MutableStateFlow(false)

    private val _navEvent = MutableSharedFlow<Long>()
    val navEvent: SharedFlow<Long> = _navEvent

    init {
        viewModelScope.launch {
            val players = playerRepository.getAllPlayers()
            val deletable = players
                .filter { playerDao.countGamesForPlayer(it.id) == 0 }
                .map { it.id }
                .toSet()
            _uiState.update { it.copy(players = players, deletablePlayerIds = deletable) }
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val conflict = sessionRepository.sessionExistsForDate(today)
            _todaySessionExists.value = conflict
            _uiState.update { it.copy(isDateConflict = conflict) }
        }
    }

    fun setSelectedDate(date: LocalDate) {
        viewModelScope.launch {
            val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val conflict = sessionRepository.sessionExistsForDate(dateStr)
            _uiState.update { it.copy(selectedDate = date, isDateConflict = conflict) }
        }
    }

    fun togglePlayer(playerId: Long) {
        _uiState.update { state ->
            val selected = state.selectedPlayerIds.toMutableSet()
            if (playerId in selected) selected.remove(playerId) else selected.add(playerId)
            state.copy(selectedPlayerIds = selected)
        }
    }

    fun onNewPlayerNameChange(name: String) = _uiState.update { it.copy(newPlayerName = name) }

    fun addNewPlayer() {
        val name = _uiState.value.newPlayerName.trim()
        if (name.isEmpty()) return
        viewModelScope.launch {
            val id = playerRepository.addPlayer(name)
            val updatedPlayers = playerRepository.getAllPlayers()
            _uiState.update { it.copy(
                players = updatedPlayers,
                deletablePlayerIds = it.deletablePlayerIds + id,
                newPlayerName = "",
                selectedPlayerIds = it.selectedPlayerIds + id
            ) }
        }
    }

    fun deletePlayer(id: Long) {
        viewModelScope.launch {
            val deleted = playerRepository.deletePlayer(id)
            if (deleted) {
                val updatedPlayers = playerRepository.getAllPlayers()
                _uiState.update { it.copy(
                    players = updatedPlayers,
                    deletablePlayerIds = it.deletablePlayerIds - id,
                    selectedPlayerIds = it.selectedPlayerIds - id
                ) }
            } else {
                _uiState.update { it.copy(error = "Este jugador tiene partidos registrados y no puede eliminarse") }
            }
        }
    }

    fun confirmPlayerSelection() {        val state = _uiState.value
        val selectedPlayers = state.players.filter { it.id in state.selectedPlayerIds }
        if (selectedPlayers.size !in 4..7) return
        if (state.isDateConflict) {
            _uiState.update { it.copy(error = "Ya existe un partido en esa fecha") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            runCatching {
                val games = newMatchRepository.generateGames(selectedPlayers.map { it.id })
                newMatchRepository.saveSession(
                    playerIds = selectedPlayers.map { it.id },
                    games = games,
                    date = state.selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                )
            }.onSuccess { sessionId ->
                _uiState.update { it.copy(isSaving = false) }
                _navEvent.emit(sessionId)
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
