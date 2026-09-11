package com.example.ktxmonitor.monitoring

import com.example.ktxmonitor.domain.model.CheckResult
import com.example.ktxmonitor.domain.model.WatchTarget

interface MonitoringEngine {
    /**
     * 지정된 감시 대상(target)의 열차 좌석 상태를 1회 조회합니다.
     */
    suspend fun check(target: WatchTarget): CheckResult
}
