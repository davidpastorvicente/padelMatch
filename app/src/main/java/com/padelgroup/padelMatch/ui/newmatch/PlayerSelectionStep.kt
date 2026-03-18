package com.padelgroup.padelMatch.ui.newmatch

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.padelgroup.padelMatch.ui.theme.playerColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerSelectionStep(
    state: NewMatchUiState,
    onTogglePlayer: (Long) -> Unit,
    onDeletePlayer: (Long) -> Unit,
    onNewPlayerNameChange: (String) -> Unit,
    onAddPlayer: () -> Unit,
    onContinue: () -> Unit
) {
    val selectedCount = state.selectedPlayerIds.size
    val isValid = selectedCount in 4..7

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Selecciona los jugadores (4–7)",
            style = MaterialTheme.typography.titleMedium
        )

        val validationText = when {
            selectedCount < 4 -> "Necesitas al menos ${4 - selectedCount} jugador(es) más"
            selectedCount > 7 -> "Demasiados jugadores, quita ${selectedCount - 7}"
            else -> "$selectedCount jugadores seleccionados ✓"
        }
        Text(
            validationText,
            style = MaterialTheme.typography.bodySmall,
            color = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.players.forEach { player ->
                val (bg, fg) = playerColors(player.name)
                val selected = player.id in state.selectedPlayerIds
                InputChip(
                    selected = selected,
                    onClick = { onTogglePlayer(player.id) },
                    label = { Text(player.name) },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = fg,
                        selectedContainerColor = bg,
                        selectedLabelColor = fg,
                        trailingIconColor = fg.copy(alpha = 0.6f),
                        selectedTrailingIconColor = fg
                    ),
                    border = InputChipDefaults.inputChipBorder(
                        enabled = true,
                        selected = selected,
                        borderColor = bg,
                        selectedBorderColor = bg,
                        borderWidth = 1.5.dp,
                        selectedBorderWidth = 0.dp
                    ),
                    trailingIcon = if (player.id in state.deletablePlayerIds) {
                        {
                            IconButton(
                                onClick = { onDeletePlayer(player.id) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Eliminar ${player.name}",
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    } else null
                )
            }
        }

        HorizontalDivider()
        Text("Añadir nuevo jugador", style = MaterialTheme.typography.titleSmall)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.newPlayerName,
                onValueChange = onNewPlayerNameChange,
                label = { Text("Nombre") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(onClick = onAddPlayer, enabled = state.newPlayerName.isNotBlank()) {
                Icon(Icons.Default.Add, contentDescription = "Añadir")
            }
        }

        Spacer(Modifier.weight(1f))
        Button(
            onClick = onContinue,
            enabled = isValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Crear partido")
        }
    }
}
