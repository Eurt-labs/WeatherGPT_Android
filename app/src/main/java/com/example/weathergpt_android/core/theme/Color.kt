package com.example.weathergpt_android.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// ==========================================
// 1. Clean Minimal Monochromatic Light Theme Palette
// (Clean porcelain white, deep charcoal, and soft warm beige/cashmere accents)
// ==========================================
val MinimalWhite = Color(0xFFFFFFFF)              // Pure crisp white
val MinimalWarmBackground = Color(0xFFFBFBFA)     // Clean minimalist background with subtle warm undertone
val MinimalSurface = Color(0xFFFFFFFF)            // Pure white card surface
val MinimalSurfaceVariant = Color(0xFFF3F1ED)     // Minimal surface with soft cashmere beige touch
val MinimalSurfaceSubtle = Color(0xFFECE8E1)      // Delicate warm stone for chips and inputs
val MinimalTextPrimary = Color(0xFF111113)        // High-contrast deep jet charcoal text
val MinimalTextSecondary = Color(0xFF575553)      // Warm slate secondary text
val MinimalTextTertiary = Color(0xFF8E8B85)       // Neutral stone tertiary text
val MinimalBeigeAccent = Color(0xFFC4BCAF)        // Refined cashmere beige accent
val MinimalBeigeHighlight = Color(0xFF9E9585)     // Deep stone beige contrast

// ==========================================
// 2. OLED Monochromatic Dark Theme Palette
// (Pure pitch black #000000, deep obsidian, crisp white, and subtle warm champagne beige)
// ==========================================
val OledBlack = Color(0xFF000000)                 // True pitch black for OLED zero-power shutoff
val OledDarkSurface = Color(0xFF09090B)           // Deep obsidian surface
val OledDarkSurfaceVariant = Color(0xFF141416)    // Elevated surface variant
val OledDarkSubtleSurface = Color(0xFF1C1C1F)     // Surface for chips/inputs
val OledTextPrimary = Color(0xFFFFFFFF)           // Pure crisp white primary typography
val OledTextSecondary = Color(0xFFA1A1AA)         // Silver neutral secondary typography
val OledTextTertiary = Color(0xFF71717A)          // Graphite tertiary typography
val OledBeigeAccent = Color(0xFFE8E3D5)           // Elegant warm champagne/beige highlight
val OledBeigeSubtle = Color(0xFFBFB8A5)           // Muted warm stone
val OledBorder = Color(0x33FFFFFF)                // Specular crisp white hairline border
val OledBorderSubtle = Color(0x1CFFFFFF)          // Ultra-fine border

// Semantic Functional Accents (Refined & subtle for monochromatic theme)
val WeatherAmber = Color(0xFFD4A373)
val WeatherAmberLight = Color(0xFFFDF0D5)
val WeatherEmerald = Color(0xFF6B705C)
val WeatherEmeraldLight = Color(0xFFE9EDC9)
val AlertRed = Color(0xFFE63946)
val AlertRedLight = Color(0xFFF8D7DA)

// Standard Aliases for backward compatibility and clean cross-component use
val M3LightPrimary = MinimalTextPrimary
val M3LightPrimaryContainer = MinimalSurfaceSubtle
val M3LightOnPrimaryContainer = MinimalTextPrimary
val M3LightSecondary = MinimalTextSecondary
val M3LightSecondaryContainer = MinimalSurfaceVariant
val M3LightTertiary = MinimalBeigeHighlight
val M3LightTertiaryContainer = MinimalSurfaceSubtle
val M3LightBackground = MinimalWarmBackground
val M3LightSurface = MinimalSurface
val M3LightSurfaceVariant = MinimalSurfaceVariant
val M3LightTextPrimary = MinimalTextPrimary
val M3LightTextSecondary = MinimalTextSecondary
val M3LightTextTertiary = MinimalTextTertiary

