package com.example.weathergpt_android.core.network

import android.content.Context
import android.content.SharedPreferences

object OpenRouterPreferences {
    private const val PREFS_NAME = "openrouter_prefs"
    private const val KEY_API_KEY = "openrouter_api_key"
    private const val KEY_MODEL = "openrouter_model"

    // Single Universal Model for All Languages, Chat & Voice AI
    const val MODEL_GEMMA_4_31B_FREE = "google/gemma-4-31b-it:free"
    const val MODEL_DISPLAY_NAME = "Google: Gemma 4 31B (Free)"

    const val DEFAULT_MODEL = MODEL_GEMMA_4_31B_FREE

    fun getApiKey(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }

    fun saveApiKey(context: Context, apiKey: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_API_KEY, apiKey.trim()).apply()
    }

    fun getSelectedModel(context: Context): String {
        return MODEL_GEMMA_4_31B_FREE
    }

    fun saveSelectedModel(context: Context, model: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MODEL, MODEL_GEMMA_4_31B_FREE).apply()
    }
}
