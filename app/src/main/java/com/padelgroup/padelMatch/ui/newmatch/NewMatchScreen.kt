package com.padelgroup.padelMatch.ui.newmatch

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMatchScreen(
    viewModel: NewMatchViewModel,
    onSaved: (sessionId: Long) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val showDatePicker = remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(viewModel.navEvent) {
        viewModel.navEvent.collect { sessionId ->
            Toast.makeText(context, "Partido creado correctamente", Toast.LENGTH_SHORT).show()
            onSaved(sessionId)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    if (showDatePicker.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.selectedDate
                .atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val picked = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC")).toLocalDate()
                        viewModel.setSelectedDate(picked)
                    }
                    showDatePicker.value = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker.value = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo partido") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            if (state.isSaving) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column {
                    // Date selector row
                    val dateLabel = state.selectedDate.format(
                        DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", Locale.forLanguageTag("es"))
                    )
                    OutlinedCard(
                        onClick = { showDatePicker.value = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null,
                                tint = if (state.isDateConflict) MaterialTheme.colorScheme.error
                                       else MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Fecha del partido",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(dateLabel,
                                    style = MaterialTheme.typography.bodyLarge)
                            }
                            if (state.isDateConflict) {
                                Text("Ya existe un partido",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    PlayerSelectionStep(
                        state = state,
                        onTogglePlayer = viewModel::togglePlayer,
                        onDeletePlayer = viewModel::deletePlayer,
                        onNewPlayerNameChange = viewModel::onNewPlayerNameChange,
                        onAddPlayer = viewModel::addNewPlayer,
                        onContinue = viewModel::confirmPlayerSelection
                    )
                }
            }
        }
    }
}
