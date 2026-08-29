package com.example.weathergpt_android.core.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// 1. Light Theme Palette (Custom Provided)
// #3A86FF, #BDB2FF, #FFD6E0, #FFF4F4
// ==========================================
val LightElectricBlue = Color(0xFF3A86FF)    // Primary Accent
val LightLavender = Color(0xFFBDB2FF)        // Secondary Accent
val LightRosePeach = Color(0xFFFFD6E0)       // Tertiary Highlight
val LightBgPorcelain = Color(0xFFFFF4F4)     // Background Screen
val LightCardWhite = Color(0xFFFFFFFF)       // Surface / Card
val LightSubtleSurface = Color(0xFFF8EEF0)   // Surface Variant
val LightTextPrimary = Color(0xFF1E293B)     // Dark Charcoal text
val LightTextSecondary = Color(0xFF64748B)   // Slate text
val LightTextTertiary = Color(0xFF94A3B8)    // Muted text

// ==========================================
// 2. Dark Theme Palette (Custom Provided)
// #2C3639, #3F4E4F, #A27B5C, #DCD7C9
// ==========================================
val DarkSlateBase = Color(0xFF2C3639)        // Background Screen
val DarkCharcoalCard = Color(0xFF3F4E4F)     // Surface / Card
val DarkBronzeAccent = Color(0xFFA27B5C)     // Primary / Accent
val DarkCreamStone = Color(0xFFDCD7C9)       // Text Primary
val DarkMutedStone = Color(0xFFA8A29E)       // Text Secondary
val DarkSubtleSurface = Color(0xFF354245)    // Surface Variant
val DarkHighlight = Color(0xFFB38B6D)        // Secondary Accent

// Semantic Weather Highlights
val WeatherAmber = Color(0xFFF59E0B)
val WeatherAmberLight = Color(0xFFFEF3C7)
val WeatherEmerald = Color(0xFF10B981)
val WeatherEmeraldLight = Color(0xFFD1FAE5)
val AlertRed = Color(0xFFEF4444)
val AlertRedLight = Color(0xFFFEE2E2)

// Standard Export Aliases
val SkyBlue = LightElectricBlue
val SkyBlueLight = Color(0xFFE0F2FE)
val AiPurple = LightLavender
val AiPurpleLight = Color(0xFFEDE9FE)
val AiIndigo = LightElectricBlue
val TextPrimary = LightTextPrimary
val TextSecondary = LightTextSecondary
val TextTertiary = LightTextTertiary

enum class AppThemeMode(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val backgroundColor: Color,
    val isDark: Boolean
) {
    LIGHT(
        title = "Flushed Light",
        subtitle = "Electric Blue & Lavender Rose",
        primaryColor = LightElectricBlue,
        backgroundColor = LightBgPorcelain,
        isDark = false
    ),
    DARK(
        title = "Slate Moss Dark",
        subtitle = "Deep Slate, Charcoal & Bronze",
        primaryColor = DarkBronzeAccent,
        backgroundColor = DarkSlateBase,
        isDark = true
    )
}
