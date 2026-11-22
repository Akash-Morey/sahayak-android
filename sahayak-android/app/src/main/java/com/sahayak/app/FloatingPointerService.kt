package com.sahayak.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat

/**
 * Draws a real floating pointer ring + multilingual callout on top of WhatsApp, Instagram, or PhonePe.
 */
class FloatingPointerService : Service() {

    private lateinit var windowManager: WindowManager
    private var pointerView: View? = null
    private var dockView: View? = null

    companion object {
        const val ACTION_SHOW_STEP = "com.sahayak.SHOW_STEP"
        const val EXTRA_CALLOUT = "callout"
        const val EXTRA_X = "x"
        const val EXTRA_Y = "y"
        const val EXTRA_STEP_INDEX = "step_index"
        const val EXTRA_WORKFLOW_ID = "workflow_id"
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        startForegroundNotif()
        showFloatingDock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_SHOW_STEP) {
            val callout = intent.getStringExtra(EXTRA_CALLOUT) ?: "Tap highlighted button"
            val x = intent.getIntExtra(EXTRA_X, 240)
            val y = intent.getIntExtra(EXTRA_Y, 600)
            showPointerAt(x, y, callout)
        }
        return START_STICKY
    }

    private fun showPointerAt(x: Int, y: Int, callout: String) {
        pointerView?.let { windowManager.removeView(it) }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }

        val bubble = TextView(this).apply {
            text = "👆 $callout"
            setTextColor(Color.WHITE)
            textSize = 14f
            setPadding(28, 16, 28, 16)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#0F172A"))
                cornerRadius = 24f
            }
        }

        val ring = View(this).apply {
            background = GradientDrawable().apply {
                setColor(Color.TRANSPARENT)
                setStroke(8, Color.parseColor("#F59E0B"))
                cornerRadius = 32f
            }
        }

        container.addView(bubble)
        container.addView(ring, LinearLayout.LayoutParams(220, 120).apply { topMargin = 12 })

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            this.x = x
            this.y = y
        }

        pointerView = container
        windowManager.addView(container, params)
    }

    private fun showFloatingDock() {
        val pill = TextView(this).apply {
            text = "🎙️ Sahayak Active (Tap to Stop)"
            setTextColor(Color.WHITE)
            textSize = 13f
            setPadding(32, 20, 32, 20)
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#2563EB"))
                cornerRadius = 999f
            }
            setOnClickListener { stopSelf() }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 90
        }

        dockView = pill
        windowManager.addView(pill, params)
    }

    private fun startForegroundNotif() {
        val channelId = "sahayak_overlay"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Sahayak Pointer", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
        val notif: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sahayak Screen Guide Active")
            .setContentText("Floating pointer is helping you navigate.")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
        startForeground(101, notif)
    }

    override fun onDestroy() {
        super.onDestroy()
        pointerView?.let { windowManager.removeView(it) }
        dockView?.let { windowManager.removeView(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
