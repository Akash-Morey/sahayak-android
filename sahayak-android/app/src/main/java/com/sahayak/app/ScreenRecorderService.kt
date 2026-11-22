package com.sahayak.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

/**
 * Foreground MediaProjection service for Persona 1 (Educator) screen + audio recording.
 */
class ScreenRecorderService : Service() {

    override fun onCreate() {
        super.onCreate()
        val channelId = "sahayak_recorder"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Sahayak Recorder", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
        val notif = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Recording Educator Walkthrough")
            .setContentText("Capturing screen steps & voiceover for transcript.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
        startForeground(102, notif)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
