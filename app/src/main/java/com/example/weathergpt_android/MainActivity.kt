package com.example.weathergpt_android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.weathergpt_android.core.components.FloatingBottomNavBar
import com.example.weathergpt_android.core.components.TopIslandHeader
import com.example.weathergpt_android.core.navigation.NavTab
import com.example.weathergpt_android.core.theme.AppThemeMode
import com.example.weathergpt_android.core.theme.ThemePreferences
import com.example.weathergpt_android.core.theme.WeatherGPTTheme
import com.example.weathergpt_android.domain.assistant.ui.GptChatScreen
import com.example.weathergpt_android.domain.location.model.LocationData
import com.example.weathergpt_android.domain.location.provider.LocationProvider
import com.example.weathergpt_android.domain.news.ui.NewsScreen
import com.example.weathergpt_android.domain.notifications.ui.NotificationSheet
import com.example.weathergpt_android.domain.profile.ui.ProfileSheet
import com.example.weathergpt_android.domain.settings.ui.SettingsScreen
import com.example.weathergpt_android.domain.voice.ui.VoiceAiScreen
import com.example.weathergpt_android.domain.weather.ui.GreetingWelcomeView
import com.example.weathergpt_android.domain.weather.ui.HomeScreen

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
    currentTheme: AppThemeMode = AppThemeMode.LIGHT,
    onThemeChange: (AppThemeMode) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val locationProvider = remember { LocationProvider(context) }
    var locationData by remember { mutableStateOf(locationProvider.getInitialCachedLocation()) }

    var currentTab by remember { mutableStateOf(NavTab.WEATHER) }
    var isInitialLaunchGreeting by remember { mutableStateOf(true) } // Clean launch welcome window
    var isTopIslandVisible by remember { mutableStateOf(true) } // Scroll-aware top island visibility
    var showNotificationSheet by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }
    var notificationCount by remember { mutableIntStateOf(3) }
    val userName = "Dhruv"

    // NestedScrollConnection to detect scrolling and fade out the top island when scrolling down
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -10f) {
                    isTopIslandVisible = false
                } else if (delta > 10f) {
                    isTopIslandVisible = true
                }
                return Offset.Zero
            }
        }
    }

    // Runtime Permission Requester for Location, Microphone, and Notifications
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locationGranted) {
            locationProvider.fetchRealtimeLocation(scope) { resolved ->
                locationData = resolved
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
            // Already granted, trigger live location fetch
            locationProvider.fetchRealtimeLocation(scope) { resolved ->
                locationData = resolved
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .nestedScroll(nestedScrollConnection)
    ) {
        // Main Screen Window Content
        if (isInitialLaunchGreeting) {
            // Initial Launch Empty Greeting Window
            GreetingWelcomeView(
                userName = userName,
                onExploreWeather = {
                    isInitialLaunchGreeting = false
                    isTopIslandVisible = true
                    currentTab = NavTab.WEATHER
                },
                onNavigateToGpt = {
                    isInitialLaunchGreeting = false
                    currentTab = NavTab.GPT
                }
            )
        } else {
            // Domain Screens with smooth transition
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
                },
                label = "tab_content_transition"
            ) { tab ->
                when (tab) {
                    NavTab.WEATHER -> HomeScreen(locationData = locationData)
                    NavTab.NEWS -> NewsScreen()
                    NavTab.VOICE_AI -> VoiceAiScreen(locationData = locationData)
                    NavTab.GPT -> GptChatScreen(locationData = locationData)
                    NavTab.SETTINGS -> SettingsScreen(
                        currentTheme = currentTheme,
                        onThemeSelected = onThemeChange
                    )
                }
            }
        }

        // Top Floating Dynamic Island (Hidden in GPT tab & Fades out when scrolling down)
        AnimatedVisibility(
            visible = (currentTab != NavTab.GPT || isInitialLaunchGreeting) && isTopIslandVisible,
            enter = fadeIn(animationSpec = tween(220)) + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut(animationSpec = tween(200)) + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
        ) {
            TopIslandHeader(
                notificationCount = notificationCount,
                userName = userName,
                onNotificationClick = {
                    showNotificationSheet = true
                    notificationCount = 0
                },
                onProfileClick = {
                    showProfileSheet = true
                }
            )
        }

        // Bottom Attached Navigation Bar with Center Raised Mic FAB
        FloatingBottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentTab = currentTab,
            onTabSelected = { selected ->
                isInitialLaunchGreeting = false // Transition to selected tab on nav click
                isTopIslandVisible = true // Reset top island visibility on tab switch
                currentTab = selected
            }
        )

        // Interactive Notification Sheet
        if (showNotificationSheet) {
            NotificationSheet(
                onDismissRequest = { showNotificationSheet = false }
            )
        }

        // Interactive User Profile Sheet
        if (showProfileSheet) {
            ProfileSheet(
                userName = "$userName Saraswat",
                userEmail = "dhruv@weathergpt.ai",
                location = locationData.formattedLocation,
                onDismissRequest = { showProfileSheet = false }
            )
        }
    }
}
