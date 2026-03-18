package com.padelgroup.padelMatch.data.repository

import android.content.Context
import androidx.core.content.edit
import com.padelgroup.padelMatch.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportRepository @Inject constructor(
    context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {
    private val prefs = context.getSharedPreferences("padel_prefs", Context.MODE_PRIVATE)
    private val key = "json_imported"

    suspend fun importIfNeeded(): Result<Unit> = withContext(ioDispatcher) {
        if (prefs.getBoolean(key, false)) return@withContext Result.success(Unit)
        // Mark as done immediately — app starts empty, data imported via the in-app import menu
        prefs.edit { putBoolean(key, true) }
        Result.success(Unit)
    }
}
