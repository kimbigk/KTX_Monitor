package com.example.ktxmonitor

import com.example.ktxmonitor.domain.model.AppSettings
import com.example.ktxmonitor.domain.model.WatchTarget
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchTargetSerializationTest {
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    @Test
    fun watchTarget_serialization_deserialization_success() {
        val target = WatchTarget(
            departureStation = "서울",
            arrivalStation = "부산",
            date = "2026-09-27",
            startTime = "12:00",
            endTime = "18:00",
            trainType = "KTX",
            seatType = "일반실",
            passengerCount = 2,
            enabled = true
        )

        val serialized = json.encodeToString(target)
        val deserialized = json.decodeFromString<WatchTarget>(serialized)

        assertEquals(target.departureStation, deserialized.departureStation)
        assertEquals(target.arrivalStation, deserialized.arrivalStation)
        assertEquals(target.date, deserialized.date)
        assertEquals(target.startTime, deserialized.startTime)
        assertEquals(target.endTime, deserialized.endTime)
        assertEquals(target.passengerCount, deserialized.passengerCount)
        assertTrue(deserialized.enabled)
    }

    @Test
    fun appSettings_serialization_deserialization_success() {
        val settings = AppSettings(
            checkIntervalSeconds = 60,
            notificationEnabled = true,
            soundEnabled = false,
            vibrateEnabled = true,
            backgroundMonitoringEnabled = true
        )

        val serialized = json.encodeToString(settings)
        val deserialized = json.decodeFromString<AppSettings>(serialized)

        assertEquals(60, deserialized.checkIntervalSeconds)
        assertTrue(deserialized.notificationEnabled)
        assertEquals(false, deserialized.soundEnabled)
        assertTrue(deserialized.vibrateEnabled)
    }
}
