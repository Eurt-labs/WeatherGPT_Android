package com.example.weathergpt_android.domain.weather.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material.icons.rounded.WbTwilight
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

data class WeatherMetrics(
    val windSpeed: String,
    val humidity: String,
    val uvIndex: String,
    val airQuality: String
)

enum class WeatherScenario(
    val label: String,
    val status: String,
    val suggestion: String,
    val temp: String,
    val condition: String,
    val highLow: String,
    val wind: String,
    val humidity: String,
    val uv: String,
    val aqi: String
) {
    SUNNY(
        label = "☀️ Sunny (Default)",
        status = "today's weather is good",
        suggestion = "Go for a walk around the park. Evening temperature will be a pleasant 22°C.",
        temp = "24°",
        condition = "Clear Sky & Gentle Breeze",
        highLow = "H: 26°  L: 17°",
        wind = "14 km/h",
        humidity = "48%",
        uv = "2 (Low)",
        aqi = "34 (Good)"
    ),
    BREEZY(
        label = "🍃 Mild Breeze",
        status = "today's weather is fresh & clear",
        suggestion = "Great conditions for cycling or jogging along the coastline before 6 PM.",
        temp = "21°",
        condition = "Partly Cloudy with Coastal Breeze",
        highLow = "H: 23°  L: 16°",
        wind = "22 km/h",
        humidity = "55%",
        uv = "3 (Moderate)",
        aqi = "28 (Excellent)"
    ),
    RAIN(
        label = "🌧️ Light Showers",
        status = "showers expected in afternoon",
        suggestion = "Carry an umbrella if heading out. Enjoy indoor activities and coffee!",
        temp = "18°",
        condition = "Passing Rain Showers",
        highLow = "H: 20°  L: 15°",
        wind = "18 km/h",
        humidity = "82%",
        uv = "1 (Low)",
        aqi = "22 (Clean)"
    ),
    SUNSET(
        label = "🌇 Golden Hour",
        status = "peaceful golden evening",
        suggestion = "Perfect rooftop view or sunset stroll. Warm light until 7:12 PM.",
        temp = "23°",
        condition = "Golden Twilight & Calm Air",
        highLow = "H: 25°  L: 18°",
        wind = "10 km/h",
        humidity = "50%",
        uv = "1 (Low)",
        aqi = "30 (Good)"
    )
}
