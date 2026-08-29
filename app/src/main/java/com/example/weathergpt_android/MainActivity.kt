package com.example.weathergpt_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.weathergpt_android.ui.components.FloatingBottomNavBar
import com.example.weathergpt_android.ui.components.NotificationSheet
import com.example.weathergpt_android.ui.components.ProfileSheet
import com.example.weathergpt_android.ui.components.TopIslandHeader
import com.example.weathergpt_android.ui.navigation.NavTab
import com.example.weathergpt_android.ui.screens.GptChatScreen
import com.example.weathergpt_android.ui.screens.HomeScreen
import com.example.weathergpt_android.ui.screens.NewsScreen
import com.example.weathergpt_android.ui.screens.SettingsScreen
import com.example.weathergpt_android.ui.screens.VoiceAiScreen
import com.example.weathergpt_android.ui.theme.FlushedBackground
import com.example.weathergpt_android.ui.theme.WeatherGPTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherGPTTheme {
                WeatherGPTApp()
            }
        }
    }
}

@Composable
fun WeatherGPTApp() {
    var currentTab by remember { mutableStateOf(NavTab.WEATHER) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    var showProfileSheet by remember { mutableStateOf(false) }
    var notificationCount by remember { mutableIntStateOf(3) }
    val userName = "Dhruv"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FlushedBackground)
    ) {
        // Main Screen Content with smooth transition animations
        AnimatedContent(
            targetState = currentTab,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180))
            },
            label = "tab_content_transition"
        ) { tab ->
            when (tab) {
                NavTab.WEATHER -> HomeScreen(
                    userName = userName,
                    onNavigateToGpt = { currentTab = NavTab.GPT }
                )
                NavTab.NEWS -> NewsScreen()
                NavTab.VOICE_AI -> VoiceAiScreen()
                NavTab.GPT -> GptChatScreen()
                NavTab.SETTINGS -> SettingsScreen()
            }
        }

        // Top Floating Dynamic Island
        TopIslandHeader(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding(),
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

        // Bottom Floating Island Navigation Bar (Floating, not attached, no outlines)
        FloatingBottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentTab = currentTab,
            onTabSelected = { selected ->
                currentTab = selected
            }
        )

        // Interactive Notification Bottom Sheet
        if (showNotificationSheet) {
            NotificationSheet(
                onDismissRequest = { showNotificationSheet = false }
            )
        }

        // Interactive User Profile Bottom Sheet
        if (showProfileSheet) {
            ProfileSheet(
                userName = "$userName Saraswat",
                userEmail = "dhruv@weathergpt.ai",
                location = "San Francisco, CA",
                onDismissRequest = { showProfileSheet = false }
            )
        }
    }
}
