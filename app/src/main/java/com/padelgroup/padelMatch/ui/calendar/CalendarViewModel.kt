package com.padelgroup.padelMatch.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padelgroup.padelMatch.data.repository.CalendarRepository
import com.padelgroup.padelMatch.data.repository.SessionRepository
import com.padelgroup.padelMatch.data.repository.SessionWithDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    val sessionDates: StateFlow<Set<String>> = calendarRepository.getAllSessionDates()
        .map { it.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    private val _selectedDate = MutableStateFlow<String?>(null)
    val selectedDate: StateFlow<String?> = _selectedDate.asStateFlow()

    val selectedSession: StateFlow<SessionWithDetails?> = _selectedDate
        .flatMapLatest { date ->
            if (date == null) flowOf(null)
            else sessionRepository.getAllSessionsFlow()
                .map { sessions -> sessions.find { it.date == date } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun previousMonth() { _currentMonth.update { it.minusMonths(1) } }
    fun nextMonth() { _currentMonth.update { it.plusMonths(1) } }
    fun selectDate(date: String?) { _selectedDate.value = date }
}
