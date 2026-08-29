package com.example.weathergpt_android.core.network

import android.content.Context
import android.content.SharedPreferences
import com.example.weathergpt_android.domain.weather.model.WeatherProviderType

object WeatherProviderPreferences {
    private const val PREFS_NAME = "weather_provider_prefs"
    private const val KEY_PROVIDER = "selected_provider"

    fun getSelectedProvider(context: Context): WeatherProviderType {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_PROVIDER, WeatherProviderType.OPEN_WEATHER.name) ?: WeatherProviderType.OPEN_WEATHER.name
        return try {
            WeatherProviderType.valueOf(name)
        } catch (e: Exception) {
            WeatherProviderType.OPEN_WEATHER
        }
    }

    fun saveSelectedProvider(context: Context, provider: WeatherProviderType) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PROVIDER, provider.name).apply()
    }
}
