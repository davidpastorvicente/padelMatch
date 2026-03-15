package com.padelgroup.padelMatch.data.repository

import com.padelgroup.padelMatch.data.db.dao.SessionDao
import com.padelgroup.padelMatch.data.db.entity.SessionEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepository @Inject constructor(
    private val sessionDao: SessionDao
) {
    fun getAllSessionDates(): Flow<List<String>> = sessionDao.getAllSessionDates()

    suspend fun getSessionForDate(date: String): SessionEntity? = sessionDao.getSessionByDate(date)
}
