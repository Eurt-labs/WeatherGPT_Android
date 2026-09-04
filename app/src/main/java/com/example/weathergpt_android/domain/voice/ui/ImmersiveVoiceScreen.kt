package com.example.weathergpt_android.domain.voice.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
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
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.weathergpt_android.domain.auth.data.UserPreferences
import com.example.weathergpt_android.domain.auth.model.UserProfile
import com.example.weathergpt_android.domain.location.model.LocationData
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

enum class VoiceConversationState {
    LISTENING,
    THINKING,
    SPEAKING,
    MUTED
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
    val userProfile = remember { UserPreferences.getProfile(context) }

    var conversationState by remember { mutableStateOf(VoiceConversationState.LISTENING) }
    var userTranscript by remember { mutableStateOf("Listening to your voice...") }
    var assistantResponse by remember { mutableStateOf("") }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var speechEnergy by remember { mutableFloatStateOf(0f) }
    var conversationProgress by remember { mutableFloatStateOf(0.10f) }

    // Linear Edge Light: dynamically expands from center bezel outwards as conversation goes
    LaunchedEffect(conversationState) {
        while (isActive) {
            delay(150)
            when (conversationState) {
                VoiceConversationState.LISTENING -> {
                    if (speechEnergy > 0.08f) {
                        conversationProgress = (conversationProgress + 0.012f).coerceAtMost(1f)
                    }
                }
                VoiceConversationState.THINKING -> {
                    conversationProgress = (conversationProgress + 0.005f).coerceAtMost(1f)
                }
                VoiceConversationState.SPEAKING -> {
                    conversationProgress = (conversationProgress + 0.009f).coerceAtMost(1f)
                }
                VoiceConversationState.MUTED -> {
                    // Suspended while muted
                }
            }
        }
    }

