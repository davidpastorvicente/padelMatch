package com.padelgroup.padelMatch.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.padelgroup.padelMatch.data.repository.SessionRepository
import com.padelgroup.padelMatch.data.repository.SessionWithDetails
import com.padelgroup.padelMatch.di.MainDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val sessionId: Long = checkNotNull(savedStateHandle["sessionId"])

    val session: StateFlow<SessionWithDetails?> = sessionRepository.getAllSessionsFlow()
        .map { sessions -> sessions.find { it.id == sessionId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _navBack = MutableSharedFlow<Unit>()
    val navBack: SharedFlow<Unit> = _navBack.asSharedFlow()

    fun deleteSession() {
        viewModelScope.launch(mainDispatcher) {
            sessionRepository.deleteSession(sessionId)
            _navBack.emit(Unit)
        }
    }
}
