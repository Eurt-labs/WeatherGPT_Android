package com.example.weathergpt_android.domain.weather.model

data class LiveWeatherData(
    val temperature: String = "24°",
    val condition: String = "Clear Sky",
    val highLow: String = "H: 26°  L: 18°",
    val windSpeed: String = "14 km/h",
    val humidity: String = "52%",
    val uvIndex: String = "3 (Mod)",
    val aqi: String = "34 (Good)",
    val isLive: Boolean = false,
    val lastUpdatedTime: String = "Live"
) {
    companion object {
        val DEFAULT = LiveWeatherData()
    }
}
