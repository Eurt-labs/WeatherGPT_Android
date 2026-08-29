package com.example.weathergpt_android.domain.voice.ui

import android.Manifest
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.weathergpt_android.domain.location.model.LocationData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

enum class VoiceModeState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

@Composable
fun VoiceAiScreen(
    modifier: Modifier = Modifier,
    locationData: LocationData = LocationData.DEFAULT
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var voiceState by remember { mutableStateOf(VoiceModeState.IDLE) }
    var userSpeechText by remember { mutableStateOf("") }
    var assistantSpeechText by remember {
        mutableStateOf("Hi! I'm WeatherGPT Voice. Tap the sphere or choose a topic to talk about live weather and advisories.")
    }
    var isTtsMuted by remember { mutableStateOf(false) }

    // TextToSpeech Engine Integration
    var ttsEngine: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        tts.language = Locale.US
        ttsEngine = tts

        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakResponse(text: String) {
        if (!isTtsMuted && isTtsReady && ttsEngine != null) {
            ttsEngine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "weather_tts_id")
        }
    }

    fun stopSpeaking() {
        ttsEngine?.stop()
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceState = VoiceModeState.LISTENING
            userSpeechText = "Listening to your voice in ${locationData.cityName}..."
        }
    }

    fun startConversation(prompt: String, response: String) {
        stopSpeaking()
        scope.launch {
            userSpeechText = prompt
            voiceState = VoiceModeState.THINKING
            delay(1000) // Simulated AI atmospheric reasoning
            assistantSpeechText = response
            voiceState = VoiceModeState.SPEAKING
            speakResponse(response)
        }
    }

    // Dynamic Voice Orb Multi-Ring Animations (ChatGPT Style)
    val infiniteTransition = rememberInfiniteTransition(label = "chatgpt_voice_anim")

    val idleBreath by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_breath"
    )

    val listeningPulse1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listen_pulse1"
    )

    val listeningPulse2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "listen_pulse2"
    )

    val thinkingRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "thinking_rot"
    )

    val speakingWave by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_wave"
    )

    val quickConversations = listOf(
        "Is today good for an evening walk?" to "In ${locationData.cityName}, temperatures are a comfortable 24°C with a gentle 14 km/h breeze and zero rain. Evening walk conditions are ideal!",
        "Will it rain anytime today?" to "Satellite radar indicates clear atmospheric pressure over ${locationData.cityName}. Precipitation chance is under 5% throughout the night.",
        "What should I wear for outside?" to "A light breathable cotton shirt with shorts or chinos is perfect right now. Keep sunglasses handy until sunset!",
        "Check air quality and UV index" to "The UV index is currently at 3 (Moderate), and Air Quality index is 38 (Good), making outdoor workouts completely safe."
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 16.dp,
            bottom = 96.dp // Generous clearance for attached bottom nav
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (voiceState) {
                                    VoiceModeState.IDLE -> MaterialTheme.colorScheme.onSurfaceVariant
                                    VoiceModeState.LISTENING -> Color(0xFF10B981)
                                    VoiceModeState.THINKING -> Color(0xFFF59E0B)
                                    VoiceModeState.SPEAKING -> MaterialTheme.colorScheme.primary
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (voiceState) {
                            VoiceModeState.IDLE -> "ChatGPT Voice Mode"
                            VoiceModeState.LISTENING -> "Listening..."
                            VoiceModeState.THINKING -> "Reasoning Forecast..."
                            VoiceModeState.SPEAKING -> "Speaking Live..."
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Weather Intelligence for ${locationData.cityName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.4).sp
                )
            }
        }

        // Center ChatGPT Style Fluid Voice Sphere
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                // Expanding Reactive Aura Waves
                when (voiceState) {
                    VoiceModeState.LISTENING -> {
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                                .scale(listeningPulse2)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        )
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .scale(listeningPulse1)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        )
                    }
                    VoiceModeState.THINKING -> {
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .rotate(thinkingRotation)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            Color(0xFFF59E0B),
                                            MaterialTheme.colorScheme.secondary,
                                            MaterialTheme.colorScheme.primary
                                        )
                                    )
                                )
                        )
                    }
                    VoiceModeState.SPEAKING -> {
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .scale(speakingWave)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        )
                    }
                    VoiceModeState.IDLE -> {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .scale(idleBreath)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }

                // Core Main Voice Orb
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            ambientColor = Color(0x30000000)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = when (voiceState) {
                                    VoiceModeState.IDLE -> listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                    VoiceModeState.LISTENING -> listOf(
                                        Color(0xFF10B981),
                                        MaterialTheme.colorScheme.primary
                                    )
                                    VoiceModeState.THINKING -> listOf(
                                        Color(0xFFF59E0B),
                                        MaterialTheme.colorScheme.primary
                                    )
                                    VoiceModeState.SPEAKING -> listOf(
                                        MaterialTheme.colorScheme.primary,
                                        Color(0xFF38BDF8)
                                    )
                                }
                            )
                        )
                        .clickable {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (!hasPermission) {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            } else {
                                when (voiceState) {
                                    VoiceModeState.SPEAKING -> {
                                        stopSpeaking()
                                        voiceState = VoiceModeState.IDLE
                                    }
                                    VoiceModeState.LISTENING -> {
                                        voiceState = VoiceModeState.IDLE
                                    }
                                    VoiceModeState.IDLE -> {
                                        voiceState = VoiceModeState.LISTENING
                                        userSpeechText = "Listening to your voice..."
                                        // Demo automatic response flow
                                        scope.launch {
                                            delay(2500)
                                            startConversation(
                                                "What's the weather like right now?",
                                                "Currently in ${locationData.cityName}, it's 24°C with pleasant clear skies, 52% humidity, and calm winds. Perfect weather for being outdoors!"
                                            )
                                        }
                                    }
                                    VoiceModeState.THINKING -> {}
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (voiceState) {
                            VoiceModeState.SPEAKING -> Icons.Rounded.GraphicEq
                            VoiceModeState.LISTENING -> Icons.Rounded.Mic
                            VoiceModeState.THINKING -> Icons.Rounded.AutoAwesome
                            VoiceModeState.IDLE -> Icons.Rounded.Mic
                        },
                        contentDescription = "Voice Mode Action",
                        tint = Color.White,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }

        // Live Equalizer Soundwave Spectrum Bars
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(16) { i ->
                    val barHeight = when (voiceState) {
                        VoiceModeState.SPEAKING -> (14 + ((i * 11) % 32)).dp
                        VoiceModeState.LISTENING -> (10 + ((i * 7) % 20)).dp
                        VoiceModeState.THINKING -> (8 + ((i * 5) % 16)).dp
                        VoiceModeState.IDLE -> 6.dp
                    }
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (voiceState != VoiceModeState.IDLE) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }

        // Conversational Dialog Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = Color(0x20000000),
                        ambientColor = Color(0x10000000)
                    ),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    // Mute / Speaker Controls Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Conversational Dialog",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // TTS Mute / Unmute Button
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        isTtsMuted = !isTtsMuted
                                        if (isTtsMuted) stopSpeaking()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isTtsMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = "Toggle Audio Voice",
                                    tint = if (isTtsMuted) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            // Stop Button
                            if (voiceState == VoiceModeState.SPEAKING || voiceState == VoiceModeState.LISTENING) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEE2E2))
                                        .clickable {
                                            stopSpeaking()
                                            voiceState = VoiceModeState.IDLE
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Stop,
                                        contentDescription = "Stop",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (userSpeechText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "You: \"$userSpeechText\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = assistantSpeechText,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Quick Conversational Turn-Taking Prompts
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tap to Ask in Voice Mode",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                quickConversations.forEach { (prompt, response) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                startConversation(prompt, response)
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "\"$prompt\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
