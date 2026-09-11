package com.example.ktxmonitor.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.example.ktxmonitor.data.SettingsRepository
import com.example.ktxmonitor.data.WatchTargetRepository
import com.example.ktxmonitor.monitoring.MockMonitoringEngine
import com.example.ktxmonitor.notification.MonitoringNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MonitoringForegroundService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START_MONITORING"
        const val ACTION_STOP = "ACTION_STOP_MONITORING"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun startService(context: Context) {
            val intent = Intent(context, MonitoringForegroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MonitoringForegroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private lateinit var notificationManager: MonitoringNotificationManager
    private lateinit var targetRepository: WatchTargetRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var mockEngine: MockMonitoringEngine

    private var monitoringJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = MonitoringNotificationManager(this)
        targetRepository = WatchTargetRepository(this)
        settingsRepository = SettingsRepository(this)
        mockEngine = MockMonitoringEngine(targetRepository, notificationManager)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopMonitoring()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START, null -> {
                startMonitoringForeground()
            }
        }
        return START_STICKY
    }

    private fun startMonitoringForeground() {
        val initialNotification = notificationManager.buildServiceNotification(0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                MonitoringNotificationManager.SERVICE_NOTIFICATION_ID,
                initialNotification,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                } else {
                    0
                }
            )
        } else {
            startForeground(MonitoringNotificationManager.SERVICE_NOTIFICATION_ID, initialNotification)
        }

        _isRunning.value = true

        // 감시 대상 수 관찰 및 상단 노티피케이션 업데이트
        serviceScope.launch {
            targetRepository.targets.collect { targets ->
                val activeCount = targets.count { it.enabled }
                val updatedNotification = notificationManager.buildServiceNotification(activeCount)
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                nm.notify(MonitoringNotificationManager.SERVICE_NOTIFICATION_ID, updatedNotification)
            }
        }

        // 백그라운드 주기적 Mock 감시 루프
        startMockMonitoringLoop()
    }

    private fun startMockMonitoringLoop() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                val settings = settingsRepository.settings.first()
                val intervalMs = (settings.checkIntervalSeconds.coerceAtLeast(10)) * 1000L

                val targets = targetRepository.targets.first().filter { it.enabled }
                for (target in targets) {
                    mockEngine.performMockCheck(target)
                }

                delay(intervalMs)
            }
        }
    }

    private fun stopMonitoring() {
        _isRunning.value = false
        monitoringJob?.cancel()
        serviceJob.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        stopMonitoring()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
