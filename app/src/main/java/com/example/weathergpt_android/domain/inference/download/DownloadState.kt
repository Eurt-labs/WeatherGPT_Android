package com.example.weathergpt_android.domain.inference.download

/**
 * State of the on-device GGUF model download process.
 */
sealed class DownloadState {
    data object Idle : DownloadState()
    data object CheckingSpace : DownloadState()
    data class Downloading(
        val progressPercent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val downloadSpeedMBs: Float = 0f
    ) : DownloadState()
    data class Paused(
        val progressPercent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : DownloadState()
    data object Verifying : DownloadState()
    data object Completed : DownloadState()
    data class Failed(val error: String) : DownloadState()
}
