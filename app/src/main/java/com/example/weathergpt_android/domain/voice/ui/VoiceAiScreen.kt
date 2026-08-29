package com.example.weathergpt_android.domain.voice.ui

import android.Manifest
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
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
import com.example.weathergpt_android.core.network.OpenRouterPreferences
import com.example.weathergpt_android.domain.location.model.LocationData
import com.example.weathergpt_android.domain.voice.sherpa.model.SherpaLanguage
import com.example.weathergpt_android.domain.voice.sherpa.pipeline.SherpaVoicePipeline
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData
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
    locationData: LocationData = LocationData.DEFAULT,
    liveWeatherData: LiveWeatherData = LiveWeatherData.DEFAULT,
    onSpeakingStateChanged: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Full-Duplex Voice Pipeline: Sherpa STT -> OpenRouter -> Sherpa TTS
    val voicePipeline = remember { SherpaVoicePipeline(context, scope) }
    var selectedLanguage by remember { mutableStateOf(SherpaLanguage.ENGLISH) }

    var voiceState by remember { mutableStateOf(VoiceModeState.IDLE) }
    var userSpeechText by remember { mutableStateOf("") }
    var assistantSpeechText by remember {
        mutableStateOf("I'm listening. Ask me anything about the weather in ${locationData.cityName}...")
    }
    var isTtsMuted by remember { mutableStateOf(false) }

    // TTS Audio Output Engine
    var ttsEngine: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    // Sync breathing aura state with parent nav bar
    LaunchedEffect(voiceState) {
        onSpeakingStateChanged(voiceState == VoiceModeState.SPEAKING)
    }

    // Stop speaking immediately when leaving tab
    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        val locale = when (selectedLanguage) {
            SherpaLanguage.ENGLISH -> Locale.US
            SherpaLanguage.HINDI -> Locale("hi", "IN")
            SherpaLanguage.MARATHI -> Locale("mr", "IN")
            SherpaLanguage.BENGALI -> Locale("bn", "IN")
            SherpaLanguage.TAMIL -> Locale("ta", "IN")
            SherpaLanguage.TELUGU -> Locale("te", "IN")
        }
        tts.language = locale
        ttsEngine = tts

        onDispose {
            onSpeakingStateChanged(false)
            tts.stop()
            tts.shutdown()
            voicePipeline.release()
        }
    }

    fun speakChunk(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!isTtsMuted && isTtsReady && ttsEngine != null && text.isNotBlank()) {
            ttsEngine?.speak(text, queueMode, null, "weather_tts_id_${System.currentTimeMillis()}")
        }
    }

    fun stopSpeaking() {
        ttsEngine?.stop()
        voicePipeline.sherpaEngine.stopStreamingSpeechRecognition()
        voicePipeline.sherpaEngine.stopAudioPlayback()
        voiceState = VoiceModeState.IDLE
        onSpeakingStateChanged(false)
    }

    fun executePipeline(prompt: String) {
        stopSpeaking()
        userSpeechText = prompt
        voiceState = VoiceModeState.THINKING
        assistantSpeechText = ""

        val apiKey = OpenRouterPreferences.getApiKey(context)
        if (apiKey.isBlank()) {
            // Live intelligent fallback with OpenWeather context
            val fallback = "Currently in ${locationData.cityName}, it is ${liveWeatherData.temperature} with ${liveWeatherData.condition}, wind at ${liveWeatherData.windSpeed}, and air quality at ${liveWeatherData.aqi}."
            assistantSpeechText = fallback
            voiceState = VoiceModeState.SPEAKING
            speakChunk(fallback, TextToSpeech.QUEUE_FLUSH)
            return
        }

        var isFirstSentence = true

        voicePipeline.processVoiceTurn(
            userPrompt = prompt,
            locationData = locationData,
            liveWeatherData = liveWeatherData,
            onTranscriptionUpdate = { transcription ->
                userSpeechText = transcription
            },
            onAiTextChunk = { aiText ->
                assistantSpeechText = aiText
            },
            onTtsSentenceChunk = { sentence ->
                voiceState = VoiceModeState.SPEAKING
                speakChunk(
                    sentence,
                    if (isFirstSentence) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                )
                isFirstSentence = false
            },
            onError = { err ->
                assistantSpeechText = "Error: $err"
                voiceState = VoiceModeState.IDLE
            }
        )
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceState = VoiceModeState.LISTENING
            userSpeechText = "Listening..."
            voicePipeline.sherpaEngine.startStreamingSpeechRecognition(
                onPartialResult = { partial -> userSpeechText = partial },
                onFinalResult = { final -> executePipeline(final) }
            )
        }
    }

    // Dynamic Gemini Live Multi-Ring Animations
    val infiniteTransition = rememberInfiniteTransition(label = "gemini_immersive_voice")

    val auraRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "aura_rot"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    val speakingPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "speaking_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Ambient Gemini Backdrop Aura Light
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(340.dp)
                .rotate(auraRotation)
                .scale(breathingScale)
                .blur(50.dp)
                .background(
                    Brush.radialGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = if (voiceState == VoiceModeState.SPEAKING) 0.28f else 0.14f),
                            Color(0xFF38BDF8).copy(alpha = if (voiceState == VoiceModeState.SPEAKING) 0.22f else 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main Immersive Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 100.dp), // Clearance for bottom navigation mic
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Immersive Header: City & Live Weather Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WeatherGPT Live",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.3).sp
                    )
                    Text(
                        text = "${locationData.cityName} • ${liveWeatherData.temperature} ${liveWeatherData.condition}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Controls (Mute / Stop)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable {
                                isTtsMuted = !isTtsMuted
                                if (isTtsMuted) stopSpeaking()
                            },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isTtsMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Toggle Mute",
                                tint = if (isTtsMuted) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (voiceState == VoiceModeState.SPEAKING || voiceState == VoiceModeState.LISTENING) {
                        Surface(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { stopSpeaking() },
                            shape = CircleShape,
                            color = Color(0xFFFEE2E2)
                        ) {
                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Stop,
                                    contentDescription = "Stop",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Center Dynamic Morphing Gemini Orb
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (!hasPermission) {
                            audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            when (voiceState) {
                                VoiceModeState.SPEAKING -> stopSpeaking()
                                VoiceModeState.LISTENING -> {
                                    voicePipeline.sherpaEngine.stopStreamingSpeechRecognition()
                                    voiceState = VoiceModeState.IDLE
                                }
                                VoiceModeState.IDLE -> {
                                    voiceState = VoiceModeState.LISTENING
                                    userSpeechText = "Listening in ${selectedLanguage.nativeName}..."
                                    voicePipeline.sherpaEngine.startStreamingSpeechRecognition(
                                        onPartialResult = { partial -> userSpeechText = partial },
                                        onFinalResult = { final -> executePipeline(final) }
                                    )
                                    executePipeline("What is the current live weather in ${locationData.cityName}?")
                                }
                                VoiceModeState.THINKING -> {}
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Outer Pulse Ring
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(if (voiceState == VoiceModeState.SPEAKING) speakingPulse else breathingScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = if (voiceState == VoiceModeState.SPEAKING) 0.35f else 0.15f),
                                    Color(0xFF38BDF8).copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Core Main Orb
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(
                            elevation = 18.dp,
                            shape = CircleShape,
                            spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            ambientColor = Color(0x30000000)
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = when (voiceState) {
                                    VoiceModeState.SPEAKING -> listOf(
                                        MaterialTheme.colorScheme.primary,
                                        Color(0xFF38BDF8),
                                        Color(0xFF818CF8)
                                    )
                                    VoiceModeState.LISTENING -> listOf(
                                        Color(0xFF10B981),
                                        MaterialTheme.colorScheme.primary
                                    )
                                    VoiceModeState.THINKING -> listOf(
                                        Color(0xFFF59E0B),
                                        MaterialTheme.colorScheme.primary
                                    )
                                    VoiceModeState.IDLE -> listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                }
                            )
                        ),
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
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            // Main Immersive Spoken Text View (Flowing Large Typography)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (userSpeechText.isNotBlank()) {
                    Text(
                        text = "\"$userSpeechText\"",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                Text(
                    text = assistantSpeechText,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp,
                    letterSpacing = (-0.3).sp
                )
            }

            // Subtle Status Hint
            Text(
                text = when (voiceState) {
                    VoiceModeState.SPEAKING -> "Tap orb to pause • Gemini Voice Live"
                    VoiceModeState.LISTENING -> "Listening to your voice..."
                    VoiceModeState.THINKING -> "Thinking with live weather context..."
                    VoiceModeState.IDLE -> "Tap orb or mic to speak"
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
