package com.example.ktxmonitor.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val checkIntervalSeconds: Int = 30,
    val notificationEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val backgroundMonitoringEnabled: Boolean = true
)
