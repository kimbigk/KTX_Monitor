package com.example.ktxmonitor.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class WatchTarget(
    val id: String = UUID.randomUUID().toString(),
    val departureStation: String,
    val arrivalStation: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val trainType: String = "KTX",
    val seatType: String = "일반실",
    val passengerCount: Int = 1,
    val enabled: Boolean = true,
    val lastCheckedAt: Long? = null,
    val lastStatus: String = "대기 중",
    val lastFoundTrain: String? = null
)
