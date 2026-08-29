package com.example.weathergpt_android.domain.weather.model

enum class WeatherProviderType(
    val title: String,
    val subtitle: String,
    val requiresApiKey: Boolean
) {
    OPEN_METEO(
        title = "Open-Meteo (Free)",
        subtitle = "Zero-config, keyless high-precision weather",
        requiresApiKey = false
    ),
    OPEN_WEATHER(
        title = "OpenWeatherMap",
        subtitle = "Requires custom OpenWeather API key",
        requiresApiKey = true
    )
}
