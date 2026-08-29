package com.example.weathergpt_android.core.network

import android.content.Context
import android.content.SharedPreferences

object OpenRouterPreferences {
    private const val PREFS_NAME = "openrouter_prefs"
    private const val KEY_API_KEY = "openrouter_api_key"
    private const val KEY_MODEL = "openrouter_model"

    const val MODEL_AUTO = "auto"
    const val MODEL_NEMOTRON_3_5 = "nvidia/nemotron-3.5-lightning"
    const val MODEL_LLAMA_3_3_FREE = "meta-llama/llama-3.3-70b-instruct:free"
    const val MODEL_QWEN_2_5_FREE = "qwen/qwen-2.5-72b-instruct:free"
    const val MODEL_GEMMA_2_FREE = "google/gemma-2-9b-it:free"

    const val DEFAULT_MODEL = MODEL_AUTO

    val AVAILABLE_MODELS = listOf(
        ModelOption(
            id = MODEL_AUTO,
            name = "⚡ Smart Auto-Switch (Recommended)",
            tag = "Auto: Nemotron 3.5 (EN) ↔ Llama 3.3 70B (Indic)",
            isMultilingual = true
        ),
        ModelOption(
            id = MODEL_NEMOTRON_3_5,
            name = "Nemotron 3.5 Lightning",
            tag = "Ultra-Fast Inference (English)",
            isMultilingual = false
        ),
        ModelOption(
            id = MODEL_LLAMA_3_3_FREE,
            name = "Llama 3.3 70B (Free)",
            tag = "Free • Fluent Hindi / Marathi / Tamil / Telugu",
            isMultilingual = true
        ),
        ModelOption(
            id = MODEL_QWEN_2_5_FREE,
            name = "Qwen 2.5 72B (Free)",
            tag = "Free • Top Indic Multilingual Reasoning",
            isMultilingual = true
        ),
        ModelOption(
            id = MODEL_GEMMA_2_FREE,
            name = "Gemma 2 9B (Free)",
            tag = "Free • Google Lightweight Multilingual",
            isMultilingual = true
        )
    )

    data class ModelOption(
        val id: String,
        val name: String,
        val tag: String,
        val isMultilingual: Boolean
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
