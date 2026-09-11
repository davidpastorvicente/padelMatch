package com.davidpv.padelmatch.data.repository

import com.davidpv.padelmatch.data.model.SeasonFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SeasonFilterStore @Inject constructor() {
    private val _filter = MutableStateFlow<SeasonFilter?>(null)
    val filter: StateFlow<SeasonFilter?> = _filter.asStateFlow()

    fun initializeIfNeeded(filter: SeasonFilter) {
        if (_filter.value == null) _filter.value = filter
    }

    fun select(filter: SeasonFilter) {
        _filter.value = filter
    }
}
