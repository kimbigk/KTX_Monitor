# Phase 2: 실제 KTX 조회 방식 조사 및 연동 설계 보고서

> **문서 버전:** v1.0  
> **작성 일자:** 2026-09-11  
> **프로젝트:** KTX 취소표 감시 Android 앱  

---

## 1. 코레일 서비스 구조 및 실제 접근 테스트 조사

### 1.1 조사 개요
`PROJECT_PLAN.md`의 Phase 2 원칙에 따라 무분별한 요청을 방지하고, 코레일 공식 서비스(앱/웹)의 통신 구조, 보안 정책, 비로그인 조회 가능 여부 및 Android 환경에서의 제약을 조사했습니다.

### 1.2 실제 호출 테스트 결과 및 분석

#### (1) 코레일 모바일 API (`smart.letskorail.com`)
* **엔드포인트:** `https://smart.letskorail.com/classes/com.korail.mobile.seatMovie.ScheduleView`
* **방식:** HTTP GET / POST (JSON 반환 구조)
* **주요 파라미터:** `txtGoStart`, `txtGoEnd`, `txtGoAbrdDt`, `txtGoHour`, `selGoTrain`, `txtPsgFlg_1`, `Version`
* **실제 호출 테스트 결과:**
  ```json
  {
      "strResult": "FAIL",
      "h_msg_cd": "MACRO ERROR",
      "h_msg_txt": "원활한 서비스 이용을 위해 앱을 최신 버전으로 업데이트한 뒤 재실행 후 안정적인 환경에서 사용해 주시기 바랍니다."
  }
  ```
* **분석 결과:**
  * 코레일 측에서 최근 **'코레일+'** 통합 개편 및 매크로 방지 솔루션을 전면 도입했습니다.
  * 앱 버전 코드(`Version`)의 최신 여부 검증 및 요청 헤더, 비정상 클라이언트 패턴을 탐지하여 `MACRO ERROR`로 차단합니다.
  * 로그인 여부와 무관하게 **단순 HTTP 요청 수준에서 엄격한 필터링**이 작동함을 확인했습니다.

#### (2) 코레일 웹페이지 (`letskorail.com`)
* **승차권 예약 페이지:** `www.letskorail.com/ebizprd/EbizPrdTicketPr21111_i1.do`
* **방식:** HTML 폼 기반 POST 요청
* **실제 호출 테스트 결과:**
  * WAF(Web Application Firewall) 및 세션 쿠키 미보유 시 에러 페이지(`방문하시려는 페이지의 주소가 잘못 입력되었거나...`)로 즉시 리다이렉트 처리됨.
  * HTML 구조 파싱 방식은 유지보수성이 매우 취약하며 서버 레이어의 접근 차단 위험이 높음.

---

## 2. 접근 방식 비교 및 평가

| 방식 | 설명 | 장점 | 단점 / 위험 | 적합 여부 |
| :--- | :--- | :--- | :--- | :---: |
| **방법 A: 앱 내부 직접 HTTP 요청** | 앱 내부에서 코레일 모바일 엔드포인트로 직접 HTTP 호출 | • 배터리/네트워크 리소스 최소화<br>• 백그라운드 백오프 제어 용이 | • 코레일 매크로 방지 솔루션(`MACRO ERROR`)에 취약<br>• 주기적 버전 검증 필요 | **부분 채택 (기본 엔진)** |
| **방법 B: Android WebView 기반 접근** | 안드로이드 내장 WebView에서 실제 브라우징 환경으로 조회 | • 정규 브라우저 엔진이므로 WAF 우회 가능<br>• 세션/쿠키 자동 관리 | • 백그라운드 메모리/CPU 점유율 높음<br>• DOM 파싱 구조 변경 취약 | **대체 옵션 (Fallback)** |
| **방법 C: 사용자 중심 하이브리드 연동** | 감시 알림 발생 시 코레일톡 공식 앱 딥링크(`korail://`) 또는 모바일 웹으로 즉시 연결 | • 보안/약관 위험 제로<br>• 최종 결제/예약은 사용자가 직접 수행<br>• 가장 빠르고 안전한 예매 흐름 | • 감시 자체는 백그라운드 엔진이 필요함 | **필수 채택 (알림 연동)** |

### 2.1 최종 채택 방식
1. **모니터링 계층:** 추상화된 `MonitoringEngine` 인터페이스를 정의하고, 1차적으로 공식 모바일 파라미터를 갖춘 HTTP 조회 엔진을 구성하되, 오류/차단 발생 시 점진적 백오프(Backoff) 및 상태 안내를 제공합니다.
2. **연계 계층 (방법 C 결합):** 사용자가 잔여석 알림을 터치했을 때, 코레일 공식 앱(코레일+)이 스마트폰에 설치되어 있으면 **코레일톡 앱으로 원클릭 딥링크 실행**, 미설치 시 **모바일 공식 예매 웹(`m.letskorail.com`)**으로 즉시 이동하여 사용자가 가장 빠르고 안전하게 취소표를 직접 예매할 수 있도록 구현합니다.
3. **안전 원칙 준수:** CAPTCHA 우회나 anti-bot 해킹 등의 무리한 침해 기술은 일체 사용하지 않으며, 감시 주기에 랜덤 지연(Jitter) 및 안전 백오프를 적용합니다.

---

## 3. MonitoringEngine 인터페이스 및 데이터 모델 확정

### 3.1 인터페이스 명세
```kotlin
interface MonitoringEngine {
    /**
     * 감시 대상(target)에 대해 1회 잔여석 조회를 수행합니다.
     */
    suspend fun check(target: WatchTarget): CheckResult
}
```

### 3.2 결과 데이터 모델
```kotlin
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
```

### 3.3 열차 정보 모델 (`TrainInfo`)
```kotlin
data class TrainInfo(
    val trainNo: String,          // 예: "00103"
    val trainType: String,        // 예: "KTX", "KTX-산천"
    val departureStation: String, // 예: "서울"
    val arrivalStation: String,   // 예: "부산"
    val departureTime: String,    // 예: "14:20"
    val arrivalTime: String,      // 예: "16:58"
    val hasGeneralSeat: Boolean,  // 일반실 예약 가능 여부
    val hasSpecialSeat: Boolean,  // 특실 예약 가능 여부
    val reservePossibleText: String // "예약가능", "매진", "입석" 등
)
```
