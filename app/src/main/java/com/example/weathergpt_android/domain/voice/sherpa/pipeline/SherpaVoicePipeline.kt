package com.example.weathergpt_android.domain.voice.sherpa.pipeline

import android.content.Context
import com.example.weathergpt_android.core.network.OpenRouterService
import com.example.weathergpt_android.domain.location.model.LocationData
import com.example.weathergpt_android.domain.voice.sherpa.engine.SherpaOnnxEngine
import com.example.weathergpt_android.domain.voice.sherpa.model.SherpaLanguage
import com.example.weathergpt_android.domain.weather.model.LiveWeatherData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * Full-Duplex Weather Voice Intelligence Pipeline:
 * sherpa-onnx (STT) -> OpenRouter API (Nemotron 3.5 Streaming with Live Weather Context) -> sherpa-onnx (TTS / Speech Synthesis)
 */
class SherpaVoicePipeline(
    private val context: Context,
    private val scope: CoroutineScope,
    val sherpaEngine: SherpaOnnxEngine = SherpaOnnxEngine(context, scope),
    val openRouterService: OpenRouterService = OpenRouterService(context)
) {
    /**
     * Executes end-to-end voice query turn with live fetched weather context:
     * 1. Takes input speech / text from Sherpa-ONNX STT
     * 2. Injects live OpenWeather atmospheric data
     * 3. Streams reasoning from OpenRouter API (Nemotron 3.5)
     * 4. Streams audio playback via Sherpa-ONNX / TTS in sub-second sentence chunks
     */
    fun processVoiceTurn(
        userPrompt: String,
        locationData: LocationData,
        liveWeatherData: LiveWeatherData,
        onTranscriptionUpdate: (String) -> Unit,
        onAiTextChunk: (String) -> Unit,
        onTtsSentenceChunk: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        onTranscriptionUpdate(userPrompt)

        val weatherContextSummary = liveWeatherData.toDenseMeteorologicalContext()

        scope.launch {
            var fullText = ""
            var sentenceBuffer = StringBuilder()

            openRouterService.streamChatCompletion(
                userMessage = userPrompt,
                locationContext = locationData.denseLocationContext,
                weatherContext = weatherContextSummary,
                isVoiceMode = true
            ).catch { e ->
                onError(e.message ?: "AI voice streaming failed")
            }.collect { token ->
                fullText += token
                sentenceBuffer.append(token)
                onAiTextChunk(fullText)

                // Detect sentence boundaries for instant low-latency audio synthesis
                val current = sentenceBuffer.toString()
                val sentenceEnd = current.indexOfAny(charArrayOf('.', '!', '?', '\n'))

                if (sentenceEnd != -1) {
                    val sentence = current.substring(0, sentenceEnd + 1).trim()
                    sentenceBuffer = StringBuilder(current.substring(sentenceEnd + 1))
                    if (sentence.isNotEmpty()) {
                        onTtsSentenceChunk(sentence)
                    }
                }
            }

            val remaining = sentenceBuffer.toString().trim()
            if (remaining.isNotEmpty()) {
                onTtsSentenceChunk(remaining)
            }
        }
    }

    fun release() {
        sherpaEngine.release()
    }
}
