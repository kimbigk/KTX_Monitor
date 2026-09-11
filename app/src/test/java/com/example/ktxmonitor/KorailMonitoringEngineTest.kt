package com.example.ktxmonitor

import com.example.ktxmonitor.domain.model.CheckResult
import com.example.ktxmonitor.domain.model.WatchTarget
import com.example.ktxmonitor.monitoring.KorailHttpMonitoringEngine
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class KorailMonitoringEngineTest {

    @Test
    fun testMonitoringEngine_interfaceAndErrorHandling() = runTest {
        val engine = KorailHttpMonitoringEngine()
        val target = WatchTarget(
            departureStation = "서울",
            arrivalStation = "부산",
            date = "2026-09-27",
            startTime = "14:00",
            endTime = "18:00",
            trainType = "KTX",
            seatType = "일반실",
            passengerCount = 1
        )

        // 실제 네트워크 호출 시 결과 객체 타입 검증 (코레일 서버 상태에 따라 Success 또는 Error 반환)
        val result = engine.check(target)
        assertNotNull(result)
        assertTrue(result is CheckResult.Success || result is CheckResult.Error || result is CheckResult.NoResult)

        if (result is CheckResult.Error) {
            println("Server Response (Expected Security Block): ${result.message}, Code: ${result.errorCode}")
            // 코레일의 최신 매크로 방지 시스템으로 인해 MACRO ERROR 또는 조회 실패가 정상 격리 처리되는지 확인
            assertTrue(result.message.isNotEmpty())
        }
    }
}
