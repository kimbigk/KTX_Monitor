package com.example.ktxmonitor.data

import android.content.Context
import com.example.ktxmonitor.domain.model.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class SettingsRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val file by lazy { File(context.filesDir, "app_settings.json") }
    private val mutex = Mutex()
    private val _settings = MutableStateFlow(AppSettings())
    val settings: Flow<AppSettings> = _settings.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            loadSettings()
        }
    }

    private suspend fun loadSettings() = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                if (file.exists()) {
                    val content = file.readText()
                    _settings.value = json.decodeFromString<AppSettings>(content)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun updateSettings(newSettings: AppSettings) = mutex.withLock {
        _settings.value = newSettings
        withContext(Dispatchers.IO) {
            try {
                file.writeText(json.encodeToString(newSettings))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
