package com.example.weathergpt_android.domain.assistant.model

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String,
    val weatherHighlight: String? = null
)
