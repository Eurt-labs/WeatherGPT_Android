package com.example.weathergpt_android.domain.notifications.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class WeatherNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val icon: ImageVector,
    val tint: Color,
    val bgTint: Color
)
