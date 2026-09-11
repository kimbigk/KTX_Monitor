package com.example.ktxmonitor.data

import android.content.Context
import com.example.ktxmonitor.domain.model.WatchTarget
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WatchTargetRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }
    private val file by lazy { File(context.filesDir, "watch_targets.json") }
    private val mutex = Mutex()
    private val _targets = MutableStateFlow<List<WatchTarget>>(emptyList())
    val targets: Flow<List<WatchTarget>> = _targets.asStateFlow()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            loadTargets()
        }
    }

    private suspend fun loadTargets() = mutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                if (file.exists()) {
                    val content = file.readText()
                    val list = json.decodeFromString<List<WatchTarget>>(content)
                    _targets.value = list
                } else {
                    val cal = Calendar.getInstance()
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                    val today = sdf.format(cal.time)
                    val defaultTarget = WatchTarget(
                        departureStation = "서울",
                        arrivalStation = "부산",
                        date = today,
                        startTime = "12:00",
                        endTime = "18:00",
                        trainType = "KTX",
                        seatType = "일반실",
                        passengerCount = 1,
                        enabled = true,
                        lastStatus = "대기 중"
                    )
                    _targets.value = listOf(defaultTarget)
                    saveToFileLocked(_targets.value)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun saveToFileLocked(list: List<WatchTarget>) {
        withContext(Dispatchers.IO) {
            try {
                val content = json.encodeToString(list)
                file.writeText(content)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun addTarget(target: WatchTarget) = mutex.withLock {
        val updated = _targets.value + target
        _targets.value = updated
        saveToFileLocked(updated)
    }

    suspend fun updateTarget(target: WatchTarget) = mutex.withLock {
        val updated = _targets.value.map { if (it.id == target.id) target else it }
        _targets.value = updated
        saveToFileLocked(updated)
    }

    suspend fun deleteTarget(id: String) = mutex.withLock {
        val updated = _targets.value.filterNot { it.id == id }
        _targets.value = updated
        saveToFileLocked(updated)
    }

    suspend fun toggleTarget(id: String, enabled: Boolean) = mutex.withLock {
        val updated = _targets.value.map {
            if (it.id == id) it.copy(enabled = enabled) else it
        }
        _targets.value = updated
        saveToFileLocked(updated)
    }

    fun getTarget(id: String): WatchTarget? {
        return _targets.value.find { it.id == id }
    }
}
