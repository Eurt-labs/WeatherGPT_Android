package com.example.weathergpt_android.core.network

import android.content.Context
import android.content.SharedPreferences
import com.example.weathergpt_android.BuildConfig

object OpenWeatherPreferences {
    private const val PREFS_NAME = "openweather_prefs"
    private const val KEY_API_KEY = "openweather_api_key"

    fun getApiKey(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_API_KEY, "") ?: ""
        return if (saved.isNotBlank()) saved else BuildConfig.OPENWEATHER_API_KEY
    }

    fun saveApiKey(context: Context, apiKey: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_API_KEY, apiKey.trim()).apply()
    }
}
