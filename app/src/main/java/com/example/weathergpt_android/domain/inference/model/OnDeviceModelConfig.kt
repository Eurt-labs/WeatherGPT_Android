package com.example.weathergpt_android.domain.inference.model

/**
 * Configuration parameters for on-device GGUF model execution.
 * Tuned for optimal performance, quality, and thermal safety on mobile SoCs.
 */
data class OnDeviceModelConfig(
    val modelFileName: String = "qwen2.5-1.5b-instruct-q4_k_m.gguf",
    val downloadUrl: String = "https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf",
    val fileSizeBytes: Long = 1_117_000_000L, // ~1.04 GB
    val modelDisplayName: String = "Qwen 2.5 1.5B Instruct (Q4_K_M)",
    val nCtx: Int = 2048,          // Mobile context window (prevents OOM while holding full weather grounding)
    val nThreads: Int = 4,         // Targeted for big performance cores
    val maxTokens: Int = 150,      // Enforces AGENTS.md brevity guardrail (3-4 sentences)
    val temperature: Float = 0.7f,
    val topP: Float = 0.9f,
    val repeatPenalty: Float = 1.1f
) {
    companion object {
        val DEFAULT = OnDeviceModelConfig()
    }
}
