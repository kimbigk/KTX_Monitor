package com.example.ktxmonitor

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey

@Serializable data class AddEdit(val targetId: String? = null) : NavKey

@Serializable data object Settings : NavKey
