package com.example.ktxmonitor.domain.model

sealed interface CheckResult {
    data class Success(
        val trains: List<TrainInfo>,
        val checkedAt: Long = System.currentTimeMillis()
    ) : CheckResult

    data class NoResult(
        val message: String = "해당 조건의 운행 열차가 없습니다.",
        val checkedAt: Long = System.currentTimeMillis()
    ) : CheckResult

    data class Error(
        val message: String,
        val errorCode: String? = null,
        val checkedAt: Long = System.currentTimeMillis()
    ) : CheckResult
}
