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

    const val DEFAULT_GEMINI_MODEL = "gemini-2.0-flash"
    const val DEFAULT_OPENROUTER_MODEL = "google/gemini-2.0-flash-exp:free"

    private fun getPrefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getProviderMode(context: Context): AiProviderMode {
        val raw = getPrefs(context).getString(KEY_PROVIDER_MODE, AiProviderMode.CLOUD_BACKEND.id)
        return AiProviderMode.values().firstOrNull { it.id == raw } ?: AiProviderMode.CLOUD_BACKEND
    }

    fun saveProviderMode(context: Context, mode: AiProviderMode) {
        getPrefs(context).edit().putString(KEY_PROVIDER_MODE, mode.id).apply()
    }

    fun getGeminiApiKey(context: Context): String =
        getPrefs(context).getString(KEY_GEMINI_API_KEY, "") ?: ""

    fun saveGeminiApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_GEMINI_API_KEY, key.trim()).apply()
    }

    fun getOpenRouterApiKey(context: Context): String =
        getPrefs(context).getString(KEY_OPENROUTER_API_KEY, "") ?: ""

    fun saveOpenRouterApiKey(context: Context, key: String) {
        getPrefs(context).edit().putString(KEY_OPENROUTER_API_KEY, key.trim()).apply()
    }

    fun getGeminiModel(context: Context): String =
        getPrefs(context).getString(KEY_GEMINI_MODEL, DEFAULT_GEMINI_MODEL) ?: DEFAULT_GEMINI_MODEL

    fun saveGeminiModel(context: Context, model: String) {
        getPrefs(context).edit().putString(KEY_GEMINI_MODEL, model.trim()).apply()
    }

    fun getOpenRouterModel(context: Context): String =
        getPrefs(context).getString(KEY_OPENROUTER_MODEL, DEFAULT_OPENROUTER_MODEL) ?: DEFAULT_OPENROUTER_MODEL

    fun saveOpenRouterModel(context: Context, model: String) {
        getPrefs(context).edit().putString(KEY_OPENROUTER_MODEL, model.trim()).apply()
    }

    fun hasCustomKey(context: Context): Boolean {
        return getGeminiApiKey(context).isNotBlank() || getOpenRouterApiKey(context).isNotBlank()
    }
}
