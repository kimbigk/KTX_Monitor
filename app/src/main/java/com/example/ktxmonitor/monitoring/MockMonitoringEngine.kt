package com.example.ktxmonitor.monitoring

import com.example.ktxmonitor.data.WatchTargetRepository
import com.example.ktxmonitor.domain.model.WatchTarget
import com.example.ktxmonitor.notification.MonitoringNotificationManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MockMonitoringEngine(
    private val repository: WatchTargetRepository,
    private val notificationManager: MonitoringNotificationManager
) {
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.KOREA)

    /**
     * 개발용 테스트 버튼: 가상 좌석 발견 이벤트를 즉시 트리거하여 알림을 발송하고 상태를 갱신합니다.
     */
    suspend fun triggerTestSeatFound(target: WatchTarget) {
        val now = System.currentTimeMillis()
        val formattedTime = timeFormat.format(Date(now))
        val sampleTrain = "${target.startTime} KTX 103열차"
        val statusText = "좌석 발견 ($formattedTime)"

        val updated = target.copy(
            lastCheckedAt = now,
            lastStatus = statusText,
            lastFoundTrain = sampleTrain
        )
        repository.updateTarget(updated)

        notificationManager.notifySeatFound(
            target = updated,
            trainInfo = "$sampleTrain ${target.seatType} 잔여석"
        )
    }

    /**
     * 주기적 가상 검사 시뮬레이션
     */
    suspend fun performMockCheck(target: WatchTarget) {
        val now = System.currentTimeMillis()
        val formattedTime = timeFormat.format(Date(now))
        val updated = target.copy(
            lastCheckedAt = now,
            lastStatus = "조회 완료 ($formattedTime, 잔여 없음)"
        )
        repository.updateTarget(updated)
    }
}
