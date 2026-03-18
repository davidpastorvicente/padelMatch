package com.padelgroup.padelMatch.ui.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padelgroup.padelMatch.data.db.entity.GameEntity
import com.padelgroup.padelMatch.data.repository.GameWithPlayerNames
import com.padelgroup.padelMatch.data.repository.SessionRepository
import com.padelgroup.padelMatch.di.MainDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditableGame(
    val game: GameWithPlayerNames,
    val isNew: Boolean = false
)

data class EditResultsUiState(
    val games: List<EditableGame> = emptyList(),
    val sessionPlayers: List<com.padelgroup.padelMatch.data.db.dao.SessionPlayerWithName> = emptyList(),
    val winnerOverrides: Map<Long, Int?> = emptyMap(),
    val pendingDeletes: Set<Long> = emptySet(),
    val isSaving: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val showAddPicker: Boolean = false,
    val editPickerGameId: Long? = null,
    val deleteConfirmGameId: Long? = null
)

@HiltViewModel
class EditResultsViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val sessionId: Long = checkNotNull(savedStateHandle["sessionId"])

    private val _uiState = MutableStateFlow(EditResultsUiState())
    val uiState: StateFlow<EditResultsUiState> = _uiState.asStateFlow()

    private val _navBack = MutableSharedFlow<Unit>()
    val navBack: SharedFlow<Unit> = _navBack.asSharedFlow()

    private var initialized = false

    init {
        viewModelScope.launch(mainDispatcher) {
            sessionRepository.getAllSessionsFlow().collect { sessions ->
                val session = sessions.find { it.id == sessionId }
                if (session != null && !initialized) {
                    initialized = true
                    _uiState.update { state ->
                        state.copy(
                            games = session.games.map { EditableGame(it) },
                            sessionPlayers = session.players,
                            winnerOverrides = session.games.associate { it.id to it.winningPair },
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun onTeamClick(gameId: Long, pair: Int) {
        _uiState.update { state ->
            val current = state.winnerOverrides[gameId]
            val newValue = if (current == pair) null else pair
            state.copy(winnerOverrides = state.winnerOverrides + (gameId to newValue))
        }
    }

    fun requestDelete(gameId: Long) = _uiState.update { it.copy(deleteConfirmGameId = gameId) }
    fun cancelDelete() = _uiState.update { it.copy(deleteConfirmGameId = null) }

    fun confirmDelete(gameId: Long) {
        _uiState.update { state ->
            state.copy(
                games = state.games.filterNot { it.game.id == gameId },
                pendingDeletes = if (gameId > 0) state.pendingDeletes + gameId else state.pendingDeletes,
                winnerOverrides = state.winnerOverrides - gameId,
                deleteConfirmGameId = null
            )
        }
    }

    fun showAddPicker() = _uiState.update { it.copy(showAddPicker = true) }
    fun hideAddPicker() = _uiState.update { it.copy(showAddPicker = false) }
    fun showEditPicker(gameId: Long) = _uiState.update { it.copy(editPickerGameId = gameId) }
    fun hideEditPicker() = _uiState.update { it.copy(editPickerGameId = null) }

    fun addGame(p1p1Id: Long, p1p2Id: Long, p2p1Id: Long, p2p2Id: Long) {
        _uiState.update { state ->
            val playerMap = state.sessionPlayers.associate { it.playerId to it.playerName }
            val nextNumber = (state.games.maxOfOrNull { it.game.gameNumber } ?: 0) + 1
            // Use negative temp ID to distinguish new games
            val tempId = -(System.currentTimeMillis())
            val newGame = GameWithPlayerNames(
                id = tempId,
                gameNumber = nextNumber,
                pair1Player1 = playerMap[p1p1Id] ?: "?",
                pair1Player2 = playerMap[p1p2Id] ?: "?",
                pair2Player1 = playerMap[p2p1Id] ?: "?",
                pair2Player2 = playerMap[p2p2Id] ?: "?",
                pair1Player1Id = p1p1Id,
                pair1Player2Id = p1p2Id,
                pair2Player1Id = p2p1Id,
                pair2Player2Id = p2p2Id,
                pair1Score = null,
                pair2Score = null,
                winningPair = null
            )
            state.copy(
                games = state.games + EditableGame(newGame, isNew = true),
                winnerOverrides = state.winnerOverrides + (tempId to null),
                showAddPicker = false
            )
        }
    }

    fun editGame(gameId: Long, p1p1Id: Long, p1p2Id: Long, p2p1Id: Long, p2p2Id: Long) {
        _uiState.update { state ->
            val playerMap = state.sessionPlayers.associate { it.playerId to it.playerName }
            val games = state.games.map { eg ->
                if (eg.game.id == gameId) {
                    eg.copy(game = eg.game.copy(
                        pair1Player1 = playerMap[p1p1Id] ?: "?",
                        pair1Player2 = playerMap[p1p2Id] ?: "?",
                        pair2Player1 = playerMap[p2p1Id] ?: "?",
                        pair2Player2 = playerMap[p2p2Id] ?: "?",
                        pair1Player1Id = p1p1Id,
                        pair1Player2Id = p1p2Id,
                        pair2Player1Id = p2p1Id,
                        pair2Player2Id = p2p2Id,
                        winningPair = null
                    ))
                } else eg
            }
            state.copy(
                games = games,
                winnerOverrides = state.winnerOverrides + (gameId to null),
                editPickerGameId = null
            )
        }
    }

    fun reorderGames(fromIndex: Int, toIndex: Int) {
        _uiState.update { state ->
            val games = state.games.toMutableList()
            if (fromIndex < 0 || toIndex < 0 || fromIndex >= games.size || toIndex >= games.size) return@update state
            games.add(toIndex, games.removeAt(fromIndex))
            state.copy(games = games)
        }
    }

    fun save() {
        viewModelScope.launch(mainDispatcher) {
            _uiState.update { it.copy(isSaving = true) }
            val state = _uiState.value

            // Renumber all games based on their final position in the list
            val renumbered = state.games.mapIndexed { index, eg ->
                eg.copy(game = eg.game.copy(gameNumber = index + 1))
            }

            val toInsert = renumbered.filter { it.isNew }.map { eg ->
                GameEntity(
                    sessionId = sessionId,
                    gameNumber = eg.game.gameNumber,
                    pair1Player1Id = eg.game.pair1Player1Id,
                    pair1Player2Id = eg.game.pair1Player2Id,
                    pair2Player1Id = eg.game.pair2Player1Id,
                    pair2Player2Id = eg.game.pair2Player2Id,
                    winningPair = state.winnerOverrides[eg.game.id]
                )
            }
            val existingWinners = state.winnerOverrides.filterKeys { id ->
                id > 0 && id !in state.pendingDeletes
            }
            // Map existing game IDs to their new game numbers
            val gameNumberUpdates = renumbered
                .filter { !it.isNew && it.game.id !in state.pendingDeletes }
                .associate { it.game.id to it.game.gameNumber }

            runCatching {
                sessionRepository.updateGameWinners(
                    sessionId = sessionId,
                    winners = existingWinners,
                    toDelete = state.pendingDeletes,
                    toInsert = toInsert,
                    gameNumberUpdates = gameNumberUpdates
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, error = null) }
                _navBack.emit(Unit)
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Error al guardar") }
            }
        }
    }
}
