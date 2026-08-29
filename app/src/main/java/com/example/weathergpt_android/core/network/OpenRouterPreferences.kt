package com.example.weathergpt_android.core.network

import android.content.Context
import android.content.SharedPreferences

object OpenRouterPreferences {
    private const val PREFS_NAME = "openrouter_prefs"
    private const val KEY_API_KEY = "openrouter_api_key"
    private const val KEY_MODEL = "openrouter_model"

    const val DEFAULT_MODEL = "nvidia/nemotron-3.5-lightning"
    const val FREE_FALLBACK_MODEL = "nvidia/nemotron-4-340b-instruct:free"

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
        prefs.edit().putString(KEY_MODEL, model.trim()).apply()
    }
}
