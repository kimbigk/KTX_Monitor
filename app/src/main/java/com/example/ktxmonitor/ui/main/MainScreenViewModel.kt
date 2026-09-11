package com.example.ktxmonitor.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ktxmonitor.data.WatchTargetRepository
import com.example.ktxmonitor.domain.model.WatchTarget
import com.example.ktxmonitor.monitoring.MockMonitoringEngine
import com.example.ktxmonitor.notification.MonitoringNotificationManager
import com.example.ktxmonitor.service.MonitoringForegroundService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    val targets: List<WatchTarget> = emptyList(),
    val isServiceRunning: Boolean = false,
    val isLoading: Boolean = false
)

class MainScreenViewModel(
    private val repository: WatchTargetRepository,
    private val notificationManager: MonitoringNotificationManager
) : ViewModel() {

    private val mockEngine = MockMonitoringEngine(repository, notificationManager)

    val uiState: StateFlow<MainUiState> = combine(
        repository.targets,
        MonitoringForegroundService.isRunning
    ) { targets, isRunning ->
        MainUiState(
            targets = targets,
            isServiceRunning = isRunning,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        MainUiState(isLoading = true)
    )

    fun toggleTarget(id: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleTarget(id, enabled)
        }
    }

    fun deleteTarget(id: String) {
        viewModelScope.launch {
            repository.deleteTarget(id)
        }
    }

    fun triggerTestFound(target: WatchTarget) {
        viewModelScope.launch {
            mockEngine.triggerTestSeatFound(target)
        }
    }

    fun toggleService(context: Context, enable: Boolean) {
        if (enable) {
            MonitoringForegroundService.startService(context)
        } else {
            MonitoringForegroundService.stopService(context)
        }
    }
}
