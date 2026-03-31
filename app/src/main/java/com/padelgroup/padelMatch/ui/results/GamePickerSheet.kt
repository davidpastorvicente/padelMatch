package com.davidpv.padelmatch.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.davidpv.padelmatch.data.db.dao.SessionPlayerWithName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamePickerSheet(
    players: List<SessionPlayerWithName>,
    preSelected: List<Long> = emptyList(),
    isEdit: Boolean = false,
    onConfirm: (p1p1: Long, p1p2: Long, p2p1: Long, p2p2: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var pair1Player1Id by remember(preSelected) { mutableStateOf(preSelected.getOrNull(0)) }
    var pair1Player2Id by remember(preSelected) { mutableStateOf(preSelected.getOrNull(1)) }
    var pair2Player1Id by remember(preSelected) { mutableStateOf(preSelected.getOrNull(2)) }
    var pair2Player2Id by remember(preSelected) { mutableStateOf(preSelected.getOrNull(3)) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val selectedIds = listOfNotNull(pair1Player1Id, pair1Player2Id, pair2Player1Id, pair2Player2Id)
    val isSelectionValid = selectedIds.size == 4 && selectedIds.distinct().size == 4

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEdit) "Editar set" else "Nuevo set",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Selecciona los 4 jugadores para las dos parejas",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Pareja A",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    PlayerDropdown(
                        label = "Jugador 1",
                        selectedPlayerId = pair1Player1Id,
                        players = players,
                        takenPlayerIds = emptyList(),
                        onSelected = {
                            pair1Player1Id = it
                            if (pair1Player2Id == it) pair1Player2Id = null
                            if (pair2Player1Id == it) pair2Player1Id = null
                            if (pair2Player2Id == it) pair2Player2Id = null
                        }
                    )
                    PlayerDropdown(
                        label = "Jugador 2",
                        selectedPlayerId = pair1Player2Id,
                        players = players,
                        takenPlayerIds = listOfNotNull(pair1Player1Id),
                        onSelected = {
                            pair1Player2Id = it
                            if (pair2Player1Id == it) pair2Player1Id = null
                            if (pair2Player2Id == it) pair2Player2Id = null
                        }
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Pareja B",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    PlayerDropdown(
                        label = "Jugador 1",
                        selectedPlayerId = pair2Player1Id,
                        players = players,
                        takenPlayerIds = listOfNotNull(pair1Player1Id, pair1Player2Id),
                        onSelected = {
                            pair2Player1Id = it
                            if (pair2Player2Id == it) pair2Player2Id = null
                        }
                    )
                    PlayerDropdown(
                        label = "Jugador 2",
                        selectedPlayerId = pair2Player2Id,
                        players = players,
                        takenPlayerIds = listOfNotNull(pair1Player1Id, pair1Player2Id, pair2Player1Id),
                        onSelected = { pair2Player2Id = it }
                    )
                }
            }

            if (isSelectionValid) {
                val playerMap = remember(players) { players.associate { it.playerId to it.playerName } }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${playerMap[pair1Player1Id]} y ${playerMap[pair1Player2Id]}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "vs",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${playerMap[pair2Player1Id]} y ${playerMap[pair2Player2Id]}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Button(
                onClick = {
                    val p1p1 = pair1Player1Id
                    val p1p2 = pair1Player2Id
                    val p2p1 = pair2Player1Id
                    val p2p2 = pair2Player2Id
                    if (p1p1 != null && p1p2 != null && p2p1 != null && p2p2 != null) {
                        onConfirm(p1p1, p1p2, p2p1, p2p2)
                    }
                },
                enabled = isSelectionValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdit) "Guardar" else "Añadir")
            }
        }
    }
}

@Composable
private fun PlayerDropdown(
    label: String,
    selectedPlayerId: Long?,
    players: List<SessionPlayerWithName>,
    takenPlayerIds: List<Long>,
    onSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val availablePlayers = remember(players, selectedPlayerId, takenPlayerIds) {
        players.filter { it.playerId == selectedPlayerId || it.playerId !in takenPlayerIds }
    }
    val selectedName = players.firstOrNull { it.playerId == selectedPlayerId }?.playerName.orEmpty()

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 14.dp, top = 8.dp, end = 16.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = selectedName.ifBlank { "Selecciona un jugador" })
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availablePlayers.forEach { player ->
                    DropdownMenuItem(
                        text = { Text(player.playerName) },
                        onClick = {
                            onSelected(player.playerId)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
