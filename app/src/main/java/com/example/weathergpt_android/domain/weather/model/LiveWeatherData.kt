package com.example.weathergpt_android.domain.weather.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.WbSunny

data class LiveWeatherData(
    val temperature: String = "32°",
    val condition: String = "Mostly Cloudy",
    val highLow: String = "H: 34°  L: 27°",
    val windSpeed: String = "6 km/h",
    val humidity: String = "59%",
    val uvIndex: String = "7 (High)",
    val aqi: String = "64 (Moderate)",
    val isLive: Boolean = false,
    val lastUpdatedTime: String = "Live",
    val hourlyList: List<HourlyForecast> = defaultHourlyList(),
    val dailyList: List<DayForecast> = defaultDailyList(),
    // Multi-Sector Meteorological Intelligence (SIH 2026)
    val soilMoisture: String = "0.33 m³/m³",
    val soilTemperature: String = "28°C",
    val irrigationAdvice: String = "Adequate soil moisture. Irrigation not required today.",
    val floodRiskLevel: String = "Normal (Low Risk)",
    val riverDischarge: String = "12.5 m³/s",
    val visibilityKm: String = "10.0 km",
    val pastRainfallTrend: String = "21.7 mm in past 3 days",
    val heatwaveAlert: String = "None"
) {
    companion object {
        val DEFAULT = LiveWeatherData()

        fun defaultHourlyList(): List<HourlyForecast> = listOf(
            HourlyForecast("Now", "32°", Icons.Rounded.Cloud, isNow = true),
            HourlyForecast("8:00 pm", "31°", Icons.Rounded.Cloud),
            HourlyForecast("9:00 pm", "31°", Icons.Rounded.Cloud),
            HourlyForecast("10:00 pm", "30°", Icons.Rounded.NightsStay),
            HourlyForecast("11:00 pm", "30°", Icons.Rounded.NightsStay),
            HourlyForecast("12:00 am", "29°", Icons.Rounded.NightsStay),
            HourlyForecast("1:00 am", "28°", Icons.Rounded.NightsStay)
        )

        fun defaultDailyList(): List<DayForecast> = listOf(
            DayForecast("Today", Icons.Rounded.Cloud, "27°", "34°", 0.75f),
            DayForecast("Tomorrow", Icons.Rounded.Cloud, "27°", "34°", 0.75f),
            DayForecast("Mon", Icons.Rounded.WbSunny, "27°", "35°", 0.80f),
            DayForecast("Tue", Icons.Rounded.Cloud, "26°", "31°", 0.60f),
            DayForecast("Wed", Icons.Rounded.Cloud, "26°", "30°", 0.55f),
            DayForecast("Thu", Icons.Rounded.WbSunny, "26°", "31°", 0.60f),
            DayForecast("Fri", Icons.Rounded.Cloud, "25°", "30°", 0.55f)
        )
    }
}
