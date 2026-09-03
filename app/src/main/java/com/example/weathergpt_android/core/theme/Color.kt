package com.example.weathergpt_android.core.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// 1. Material 3 Clean Light Theme Palette
// ==========================================
val M3LightPrimary = Color(0xFF006494)          // Material 3 Primary Deep Sky Blue
val M3LightPrimaryContainer = Color(0xFFD0E4FF) // Container
val M3LightOnPrimaryContainer = Color(0xFF001D36)
val M3LightSecondary = Color(0xFF535F70)
val M3LightSecondaryContainer = Color(0xFFD7E3F8)
val M3LightTertiary = Color(0xFF006874)
val M3LightTertiaryContainer = Color(0xFF9EEFFD)
val M3LightBackground = Color(0xFFF8FAFC)        // Crisp Neutral Background
val M3LightSurface = Color(0xFFFFFFFF)           // Pure White Surface
val M3LightSurfaceVariant = Color(0xFFEDF2F7)    // Clean Surface Variant
val M3LightTextPrimary = Color(0xFF0F172A)       // High-contrast Slate Text
val M3LightTextSecondary = Color(0xFF475569)
val M3LightTextTertiary = Color(0xFF94A3B8)

// ==========================================
// 2. Slate Moss Dark Theme Palette
// #2C3639, #3F4E4F, #A27B5C, #DCD7C9
// ==========================================
val DarkSlateBase = Color(0xFF2C3639)            // Dark Background
val DarkCharcoalCard = Color(0xFF3F4E4F)         // Dark Surface / Card
val DarkBronzeAccent = Color(0xFFA27B5C)         // Primary Accent
val DarkCreamStone = Color(0xFFDCD7C9)           // Primary Typography
val DarkMutedStone = Color(0xFFA8A29E)           // Secondary Typography
val DarkSubtleSurface = Color(0xFF354245)        // Dark Variant
val DarkHighlight = Color(0xFFB38B6D)

// Semantic Accents
val WeatherAmber = Color(0xFFF59E0B)
val WeatherAmberLight = Color(0xFFFEF3C7)
val WeatherEmerald = Color(0xFF10B981)
val WeatherEmeraldLight = Color(0xFFD1FAE5)
val AlertRed = Color(0xFFEF4444)
val AlertRedLight = Color(0xFFFEE2E2)

// Standard Aliases for Components
val SkyBlue = M3LightPrimary
val SkyBlueLight = M3LightPrimaryContainer
val AiPurple = Color(0xFF6750A4)
val AiPurpleLight = Color(0xFFEADDFF)
val AiIndigo = M3LightPrimary
val TextPrimary = M3LightTextPrimary
val TextSecondary = M3LightTextSecondary
val TextTertiary = M3LightTextTertiary

enum class AppThemeMode(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val backgroundColor: Color,
    val isDark: Boolean
) {
    SYSTEM(
        title = "System Default",
        subtitle = "Follow device appearance",
        primaryColor = M3LightPrimary,
        backgroundColor = M3LightBackground,
        isDark = false
    ),
    LIGHT(
        title = "Material 3 Light",
        subtitle = "Clean Material Design 3 palette",
        primaryColor = M3LightPrimary,
        backgroundColor = M3LightBackground,
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
