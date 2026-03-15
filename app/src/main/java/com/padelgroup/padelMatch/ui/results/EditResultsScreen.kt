package com.padelgroup.padelMatch.ui.results

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.unit.dp
import com.padelgroup.padelMatch.ui.history.BracketGameCard
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditResultsScreen(
    viewModel: EditResultsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.navBack.collect { onBack() }
    }

    // Delete confirmation dialog
    state.deleteConfirmGameId?.let { gameId ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Eliminar partido") },
            text = { Text("¿Eliminar este partido?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete(gameId) }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelDelete) { Text("Cancelar") }
            }
        )
    }

    // Add game picker
    if (state.showAddPicker) {
        GamePickerSheet(
            players = state.sessionPlayers,
            isEdit = false,
            onConfirm = { p1p1, p1p2, p2p1, p2p2 -> viewModel.addGame(p1p1, p1p2, p2p1, p2p2) },
            onDismiss = viewModel::hideAddPicker
        )
    }

    // Edit game picker
    state.editPickerGameId?.let { gameId ->
        val game = state.games.find { it.game.id == gameId }?.game
        GamePickerSheet(
            players = state.sessionPlayers,
            preSelected = game?.let {
                listOf(it.pair1Player1Id, it.pair1Player2Id, it.pair2Player1Id, it.pair2Player2Id)
            } ?: emptyList(),
            isEdit = true,
            onConfirm = { p1p1, p1p2, p2p1, p2p2 -> viewModel.editGame(gameId, p1p1, p1p2, p2p1, p2p2) },
            onDismiss = viewModel::hideEditPicker
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                val lazyListState = rememberLazyListState()
                val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
                    // Only reorder within the games range (exclude the add button at the end)
                    val gamesCount = state.games.size
                    if (from.index < gamesCount && to.index < gamesCount) {
                        viewModel.reorderGames(from.index, to.index)
                    }
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.games, key = { it.game.id }) { editable ->
                        ReorderableItem(reorderState, key = editable.game.id) { isDragging ->
                            val gameId = editable.game.id
                            val currentWinner = state.winnerOverrides[gameId]
                            val displayGame = editable.game.copy(winningPair = currentWinner)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    modifier = Modifier.draggableHandle(),
                                    onClick = {}
                                ) {
                                    Icon(
                                        Icons.Default.DragHandle,
                                        contentDescription = "Reordenar",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(modifier = Modifier.weight(1f)) {
                                    BracketGameCard(
                                        game = displayGame,
                                        onTeamClick = { pair -> viewModel.onTeamClick(gameId, pair) },
                                        onEdit = { viewModel.showEditPicker(gameId) },
                                        onDelete = { viewModel.requestDelete(gameId) }
                                    )
                                }
                            }
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = viewModel::showAddPicker,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Añadir partido")
                        }
                    }
                }

                HorizontalDivider()
                Box(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Button(
                        onClick = viewModel::save,
                        enabled = !state.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}
