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
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> lightColorScheme(
            primary = LightElectricBlue,
            onPrimary = Color.White,
            primaryContainer = LightLavender.copy(alpha = 0.35f),
            onPrimaryContainer = LightElectricBlue,
            secondary = LightLavender,
            onSecondary = Color.White,
            secondaryContainer = LightRosePeach.copy(alpha = 0.55f),
            onSecondaryContainer = LightTextPrimary,
            tertiary = WeatherEmerald,
            onTertiary = Color.White,
            background = LightBgPorcelain,
            onBackground = LightTextPrimary,
            surface = LightCardWhite,
            onSurface = LightTextPrimary,
            surfaceVariant = LightSubtleSurface,
            onSurfaceVariant = LightTextSecondary
        )
        AppThemeMode.DARK -> darkColorScheme(
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
