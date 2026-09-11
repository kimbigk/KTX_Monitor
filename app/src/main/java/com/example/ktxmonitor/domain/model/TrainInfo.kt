package com.example.ktxmonitor.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TrainInfo(
    val trainNo: String,
    val trainType: String,
    val departureStation: String,
    val arrivalStation: String,
    val departureTime: String,
    val arrivalTime: String,
    val hasGeneralSeat: Boolean,
    val hasSpecialSeat: Boolean,
    val reservePossibleText: String
)