val DarkSlateBase = OledBlack
val DarkCharcoalCard = OledDarkSurface
val DarkBronzeAccent = OledBeigeAccent
val DarkCreamStone = OledBeigeAccent
val DarkMutedStone = OledTextSecondary
val DarkSubtleSurface = OledDarkSubtleSurface
val DarkHighlight = OledTextPrimary

val SkyBlue = MinimalTextPrimary
val SkyBlueLight = MinimalSurfaceSubtle
val AiPurple = OledBeigeAccent
val AiPurpleLight = MinimalSurfaceSubtle
val AiIndigo = MinimalTextPrimary
val TextPrimary = MinimalTextPrimary
val TextSecondary = MinimalTextSecondary
val TextTertiary = MinimalTextTertiary

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
        primaryColor = OledTextPrimary,
        backgroundColor = OledBlack,
        isDark = false
    ),
    LIGHT(
        title = "Minimal Light",
        subtitle = "Clean porcelain & warm beige",
        primaryColor = MinimalTextPrimary,
        backgroundColor = MinimalWhite,
        isDark = false
    ),
    DARK(
        title = "OLED Dark",
        subtitle = "Pitch black & champagne stone",
        primaryColor = OledBeigeAccent,
        backgroundColor = OledBlack,
        isDark = true
    )
}

// ==========================================
// 3. Centralized Frosted Glass Design Tokens
// ==========================================
object FrostedGlassTokens {
    // Translucent Frosted Glass Surfaces for OLED Dark Theme:
    // Pure pitch-black / obsidian with subtle depth (~93% opacity)
    val DarkSurface = Color(0xEB0A0A0C)
    // Deep pure black glass for bottom bars and floating sheets (~98% opacity)
    val DarkSurfaceRaised = Color(0xFA000000)
    // Specular silver-white sheen for chips & inputs
    val DarkSurfaceSubtle = Color(0x1AFFFFFF)
    // Warm champagne beige sheen
    val DarkSurfaceBeige = Color(0x22E8E3D5)

    // Translucent Frosted Glass Surfaces for Minimal Light Theme:
    val LightSurface = Color(0xF2FFFFFF)          // ~95% milky pure white
    val LightSurfaceRaised = Color(0xFCFFFFFF)    // ~99% pure crisp surface for sheets
    val LightSurfaceSubtle = Color(0x80F2EFEB)    // Delicate cashmere beige tint
    val LightSurfaceBeige = Color(0xA0EAE5DC)

    // Specular Edge Borders
    val DarkBorder = Color(0x30FFFFFF)
    val DarkBorderSubtle = Color(0x1AFFFFFF)
    val DarkBorderBeige = Color(0x45E8E3D5)

    val LightBorder = Color(0x1F000000)
    val LightBorderSubtle = Color(0x10000000)
    val LightBorderBeige = Color(0x35B8AE9C)

    // Accent Borders
    val DarkAccentBorder = Color(0x60E8E3D5)
    val LightAccentBorder = Color(0x45111113)

    // Shadows
    val ShadowColor = Color(0x55000000)
    val ShadowSubtle = Color(0x20000000)

    // Elevation tokens
    val ElevationDefault = 12.dp
    val ElevationRaised = 22.dp
    val ElevationSubtle = 4.dp

    // Helper functions for reactive theming
    fun surface(isDark: Boolean) = if (isDark) DarkSurface else LightSurface
    fun surfaceRaised(isDark: Boolean) = if (isDark) DarkSurfaceRaised else LightSurfaceRaised
    fun surfaceSubtle(isDark: Boolean) = if (isDark) DarkSurfaceSubtle else LightSurfaceSubtle
    fun surfaceBeige(isDark: Boolean) = if (isDark) DarkSurfaceBeige else LightSurfaceBeige
    fun border(isDark: Boolean) = if (isDark) DarkBorder else LightBorder
    fun borderSubtle(isDark: Boolean) = if (isDark) DarkBorderSubtle else LightBorderSubtle
    fun borderBeige(isDark: Boolean) = if (isDark) DarkBorderBeige else LightBorderBeige
}

