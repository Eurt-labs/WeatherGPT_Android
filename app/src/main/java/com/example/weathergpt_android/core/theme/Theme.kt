package com.example.weathergpt_android.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun WeatherGPTTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = if (!isDark) {
        lightColorScheme(
            primary = M3LightPrimary,
            onPrimary = Color.White,
            primaryContainer = M3LightPrimaryContainer,
            onPrimaryContainer = M3LightOnPrimaryContainer,
            secondary = M3LightSecondary,
            onSecondary = Color.White,
            secondaryContainer = M3LightSecondaryContainer,
            onSecondaryContainer = M3LightTextPrimary,
            tertiary = WeatherEmerald,
            onTertiary = Color.White,
            background = M3LightBackground,
            onBackground = M3LightTextPrimary,
            surface = M3LightSurface,
            onSurface = M3LightTextPrimary,
            surfaceVariant = M3LightSurfaceVariant,
            onSurfaceVariant = M3LightTextSecondary
        )
    } else {
        darkColorScheme(
            primary = DarkBronzeAccent,
            onPrimary = Color.White,
            primaryContainer = DarkBronzeAccent.copy(alpha = 0.3f),
            onPrimaryContainer = DarkCreamStone,
            secondary = DarkHighlight,
            onSecondary = Color.White,
            secondaryContainer = DarkSubtleSurface,
            onSecondaryContainer = DarkCreamStone,
            tertiary = WeatherEmerald,
            onTertiary = Color.White,
            background = DarkSlateBase,
            onBackground = DarkCreamStone,
            surface = DarkCharcoalCard,
            onSurface = DarkCreamStone,
            surfaceVariant = DarkSubtleSurface,
            onSurfaceVariant = DarkMutedStone
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // In Light mode: dark icons (true). In Dark mode: light icons (false).
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
