package com.davidpv.padelmatch.ui.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverflowMenu(onImport: () -> Unit, onExport: () -> Unit) {
    val expanded = remember { mutableStateOf(false) }
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text("Más opciones") } },
        state = tooltipState
    ) {
        IconButton(onClick = { expanded.value = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
    DropdownMenu(expanded = expanded.value, onDismissRequest = { expanded.value = false }) {
        DropdownMenuItem(
            text = { Text("Importar") },
            onClick = { expanded.value = false; onImport() }
        )
        DropdownMenuItem(
            text = { Text("Exportar") },
            onClick = { expanded.value = false; onExport() }
        )
    }
}
