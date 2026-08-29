package com.example.weathergpt_android.core.theme

import androidx.compose.ui.graphics.Color

// Light Theme Palette (Default Frost)
val SkyBlue = Color(0xFF0284C7)
val SkyBlueLight = Color(0xFFE0F2FE)
val SkyBlueDark = Color(0xFF0369A1)

val WeatherAmber = Color(0xFFF59E0B)
val WeatherAmberLight = Color(0xFFFEF3C7)

val WeatherEmerald = Color(0xFF10B981)
val WeatherEmeraldLight = Color(0xFFD1FAE5)

val AiPurple = Color(0xFF8B5CF6)
val AiPurpleLight = Color(0xFFEDE9FE)
val AiIndigo = Color(0xFF6366F1)

val AlertRed = Color(0xFFEF4444)
val AlertRedLight = Color(0xFFFEE2E2)

// Light Theme Flushed Neutrals
val FrostBackground = Color(0xFFF1F5F9) // Slightly deeper background for card contrast
val IslandBackgroundLight = Color(0xFFFFFFFF)
val CardBackgroundLight = Color(0xFFFFFFFF)
val SubtleSurfaceLight = Color(0xFFF1F5F9)
val SubtleSurfaceHoverLight = Color(0xFFE2E8F0)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val TextTertiaryLight = Color(0xFF94A3B8)
val TextOnAccent = Color(0xFFFFFFFF)

val TextPrimary = TextPrimaryLight
val TextSecondary = TextSecondaryLight
val TextTertiary = TextTertiaryLight

// Dark / Midnight OLED Palette
val MidnightBackground = Color(0xFF0B1120)
val MidnightCard = Color(0xFF1E293B)
val MidnightSurface = Color(0xFF334155)
val MidnightIsland = Color(0xFF1E293B)
val MidnightAccent = Color(0xFF38BDF8)
val MidnightTextPrimary = Color(0xFFF8FAFC)
val MidnightTextSecondary = Color(0xFF94A3B8)

// Sunset Glow Palette
val SunsetBackground = Color(0xFFFFF7ED)
val SunsetCard = Color(0xFFFFFFFF)
val SunsetSurface = Color(0xFFFFEDD5)
val SunsetAccent = Color(0xFFEA580C)

// Oceanic Teal Palette
val OceanBackground = Color(0xFFF0FDFA)
val OceanCard = Color(0xFFFFFFFF)
val OceanSurface = Color(0xFFCCFBF1)
val OceanAccent = Color(0xFF0D9488)

enum class AppThemeMode(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val backgroundColor: Color,
    val isDark: Boolean = false
) {
    FROST_LIGHT("Frost Light", "Clean Apple-inspired design", SkyBlue, FrostBackground, false),
    MIDNIGHT_DARK("Midnight OLED", "Deep slate dark mode", MidnightAccent, MidnightBackground, true),
    SUNSET_GLOW("Sunset Glow", "Warm twilight amber vibes", SunsetAccent, SunsetBackground, false),
    OCEAN_TEAL("Oceanic Breeze", "Refreshing coastal teal", OceanAccent, OceanBackground, false)
}
