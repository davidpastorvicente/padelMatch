package com.padelgroup.padelMatch.data.repository

import android.content.Context
import androidx.core.content.edit
import com.padelgroup.padelMatch.data.importer.JsonImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportRepository @Inject constructor(
    private val context: Context,
    private val jsonImporter: JsonImporter
) {
    private val prefs = context.getSharedPreferences("padel_prefs", Context.MODE_PRIVATE)
    private val KEY_IMPORTED = "json_imported"

    suspend fun importIfNeeded(): Result<Unit> = withContext(Dispatchers.IO) {
        if (prefs.getBoolean(KEY_IMPORTED, false)) return@withContext Result.success(Unit)
        // Mark as done immediately — app starts empty, data imported via the in-app import menu
        prefs.edit { putBoolean(KEY_IMPORTED, true) }
        Result.success(Unit)
    }
}
