package com.example.weathergpt_android.domain.inference.download

import android.content.Context
import android.os.StatFs
import com.example.weathergpt_android.domain.inference.model.OnDeviceModelConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.concurrent.TimeUnit

/**
 * Robust, resumable download manager for on-device GGUF language models.
 * Stores weights securely in internal app storage (context.filesDir/models/).
 */
class ModelDownloadManager(
    private val context: Context,
    private val config: OnDeviceModelConfig = OnDeviceModelConfig.DEFAULT
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val _downloadState = MutableStateFlow<DownloadState>(
        if (isModelDownloaded()) DownloadState.Completed else DownloadState.Idle
    )
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private var downloadJob: Job? = null

    val modelsDir: File
        get() = File(context.filesDir, "models").apply { if (!exists()) mkdirs() }

    val modelFile: File
        get() = File(modelsDir, config.modelFileName)

    private val tempFile: File
        get() = File(modelsDir, "${config.modelFileName}.part")

    fun isModelDownloaded(): Boolean {
        val file = File(modelsDir, config.modelFileName)
        // Check existence and reasonable minimum size (at least 90% of expected size)
        return file.exists() && file.length() >= (config.fileSizeBytes * 0.90)
    }

    fun getModelFileSizeMB(): Long {
        val file = modelFile
        return if (file.exists()) file.length() / (1024 * 1024) else 0L
    }

    fun getAvailableStorageBytes(): Long {
        return try {
            val stat = StatFs(context.filesDir.absolutePath)
            stat.availableBlocksLong * stat.blockSizeLong
        } catch (_: Exception) {
            Long.MAX_VALUE
        }
    }

    /**
     * Initiates or resumes downloading the model file.
     */
    fun startDownload(scope: CoroutineScope) {
        if (isModelDownloaded()) {
            _downloadState.value = DownloadState.Completed
            return
        }

        if (_downloadState.value is DownloadState.Downloading) {
            return
        }

        downloadJob?.cancel()
        downloadJob = scope.launch(Dispatchers.IO) {
            try {
                _downloadState.value = DownloadState.CheckingSpace

                // 1. Verify internal storage space (model size + 500 MB buffer)
                val requiredBytes = config.fileSizeBytes + (500L * 1024 * 1024)
                val availableBytes = getAvailableStorageBytes()
                if (availableBytes < requiredBytes) {
                    val availableMB = availableBytes / (1024 * 1024)
                    val requiredMB = requiredBytes / (1024 * 1024)
                    _downloadState.value = DownloadState.Failed(
                        "Insufficient storage space: $availableMB MB available, $requiredMB MB required."
                    )
                    return@launch
                }

                // 2. Resume handling
                val startByte = if (tempFile.exists()) tempFile.length() else 0L

                val requestBuilder = Request.Builder()
                    .url(config.downloadUrl)

                if (startByte > 0) {
                    requestBuilder.addHeader("Range", "bytes=$startByte-")
                }

                val request = requestBuilder.build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful && response.code != 206) {
                    _downloadState.value = DownloadState.Failed("Download failed with HTTP ${response.code}")
                    response.close()
                    return@launch
                }

                val body = response.body
                if (body == null) {
                    _downloadState.value = DownloadState.Failed("Empty response body from server")
                    return@launch
                }

                val responseLength = body.contentLength()
                val totalBytes = if (response.code == 206) {
                    startByte + responseLength
                } else {
                    if (responseLength > 0) responseLength else config.fileSizeBytes
                }

                val outputStream = if (startByte > 0 && response.code == 206) {
                    FileOutputStream(tempFile, true)
                } else {
                    FileOutputStream(tempFile, false)
                }

                val buffer = ByteArray(64 * 1024)
                val inputStream = body.byteStream()
                var bytesRead: Int
                var totalDownloaded = if (response.code == 206) startByte else 0L
                var lastProgressUpdate = System.currentTimeMillis()
                var bytesSinceLastUpdate = 0L

                outputStream.use { out ->
                    inputStream.use { inStream ->
                        while (inStream.read(buffer).also { bytesRead = it } != -1) {
                            out.write(buffer, 0, bytesRead)
                            totalDownloaded += bytesRead
                            bytesSinceLastUpdate += bytesRead

                            val now = System.currentTimeMillis()
                            if (now - lastProgressUpdate >= 300) {
                                val elapsedSec = (now - lastProgressUpdate) / 1000f
                                val speedMBs = if (elapsedSec > 0) {
                                    (bytesSinceLastUpdate / (1024f * 1024f)) / elapsedSec
                                } else 0f

                                val percent = if (totalBytes > 0) {
                                    ((totalDownloaded * 100) / totalBytes).toInt().coerceIn(0, 100)
                                } else 0

                                _downloadState.value = DownloadState.Downloading(
                                    progressPercent = percent,
                                    downloadedBytes = totalDownloaded,
                                    totalBytes = totalBytes,
                                    downloadSpeedMBs = speedMBs
                                )

                                lastProgressUpdate = now
                                bytesSinceLastUpdate = 0L
                            }
                        }
                        out.flush()
                    }
                }

                // 3. Verification & Rename
                _downloadState.value = DownloadState.Verifying
                if (tempFile.exists() && tempFile.length() >= (config.fileSizeBytes * 0.90)) {
                    if (modelFile.exists()) modelFile.delete()
                    val renameSuccess = tempFile.renameTo(modelFile)
                    if (renameSuccess) {
                        _downloadState.value = DownloadState.Completed
                    } else {
                        _downloadState.value = DownloadState.Failed("Failed to finalize downloaded model file.")
                    }
                } else {
                    _downloadState.value = DownloadState.Failed("Downloaded file is incomplete or corrupted.")
                }

            } catch (e: CancellationException) {
                // Interrupted / paused by user or app lifecycle
                _downloadState.value = DownloadState.Idle
            } catch (e: Exception) {
                _downloadState.value = DownloadState.Failed(e.localizedMessage ?: "Unknown download error")
            }
        }
    }

    fun pauseDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _downloadState.value = DownloadState.Idle
    }

    fun deleteModel(): Boolean {
        downloadJob?.cancel()
        downloadJob = null
        var deleted = false
        if (modelFile.exists()) {
            deleted = modelFile.delete()
        }
        if (tempFile.exists()) {
            tempFile.delete()
        }
        _downloadState.value = DownloadState.Idle
        return deleted
    }
}
