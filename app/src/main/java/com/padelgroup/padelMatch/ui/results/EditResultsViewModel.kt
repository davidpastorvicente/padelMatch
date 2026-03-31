package com.davidpv.padelmatch.ui.results

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.davidpv.padelmatch.data.db.entity.GameEntity
import com.davidpv.padelmatch.data.repository.GameWithPlayerNames
import com.davidpv.padelmatch.data.repository.SessionRepository
import com.davidpv.padelmatch.di.MainDispatcher
import com.davidpv.padelmatch.ui.navigation.EditResultsRoute
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
    val sessionPlayers: List<com.davidpv.padelmatch.data.db.dao.SessionPlayerWithName> = emptyList(),
    val winnerOverrides: Map<Long, Int?> = emptyMap(),
    val pendingDeletes: Set<Long> = emptySet(),
    val isSaving: Boolean = false,
    val isLoading: Boolean = true,
    val hasUnsavedChanges: Boolean = false,
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

    private val sessionId: Long = savedStateHandle.toRoute<EditResultsRoute>().sessionId

    private val _uiState = MutableStateFlow(EditResultsUiState())
    val uiState: StateFlow<EditResultsUiState> = _uiState.asStateFlow()

    private val _navBack = MutableSharedFlow<Unit>()
    val navBack: SharedFlow<Unit> = _navBack.asSharedFlow()

    private var initialized = false
    private var initialGames: List<EditableGame> = emptyList()
    private var initialWinnerOverrides: Map<Long, Int?> = emptyMap()

    init {
        viewModelScope.launch(mainDispatcher) {
            sessionRepository.getAllSessionsFlow().collect { sessions ->
                val session = sessions.find { it.id == sessionId }
                if (session != null && !initialized) {
                    initialized = true
                    initialGames = session.games.map { EditableGame(it) }
                    initialWinnerOverrides = session.games.associate { it.id to it.winningPair }
                    _uiState.update { state ->
                        state.copy(
                            games = initialGames,
                            sessionPlayers = session.players,
                            winnerOverrides = initialWinnerOverrides,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun onTeamClick(gameId: Long, pair: Int) {
        updateUiState { state ->
            val current = state.winnerOverrides[gameId]
            val newValue = if (current == pair) null else pair
            state.copy(winnerOverrides = state.winnerOverrides + (gameId to newValue))
        }
    }

    fun requestDelete(gameId: Long) = _uiState.update { it.copy(deleteConfirmGameId = gameId) }
    fun cancelDelete() = _uiState.update { it.copy(deleteConfirmGameId = null) }

    fun confirmDelete(gameId: Long) {
        updateUiState { state ->
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
        updateUiState { state ->
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
        updateUiState { state ->
            val playerMap = state.sessionPlayers.associate { it.playerId to it.playerName }
            val games = state.games.map { eg ->
                if (eg.game.id == gameId) {
                    val playersChanged = eg.game.pair1Player1Id != p1p1Id ||
                        eg.game.pair1Player2Id != p1p2Id ||
                        eg.game.pair2Player1Id != p2p1Id ||
                        eg.game.pair2Player2Id != p2p2Id
                    val preservedWinner = if (playersChanged) null else (state.winnerOverrides[gameId] ?: eg.game.winningPair)
                    eg.copy(game = eg.game.copy(
                        pair1Player1 = playerMap[p1p1Id] ?: "?",
                        pair1Player2 = playerMap[p1p2Id] ?: "?",
                        pair2Player1 = playerMap[p2p1Id] ?: "?",
                        pair2Player2 = playerMap[p2p2Id] ?: "?",
                        pair1Player1Id = p1p1Id,
                        pair1Player2Id = p1p2Id,
                        pair2Player1Id = p2p1Id,
                        pair2Player2Id = p2p2Id,
                        winningPair = preservedWinner
                    ))
                } else eg
            }
            state.copy(
                games = games,
                winnerOverrides = state.winnerOverrides + (gameId to games.first { it.game.id == gameId }.game.winningPair),
                editPickerGameId = null
            )
        }
    }

    fun reorderGames(fromIndex: Int, toIndex: Int) {
        updateUiState { state ->
            val games = state.games.toMutableList()
            if (fromIndex < 0 || toIndex < 0 || fromIndex >= games.size || toIndex >= games.size) {
                state
            } else {
                games.add(toIndex, games.removeAt(fromIndex))
                state.copy(games = games)
            }
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
            val toUpdate = renumbered
                .filter { !it.isNew && it.game.id !in state.pendingDeletes }
                .map { eg ->
                    GameEntity(
                        id = eg.game.id,
                        sessionId = sessionId,
                        gameNumber = eg.game.gameNumber,
                        pair1Player1Id = eg.game.pair1Player1Id,
                        pair1Player2Id = eg.game.pair1Player2Id,
                        pair2Player1Id = eg.game.pair2Player1Id,
                        pair2Player2Id = eg.game.pair2Player2Id,
                        pair1Score = eg.game.pair1Score,
                        pair2Score = eg.game.pair2Score,
                        winningPair = state.winnerOverrides[eg.game.id]
                    )
                }

            runCatching {
                sessionRepository.updateSessionGames(
                    sessionId = sessionId,
                    toDelete = state.pendingDeletes,
                    toInsert = toInsert,
                    toUpdate = toUpdate
                )
            }.onSuccess {
                _uiState.update { it.copy(isSaving = false, error = null, hasUnsavedChanges = false) }
                _navBack.emit(Unit)
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Error al guardar") }
            }
        }
    }

    private fun updateUiState(transform: (EditResultsUiState) -> EditResultsUiState) {
        _uiState.update { state ->
            val updatedState = transform(state)
            updatedState.copy(hasUnsavedChanges = hasUnsavedChanges(updatedState))
        }
    }

    private fun hasUnsavedChanges(state: EditResultsUiState): Boolean {
        if (state.pendingDeletes.isNotEmpty()) return true
        if (state.games.size != initialGames.size) return true
        if (state.winnerOverrides != initialWinnerOverrides) return true
        return state.games != initialGames
    }
}
