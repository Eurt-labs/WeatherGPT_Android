package com.example.weathergpt_android.domain.inference.router

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.weathergpt_android.core.network.BackendConfig
import com.example.weathergpt_android.core.network.OpenRouterService
import com.example.weathergpt_android.domain.inference.engine.OnDeviceEngine
import com.example.weathergpt_android.domain.inference.model.InferenceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Unified AI routing engine for WeatherGPT.
 * Automatically arbitrates between Cloud Gemini 3.6 Flash, Local PC Server,
 * and On-Device Offline SLM with seamless auto-fallback.
 */
class InferenceRouter(private val context: Context) {
    val openRouterService = OpenRouterService(context)
    val onDeviceEngine = OnDeviceEngine.getInstance(context)

    fun isNetworkAvailable(): Boolean {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return false
            val network = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Determines the active inference mode based on user preferences.
     */
    fun getActiveMode(): InferenceMode {
        val configuredMode = BackendConfig.getBackendMode(context)

        return when (configuredMode) {
            BackendConfig.MODE_ON_DEVICE -> InferenceMode.ON_DEVICE
            BackendConfig.MODE_LOCAL_USB,
            BackendConfig.MODE_LOCAL_EMULATOR,
            BackendConfig.MODE_LOCAL_CUSTOM -> InferenceMode.PC_SERVER
            else -> InferenceMode.CLOUD
        }
    }

    /**
     * Reactive flow that emits immediately whenever backend mode is changed in Settings.
     */
    fun getActiveModeFlow(): Flow<InferenceMode> {
        return BackendConfig.backendModeFlow.map {
            getActiveMode()
        }
    }

    /**
     * Single unified streaming chat interface for both GptChatScreen and VoiceAiScreen.
     * Guaranteed to emit standard token stream.
     */
    fun streamChat(
        userMessage: String,
        locationContext: String = "Live Location",
        weatherContext: String = "",
        history: List<Pair<String, String>> = emptyList(),
        isVoiceMode: Boolean = false
    ): Flow<String> = flow {
        val activeMode = getActiveMode()

        when (activeMode) {
            InferenceMode.ON_DEVICE -> {
                onDeviceEngine.generateStream(
                    userMessage = userMessage,
                    locationContext = locationContext,
                    weatherContext = weatherContext,
                    history = history,
                    isVoiceMode = isVoiceMode
                ).collect { token ->
                    emit(token)
                }
            }
            InferenceMode.PC_SERVER -> {
                openRouterService.streamChatCompletion(
                    userMessage = userMessage,
                    locationContext = locationContext,
                    weatherContext = weatherContext,
                    history = history,
                    isVoiceMode = isVoiceMode
                ).collect { token ->
                    emit(token)
                }
            }
            InferenceMode.CLOUD -> {
                // Online mode active: check if idle offline model should be released from RAM
                onDeviceEngine.checkOnlineIdleThreshold()

                var emittedAny = false
                var failedWithNetwork = false

                openRouterService.streamChatCompletion(
                    userMessage = userMessage,
                    locationContext = locationContext,
                    weatherContext = weatherContext,
                    history = history,
                    isVoiceMode = isVoiceMode
                ).catch { error ->
                    failedWithNetwork = true
                    Log.w("InferenceRouter", "Cloud stream error: ${error.localizedMessage}")
                }.collect { token ->
                    emittedAny = true
                    emit(token)
                }

                // If cloud completely failed before emitting any token,
                // and auto-fallback is enabled and on-device model is ready, fall back!
                if (!emittedAny && (failedWithNetwork || !isNetworkAvailable())) {
                    if (BackendConfig.isAutoFallbackEnabled(context) && onDeviceEngine.isModelReady) {
                        Log.i("InferenceRouter", "Triggering seamless offline auto-fallback to OnDeviceEngine.")
                        onDeviceEngine.generateStream(
                            userMessage = userMessage,
                            locationContext = locationContext,
                            weatherContext = weatherContext,
                            history = history,
                            isVoiceMode = isVoiceMode
                        ).collect { token ->
                            emit(token)
                        }
                    } else if (failedWithNetwork) {
                        emit("⚠️ Connection error: Unable to reach AI server. Please check internet connection or download the On-Device model in Settings.")
                    }
                }
            }
        }
    }
}
