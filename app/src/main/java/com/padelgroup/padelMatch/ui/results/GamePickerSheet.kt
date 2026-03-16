package com.padelgroup.padelMatch.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.padelgroup.padelMatch.data.db.dao.SessionPlayerWithName
import com.padelgroup.padelMatch.ui.theme.playerColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GamePickerSheet(
    players: List<SessionPlayerWithName>,
    preSelected: List<Long> = emptyList(),
    isEdit: Boolean = false,
    onConfirm: (p1p1: Long, p1p2: Long, p2p1: Long, p2p2: Long) -> Unit,
    onDismiss: () -> Unit
) {
    // Maintain selection order so first 2 = pair 1, last 2 = pair 2
    var selectedOrdered by remember { mutableStateOf(preSelected.take(4)) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEdit) "Editar jugadores" else "Nuevo partido",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Selecciona 4 jugadores (los 2 primeros = Pareja A, los 2 últimos = Pareja B)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                players.forEach { player ->
                    val (bg, fg) = playerColors(player.playerName)
                    val position = selectedOrdered.indexOf(player.playerId) + 1 // 0 = not selected
                    val isSelected = position > 0
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedOrdered = if (isSelected) {
                                selectedOrdered.filter { it != player.playerId }
                            } else if (selectedOrdered.size < 4) {
                                selectedOrdered + player.playerId
                            } else selectedOrdered
                        },
                        label = {
                            Text(if (isSelected) "${player.playerName} ($position)" else player.playerName)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = fg,
                            selectedContainerColor = bg,
                            selectedLabelColor = fg
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = bg,
                            selectedBorderColor = bg,
                            borderWidth = 1.5.dp,
                            selectedBorderWidth = 0.dp
                        )
                    )
                }
            }

            // Preview pairs when 4 selected
            if (selectedOrdered.size == 4) {
                val playerMap = players.associate { it.playerId to it.playerName }
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pareja A", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(playerMap[selectedOrdered[0]] ?: "?", style = MaterialTheme.typography.bodySmall)
                        Text(playerMap[selectedOrdered[1]] ?: "?", style = MaterialTheme.typography.bodySmall)
                    }
                    Text("vs", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.CenterVertically))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pareja B", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        Text(playerMap[selectedOrdered[2]] ?: "?", style = MaterialTheme.typography.bodySmall)
                        Text(playerMap[selectedOrdered[3]] ?: "?", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Button(
                onClick = {
                    if (selectedOrdered.size == 4) {
                        onConfirm(selectedOrdered[0], selectedOrdered[1], selectedOrdered[2], selectedOrdered[3])
                    }
                },
                enabled = selectedOrdered.size == 4,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdit) "Guardar" else "Añadir")
            }
        }
    }
}
