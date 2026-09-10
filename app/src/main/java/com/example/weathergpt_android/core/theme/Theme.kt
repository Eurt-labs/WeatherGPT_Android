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
            primary = MinimalTextPrimary,
            onPrimary = Color.White,
            primaryContainer = MinimalSurfaceSubtle,
            onPrimaryContainer = MinimalTextPrimary,
            secondary = MinimalTextSecondary,
            onSecondary = Color.White,
            secondaryContainer = MinimalSurfaceVariant,
            onSecondaryContainer = MinimalTextPrimary,
            tertiary = MinimalBeigeHighlight,
            onTertiary = Color.White,
            background = MinimalWarmBackground,
            onBackground = MinimalTextPrimary,
            surface = MinimalSurface,
            onSurface = MinimalTextPrimary,
            surfaceVariant = MinimalSurfaceVariant,
            onSurfaceVariant = MinimalTextSecondary
        )
    } else {
        darkColorScheme(
            primary = OledTextPrimary,
            onPrimary = OledBlack,
            primaryContainer = OledDarkSubtleSurface,
            onPrimaryContainer = OledBeigeAccent,
            secondary = OledBeigeAccent,
            onSecondary = OledBlack,
            secondaryContainer = OledDarkSurfaceVariant,
            onSecondaryContainer = OledTextPrimary,
            tertiary = OledBeigeSubtle,
            onTertiary = OledBlack,
            background = OledBlack,
            onBackground = OledTextPrimary,
            surface = OledDarkSurface,
            onSurface = OledTextPrimary,
            surfaceVariant = OledDarkSurfaceVariant,
            onSurfaceVariant = OledTextSecondary
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
