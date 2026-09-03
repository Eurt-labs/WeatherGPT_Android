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
import com.example.weathergpt_android.domain.assistant.ui.NewChatBentoScreen
import com.example.weathergpt_android.domain.assistant.ui.SmartAssistantScreen
import com.example.weathergpt_android.domain.assistant.ui.WeatherGptWelcomeScreen
import com.example.weathergpt_android.domain.location.model.LocationData
import com.example.weathergpt_android.domain.location.provider.LocationProvider
import com.example.weathergpt_android.domain.settings.ui.FrostedSettingsSheet
import com.example.weathergpt_android.domain.voice.ui.VoiceAiScreen
import com.example.weathergpt_android.domain.weather.repository.UnifiedWeatherRepository
import kotlinx.coroutines.launch

/**
 * WeatherGPT Application Screens (Voice & Chat Focused Modern Flow)
 */
enum class AppScreen {
    WELCOME,          // Screen 1: Welcome & Feature Overview (Left Phone)
    SMART_ASSISTANT,  // Screen 2: Breathing Waveform & Quick Query Chips (Center Phone)
    NEW_CHAT_BENTO,   // Screen 3: 2x2 Bento Action Grid (Right Phone)
    ACTIVE_CHAT,      // Full Screen Chat Conversation with Gemini 2.5 Flash
    VOICE_AI          // Hands-Free Full Screen Multimodal Voice AI
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

    var currentScreen by remember { mutableStateOf(AppScreen.SMART_ASSISTANT) }
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
    BackHandler(enabled = currentScreen != AppScreen.SMART_ASSISTANT) {
        when (currentScreen) {
            AppScreen.ACTIVE_CHAT -> currentScreen = AppScreen.SMART_ASSISTANT
            AppScreen.NEW_CHAT_BENTO -> currentScreen = AppScreen.SMART_ASSISTANT
            AppScreen.VOICE_AI -> currentScreen = AppScreen.SMART_ASSISTANT
            AppScreen.WELCOME -> currentScreen = AppScreen.SMART_ASSISTANT
            AppScreen.SMART_ASSISTANT -> {}
        }
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
                fadeIn(animationSpec = tween(240)) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                AppScreen.WELCOME -> WeatherGptWelcomeScreen(
                    currentTheme = currentTheme,
                    liveWeatherData = liveWeatherData,
                    onNavigateToAssistant = { currentScreen = AppScreen.SMART_ASSISTANT },
                    onNavigateToBento = { currentScreen = AppScreen.NEW_CHAT_BENTO },
                    onNavigateToVoice = { currentScreen = AppScreen.VOICE_AI },
                    onOpenSettings = { showSettingsSheet = true }
                )

                AppScreen.SMART_ASSISTANT -> SmartAssistantScreen(
                    currentTheme = currentTheme,
                    liveWeatherData = liveWeatherData,
                    onBack = { currentScreen = AppScreen.WELCOME },
                    onClose = { currentScreen = AppScreen.NEW_CHAT_BENTO },
                    onStartChatWithPrompt = { prompt ->
                        activeChatPrompt = prompt
                        currentScreen = AppScreen.ACTIVE_CHAT
                    },
                    onLaunchVoiceAi = { currentScreen = AppScreen.VOICE_AI }
                )

                AppScreen.NEW_CHAT_BENTO -> NewChatBentoScreen(
                    currentTheme = currentTheme,
                    liveWeatherData = liveWeatherData,
                    onBack = { currentScreen = AppScreen.SMART_ASSISTANT },
                    onOpenSettings = { showSettingsSheet = true },
                    onStartVoiceMode = { currentScreen = AppScreen.VOICE_AI },
                    onStartChatWithPrompt = { prompt ->
                        activeChatPrompt = prompt
                        currentScreen = AppScreen.ACTIVE_CHAT
                    }
                )

                AppScreen.ACTIVE_CHAT -> GptChatScreen(
                    currentTheme = currentTheme,
                    locationData = locationData,
                    liveWeatherData = liveWeatherData,
                    initialPrompt = activeChatPrompt,
                    onBack = { currentScreen = AppScreen.SMART_ASSISTANT },
                    onOpenSettings = { showSettingsSheet = true },
                    onLaunchVoice = { currentScreen = AppScreen.VOICE_AI }
                )

                AppScreen.VOICE_AI -> VoiceAiScreen(
                    locationData = locationData,
                    liveWeatherData = liveWeatherData,
                    onSpeakingStateChanged = {}
                )
            }
        }

        // Frosted Glass Settings Sheet Dialog / Overlay
        AnimatedVisibility(
            visible = showSettingsSheet,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x60000000)),
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
