package com.padelgroup.padelMatch.ui.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*

@Composable
fun OverflowMenu(onImport: () -> Unit, onExport: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones", tint = MaterialTheme.colorScheme.onPrimary)
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text("Importar datos") },
            onClick = { expanded = false; onImport() }
        )
        DropdownMenuItem(
            text = { Text("Exportar datos") },
            onClick = { expanded = false; onExport() }
        )
    }
}
