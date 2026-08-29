package com.example.weathergpt_android.domain.news.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val category: String,
    val timeAgo: String,
    val readTime: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconBg: Color
)
