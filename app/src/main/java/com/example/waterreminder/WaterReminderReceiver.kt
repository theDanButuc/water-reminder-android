package com.example.waterreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.*

class WaterReminderReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "water_reminder"
        private const val NOTIF_ID = 1
        private const val PREFS_NAME = "WaterReminder"
        private const val ACTION_DRINK = "DRINK_WATER"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val startHour = prefs.getInt("startHour", 8)
        val endHour = prefs.getInt("endHour", 22)

        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        // Support intervals that wrap past midnight: e.g. start=22, end=6
        val inWindow = if (startHour <= endHour) {
            currentHour in startHour..endHour
        } else {
            currentHour >= startHour || currentHour <= endHour
        }

        if (!inWindow) return

        if (intent.action == ACTION_DRINK && intent.`package` == context.packageName) {
            val todayKey = "today_${getTodayDate()}"
            val currentGlasses = prefs.getInt(todayKey, 0)
            prefs.edit().putInt(todayKey, currentGlasses + 1).apply()

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIF_ID)
        } else {
            showNotification(context)
        }
    }

    private fun showNotification(context: Context) {
        // On Android 13+ we must have POST_NOTIFICATIONS permission to post notifications.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Can't request permission from a BroadcastReceiver. Bail out silently.
                return
            }
        }

        createNotificationChannelIfNeeded(context)

        val intentMain = Intent(context, MainActivity::class.java)
        val pendingIntentMain = PendingIntent.getActivity(
            context,
            0,
            intentMain,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intentDrink = Intent(context, WaterReminderReceiver::class.java).apply {
            action = ACTION_DRINK
            // Restrict the broadcast so only our app can send it / receive it
            setPackage(context.packageName)
        }
        val pendingIntentDrink = PendingIntent.getBroadcast(
            context,
            1,
            intentDrink,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💧 Timp să bei apă!")
            .setContentText("Nu uita să bei un pahar cu apă (250ml)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntentMain)
            .addAction(
                android.R.drawable.ic_menu_add,
                "Am băut",
                pendingIntentDrink
            )
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID, notification)
    }

    private fun createNotificationChannelIfNeeded(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            val existing = nm.getNotificationChannel(CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Water reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Memento-uri pentru hidratare"
                }
                nm.createNotificationChannel(channel)
            }
        }
    }

    private fun getTodayDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
