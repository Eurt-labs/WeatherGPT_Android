package com.example.weathergpt_android.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Feed
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavTab(
    val title: String,
    val icon: ImageVector,
    val route: String
) {
    WEATHER("Weather", Icons.Rounded.Cloud, "weather"),
    NEWS("News", Icons.AutoMirrored.Rounded.Feed, "news"),
    VOICE_AI("Voice AI", Icons.Rounded.Mic, "voice_ai"),
    GPT("WeatherGPT", Icons.Rounded.AutoAwesome, "gpt"),
    SETTINGS("Settings", Icons.Rounded.Settings, "settings");

    companion object {
        val entriesList = entries
    }
}
