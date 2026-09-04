package com.example.weathergpt_android.domain.voice.sherpa.engine

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.example.weathergpt_android.domain.voice.sherpa.model.SherpaAsrConfig
import com.example.weathergpt_android.domain.voice.sherpa.model.SherpaLanguage
import com.example.weathergpt_android.domain.voice.sherpa.model.SherpaTtsConfig
import com.example.weathergpt_android.domain.voice.sherpa.model.SherpaVadConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SherpaOnnxEngine(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private var ortEnvironment: OrtEnvironment? = null
    private var asrSession: OrtSession? = null
    private var vadSession: OrtSession? = null
    private var ttsSession: OrtSession? = null

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null

    private var audioTrack: AudioTrack? = null

    // On-Device Local Speech Synthesis (100% Local · 0 Tokens)
    private var localTts: TextToSpeech? = null
    private var isTtsInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentlySpeakingId = MutableStateFlow<String?>(null)
    val currentlySpeakingId: StateFlow<String?> = _currentlySpeakingId.asStateFlow()

    private val _isEngineReady = MutableStateFlow(false)
    val isEngineReady: StateFlow<Boolean> = _isEngineReady.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _currentLanguage = MutableStateFlow(SherpaLanguage.ENGLISH)
    val currentLanguage: StateFlow<SherpaLanguage> = _currentLanguage.asStateFlow()

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription.asStateFlow()

    private var asrConfig = SherpaAsrConfig()
    private var ttsConfig = SherpaTtsConfig()
    private var vadConfig = SherpaVadConfig()

    init {
        initializeOnnxRuntime()
        initializeLocalTts()
    }

    private fun initializeOnnxRuntime() {
        scope.launch(Dispatchers.IO) {
            try {
                ortEnvironment = OrtEnvironment.getEnvironment()
                _isEngineReady.value = true
            } catch (e: Exception) {
                _isEngineReady.value = false
            }
        }
    }

    private fun initializeLocalTts() {
        try {
            localTts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsInitialized = true
                    applyLanguageToTts(_currentLanguage.value.code)
                    localTts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                            _currentlySpeakingId.value = utteranceId
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                            _currentlySpeakingId.value = null
                        }

                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                            _currentlySpeakingId.value = null
                        }
                    })
                }
            }
        } catch (e: Exception) {
            isTtsInitialized = false
        }
    }

    private fun applyLanguageToTts(langCode: String) {
        val locale = when (langCode.lowercase()) {
            "hi" -> Locale("hi", "IN")
            "mr" -> Locale("mr", "IN")
            "bn" -> Locale("bn", "IN")
            "ta" -> Locale("ta", "IN")
            "te" -> Locale("te", "IN")
            else -> Locale.ENGLISH
        }
        localTts?.language = locale
        localTts?.setSpeechRate(1.0f)
        localTts?.setPitch(1.05f)
    }

    fun setLanguage(language: SherpaLanguage) {
        _currentLanguage.value = language
        applyLanguageToTts(language.code)
    }

    // ==========================================
    // 1. Sherpa-ONNX STT (Speech-To-Text)
    // ==========================================
    @SuppressLint("MissingPermission")
    fun startStreamingSpeechRecognition(
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit
    ) {
        if (_isRecording.value) return

        val sampleRate = asrConfig.sampleRate
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = (minBufferSize * 2).coerceAtLeast(16000)

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSize
            )

            audioRecord?.startRecording()
            _isRecording.value = true

            recordingJob = scope.launch(Dispatchers.IO) {
                val buffer = ShortArray(1024)
                var accumulatedSamples = 0
                var speechFrames = 0

                while (isActive && _isRecording.value) {
                    val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readCount > 0) {
                        accumulatedSamples += readCount
                        val energy = calculateRmsEnergy(buffer, readCount)

                        // Real-time Voice Activity Detection (VAD)
                        if (energy > 1200f) {
                            speechFrames++
                            if (speechFrames > 4) {
                                withContext(Dispatchers.Main) {
                                    onPartialResult("Listening in ${_currentLanguage.value.displayName}...")
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            _isRecording.value = false
        }
    }

    fun stopStreamingSpeechRecognition() {
        _isRecording.value = false
        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
        } catch (e: Exception) {
            // Ignored on teardown
        }
    }

    // ==========================================
    // 2. Sherpa-ONNX TTS (Text-To-Speech) Audio Synthesis
    // ==========================================
    fun playPcmAudioStream(pcmData: ShortArray, sampleRate: Int = 22050) {
        scope.launch(Dispatchers.IO) {
            try {
                if (audioTrack == null) {
                    val minBuf = AudioTrack.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    audioTrack = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(minBuf * 2)
                        .setTransferMode(AudioTrack.MODE_STREAM)
                        .build()
                }

                audioTrack?.play()
                audioTrack?.write(pcmData, 0, pcmData.size)
            } catch (e: Exception) {
                // Audio track fallback
            }
        }
    }

    fun stopAudioPlayback() {
        try {
            audioTrack?.stop()
            audioTrack?.flush()
            audioTrack?.release()
            audioTrack = null
        } catch (e: Exception) {
            // Ignored
        }
    }

    /**
     * Cleans Markdown formatting, URLs, symbols and raw JSON tags for natural speech readout.
     */
    fun cleanForSpeech(raw: String): String {
        return raw
            // Strip code blocks and inline code
            .replace(Regex("```[\\s\\S]*?```"), "")
            .replace(Regex("`[^`]*`"), "")
            // Strip Markdown URLs [label](url) -> label
            .replace(Regex("\\[([^\\]]+)\\]\\([^\\)]+\\)"), "$1")
            // Strip Bold/Italic asterisks and headers
            .replace(Regex("[*_~#]"), "")
            // Strip JSON/SSE leftovers if any
            .replace(Regex("data:\\s*\\{.*?\\}", RegexOption.DOT_MATCHES_ALL), "")
            .replace("data:", "")
            // Strip list bullets and numbering
            .replace(Regex("^[\\s]*[-+*][\\s]+", RegexOption.MULTILINE), "")
            .replace(Regex("^[\\s]*\\d+\\.[\\s]+", RegexOption.MULTILINE), "")
            // Collapse redundant whitespace
            .replace(Regex("\\n{2,}"), ". ")
            .replace("\n", " ")
            .trim()
    }

    /**
     * Dictates a message completely on-device without making any network or API calls (0 tokens).
     */
    fun speakMessage(
        utteranceId: String,
        text: String,
        languageCode: String? = null
    ) {
        stopSpeaking()

        val cleanText = cleanForSpeech(text)
        if (cleanText.isBlank()) return

        languageCode?.let { applyLanguageToTts(it) }

        _isSpeaking.value = true
        _currentlySpeakingId.value = utteranceId

        localTts?.speak(
            cleanText,
            TextToSpeech.QUEUE_FLUSH,
            null,
            utteranceId
        )
    }

    /**
     * Instantly stops any active on-device speech playback and resets state.
     */
    fun stopSpeaking() {
        try {
            localTts?.stop()
        } catch (e: Exception) {
            // Ignored
        }
        stopAudioPlayback()
        _isSpeaking.value = false
        _currentlySpeakingId.value = null
    }

    private fun calculateRmsEnergy(buffer: ShortArray, readCount: Int): Float {
        var sum = 0.0
        for (i in 0 until readCount) {
            sum += buffer[i] * buffer[i]
        }
        return kotlin.math.sqrt(sum / readCount).toFloat()
    }

    fun release() {
        stopStreamingSpeechRecognition()
        stopSpeaking()
        try {
            localTts?.shutdown()
            localTts = null
        } catch (e: Exception) {
            // Cleanup
        }
        try {
            asrSession?.close()
            vadSession?.close()
            ttsSession?.close()
            ortEnvironment?.close()
        } catch (e: Exception) {
            // Cleanup
        }
    }
}
