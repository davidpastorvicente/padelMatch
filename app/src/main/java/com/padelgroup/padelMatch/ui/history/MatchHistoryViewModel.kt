package com.padelgroup.padelMatch.ui.history

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padelgroup.padelMatch.data.exporter.JsonExporter
import com.padelgroup.padelMatch.data.importer.JsonImporter
import com.padelgroup.padelMatch.data.repository.ImportRepository
import com.padelgroup.padelMatch.data.repository.SessionRepository
import com.padelgroup.padelMatch.data.repository.SessionWithDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import javax.inject.Inject

sealed class MatchHistoryUiState {
    object Loading : MatchHistoryUiState()
    object Empty : MatchHistoryUiState()
    data class Success(val sessions: List<SessionWithDetails>) : MatchHistoryUiState()
    data class Error(val message: String) : MatchHistoryUiState()
}

@HiltViewModel
class MatchHistoryViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val importRepository: ImportRepository,
    private val jsonExporter: JsonExporter,
    private val jsonImporter: JsonImporter,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState

    private val _dataEvents = MutableSharedFlow<DataEvent>()
    val dataEvents: SharedFlow<DataEvent> = _dataEvents

    val uiState: StateFlow<MatchHistoryUiState> = sessionRepository.getAllSessionsFlow()
        .map { sessions ->
            if (sessions.isEmpty()) MatchHistoryUiState.Empty
            else MatchHistoryUiState.Success(sessions)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MatchHistoryUiState.Loading)

    val todaySessionExists: StateFlow<Boolean> = sessionRepository.getAllSessionsFlow()
        .map { sessions ->
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            sessions.any { it.date == today }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Calendar state
    private val _calendarVisible = MutableStateFlow(false)
    val calendarVisible: StateFlow<Boolean> = _calendarVisible

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate

    val sessionDates: StateFlow<Set<String>> = sessionRepository.getAllSessionsFlow()
        .map { sessions -> sessions.map { it.date }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun toggleCalendar() {
        val isVisible = _calendarVisible.value
        if (isVisible) _selectedDate.value = null
        _calendarVisible.value = !isVisible
    }

    fun previousMonth() { _currentMonth.update { it.minusMonths(1) } }
    fun nextMonth() { _currentMonth.update { it.plusMonths(1) } }

    fun selectDate(date: String?) {
        _selectedDate.value = if (_selectedDate.value == date) null else date
    }

    fun triggerImportIfNeeded() {
        viewModelScope.launch {
            _importState.update { ImportState.Importing }
            val result = importRepository.importIfNeeded()
            _importState.update {
                if (result.isSuccess) ImportState.Done
                else ImportState.Error(result.exceptionOrNull()?.message ?: "Import failed")
            }
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            sessionRepository.deleteSession(sessionId)
        }
    }

    fun exportData() {
        viewModelScope.launch {
            runCatching { jsonExporter.export() }
                .onSuccess { uri -> _dataEvents.emit(DataEvent.Share(uri)) }
                .onFailure { _dataEvents.emit(DataEvent.SnackbarMessage("Error al exportar")) }
        }
    }

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            runCatching {
                context.contentResolver.openInputStream(uri)!!.use { stream ->
                    jsonImporter.import(stream, skipExisting = true)
                }
            }
            .onSuccess { _dataEvents.emit(DataEvent.SnackbarMessage("Importación completada")) }
            .onFailure { _dataEvents.emit(DataEvent.SnackbarMessage("Archivo no válido")) }
        }
    }

    sealed class ImportState {
        object Idle : ImportState()
        object Importing : ImportState()
        object Done : ImportState()
        data class Error(val message: String) : ImportState()
    }

    sealed class DataEvent {
        data class Share(val uri: Uri) : DataEvent()
        data class SnackbarMessage(val text: String) : DataEvent()
    }
}
