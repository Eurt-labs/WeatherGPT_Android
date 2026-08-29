package com.example.weathergpt_android.domain.voice.ui

import android.Manifest
import android.content.pm.PackageManager
import android.speech.tts.TextToSpeech
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.weathergpt_android.core.network.OpenRouterPreferences
import com.example.weathergpt_android.core.network.OpenRouterService
import com.example.weathergpt_android.domain.location.model.LocationData
import com.example.weathergpt_android.domain.voice.sherpa.engine.SherpaOnnxEngine
import com.example.weathergpt_android.domain.voice.sherpa.model.SherpaLanguage
import kotlinx.coroutines.flow.catch
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
    val openRouterService = remember { OpenRouterService(context) }

    // Sherpa-ONNX Offline Speech Engine
    val sherpaEngine = remember { SherpaOnnxEngine(context, scope) }
    val isSherpaReady by sherpaEngine.isEngineReady.collectAsState()
    var selectedLanguage by remember { mutableStateOf(SherpaLanguage.ENGLISH) }

    var voiceState by remember { mutableStateOf(VoiceModeState.IDLE) }
    var userSpeechText by remember { mutableStateOf("") }
    var assistantSpeechText by remember {
        mutableStateOf("WeatherGPT Voice with Nemotron 3.5 is active. Tap the sphere or tap a question for low-latency voice answers.")
    }
    var isTtsMuted by remember { mutableStateOf(false) }

    // TextToSpeech Engine Integration
    var ttsEngine: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(selectedLanguage) {
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
            tts.stop()
            tts.shutdown()
            sherpaEngine.release()
        }
    }

    fun speakResponseChunk(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!isTtsMuted && isTtsReady && ttsEngine != null && text.isNotBlank()) {
            ttsEngine?.speak(text, queueMode, null, "weather_tts_id_${System.currentTimeMillis()}")
        }
    }

    fun stopSpeaking() {
        ttsEngine?.stop()
        sherpaEngine.stopStreamingSpeechRecognition()
    }

    // Ultra-Fast Conversational Streaming with Sentence-by-Sentence TTS Synthesis
    fun startLowLatencyConversation(prompt: String) {
        stopSpeaking()
        userSpeechText = prompt
        voiceState = VoiceModeState.THINKING
        assistantSpeechText = ""

        scope.launch {
            val apiKey = OpenRouterPreferences.getApiKey(context)

            if (apiKey.isBlank()) {
                // Fallback instant local answers if API Key is not yet set
                val fallbackResponse = when {
                    prompt.contains("walk", ignoreCase = true) ->
                        "In ${locationData.cityName}, temperatures are 24 degrees Celsius with a calm breeze. Evening walk conditions are ideal!"
                    prompt.contains("rain", ignoreCase = true) ->
                        "Radar shows clear skies over ${locationData.cityName}. Precipitation chance is under 5% today."
                    else ->
                        "Currently in ${locationData.cityName}, it's 24°C with pleasant skies, 52% humidity, and calm winds."
                }
                assistantSpeechText = fallbackResponse
                voiceState = VoiceModeState.SPEAKING
                speakResponseChunk(fallbackResponse, TextToSpeech.QUEUE_FLUSH)
                return@launch
            }

            var fullText = ""
            var sentenceBuffer = StringBuilder()
            var isFirstSentence = true

            openRouterService.streamChatCompletion(
                userMessage = prompt,
                locationContext = locationData.formattedLocation,
                weatherContext = "24°C, Clear Sky, Humidity 52%, Wind 14 km/h, AQI 34",
                isVoiceMode = true
            ).catch { err ->
                assistantSpeechText = "Error: ${err.message}"
                voiceState = VoiceModeState.IDLE
            }.collect { token ->
                fullText += token
                sentenceBuffer.append(token)
                assistantSpeechText = fullText

                // Detect sentence boundaries (. ? ! \n) for sub-second audio synthesis
                val currentBuffer = sentenceBuffer.toString()
                val sentenceEndIndex = currentBuffer.indexOfAny(charArrayOf('.', '!', '?', '\n'))

                if (sentenceEndIndex != -1) {
                    val completeSentence = currentBuffer.substring(0, sentenceEndIndex + 1).trim()
                    sentenceBuffer = StringBuilder(currentBuffer.substring(sentenceEndIndex + 1))

                    if (completeSentence.isNotEmpty()) {
                        voiceState = VoiceModeState.SPEAKING
                        speakResponseChunk(
                            completeSentence,
                            if (isFirstSentence) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                        )
                        isFirstSentence = false
                    }
                }
            }

            // Speak remaining buffer tail if any
            val remaining = sentenceBuffer.toString().trim()
            if (remaining.isNotEmpty()) {
                voiceState = VoiceModeState.SPEAKING
                speakResponseChunk(
                    remaining,
                    if (isFirstSentence) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                )
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            voiceState = VoiceModeState.LISTENING
            userSpeechText = "Listening in ${selectedLanguage.nativeName} (${locationData.cityName})..."
            sherpaEngine.startStreamingSpeechRecognition(
                onPartialResult = { partial -> userSpeechText = partial },
                onFinalResult = { final -> userSpeechText = final }
            )
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
        "Is today good for an evening walk?",
        "Will it rain anytime today?",
        "What should I wear for outside right now?",
        "Check air quality and UV safety"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = 18.dp,
            end = 18.dp,
            top = 16.dp,
            bottom = 96.dp // Clearance for bottom attached nav bar
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Engine Badge & Title with Low-Latency Indicator
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Memory,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isSherpaReady) "Sherpa-ONNX Active" else "Nemotron 3.5 Streaming",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFD1FAE5)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bolt,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "<400ms Audio Stream",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Weather Voice AI for ${locationData.cityName}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.4).sp
                )
            }
        }

        // Multilingual Language Selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SherpaLanguage.entries.forEach { lang ->
                    val isSelected = lang == selectedLanguage
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedLanguage = lang
                                sherpaEngine.setLanguage(lang)
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Language,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${lang.displayName} (${lang.nativeName})",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Center Fluid Morphing Voice Sphere
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                contentAlignment = Alignment.Center
            ) {
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
                                        sherpaEngine.stopStreamingSpeechRecognition()
                                        voiceState = VoiceModeState.IDLE
                                    }
                                    VoiceModeState.IDLE -> {
                                        voiceState = VoiceModeState.LISTENING
                                        userSpeechText = "Listening in ${selectedLanguage.nativeName}..."
                                        sherpaEngine.startStreamingSpeechRecognition(
                                            onPartialResult = { partial -> userSpeechText = partial },
                                            onFinalResult = { final -> userSpeechText = final }
                                        )
                                        // Trigger live stream answer
                                        startLowLatencyConversation("What is the live weather forecast right now?")
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
                    // Controls Header
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
                                text = "Live Conversational Stream",
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
                        text = if (assistantSpeechText.isEmpty() && voiceState == VoiceModeState.THINKING) "Streaming response from Nemotron 3.5..." else assistantSpeechText,
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
                    text = "Tap to Stream Voice Answer (${selectedLanguage.displayName})",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                quickConversations.forEach { prompt ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                startLowLatencyConversation(prompt)
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
