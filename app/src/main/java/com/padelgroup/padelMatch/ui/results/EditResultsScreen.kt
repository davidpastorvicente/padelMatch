package com.davidpv.padelmatch.ui.results

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import com.davidpv.padelmatch.ui.history.BracketGameCard
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun EditResultsScreen(
    viewModel: EditResultsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val showDiscardChangesDialog = remember { mutableStateOf(false) }

    val attemptBackNavigation = remember(state.hasUnsavedChanges, state.isSaving) {
        {
            if (state.hasUnsavedChanges && !state.isSaving) {
                showDiscardChangesDialog.value = true
            } else {
                onBack()
            }
        }
    }

    LaunchedEffect(viewModel.navBack, lifecycleOwner) {
        viewModel.navBack
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { onBack() }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    BackHandler(onBack = attemptBackNavigation)

    if (showDiscardChangesDialog.value) {
        AlertDialog(
            onDismissRequest = { showDiscardChangesDialog.value = false },
            title = { Text("Descartar cambios") },
            text = { Text("Hay cambios sin guardar. Si sales ahora, se perderán.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardChangesDialog.value = false
                    onBack()
                }) {
                    Text("Salir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardChangesDialog.value = false }) {
                    Text("Seguir editando")
                }
            }
        )
    }

    // Delete confirmation dialog
    state.deleteConfirmGameId?.let { gameId ->
        AlertDialog(
            onDismissRequest = viewModel::cancelDelete,
            title = { Text("Eliminar set") },
            text = { Text("Esta acción no se puede deshacer") },
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
                title = { Text("Editar sets") },
                navigationIcon = {
                    IconButton(onClick = attemptBackNavigation) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                        ReorderableItem(reorderState, key = editable.game.id) { _ ->
                            val gameId = editable.game.id
                            val currentWinner = state.winnerOverrides[gameId]
                            val displayGame = editable.game.copy(winningPair = currentWinner)
                            val onTeamClick = remember(gameId) { { pair: Int -> viewModel.onTeamClick(gameId, pair) } }
                            val onEdit = remember(gameId) { { viewModel.showEditPicker(gameId) } }
                            val onDelete = remember(gameId) { { viewModel.requestDelete(gameId) } }
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
                                        onTeamClick = onTeamClick,
                                        onEdit = onEdit,
                                        onDelete = onDelete
                                    )
                                }
                            }
                        }
                    }
                    item {
                        OutlinedButton(
                            onClick = viewModel::showAddPicker,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Añadir set")
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
