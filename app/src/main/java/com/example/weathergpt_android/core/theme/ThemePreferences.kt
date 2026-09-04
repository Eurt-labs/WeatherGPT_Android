package com.example.weathergpt_android.core.theme

import android.content.Context
import android.content.SharedPreferences

object ThemePreferences {
    private const val PREFS_NAME = "weathergpt_prefs"
    private const val KEY_THEME = "app_theme_mode_v2"

    fun getSavedTheme(context: Context): AppThemeMode {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_THEME, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        return try {
            AppThemeMode.valueOf(savedName)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    }

    fun saveTheme(context: Context, themeMode: AppThemeMode) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, themeMode.name).apply()
    }
}
