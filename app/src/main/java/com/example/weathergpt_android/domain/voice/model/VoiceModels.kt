package com.example.weathergpt_android.domain.voice.model

data class VoiceInteractionState(
    val isListening: Boolean = false,
    val userPrompt: String = "",
    val assistantResponse: String = ""
)

data class VoiceSuggestion(
    val query: String,
    val category: String = "Weather"
)
