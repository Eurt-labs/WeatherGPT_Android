package com.example.weathergpt_android.domain.voice.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathergpt_android.core.components.ConcentricPulsingOrb
import com.example.weathergpt_android.core.components.FrostedIconButton
import com.example.weathergpt_android.core.components.VoiceEdgeLighting
import com.example.weathergpt_android.core.network.OpenRouterService
import com.example.weathergpt_android.domain.assistant.data.ChatDatabaseHelper
import com.example.weathergpt_android.domain.assistant.model.ChatMessage
import com.example.weathergpt_android.domain.location.model.LocationData
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

enum class VoiceConversationState {
    LISTENING,
    THINKING,
    SPEAKING
}

/**
 * Immersive Voice AI Screen matching the exact UI in user's reference (Screenshot 3).
 * Features animated edge lighting, live subtitles, concentric pulsing orb, and a continuous
 * conversational loop powered by Google Gemini 2.5 Flash (Puck voice).
 */
@Composable
fun ImmersiveVoiceScreen(
    locationData: LocationData,
    liveWeatherData: LiveWeatherData,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val openRouterService = remember { OpenRouterService(context) }
    val dbHelper = remember { ChatDatabaseHelper.getInstance(context) }

    var conversationState by remember { mutableStateOf(VoiceConversationState.LISTENING) }
    var userTranscript by remember { mutableStateOf("Listening to your voice...") }
    var assistantResponse by remember { mutableStateOf("") }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    // Start Listening Helper Function
    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer?.startListening(intent)
        conversationState = VoiceConversationState.LISTENING
    }

    // Process user query with Gemini 2.5 Flash
    fun processVoiceQuery(query: String) {
        if (query.isBlank()) return
        conversationState = VoiceConversationState.THINKING
        assistantResponse = ""

        // Save user message to database
        scope.launch {
            dbHelper.saveMessage(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = query,
                    isUser = true,
                    timestamp = "Now"
                ),
                estimatedTokens = query.length / 4
            )
        }

        val weatherContext = "${liveWeatherData.temperature}, ${liveWeatherData.condition}, Humidity ${liveWeatherData.humidity}, Wind ${liveWeatherData.windSpeed}, AQI ${liveWeatherData.aqi}"

        scope.launch {
            var fullAnswer = ""
            openRouterService.streamChatCompletion(
                userMessage = query,
                locationContext = "${locationData.cityName}, ${locationData.country}",
                weatherContext = weatherContext,
                isVoiceMode = true
            ).catch { err ->
                assistantResponse = "Connection error. Please try again."
                conversationState = VoiceConversationState.LISTENING
                startListening()
            }.collect { token ->
                val cleanToken = sanitizeVoiceToken(token)
                if (cleanToken.isNotEmpty()) {
                    fullAnswer += cleanToken
                    val displayAnswer = stripRawJsonArtifacts(fullAnswer)
                    assistantResponse = displayAnswer
                }
            }

            val cleanToSpeak = stripRawJsonArtifacts(fullAnswer)
            // Save assistant message to database
            dbHelper.saveMessage(
                ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = cleanToSpeak,
                    isUser = false,
                    timestamp = "Now"
                ),
                estimatedTokens = cleanToSpeak.length / 4
            )

            // Speak response using Aoede Voice profile
            if (cleanToSpeak.isNotBlank()) {
                conversationState = VoiceConversationState.SPEAKING
                textToSpeech?.speak(cleanToSpeak, TextToSpeech.QUEUE_FLUSH, null, "WEATHER_VOICE_UTTERANCE")
            } else {
                conversationState = VoiceConversationState.LISTENING
                startListening()
            }
        }
    }

    // Initialize TTS and SpeechRecognizer
    DisposableEffect(Unit) {
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer = recognizer

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                // If timed out or error while idle, restart listening automatically
                if (conversationState == VoiceConversationState.LISTENING) {
                    startListening()
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull() ?: ""
                if (recognizedText.isNotBlank()) {
                    userTranscript = recognizedText
                    processVoiceQuery(recognizedText)
                } else {
                    startListening()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                if (!partial.isNullOrBlank()) {
                    userTranscript = partial
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                tts?.language = Locale.getDefault()

                // Configure Aoede Voice Profile (Warm, clear, melodic and natural)
                val targetVoice = tts?.voices?.find { voice ->
                    !voice.isNetworkConnectionRequired &&
                    (voice.name.contains("female", ignoreCase = true) ||
                     voice.name.contains("en-us-x-sfg", ignoreCase = true) ||
                     voice.name.contains("en-in-x-cfl", ignoreCase = true) ||
                     voice.name.contains("en-us-x-tpd", ignoreCase = true))
                } ?: tts?.voices?.firstOrNull { it.locale.language == Locale.getDefault().language }

                if (targetVoice != null) {
                    tts?.voice = targetVoice
                }
                tts?.setPitch(1.08f)
                tts?.setSpeechRate(1.0f)

                // Listener for Continuous Conversation Loop:
                // When TTS finishes speaking, immediately re-arm SpeechRecognizer!
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        conversationState = VoiceConversationState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        // CRITICAL FIX: Automatically return to LISTENING so the user can speak again!
                        scope.launch {
                            userTranscript = "Listening..."
                            conversationState = VoiceConversationState.LISTENING
                            startListening()
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        scope.launch {
                            conversationState = VoiceConversationState.LISTENING
                            startListening()
                        }
                    }
                })

                // Kick off initial listening
                startListening()
            }
        }
        textToSpeech = tts

        onDispose {
            recognizer.destroy()
            tts.stop()
            tts.shutdown()
        }
    }

    // Voice Edge Lighting Wrapper (Screen 3)
    VoiceEdgeLighting(
        isActive = conversationState == VoiceConversationState.LISTENING || conversationState == VoiceConversationState.SPEAKING
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF07080B))
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Back Button & AI Sparkle Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FrostedIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    onClick = onClose,
                    size = 42.dp,
                    isDark = true
                )

                // Neon Sparkle Badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x359333EA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "AI Voice Engine",
                        tint = Color(0xFFE879F9),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Central Subtitle & Spoken Speech Stream (Matching Screenshot 3)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = when (conversationState) {
                        VoiceConversationState.LISTENING -> userTranscript
                        VoiceConversationState.THINKING -> "Thinking with Voice AI..."
                        VoiceConversationState.SPEAKING -> assistantResponse
                    },
                    fontSize = 22.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.88f)
                )
            }

            // State Label ("Listening...", "Speaking...")
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = when (conversationState) {
                        VoiceConversationState.LISTENING -> "Listening..."
                        VoiceConversationState.THINKING -> "Voice AI is thinking..."
                        VoiceConversationState.SPEAKING -> "Voice AI is speaking..."
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.55f)
                )

                // Concentric Pulsing Mic Orb
                ConcentricPulsingOrb(
                    isActive = conversationState == VoiceConversationState.LISTENING || conversationState == VoiceConversationState.SPEAKING,
                    onClick = {
                        if (conversationState == VoiceConversationState.SPEAKING) {
                            textToSpeech?.stop()
                            startListening()
                        } else {
                            startListening()
                        }
                    },
                    size = 84.dp
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun sanitizeVoiceToken(raw: String): String {
    if (!raw.contains("{\"id\":") && !raw.contains("data:") && !raw.contains("\"choices\":") && !raw.contains("\"object\":")) {
        return raw
    }
    return try {
        val match = Regex("\"content\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").find(raw)
        match?.groupValues?.get(1)
            ?.replace("\\n", "\n")
            ?.replace("\\\"", "\"")
            ?.replace("\\\\", "\\") ?: ""
    } catch (_: Exception) {
        ""
    }
}

private fun stripRawJsonArtifacts(text: String): String {
    return text
        .replace(Regex("data:\\s*\\{.*?\\}", RegexOption.DOT_MATCHES_ALL), "")
        .replace(Regex("\\{\"id\":.*?\\}", RegexOption.DOT_MATCHES_ALL), "")
        .replace("data:", "")
        .trimEnd()
}
