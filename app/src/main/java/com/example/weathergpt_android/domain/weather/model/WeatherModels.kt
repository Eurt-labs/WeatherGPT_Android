package com.example.weathergpt_android.domain.weather.model

import androidx.compose.ui.graphics.vector.ImageVector

data class HourlyForecast(
    val time: String,
    val temp: String,
    val icon: ImageVector,
    val isNow: Boolean = false
)

data class DayForecast(
    val day: String,
    val condition: ImageVector,
    val minTemp: String,
    val maxTemp: String,
    val progress: Float
)
