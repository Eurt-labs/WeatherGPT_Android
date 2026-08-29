package com.example.weathergpt_android.domain.voice.sherpa.model

data class SherpaAsrConfig(
    val modelType: String = "sherpa-onnx-streaming-zipformer-en",
    val encoderModelPath: String = "models/encoder.onnx",
    val decoderModelPath: String = "models/decoder.onnx",
    val joinerModelPath: String = "models/joiner.onnx",
    val tokensPath: String = "models/tokens.txt",
    val sampleRate: Int = 16000,
    val numThreads: Int = 2,
    val isModelLoaded: Boolean = false
)

data class SherpaTtsConfig(
    val modelType: String = "vits-piper-en",
    val vitsModelPath: String = "models/vits_model.onnx",
    val tokensPath: String = "models/vits_tokens.txt",
    val dataDir: String = "models/espeak-ng-data",
    val sampleRate: Int = 22050,
    val speed: Float = 1.0f,
    val isModelLoaded: Boolean = false
)

data class SherpaVadConfig(
    val sileroVadPath: String = "models/silero_vad.onnx",
    val threshold: Float = 0.5f,
    val minSpeechDurationMs: Float = 250f,
    val maxSpeechDurationS: Float = 15f
)

enum class SherpaLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val defaultSampleRate: Int = 16000
) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    MARATHI("mr", "Marathi", "मराठी"),
    BENGALI("bn", "Bengali", "বাংলা"),
    TAMIL("ta", "Tamil", "தமிழ்"),
    TELUGU("te", "Telugu", "తెలుగు")
}

data class SherpaTranscriptionResult(
    val text: String,
    val isFinal: Boolean,
    val confidence: Float = 0.95f,
    val language: SherpaLanguage = SherpaLanguage.ENGLISH
)
