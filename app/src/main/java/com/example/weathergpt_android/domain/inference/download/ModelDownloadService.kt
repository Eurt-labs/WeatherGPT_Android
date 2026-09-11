package com.example.weathergpt_android.domain.inference.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.weathergpt_android.MainActivity
import com.example.weathergpt_android.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground Service for downloading the on-device GGUF language model in the background.
 * Ensures the ~1.1 GB download completes uninterrupted even if the app is minimized or screen is locked.
 * Features live progress notification with Pause, Resume, and Cancel actions.
 */
class ModelDownloadService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var stateObserverJob: Job? = null
    private lateinit var downloadManager: ModelDownloadManager
    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        super.onCreate()
        downloadManager = ModelDownloadManager.getInstance(this)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        when (action) {
            ACTION_START -> {
                startForegroundWithNotification()
                downloadManager.startDownload()
                observeDownloadState()
            }
            ACTION_PAUSE -> {
                downloadManager.pauseDownload()
            }
            ACTION_RESUME -> {
                downloadManager.startDownload()
            }
            ACTION_CANCEL -> {
                downloadManager.cancelDownload()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // If the download is actively in progress, allow it to continue running in background
        val currentState = downloadManager.downloadState.value
        if (currentState !is DownloadState.Downloading && currentState !is DownloadState.CheckingSpace) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Offline AI Model Download",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live progress while downloading on-device AI weights"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun startForegroundWithNotification() {
        val notification = buildProgressNotification(
            progressPercent = 0,
            text = "Preparing model download...",
            isPaused = false
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun observeDownloadState() {
        stateObserverJob?.cancel()
        stateObserverJob = serviceScope.launch {
            downloadManager.downloadState.collect { state ->
                when (state) {
                    is DownloadState.Downloading -> {
                        val downloadedMB = state.downloadedBytes / (1024 * 1024)
                        val totalMB = state.totalBytes / (1024 * 1024)
                        val speedStr = "%.1f".format(state.downloadSpeedMBs)
                        val text = "$downloadedMB MB / $totalMB MB (${state.progressPercent}%) • $speedStr MB/s"

                        notificationManager.notify(
                            NOTIFICATION_ID,
                            buildProgressNotification(state.progressPercent, text, isPaused = false)
                        )
                    }
                    is DownloadState.CheckingSpace -> {
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            buildProgressNotification(0, "Checking storage space...", isPaused = false)
                        )
                    }
                    is DownloadState.Verifying -> {
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            buildProgressNotification(100, "Verifying model file integrity...", isPaused = false)
                        )
                    }
                    is DownloadState.Paused -> {
                        val downloadedMB = state.downloadedBytes / (1024 * 1024)
                        val totalMB = state.totalBytes / (1024 * 1024)
                        val text = "Paused: $downloadedMB MB / $totalMB MB (${state.progressPercent}%)"

                        notificationManager.notify(
                            NOTIFICATION_ID,
                            buildProgressNotification(state.progressPercent, text, isPaused = true)
                        )
                    }
                    is DownloadState.Completed -> {
                        // Pre-load on-device AI weights into memory ASAP upon download completion
                        com.example.weathergpt_android.domain.inference.engine.OnDeviceEngine
                            .getInstance(this@ModelDownloadService)
                            .ensureModelLoaded()

                        stopForeground(STOP_FOREGROUND_REMOVE)
                        showCompletionNotification()
                        stopSelf()
                    }
                    is DownloadState.Failed -> {
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        showFailureNotification(state.error)
                        stopSelf()
                    }
                    is DownloadState.Idle -> {
                        // User cancelled or reset
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stopSelf()
                    }
                }
            }
        }
    }

    private fun buildProgressNotification(
        progressPercent: Int,
        text: String,
        isPaused: Boolean
    ) = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle("WeatherGPT • Offline AI Model")
        .setContentText(text)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setProgress(100, progressPercent, progressPercent == 0 && !isPaused)
        .setContentIntent(createContentIntent())
        .apply {
            if (isPaused) {
                addAction(
                    android.R.drawable.ic_media_play,
                    "Resume",
                    createServiceActionIntent(ACTION_RESUME)
                )
            } else {
                addAction(
                    android.R.drawable.ic_media_pause,
                    "Pause",
                    createServiceActionIntent(ACTION_PAUSE)
                )
            }
            addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Cancel",
                createServiceActionIntent(ACTION_CANCEL)
            )
        }
        .build()

    private fun showCompletionNotification() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Offline AI Model Ready ✓")
            .setContentText("WeatherGPT can now answer weather & agricultural queries 100% offline.")
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(createContentIntent())
            .build()

        notificationManager.notify(NOTIFICATION_ID_COMPLETED, notification)
    }

    private fun showFailureNotification(error: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Model Download Failed")
            .setContentText(error)
            .setAutoCancel(true)
            .setOngoing(false)
            .setContentIntent(createContentIntent())
            .build()

        notificationManager.notify(NOTIFICATION_ID_FAILED, notification)
    }

    private fun createContentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createServiceActionIntent(action: String): PendingIntent {
        val intent = Intent(this, ModelDownloadService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_ID = "weathergpt_model_download"
        const val NOTIFICATION_ID = 2001
        const val NOTIFICATION_ID_COMPLETED = 2002
        const val NOTIFICATION_ID_FAILED = 2003

        const val ACTION_START = "com.example.weathergpt_android.ACTION_START_MODEL_DOWNLOAD"
        const val ACTION_PAUSE = "com.example.weathergpt_android.ACTION_PAUSE_MODEL_DOWNLOAD"
        const val ACTION_RESUME = "com.example.weathergpt_android.ACTION_RESUME_MODEL_DOWNLOAD"
        const val ACTION_CANCEL = "com.example.weathergpt_android.ACTION_CANCEL_MODEL_DOWNLOAD"

        fun start(context: Context) {
            val intent = Intent(context, ModelDownloadService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            val intent = Intent(context, ModelDownloadService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resume(context: Context) {
            val intent = Intent(context, ModelDownloadService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }

        fun cancel(context: Context) {
            val intent = Intent(context, ModelDownloadService::class.java).apply {
                action = ACTION_CANCEL
            }
            context.startService(intent)
        }
    }
}
