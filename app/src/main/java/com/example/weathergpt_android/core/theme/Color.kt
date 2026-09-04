package com.example.weathergpt_android.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

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

// ==========================================
// 3. Centralized Frosted Glass Design Tokens
// ==========================================
object FrostedGlassTokens {
    // Translucent Frosted Glass Surfaces for Dark Theme:
    // DarkSurface: Frosted Obsidian glass (~83% opacity) for cards on ambient background
    // Allows background ambient glow to softly permeate, but diffuses and prevents see-through transparency
    val DarkSurface = Color(0xD4130C28)

    // DarkSurfaceRaised: Deep Frosted Obsidian glass (~96% opacity) for modal sheets, dialogs, floating bottom bars
    // Crucial for modals: prevents background text from overlapping or bleeding through
    val DarkSurfaceRaised = Color(0xF5100922)

    // DarkSurfaceSubtle: Frosted glass sheen (~14% specular white) for chips, pills, and inputs inside cards/sheets
    val DarkSurfaceSubtle = Color(0x24FFFFFF)

    // Translucent Frosted Glass Surfaces for Light Theme (milky glass):
    val LightSurface = Color(0xEBFFFFFF)       // ~92% milky frosted glass
    val LightSurfaceRaised = Color(0xF8FFFFFF) // ~97% milky frosted glass for sheets
    val LightSurfaceSubtle = Color(0x50F1F5F9)

    // Specular Edge Borders (top-lit glass sheen)
    val DarkBorder = Color(0x38FFFFFF)
    val DarkBorderSubtle = Color(0x22FFFFFF)
    val LightBorder = Color(0x80FFFFFF)
    val LightBorderSubtle = Color(0x45CBD5E1)

    // Accent Gradient Borders
    val DarkAccentBorder = Color(0x55C026D3)
    val LightAccentBorder = Color(0x409333EA)

    // Ambient Soft Shadows for Glass Elevation
    val ShadowColor = Color(0x50000000)
    val ShadowSubtle = Color(0x25000000)

    // Elevation tokens for glass surfaces
    val ElevationDefault = 12.dp
    val ElevationRaised = 22.dp
    val ElevationSubtle = 4.dp

    // Helper functions for reactive theming
    fun surface(isDark: Boolean) = if (isDark) DarkSurface else LightSurface
    fun surfaceRaised(isDark: Boolean) = if (isDark) DarkSurfaceRaised else LightSurfaceRaised
    fun surfaceSubtle(isDark: Boolean) = if (isDark) DarkSurfaceSubtle else LightSurfaceSubtle
    fun border(isDark: Boolean) = if (isDark) DarkBorder else LightBorder
    fun borderSubtle(isDark: Boolean) = if (isDark) DarkBorderSubtle else LightBorderSubtle
}

