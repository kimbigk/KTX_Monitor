package com.example.ktxmonitor.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ktxmonitor.data.SettingsRepository
import kotlinx.coroutines.launch

private val INTERVAL_OPTIONS = listOf(
    10 to "10초 (테스트)",
    30 to "30초",
    60 to "1분",
    120 to "2분",
    300 to "5분"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context.applicationContext) }
    val settings by repository.settings.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("설정") },
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
        if (settings != null) {
            val currentSettings = settings!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 1. 감시 주기
                Column {
                    Text("감시 주기", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "좌석 상태를 확인하는 반복 주기입니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        INTERVAL_OPTIONS.forEach { (seconds, label) ->
                            FilterChip(
                                selected = currentSettings.checkIntervalSeconds == seconds,
                                onClick = {
                                    scope.launch {
                                        repository.updateSettings(
                                            currentSettings.copy(checkIntervalSeconds = seconds)
                                        )
                                    }
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                // 2. 알림 설정
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("알림 설정", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("알림 받기", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "좌석 발견 시 즉시 알림 발송",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = currentSettings.notificationEnabled,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    repository.updateSettings(
                                        currentSettings.copy(notificationEnabled = checked)
                                    )
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("진동 알림", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = currentSettings.vibrateEnabled,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    repository.updateSettings(
                                        currentSettings.copy(vibrateEnabled = checked)
                                    )
                                }
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("소리 알림", style = MaterialTheme.typography.bodyLarge)
                        Switch(
                            checked = currentSettings.soundEnabled,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    repository.updateSettings(
                                        currentSettings.copy(soundEnabled = checked)
                                    )
                                }
                            }
                        )
                    }
                }

                // 3. 백그라운드 감시
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("백그라운드 실행", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("백그라운드 지속 감시", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "화면을 끄거나 다른 앱을 사용할 때도 감시 유지",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = currentSettings.backgroundMonitoringEnabled,
                            onCheckedChange = { checked ->
                                scope.launch {
                                    repository.updateSettings(
                                        currentSettings.copy(backgroundMonitoringEnabled = checked)
                                    )
                                }
                            }
                        )
                    }
                }

                // 4. 배터리 최적화 안내 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(top = 2.dp, end = 12.dp)
                        )
                        Column {
                            Text(
                                text = "배터리 최적화 예외 안내",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "스마트폰 화면이 꺼진 상태에서도 감시가 중단되지 않으려면 시스템 설정 > 앱 > KTX Monitor > 배터리 메뉴에서 [제한 없음]으로 설정하는 것을 권장합니다.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
