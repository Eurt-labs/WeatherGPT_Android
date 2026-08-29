package com.example.weathergpt_android.core.network

import android.content.Context
import android.content.SharedPreferences

object OpenRouterPreferences {
    private const val PREFS_NAME = "openrouter_prefs"
    private const val KEY_API_KEY = "openrouter_api_key"
    private const val KEY_MODEL = "openrouter_model"

    // Primary Unified Multilingual Model
    const val MODEL_GEMMA_4_31B_FREE = "google/gemma-4-31b-it:free"
    const val MODEL_NEMOTRON_3_5 = "nvidia/nemotron-3.5-lightning"
    const val MODEL_LLAMA_3_3_FREE = "meta-llama/llama-3.3-70b-instruct:free"

    const val DEFAULT_MODEL = MODEL_GEMMA_4_31B_FREE

    val AVAILABLE_MODELS = listOf(
        ModelOption(
            id = MODEL_GEMMA_4_31B_FREE,
            name = "Google: Gemma 4 31B (Free)",
            tag = "Default • Universal Multilingual & Fast Voice AI",
            isMultilingual = true
        ),
        ModelOption(
            id = MODEL_LLAMA_3_3_FREE,
            name = "Meta: Llama 3.3 70B (Free)",
            tag = "Free • 70B Deep Reasoning & Indic AI",
            isMultilingual = true
        ),
        ModelOption(
            id = MODEL_NEMOTRON_3_5,
            name = "NVIDIA: Nemotron 3.5",
            tag = "Fast English Specialist",
            isMultilingual = false
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
