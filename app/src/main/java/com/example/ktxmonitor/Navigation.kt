package com.example.ktxmonitor

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.ktxmonitor.ui.addedit.WatchTargetAddEditScreen
import com.example.ktxmonitor.ui.main.MainScreen
import com.example.ktxmonitor.ui.settings.SettingsScreen

@Composable
fun MainNavigation() {
    val backStack = rememberNavBackStack(Main)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Main> {
                MainScreen(
                    onNavigateToAddEdit = { targetId -> backStack.add(AddEdit(targetId)) },
                    onNavigateToSettings = { backStack.add(Settings) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<AddEdit> { key ->
                WatchTargetAddEditScreen(
                    targetId = key.targetId,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<Settings> {
                SettingsScreen(
                    onNavigateBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
}
