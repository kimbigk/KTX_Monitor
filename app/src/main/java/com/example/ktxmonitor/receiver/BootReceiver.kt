package com.example.ktxmonitor.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.ktxmonitor.data.SettingsRepository
import com.example.ktxmonitor.data.WatchTargetRepository
import com.example.ktxmonitor.service.MonitoringForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settingsRepo = SettingsRepository(context)
                    val targetRepo = WatchTargetRepository(context)

                    val settings = settingsRepo.settings.first()
                    val targets = targetRepo.targets.first()

                    if (settings.backgroundMonitoringEnabled && targets.any { it.enabled }) {
                        MonitoringForegroundService.startService(context)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
