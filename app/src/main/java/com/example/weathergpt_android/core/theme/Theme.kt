package com.example.weathergpt_android.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
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
    tertiaryContainer = WeatherEmeraldLight,
    onTertiaryContainer = WeatherEmerald,
    background = FlushedBackground,
    onBackground = TextPrimary,
    surface = CardBackground,
    onSurface = TextPrimary,
    surfaceVariant = SubtleSurface,
    onSurfaceVariant = TextSecondary,
    error = AlertRed,
    onError = TextOnAccent,
    errorContainer = AlertRedLight,
    onErrorContainer = AlertRed
)

@Composable
fun WeatherGPTTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = true
                insetsController.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
