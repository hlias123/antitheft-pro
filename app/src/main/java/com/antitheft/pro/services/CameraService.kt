package com.antitheft.pro.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.antitheft.pro.R

class CameraService : Service() {
    
    private val CHANNEL_ID = "CameraServiceChannel"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        // Capture photo from front camera
        captureIntruderPhoto()
        
        return START_STICKY
    }
    
    private fun captureIntruderPhoto() {
        // TODO: Implement full camera capture logic using CameraX
        // This is a simplified version
        // In production, use CameraX to capture from front camera
        // Upload to Firebase Storage
        // Save metadata to Firestore
        // Send notification email
        
        // For now, just log that service started
        android.util.Log.d("CameraService", "Intruder detection activated")
        
        // Stop service after capture attempt
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            stopSelf()
        }, 5000) // Stop after 5 seconds
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION.CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Camera Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Intruder detection active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }
}
