package com.davidpv.padelmatch.ui.history

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davidpv.padelmatch.data.exporter.JsonExporter
import com.davidpv.padelmatch.data.importer.JsonImporter
import com.davidpv.padelmatch.data.repository.ImportRepository
import com.davidpv.padelmatch.data.repository.SessionRepository
import com.davidpv.padelmatch.data.repository.SessionWithDetails
import com.davidpv.padelmatch.di.MainDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

sealed class MatchHistoryUiState {
    object Loading : MatchHistoryUiState()
    object Empty : MatchHistoryUiState()
    data class Success(val sessions: List<SessionWithDetails>) : MatchHistoryUiState()
    data class Error(val message: String) : MatchHistoryUiState()
}

@HiltViewModel
class MatchHistoryViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    private val importRepository: ImportRepository,
    private val jsonExporter: JsonExporter,
    private val jsonImporter: JsonImporter,
    @param:ApplicationContext private val context: Context,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState.asStateFlow()

    private val _dataEvents = MutableSharedFlow<DataEvent>()
    val dataEvents: SharedFlow<DataEvent> = _dataEvents.asSharedFlow()

    val uiState: StateFlow<MatchHistoryUiState> = sessionRepository.getAllSessionsFlow()
        .map { sessions ->
            if (sessions.isEmpty()) MatchHistoryUiState.Empty
            else MatchHistoryUiState.Success(sessions)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MatchHistoryUiState.Loading)

    init {
        viewModelScope.launch(mainDispatcher) {
            var prevCount = -1
            uiState.collect { state ->
                val count = (state as? MatchHistoryUiState.Success)?.sessions?.size ?: return@collect
                if (prevCount in 0..<count) {
                    _dataEvents.emit(DataEvent.ScrollToTop)
                }
                prevCount = count
            }
        }
    }

    // Calendar state
    private val _calendarVisible = MutableStateFlow(false)
    val calendarVisible: StateFlow<Boolean> = _calendarVisible.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

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
        viewModelScope.launch(mainDispatcher) {
            _importState.update { ImportState.Importing }
            val result = importRepository.importIfNeeded()
            _importState.update {
                if (result.isSuccess) ImportState.Done
                else ImportState.Error(result.exceptionOrNull()?.message ?: "Error al importar")
            }
        }
    }

    fun exportData() {
        viewModelScope.launch(mainDispatcher) {
            runCatching { jsonExporter.export() }
                .onSuccess { uri -> _dataEvents.emit(DataEvent.Share(uri)) }
                .onFailure { _dataEvents.emit(DataEvent.ToastMessage("Error al exportar")) }
        }
    }

    fun importFromUri(uri: Uri) {
        viewModelScope.launch(mainDispatcher) {
            runCatching {
                context.contentResolver.openInputStream(uri)!!.use { stream ->
                    jsonImporter.import(stream, skipExisting = true)
                }
            }
            .onSuccess { _dataEvents.emit(DataEvent.ToastMessage("Importación completada")) }
            .onFailure { _dataEvents.emit(DataEvent.ToastMessage("Archivo no válido")) }
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
        data class ToastMessage(val text: String) : DataEvent()
        object ScrollToTop : DataEvent()
    }
}
