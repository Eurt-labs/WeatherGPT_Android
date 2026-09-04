package com.example.weathergpt_android.domain.assistant.model

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String,
    val weatherHighlight: String? = null,
    val userId: String = "",
    val sessionId: String = "default",
    val createdAt: Long = System.currentTimeMillis()
)

data class ChatSessionSummary(
    val sessionId: String,
    val title: String,
    val snippet: String,
    val lastTimestamp: String,
    val messageCount: Int
)
