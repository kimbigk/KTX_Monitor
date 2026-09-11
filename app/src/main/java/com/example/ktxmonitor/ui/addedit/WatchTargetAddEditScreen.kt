package com.example.ktxmonitor.ui.addedit

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ktxmonitor.data.WatchTargetRepository
import com.example.ktxmonitor.domain.model.WatchTarget
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val POPULAR_STATIONS = listOf("서울", "용산", "광명", "대전", "동대구", "부산", "광주송정", "포항", "강릉")
private val TRAIN_TYPES = listOf("전체", "KTX", "KTX-산천")
private val SEAT_TYPES = listOf("전체", "일반실", "특실")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WatchTargetAddEditScreen(
    targetId: String?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { WatchTargetRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val cal = Calendar.getInstance()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
    val today = sdf.format(cal.time)
    cal.add(Calendar.DAY_OF_YEAR, 1)
    val tomorrow = sdf.format(cal.time)

    var departureStation by remember { mutableStateOf("서울") }
    var arrivalStation by remember { mutableStateOf("부산") }
    var date by remember { mutableStateOf(today) }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("18:00") }
    var trainType by remember { mutableStateOf("KTX") }
    var seatType by remember { mutableStateOf("일반실") }
    var passengerCount by remember { mutableIntStateOf(1) }
    var isEditMode by remember { mutableStateOf(false) }

    LaunchedEffect(targetId) {
        if (targetId != null) {
            val existing = repository.getTarget(targetId)
            if (existing != null) {
                isEditMode = true
                departureStation = existing.departureStation
                arrivalStation = existing.arrivalStation
                date = existing.date
                startTime = existing.startTime
                endTime = existing.endTime
                trainType = existing.trainType
                seatType = existing.seatType
                passengerCount = existing.passengerCount
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "감시 대상 수정" else "감시 대상 추가") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. 출발역
            Column {
                Text("출발역", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = departureStation,
                    onValueChange = { departureStation = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    POPULAR_STATIONS.take(6).forEach { station ->
                        FilterChip(
                            selected = departureStation == station,
                            onClick = { departureStation = station },
                            label = { Text(station) }
                        )
                    }
                }
            }

            // 2. 도착역
            Column {
                Text("도착역", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = arrivalStation,
                    onValueChange = { arrivalStation = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    POPULAR_STATIONS.take(6).forEach { station ->
                        FilterChip(
                            selected = arrivalStation == station,
                            onClick = { arrivalStation = station },
                            label = { Text(station) }
                        )
                    }
                }
            }

            // 3. 탑승 날짜
            Column {
                Text("탑승 날짜 (YYYY-MM-DD)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = date == today,
                        onClick = { date = today },
                        label = { Text("오늘 ($today)") }
                    )
                    FilterChip(
                        selected = date == tomorrow,
                        onClick = { date = tomorrow },
                        label = { Text("내일 ($tomorrow)") }
                    )
                }
            }

            // 4. 시간 범위 (시작 ~ 종료)
            Column {
                Text("시간대 범위 (HH:mm)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("시작 시간") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("종료 시간") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            // 5. 열차 종류
            Column {
                Text("열차 종류", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TRAIN_TYPES.forEach { type ->
                        FilterChip(
                            selected = trainType == type,
                            onClick = { trainType = type },
                            label = { Text(type) }
                        )
                    }
                }
            }

            // 6. 좌석 등급
            Column {
                Text("좌석 등급", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SEAT_TYPES.forEach { type ->
                        FilterChip(
                            selected = seatType == type,
                            onClick = { seatType = type },
                            label = { Text(type) }
                        )
                    }
                }
            }

            // 7. 승객 수
            Column {
                Text("승객 수", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (passengerCount > 1) passengerCount-- }
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "감소")
                    }
                    Text(
                        text = "$passengerCount 명",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedButton(
                        onClick = { if (passengerCount < 9) passengerCount++ }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "증가")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8. 저장 버튼
            Button(
                onClick = {
                    if (departureStation.isBlank() || arrivalStation.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("출발역과 도착역을 모두 입력해주세요.")
                        }
                        return@Button
                    }
                    if (departureStation.trim() == arrivalStation.trim()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("출발역과 도착역이 같을 수 없습니다.")
                        }
                        return@Button
                    }

                    scope.launch {
                        if (isEditMode && targetId != null) {
                            val existing = repository.getTarget(targetId)
                            if (existing != null) {
                                repository.updateTarget(
                                    existing.copy(
                                        departureStation = departureStation.trim(),
                                        arrivalStation = arrivalStation.trim(),
                                        date = date.trim(),
                                        startTime = startTime.trim(),
                                        endTime = endTime.trim(),
                                        trainType = trainType,
                                        seatType = seatType,
                                        passengerCount = passengerCount
                                    )
                                )
                            }
                        } else {
                            val newTarget = WatchTarget(
                                departureStation = departureStation.trim(),
                                arrivalStation = arrivalStation.trim(),
                                date = date.trim(),
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                trainType = trainType,
                                seatType = seatType,
                                passengerCount = passengerCount,
                                enabled = true
                            )
                            repository.addTarget(newTarget)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (isEditMode) "수정 완료" else "감시 대상 등록", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
