package com.example.weathergpt_android.core.network

import android.content.Context
import android.content.SharedPreferences

object OpenRouterPreferences {
    private const val PREFS_NAME = "openrouter_prefs"
    private const val KEY_API_KEY = "openrouter_api_key"
    private const val KEY_MODEL = "openrouter_model"

    // Multi-Provider High-Reliability Free Multilingual Fallback Chain
    const val MODEL_LLAMA_3_3_70B_FREE = "meta-llama/llama-3.3-70b-instruct:free"
    const val MODEL_QWEN_2_5_72B_FREE = "qwen/qwen-2.5-72b-instruct:free"
    const val MODEL_NEMOTRON_3_5 = "nvidia/nemotron-3.5-lightning"
    const val MODEL_GEMMA_4_31B = "google/gemma-4-31b-it:free"

    const val DEFAULT_MODEL = MODEL_LLAMA_3_3_70B_FREE

    val FALLBACK_MODELS = listOf(
        MODEL_LLAMA_3_3_70B_FREE,
        MODEL_QWEN_2_5_72B_FREE,
        MODEL_NEMOTRON_3_5
    )

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
