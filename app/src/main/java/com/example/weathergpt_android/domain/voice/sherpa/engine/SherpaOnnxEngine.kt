package com.example.weathergpt_android.domain.voice.sherpa.engine

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

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

    fun setLanguage(language: SherpaLanguage) {
        _currentLanguage.value = language
    }

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

                while (isActive && _isRecording.value) {
                    val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (readCount > 0) {
                        accumulatedSamples += readCount
                        // Streaming VAD / ASR Feature extraction
                        val energy = calculateRmsEnergy(buffer, readCount)
                        if (energy > 1200f) {
                            withContext(Dispatchers.Main) {
                                onPartialResult("Listening (${_currentLanguage.value.displayName})...")
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

    private fun calculateRmsEnergy(buffer: ShortArray, readCount: Int): Float {
        var sum = 0.0
        for (i in 0 until readCount) {
            sum += buffer[i] * buffer[i]
        }
        return kotlin.math.sqrt(sum / readCount).toFloat()
    }

    fun release() {
        stopStreamingSpeechRecognition()
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
