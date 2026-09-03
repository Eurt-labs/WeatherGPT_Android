package com.example.weathergpt_android.core.network

import android.content.Context
import android.content.SharedPreferences

object OpenRouterPreferences {
    private const val PREFS_NAME = "openrouter_prefs"
    private const val KEY_API_KEY = "openrouter_api_key"
    private const val KEY_MODEL = "openrouter_model"

    // Primary Google Gemini 2.5 Flash Multimodal Engine
    const val MODEL_GEMINI_2_5_FLASH = "google/gemini-2.5-flash"
    const val MODEL_GEMMA_4_31B = "google/gemma-4-31b-it:free"

    const val MODEL_DISPLAY_NAME = "Google: Gemini 2.5 Flash"
    const val DEFAULT_MODEL = MODEL_GEMINI_2_5_FLASH

    fun getApiKey(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }

    fun saveApiKey(context: Context, apiKey: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_API_KEY, apiKey.trim()).apply()
    }

    fun getSelectedModel(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_MODEL, DEFAULT_MODEL) ?: DEFAULT_MODEL
    }

    fun saveSelectedModel(context: Context, model: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MODEL, model).apply()
    }
}
