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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.example.weathergpt_android.domain.assistant.ui.PreviousChatsSheet
import com.example.weathergpt_android.domain.assistant.ui.UnifiedMainScreen
import com.example.weathergpt_android.domain.auth.data.UserPreferences
import com.example.weathergpt_android.domain.auth.model.UserProfile
import com.example.weathergpt_android.domain.auth.ui.AuthOtpScreen
import com.example.weathergpt_android.domain.location.model.LocationData
import com.example.weathergpt_android.domain.location.provider.LocationProvider
import com.example.weathergpt_android.domain.onboarding.ui.PersonalizedOnboardingScreen
import com.example.weathergpt_android.domain.settings.ui.FrostedSettingsSheet
import com.example.weathergpt_android.domain.voice.ui.ImmersiveVoiceScreen
import com.example.weathergpt_android.domain.weather.repository.UnifiedWeatherRepository
import kotlinx.coroutines.launch

/**
 * Single Unified WeatherGPT Flow with Supabase OTP Auth & Multi-Sector Personalization
 */
enum class AppScreen {
    AUTH_OTP,          // Step 1: Sign in with Email / Phone & 6-digit OTP
    ONBOARDING_SETUP,  // Step 2: Multi-Sector Questionnaire
    MAIN_HUB,          // Step 3: Personalized Unified Hub (Screenshot 2)
    VOICE_AI,          // Step 4: Immersive Voice AI with Edge Lighting (Screenshot 3)
    ACTIVE_CHAT        // Conversational Chat Screen
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

    var userProfile by remember { mutableStateOf(UserPreferences.getProfile(context)) }

    // Initial Screen Check: Auth -> Onboarding -> Main Hub
    val initialScreen = when {
        !UserPreferences.isLoggedIn(context) -> AppScreen.AUTH_OTP
        !UserPreferences.isOnboarded(context) -> AppScreen.ONBOARDING_SETUP
        else -> AppScreen.MAIN_HUB
    }

    var currentScreen by remember { mutableStateOf(initialScreen) }
    var activeChatPrompt by remember { mutableStateOf<String?>(null) }
    var activeChatSessionId by remember { mutableStateOf("default") }
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showPreviousChatsSheet by remember { mutableStateOf(false) }

    fun refreshLiveWeather(loc: LocationData) {
        scope.launch {
            weatherRepository.fetchLiveWeather(loc.latitude, loc.longitude).onSuccess { data ->
                liveWeatherData = data
            }
        }
    }

    // Android System Back Button Handling
    BackHandler(enabled = showPreviousChatsSheet || showSettingsSheet || (currentScreen != AppScreen.MAIN_HUB && currentScreen != AppScreen.AUTH_OTP)) {
        if (showPreviousChatsSheet) {
            showPreviousChatsSheet = false
            return@BackHandler
        }
        if (showSettingsSheet) {
            showSettingsSheet = false
            return@BackHandler
        }
        when (currentScreen) {
            AppScreen.ACTIVE_CHAT -> currentScreen = AppScreen.MAIN_HUB
            AppScreen.VOICE_AI -> currentScreen = AppScreen.MAIN_HUB
            AppScreen.ONBOARDING_SETUP -> {
                if (UserPreferences.isOnboarded(context)) {
                    currentScreen = AppScreen.MAIN_HUB
                }
            }
            else -> {}
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
        // Screen Router with Smooth Crossfade Transition
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
            },
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                AppScreen.AUTH_OTP -> AuthOtpScreen(
                    currentTheme = currentTheme,
                    onAuthSuccess = { profile, isNewUser ->
                        userProfile = profile
                        if (isNewUser || !profile.isOnboarded) {
                            currentScreen = AppScreen.ONBOARDING_SETUP
                        } else {
                            currentScreen = AppScreen.MAIN_HUB
                        }
                    }
                )

                AppScreen.ONBOARDING_SETUP -> PersonalizedOnboardingScreen(
                    currentTheme = currentTheme,
                    initialProfile = userProfile,
                    onComplete = { completedProfile ->
                        userProfile = completedProfile
                        currentScreen = AppScreen.MAIN_HUB
                    }
                )

                AppScreen.MAIN_HUB -> UnifiedMainScreen(
                    currentTheme = currentTheme,
                    liveWeatherData = liveWeatherData,
                    userProfile = userProfile,
                    onLaunchVoice = { currentScreen = AppScreen.VOICE_AI },
                    onLaunchChatWithPrompt = { prompt ->
                        activeChatPrompt = prompt
                        activeChatSessionId = "default"
                        currentScreen = AppScreen.ACTIVE_CHAT
                    },
                    onOpenPreviousChats = { showPreviousChatsSheet = true },
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
                    activeSessionId = activeChatSessionId,
                    onBack = { currentScreen = AppScreen.MAIN_HUB },
                    onOpenPreviousChats = { showPreviousChatsSheet = true },
                    onOpenSettings = { showSettingsSheet = true },
                    onLaunchVoice = { currentScreen = AppScreen.VOICE_AI }
                )
            }
        }

        // Frosted Glass Previous Chats Sheet Dialog / Overlay
        AnimatedVisibility(
            visible = showPreviousChatsSheet,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xB0000000))
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .clickable { showPreviousChatsSheet = false },
                contentAlignment = Alignment.Center
            ) {
                PreviousChatsSheet(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* absorb clicks on the sheet */ },
                    currentTheme = currentTheme,
                    userProfile = userProfile,
                    onSelectSession = { sessionId ->
                        activeChatSessionId = sessionId
                        activeChatPrompt = null
                        showPreviousChatsSheet = false
                        currentScreen = AppScreen.ACTIVE_CHAT
                    },
                    onStartNewChat = { newSessionId ->
                        activeChatSessionId = newSessionId
                        activeChatPrompt = null
                        showPreviousChatsSheet = false
                        currentScreen = AppScreen.ACTIVE_CHAT
                    },
                    onDismiss = { showPreviousChatsSheet = false }
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
                    .background(Color(0xB0000000))
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .clickable { showSettingsSheet = false },
                contentAlignment = Alignment.Center
            ) {
                FrostedSettingsSheet(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* absorb clicks on the sheet */ },
                    currentTheme = currentTheme,
                    userProfile = userProfile,
                    onThemeSelected = onThemeChange,
                    onEditPersona = {
                        showSettingsSheet = false
                        currentScreen = AppScreen.ONBOARDING_SETUP
                    },
                    onSignOut = {
                        UserPreferences.logout(context)
                        showSettingsSheet = false
                        userProfile = UserProfile()
                        currentScreen = AppScreen.AUTH_OTP
                    },
                    onDismiss = { showSettingsSheet = false }
                )
            }
        }
    }
}
