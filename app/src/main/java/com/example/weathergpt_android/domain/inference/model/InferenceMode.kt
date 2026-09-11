package com.example.weathergpt_android.domain.inference.model

/**
 * Supported AI inference modes for WeatherGPT.
 */
enum class InferenceMode(val id: String, val title: String, val description: String) {
    CLOUD("cloud", "Cloud (Gemini 3.6 Flash)", "High-performance AI reasoning via cloud backend"),
    ON_DEVICE("on_device", "On-Device (Offline)", "Fully offline SLM running directly on your phone's processor");

    companion object {
        fun fromId(id: String): InferenceMode {
            return entries.firstOrNull { it.id == id } ?: CLOUD
        }
    }
}
