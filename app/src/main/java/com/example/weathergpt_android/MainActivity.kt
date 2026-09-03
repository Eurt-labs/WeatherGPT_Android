package com.example.weathergpt_android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.core.theme.ThemePreferences
import com.example.weathergpt_android.core.theme.WeatherGPTTheme
import com.example.weathergpt_android.domain.assistant.ui.GptChatScreen
import com.example.weathergpt_android.domain.assistant.ui.UnifiedMainScreen
import com.example.weathergpt_android.domain.location.model.LocationData
import com.example.weathergpt_android.domain.location.provider.LocationProvider
import com.example.weathergpt_android.domain.settings.ui.FrostedSettingsSheet
import com.example.weathergpt_android.domain.voice.ui.ImmersiveVoiceScreen
import com.example.weathergpt_android.domain.weather.repository.UnifiedWeatherRepository
import kotlinx.coroutines.launch

/**
 * Single Unified WeatherGPT Flow (Matching Screenshot 2 & 3)
 */
enum class AppScreen {
    MAIN_HUB,     // Single Primary Dashboard (Screenshot 2)
    VOICE_AI,     // Immersive Full-Screen Voice with Animated Edge Lighting (Screenshot 3)
    ACTIVE_CHAT   // Conversational Chat View
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var themeMode by remember { mutableStateOf(ThemePreferences.getSavedTheme(context)) }

            WeatherGPTTheme(themeMode = themeMode) {
                WeatherGPTApp(
                    currentTheme = themeMode,
                    onThemeChange = { newTheme ->
                        themeMode = newTheme
                        ThemePreferences.saveTheme(context, newTheme)
                    }
                )
            }
        }
    }
}

@Composable
fun WeatherGPTApp(
    currentTheme: AppThemeMode = AppThemeMode.SYSTEM,
    onThemeChange: (AppThemeMode) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationProvider = remember { LocationProvider(context) }
    val weatherRepository = remember { UnifiedWeatherRepository(context) }

    var locationData by remember { mutableStateOf(locationProvider.getInitialCachedLocation()) }
    var liveWeatherData by remember { mutableStateOf(weatherRepository.getCachedWeather()) }

    var currentScreen by remember { mutableStateOf(AppScreen.MAIN_HUB) }
    var activeChatPrompt by remember { mutableStateOf<String?>(null) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    fun refreshLiveWeather(loc: LocationData) {
        scope.launch {
            weatherRepository.fetchLiveWeather(loc.latitude, loc.longitude).onSuccess { data ->
                liveWeatherData = data
            }
        }
    }

    // Android System Back Button Handling
    BackHandler(enabled = currentScreen != AppScreen.MAIN_HUB) {
        currentScreen = AppScreen.MAIN_HUB
    }

    // Permission Requester for Location and Audio
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            locationProvider.fetchRealtimeLocation(scope) { resolved ->
                locationData = resolved
                refreshLiveWeather(resolved)
            }
        }
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissionsToRequest.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            permissionsLauncher.launch(missingPermissions.toTypedArray())
        } else {
            locationProvider.fetchRealtimeLocation(scope) { resolved ->
                locationData = resolved
                refreshLiveWeather(resolved)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Main Screen Router with Smooth Crossfade Transition
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
            },
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                AppScreen.MAIN_HUB -> UnifiedMainScreen(
                    currentTheme = currentTheme,
                    liveWeatherData = liveWeatherData,
                    userName = "Dhruv",
                    onLaunchVoice = { currentScreen = AppScreen.VOICE_AI },
                    onLaunchChatWithPrompt = { prompt ->
                        activeChatPrompt = prompt
                        currentScreen = AppScreen.ACTIVE_CHAT
                    },
                    onOpenSettings = { showSettingsSheet = true }
                )

                AppScreen.VOICE_AI -> ImmersiveVoiceScreen(
                    locationData = locationData,
                    liveWeatherData = liveWeatherData,
                    onClose = { currentScreen = AppScreen.MAIN_HUB }
                )

                AppScreen.ACTIVE_CHAT -> GptChatScreen(
                    currentTheme = currentTheme,
                    locationData = locationData,
                    liveWeatherData = liveWeatherData,
                    initialPrompt = activeChatPrompt,
                    onBack = { currentScreen = AppScreen.MAIN_HUB },
                    onOpenSettings = { showSettingsSheet = true },
                    onLaunchVoice = { currentScreen = AppScreen.VOICE_AI }
                )
            }
        }

        // Frosted Glass Settings Sheet Dialog / Overlay (High Contrast & Visible)
        AnimatedVisibility(
            visible = showSettingsSheet,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x75000000)),
                contentAlignment = Alignment.Center
            ) {
                FrostedSettingsSheet(
                    currentTheme = currentTheme,
                    onThemeSelected = onThemeChange,
                    onDismiss = { showSettingsSheet = false }
                )
            }
        }
    }
}
