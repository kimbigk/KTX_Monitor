package com.example.ktxmonitor.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

interface DataRepository {
  val data: Flow<List<String>>
}

class DefaultDataRepository : DataRepository {
  override val data: Flow<List<String>> = flow {
    emit(
      listOf(
        "KTX 취소표 감시 앱",
        "Phase 0: 프로젝트 초기화 완료",
        "준비 상태: 빌드 및 환경 구성 성공"
      )
    )
  }
}