    // Start Listening Helper Function with Multilingual Indian Language support
    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) return
        val targetLocaleTag = when (userProfile.preferredLanguage.lowercase()) {
            "hi" -> "hi-IN"
            "mr" -> "mr-IN"
            "bn" -> "bn-IN"
            "ta" -> "ta-IN"
            "te" -> "te-IN"
            "gu" -> "gu-IN"
            else -> "en-IN"
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, targetLocaleTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, targetLocaleTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("hi-IN", "en-IN", "mr-IN", "bn-IN", "ta-IN", "te-IN", "gu-IN"))
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
        conversationProgress = (conversationProgress + 0.12f).coerceAtMost(1f)

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

        val weatherContext = liveWeatherData.toDenseMeteorologicalContext()

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

            // Speak response using native Indian voice tuned to the language spoken
            if (cleanToSpeak.isNotBlank()) {
                if (conversationState != VoiceConversationState.MUTED) {
                    conversationState = VoiceConversationState.SPEAKING
                    textToSpeech?.let { ttsInstance ->
                        val detectedLocale = detectVoiceLocale(cleanToSpeak, userProfile.preferredLanguage)
                        applyNativeIndianVoice(ttsInstance, detectedLocale)
                        val spokenText = prepareVoiceTextForSpeech(cleanToSpeak)
                        ttsInstance.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, "WEATHER_VOICE_UTTERANCE")
                    }
                }
            } else {
                if (conversationState != VoiceConversationState.MUTED) {
                    conversationState = VoiceConversationState.LISTENING
                    startListening()
                }
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
            override fun onRmsChanged(rmsdB: Float) {
                if (conversationState == VoiceConversationState.LISTENING) {
                    speechEnergy = ((rmsdB + 2f) / 10f).coerceIn(0f, 1f)
                }
            }
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
                    val wordCount = partial.split(" ").filter { it.isNotBlank() }.size
                    conversationProgress = (conversationProgress + wordCount * 0.003f).coerceAtMost(1f)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                val initialLocale = detectVoiceLocale("", userProfile.preferredLanguage)
                applyNativeIndianVoice(tts, initialLocale)

                // Listener for Continuous Conversation Loop:
                // When TTS finishes speaking, immediately re-arm SpeechRecognizer!
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        conversationState = VoiceConversationState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        // CRITICAL FIX: Automatically return to LISTENING so the user can speak again!
                        scope.launch {
                            if (conversationState != VoiceConversationState.MUTED) {
                                userTranscript = "Listening..."
                                conversationState = VoiceConversationState.LISTENING
                                startListening()
                            }
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        scope.launch {
                            if (conversationState != VoiceConversationState.MUTED) {
                                conversationState = VoiceConversationState.LISTENING
                                startListening()
                            }
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
        isActive = conversationState != VoiceConversationState.MUTED,
        speechEnergy = when (conversationState) {
            VoiceConversationState.SPEAKING -> 0.65f
            VoiceConversationState.THINKING -> 0.22f
            VoiceConversationState.LISTENING -> speechEnergy
            VoiceConversationState.MUTED -> 0f
        },
        conversationProgress = conversationProgress
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
                        VoiceConversationState.MUTED -> if (assistantResponse.isNotBlank()) assistantResponse else "Microphone muted. Tap the button below to resume."
                    },
                    fontSize = 22.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.88f)
                )
            }

            // State Label ("Listening...", "Speaking...", "Muted")
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
                        VoiceConversationState.MUTED -> "Microphone Muted · Tap to speak"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (conversationState == VoiceConversationState.MUTED) Color(0xFFF87171) else Color.White.copy(alpha = 0.55f)
                )

                // Concentric Pulsing Mic Orb with Pause on Speak & Mute on Click
                ConcentricPulsingOrb(
                    isActive = conversationState == VoiceConversationState.LISTENING || conversationState == VoiceConversationState.SPEAKING,
                    isMuted = conversationState == VoiceConversationState.MUTED,
                    onClick = {
                        when (conversationState) {
                            VoiceConversationState.SPEAKING -> {
                                // Pause speech immediately and mute
                                textToSpeech?.stop()
                                speechRecognizer?.stopListening()
                                speechEnergy = 0f
                                conversationState = VoiceConversationState.MUTED
                            }
                            VoiceConversationState.LISTENING -> {
                                // Mute microphone
                                speechRecognizer?.stopListening()
                                speechEnergy = 0f
                                conversationState = VoiceConversationState.MUTED
                            }
                            VoiceConversationState.THINKING -> {
                                speechRecognizer?.stopListening()
                                speechEnergy = 0f
                                conversationState = VoiceConversationState.MUTED
                            }
                            VoiceConversationState.MUTED -> {
                                // Unmute and resume listening
                                userTranscript = "Listening to your voice..."
                                startListening()
                            }
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

/**
 * Intelligent Script and Language Detector.
 * Analyzes whether the spoken text contains Indic scripts (Devanagari, Bengali, Tamil, Telugu, Gujarati)
 * and selects the native regional Locale, falling back to the user's preferred language or Indian English.
 */
private fun detectVoiceLocale(text: String, preferredLangCode: String): Locale {
    var hasDevanagari = false
    var hasBengali = false
    var hasTamil = false
    var hasTelugu = false
    var hasGujarati = false

    for (ch in text) {
        when (ch.code) {
            in 0x0900..0x097F -> hasDevanagari = true
            in 0x0980..0x09FF -> hasBengali = true
            in 0x0B80..0x0BFF -> hasTamil = true
            in 0x0C00..0x0C7F -> hasTelugu = true
            in 0x0A80..0x0AFF -> hasGujarati = true
        }
    }

    return when {
        hasDevanagari -> if (preferredLangCode.equals("mr", ignoreCase = true)) Locale("mr", "IN") else Locale("hi", "IN")
        hasBengali -> Locale("bn", "IN")
        hasTamil -> Locale("ta", "IN")
        hasTelugu -> Locale("te", "IN")
        hasGujarati -> Locale("gu", "IN")
        else -> when (preferredLangCode.lowercase()) {
            "hi" -> Locale("hi", "IN")
            "mr" -> Locale("mr", "IN")
            "bn" -> Locale("bn", "IN")
            "ta" -> Locale("ta", "IN")
            "te" -> Locale("te", "IN")
            "gu" -> Locale("gu", "IN")
            else -> Locale("en", "IN") // Indian English for natural regional cadence
        }
    }
}

/**
 * Program the TextToSpeech engine with native high-quality regional female voice profiles.
 * Finds Google TTS female neural voices for Indian languages (e.g. hi-in-x-hie-local, mr-in-x-mrd-local, en-in-x-cfl-local)
 * and applies warm, sweet female pitch and native human conversational pacing.
 */
private fun applyNativeIndianVoice(tts: TextToSpeech?, targetLocale: Locale) {
    if (tts == null) return
    try {
        val status = tts.isLanguageAvailable(targetLocale)
        if (status >= TextToSpeech.LANG_AVAILABLE) {
            tts.language = targetLocale
        }

        val matchingVoices = tts.voices?.filter { voice ->
            voice.locale.language.equals(targetLocale.language, ignoreCase = true)
        } ?: emptyList()

        // Known high-fidelity Google female neural voices for Indian languages
        val knownFemaleSignatures = listOf(
            "x-hie", "x-hid", "x-hia", "x-hif", // Hindi female neural
            "x-cfl", "x-ene", "x-end", "x-ena", // Indian English female
            "x-mrd", "x-mra",                   // Marathi female
            "x-bnd", "x-bna",                   // Bengali female
            "x-tad", "x-taa",                   // Tamil female
            "x-ted", "x-tea",                   // Telugu female
            "x-gud", "x-gua",                   // Gujarati female
            "x-pad", "x-paa"                    // Punjabi female
        )

        val bestVoice = matchingVoices.maxByOrNull { voice ->
            var score = 0
            val vName = voice.name.lowercase(Locale.ROOT)

            // 1. Strongly prioritize proven female neural profiles
            if (knownFemaleSignatures.any { vName.contains(it) }) score += 80
            if (vName.contains("female") || vName.contains("woman") || vName.contains("fem")) score += 50

            // 2. Strongly penalize male profiles so male voices are never selected
            if (vName.contains("male") || vName.contains("man") ||
                vName.contains("x-hic") || vName.contains("x-enc") || vName.contains("x-mrc") ||
                vName.contains("x-bnc") || vName.contains("x-tac") || vName.contains("x-tec") ||
                vName.contains("x-guc") || vName.contains("x-pac")) {
                score -= 100
            }

            // 3. Indian country accent priority (especially for English)
            if (voice.locale.country.equals("IN", ignoreCase = true)) score += 30

            // 4. Prefer local on-device voice (no network latency)
            if (!voice.isNetworkConnectionRequired) score += 20

            // 5. Higher quality level
            if (voice.quality >= Voice.QUALITY_HIGH) score += 15

            score
        } ?: matchingVoices.firstOrNull { voice ->
            voice.locale.country.equals("IN", ignoreCase = true)
        } ?: matchingVoices.firstOrNull()

        if (bestVoice != null) {
            tts.voice = bestVoice
        }

        // Indian female voice cadence & natural inflection tuning:
        // Slightly raised pitch (1.10f - 1.12f) ensures clear, warm female formant.
        // Pacing (0.95f for Indian languages, 0.98f for English) provides fluent, native articulation.
        val isIndianRegional = targetLocale.language != "en"
        tts.setPitch(if (isIndianRegional) 1.12f else 1.08f)
        tts.setSpeechRate(if (isIndianRegional) 0.95f else 0.98f)
    } catch (_: Exception) {
        tts.language = targetLocale
    }
}

/**
 * Expands meteorological units and cleans markdown so text-to-speech speaks fluent, natural sentences.
 */
private fun prepareVoiceTextForSpeech(raw: String): String {
    var text = raw
        .replace(Regex("[*_~#`]"), "")
        .replace(Regex("data:\\s*\\{.*?\\}", RegexOption.DOT_MATCHES_ALL), "")
        .replace("data:", "")

    val isDevanagari = text.any { it.code in 0x0900..0x097F }

    text = if (isDevanagari) {
        text.replace("°C", " डिग्री सेल्सियस")
            .replace("°", " डिग्री")
            .replace("km/h", " किलोमीटर प्रति घंटा")
            .replace("mm/day", " मिलीमीटर प्रतिदिन")
            .replace("mm/h", " मिलीमीटर प्रति घंटा")
            .replace("mm", " मिलीमीटर")
            .replace("%", " प्रतिशत")
    } else {
        text.replace("°C", " degrees Celsius")
            .replace("°", " degrees")
            .replace("km/h", " kilometers per hour")
            .replace("mm/day", " millimeters per day")
            .replace("mm/h", " millimeters per hour")
            .replace("mm", " millimeters")
            .replace("%", " percent")
    }

    return text
        .replace(Regex("\\n{2,}"), ". ")
        .replace("\n", " ")
        .trim()
}

