package com.example.weathergpt_android.domain.weather.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.weathergpt_android.core.network.WeatherProviderPreferences
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData
import com.example.weathergpt_android.domain.weather.model.WeatherProviderType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UnifiedWeatherRepository(private val context: Context) {
    private val openMeteoRepo = OpenMeteoRepository()
    private val openWeatherRepo = OpenWeatherRepository(context)

    private val prefs: SharedPreferences =
        context.getSharedPreferences("weathergpt_live_cache", Context.MODE_PRIVATE)

    fun getCachedWeather(): LiveWeatherData {
        val temp = prefs.getString("cached_temp", "24°") ?: "24°"
        val cond = prefs.getString("cached_cond", "Clear Sky") ?: "Clear Sky"
        val hl = prefs.getString("cached_hl", "H: 26°  L: 18°") ?: "H: 26°  L: 18°"
        val wind = prefs.getString("cached_wind", "14 km/h") ?: "14 km/h"
        val hum = prefs.getString("cached_hum", "52%") ?: "52%"
        val uv = prefs.getString("cached_uv", "3 (Mod)") ?: "3 (Mod)"
        val aqi = prefs.getString("cached_aqi", "34 (Good)") ?: "34 (Good)"

        return LiveWeatherData(
            temperature = temp,
            condition = cond,
            highLow = hl,
            windSpeed = wind,
            humidity = hum,
            uvIndex = uv,
            aqi = aqi,
            isLive = true
        )
    }

    private fun cacheWeather(data: LiveWeatherData) {
        prefs.edit()
            .putString("cached_temp", data.temperature)
            .putString("cached_cond", data.condition)
            .putString("cached_hl", data.highLow)
            .putString("cached_wind", data.windSpeed)
            .putString("cached_hum", data.humidity)
            .putString("cached_uv", data.uvIndex)
            .putString("cached_aqi", data.aqi)
            .putLong("cached_time", System.currentTimeMillis())
            .apply()
    }

    suspend fun fetchLiveWeather(
        latitude: Double,
        longitude: Double,
        forceProvider: WeatherProviderType? = null
    ): Result<LiveWeatherData> = withContext(Dispatchers.IO) {
        val selectedProvider = forceProvider ?: WeatherProviderPreferences.getSelectedProvider(context)

        val result = when (selectedProvider) {
            WeatherProviderType.OPEN_METEO -> {
                openMeteoRepo.fetchWeather(latitude, longitude)
            }
            WeatherProviderType.OPEN_WEATHER -> {
                val owResult = openWeatherRepo.fetchLiveWeather(latitude, longitude)
                if (owResult.isSuccess) {
                    owResult
                } else {
                    // Seamless fallback to Open-Meteo if OpenWeather fails
                    openMeteoRepo.fetchWeather(latitude, longitude)
                }
            }
        }

        result.onSuccess { liveData ->
            cacheWeather(liveData)
        }

        result
    }
}
