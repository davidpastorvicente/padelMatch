package com.padelgroup.padelMatch.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.padelgroup.padelMatch.data.repository.SessionRepository
import com.padelgroup.padelMatch.data.repository.SessionWithDetails
import com.padelgroup.padelMatch.di.MainDispatcher
import com.padelgroup.padelMatch.ui.navigation.SessionDetailRoute
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

sealed class SessionDetailUiState {
    object Loading : SessionDetailUiState()
    object NotFound : SessionDetailUiState()
    data class Success(val session: SessionWithDetails) : SessionDetailUiState()
}

@HiltViewModel
class SessionDetailViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    savedStateHandle: SavedStateHandle,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val sessionId: Long = savedStateHandle.toRoute<SessionDetailRoute>().sessionId

    val uiState: StateFlow<SessionDetailUiState> = sessionRepository.getAllSessionsFlow()
        .map { sessions -> sessions.find { it.id == sessionId } }
        .map { session ->
            if (session == null) SessionDetailUiState.NotFound else SessionDetailUiState.Success(session)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SessionDetailUiState.Loading)

    private val _navBack = MutableSharedFlow<Unit>()
    val navBack: SharedFlow<Unit> = _navBack.asSharedFlow()

    fun deleteSession() {
        viewModelScope.launch(mainDispatcher) {
            sessionRepository.deleteSession(sessionId)
            _navBack.emit(Unit)
        }
    }
}
