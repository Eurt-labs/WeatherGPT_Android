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
        val prefs = getPrefs(context)
        if (prefs.contains(KEY_PROVIDER_MODE)) {
            val raw = prefs.getString(KEY_PROVIDER_MODE, AiProviderMode.CLOUD_BACKEND.id)
            return AiProviderMode.values().firstOrNull { it.id == raw } ?: AiProviderMode.CLOUD_BACKEND
        }
        // If an OpenRouter key is provided via BuildConfig / local.properties, default to OPENROUTER_DIRECT!
        if (getOpenRouterApiKey(context).isNotBlank()) {
            return AiProviderMode.OPENROUTER_DIRECT
        }
        // If a Gemini key is provided, default to GEMINI_DIRECT!
        if (getGeminiApiKey(context).isNotBlank()) {
            return AiProviderMode.GEMINI_DIRECT
        }
        return AiProviderMode.CLOUD_BACKEND
    }

    fun saveProviderMode(context: Context, mode: AiProviderMode) {
        getPrefs(context).edit().putString(KEY_PROVIDER_MODE, mode.id).apply()
    }

    fun getGeminiApiKey(context: Context): String {
        val saved = getPrefs(context).getString(KEY_GEMINI_API_KEY, "") ?: ""
        if (saved.isNotBlank() && !saved.startsWith("sk-or-")) return saved
        val buildConfigKey = com.example.weathergpt_android.BuildConfig.GEMINI_API_KEY
        if (buildConfigKey.isNotBlank() && !buildConfigKey.startsWith("sk-or-")) return buildConfigKey
        // Smart fallback: if user accidentally placed an AIzaSy Google key in OpenRouter field
        val openRouterSaved = getPrefs(context).getString(KEY_OPENROUTER_API_KEY, "") ?: ""
        if (openRouterSaved.startsWith("AIzaSy")) return openRouterSaved
        val openRouterBc = com.example.weathergpt_android.BuildConfig.OPENROUTER_API_KEY
        if (openRouterBc.startsWith("AIzaSy")) return openRouterBc
        return ""
    }

    fun saveGeminiApiKey(context: Context, key: String) {
        val trimmed = key.trim()
        getPrefs(context).edit().putString(KEY_GEMINI_API_KEY, trimmed).apply()
        if (trimmed.startsWith("sk-or-")) {
            getPrefs(context).edit().putString(KEY_OPENROUTER_API_KEY, trimmed).apply()
        }
    }

    fun getOpenRouterApiKey(context: Context): String {
        val saved = getPrefs(context).getString(KEY_OPENROUTER_API_KEY, "") ?: ""
        if (saved.isNotBlank() && !saved.startsWith("AIzaSy")) return saved
        val buildConfigKey = com.example.weathergpt_android.BuildConfig.OPENROUTER_API_KEY
        if (buildConfigKey.isNotBlank() && !buildConfigKey.startsWith("AIzaSy")) return buildConfigKey
        // Smart fallback: if user accidentally placed an sk-or- key in Gemini field
        val geminiSaved = getPrefs(context).getString(KEY_GEMINI_API_KEY, "") ?: ""
        if (geminiSaved.startsWith("sk-or-")) return geminiSaved
        val geminiBc = com.example.weathergpt_android.BuildConfig.GEMINI_API_KEY
        if (geminiBc.startsWith("sk-or-")) return geminiBc
        return ""
    }

    fun saveOpenRouterApiKey(context: Context, key: String) {
        val trimmed = key.trim()
        getPrefs(context).edit().putString(KEY_OPENROUTER_API_KEY, trimmed).apply()
        if (trimmed.startsWith("AIzaSy")) {
            getPrefs(context).edit().putString(KEY_GEMINI_API_KEY, trimmed).apply()
        }
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
