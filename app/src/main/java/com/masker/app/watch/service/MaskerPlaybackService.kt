package com.masker.app.watch.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.masker.app.watch.MainActivity
import com.masker.app.watch.R
import com.masker.app.watch.audio.NoiseEngine
import com.masker.app.watch.sync.MaskerRepository

/**
 * سرویس فورگراند پخش صدای ماسکر روی ساعت. موتور صدا ([NoiseEngine]) به‌عنوان singleton در
 * companion object نگه‌داری می‌شود تا هم [MaskerRepository] (هنگام رسیدن تنظیمات جدید از
 * گوشی) و هم صفحه اصلی (برای شروع/توقف پخش) به همان نمونه دسترسی داشته باشند - دقیقاً همان
 * الگوی PlaybackService.noiseEngine در برنامه گوشی.
 */
class MaskerPlaybackService : Service() {

    companion object {
        const val ACTION_START = "com.masker.app.watch.action.START"
        const val ACTION_STOP = "com.masker.app.watch.action.STOP"

        private const val CHANNEL_ID = "masker_watch_playback_channel"
        private const val NOTIFICATION_ID = 2001

        val noiseEngine = NoiseEngine()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                noiseEngine.stop(applicationContext)
                MaskerRepository.setPlaying(false)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification())
                noiseEngine.start(applicationContext)
                MaskerRepository.setPlaying(true)
            }
        }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        createChannelIfNeeded()

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MaskerPlaybackService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_masker_notification)
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .addAction(0, getString(R.string.stop), stopPendingIntent)
            .build()
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        noiseEngine.stop(applicationContext)
        MaskerRepository.setPlaying(false)
        super.onDestroy()
    }
}
