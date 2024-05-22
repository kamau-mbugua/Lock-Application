package com.kelvin.demoapplication

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.view.WindowManager
import android.widget.TextView
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat

class AppBlockerService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayLayout: LinearLayout

    private val handler = Handler()
    private lateinit var blockedApps: Set<String>
    private var stopHandler: Handler? = null
    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "App Blocker Service"

    private var blockStartTime: Long = 0L
    private var blockDuration: Long = 0L

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        blockedApps = getBlockedApps(this)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        // Create a LinearLayout
        overlayLayout = LinearLayout(this)
        overlayLayout.orientation = LinearLayout.VERTICAL
        overlayLayout.gravity = Gravity.CENTER

        // Create an ImageView and add it to the LinearLayout
        val overlayImage = ImageView(this)
        overlayImage.setImageResource(R.drawable.th) // Replace with your image resource
        overlayLayout.addView(overlayImage)

        // Create a TextView and add it to the LinearLayout
        val overlayText = TextView(this)
        overlayText.text = "App Blocked, Take a Break!"
        overlayText.setBackgroundColor(0x7F000000) // Semi-transparent black
        overlayText.setTextColor(0xFFFFFFFF.toInt())
        overlayText.textSize = 24f
        overlayText.gravity = Gravity.CENTER
        overlayLayout.addView(overlayText)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        windowManager.addView(overlayLayout, params)

        handler.post(checkForegroundApp)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "App Blocker Service"
            val descriptionText = "This channel is used by the App Blocker Service"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        blockDuration = intent?.getLongExtra("BLOCK_DURATION", 0L) ?: 0L
        if (blockDuration > 0) {
            stopHandler = Handler()
            blockStartTime = System.currentTimeMillis()
            stopHandler?.postDelayed({
                stopSelf()
            }, blockDuration)
        }

        // Create a notification
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Blocker Service")
            .setContentText("Blocking distracting apps...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()

        // Start the service in the foreground with the notification


        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    private val checkForegroundApp = object : Runnable {
        override fun run() {
            val foregroundApp =
                getForegroundApp(this@AppBlockerService) ?: getBackgroundApp(this@AppBlockerService)

            Log.e("blockedApps", "blockedApps app: $blockedApps")
            if (foregroundApp != null && blockedApps.contains(foregroundApp)) {
                overlayLayout.visibility = TextView.VISIBLE
            } else {
                overlayLayout.visibility = TextView.GONE
            }
            handler.postDelayed(this, 1000) // Check every second
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkForegroundApp)
        windowManager.removeView(overlayLayout)
        stopHandler?.removeCallbacksAndMessages(null)
    }
}