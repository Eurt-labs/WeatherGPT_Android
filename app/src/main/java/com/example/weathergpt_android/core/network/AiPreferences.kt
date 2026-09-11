package com.example.weathergpt_android.core.network

import android.content.Context
import android.content.SharedPreferences

enum class AiProviderMode(val id: String, val title: String, val subtitle: String) {
    CLOUD_BACKEND("cloud", "Cloud Backend", "FastAPI on Render (Default)"),
    GEMINI_DIRECT("gemini", "Google Gemini", "Direct API key (Free tier)"),
    OPENROUTER_DIRECT("openrouter", "OpenRouter", "Direct API key (Custom models)")
}

object AiPreferences {
    private const val PREFS_NAME = "weathergpt_ai_prefs"
    private const val KEY_PROVIDER_MODE = "ai_provider_mode"
    private const val KEY_GEMINI_API_KEY = "gemini_api_key"
    private const val KEY_OPENROUTER_API_KEY = "openrouter_api_key"
    private const val KEY_GEMINI_MODEL = "gemini_model"
    private const val KEY_OPENROUTER_MODEL = "openrouter_model"

    const val DEFAULT_GEMINI_MODEL = "gemini-3.6-flash"
    const val DEFAULT_OPENROUTER_MODEL = "google/gemini-3.6-flash"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getProviderMode(context: Context): AiProviderMode {
        return AiProviderMode.CLOUD_BACKEND
    }

    fun saveProviderMode(context: Context, mode: AiProviderMode) {
        getPrefs(context).edit().putString(KEY_PROVIDER_MODE, AiProviderMode.CLOUD_BACKEND.id).apply()
    }

    fun getGeminiApiKey(context: Context): String = ""

    fun saveGeminiApiKey(context: Context, key: String) {}

    fun getOpenRouterApiKey(context: Context): String = ""

    fun saveOpenRouterApiKey(context: Context, key: String) {}

    fun getGeminiModel(context: Context): String = DEFAULT_GEMINI_MODEL

    fun saveGeminiModel(context: Context, model: String) {}

    fun getOpenRouterModel(context: Context): String = DEFAULT_OPENROUTER_MODEL

    fun saveOpenRouterModel(context: Context, model: String) {}

    fun hasCustomKey(context: Context): Boolean = false
}
