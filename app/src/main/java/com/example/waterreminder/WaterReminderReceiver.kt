
 package com.example.waterreminder

 import android.app.NotificationChannel
 import android.app.NotificationManager
 import android.content.BroadcastReceiver
 import android.content.Context
 import android.content.Intent
 import android.os.Build
 import androidx.core.app.NotificationCompat

 class WaterReminderReceiver : BroadcastReceiver() {

    private val CHANNEL_ID = "water_reminder_channel"

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == "SHOW_NOTIFICATION") {
            // 1. Show the notification immediately
            showNotification(context)

            // 2. Schedule the next alarm for the next hour
            NotificationScheduler.scheduleNextAlarm(context)
        }
    }

    fun showNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Water Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for water reminder notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Ensure you have this drawable
            .setContentTitle("Time for a glass of water!")
            .setContentText("Stay hydrated and keep up with your daily goal.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NotificationScheduler.NOTIFICATION_ID, notification)
    }
 }
