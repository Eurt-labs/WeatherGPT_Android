package com.example.weathergpt_android.core.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun WeatherGPTTheme(
    themeMode: AppThemeMode = AppThemeMode.FROST_LIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.FROST_LIGHT -> lightColorScheme(
            primary = SkyBlue,
            onPrimary = TextOnAccent,
            primaryContainer = SkyBlueLight,
            onPrimaryContainer = SkyBlueDark,
            secondary = AiPurple,
            onSecondary = TextOnAccent,
            secondaryContainer = AiPurpleLight,
            onSecondaryContainer = AiIndigo,
            tertiary = WeatherEmerald,
            onTertiary = TextOnAccent,
            background = FrostBackground,
            onBackground = TextPrimaryLight,
            surface = CardBackgroundLight,
            onSurface = TextPrimaryLight,
            surfaceVariant = SubtleSurfaceLight,
            onSurfaceVariant = TextSecondaryLight
        )
        AppThemeMode.MIDNIGHT_DARK -> darkColorScheme(
            primary = MidnightAccent,
            onPrimary = Color(0xFF0F172A),
            primaryContainer = Color(0xFF0369A1),
            onPrimaryContainer = Color.White,
            secondary = AiPurple,
            onSecondary = Color.White,
            secondaryContainer = Color(0xFF4C1D95),
            onSecondaryContainer = Color.White,
            tertiary = WeatherEmerald,
            onTertiary = Color.White,
            background = MidnightBackground,
            onBackground = MidnightTextPrimary,
            surface = MidnightCard,
            onSurface = MidnightTextPrimary,
            surfaceVariant = MidnightSurface,
            onSurfaceVariant = MidnightTextSecondary
        )
        AppThemeMode.SUNSET_GLOW -> lightColorScheme(
            primary = SunsetAccent,
            onPrimary = TextOnAccent,
            primaryContainer = SunsetSurface,
            onPrimaryContainer = Color(0xFF9A3412),
            secondary = WeatherAmber,
            onSecondary = TextOnAccent,
            secondaryContainer = WeatherAmberLight,
            onSecondaryContainer = Color(0xFF78350F),
            tertiary = WeatherEmerald,
            onTertiary = TextOnAccent,
            background = SunsetBackground,
            onBackground = TextPrimaryLight,
            surface = SunsetCard,
            onSurface = TextPrimaryLight,
            surfaceVariant = SunsetSurface,
            onSurfaceVariant = TextSecondaryLight
        )
        AppThemeMode.OCEAN_TEAL -> lightColorScheme(
            primary = OceanAccent,
            onPrimary = TextOnAccent,
            primaryContainer = OceanSurface,
            onPrimaryContainer = Color(0xFF115E59),
            secondary = SkyBlue,
            onSecondary = TextOnAccent,
            secondaryContainer = SkyBlueLight,
            onSecondaryContainer = SkyBlueDark,
            tertiary = WeatherEmerald,
            onTertiary = TextOnAccent,
            background = OceanBackground,
            onBackground = TextPrimaryLight,
            surface = OceanCard,
            onSurface = TextPrimaryLight,
            surfaceVariant = OceanSurface,
            onSurfaceVariant = TextSecondaryLight
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !themeMode.isDark
                insetsController.isAppearanceLightNavigationBars = !themeMode.isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
