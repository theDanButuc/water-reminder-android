package com.example.waterreminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import java.util.*

class WaterReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("WaterReminder", Context.MODE_PRIVATE)
        val startHour = prefs.getInt("startHour", 8)
        val endHour = prefs.getInt("endHour", 22)
        
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        if (currentHour in startHour..endHour) {
            if (intent.action == "DRINK_WATER") {
                val todayKey = "today_${getTodayDate()}"
                val currentGlasses = prefs.getInt(todayKey, 0)
                prefs.edit().putInt(todayKey, currentGlasses + 1).apply()
                
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(1)
            } else {
                showNotification(context)
            }
        }
    }

    private fun showNotification(context: Context) {
        val intentMain = Intent(context, MainActivity::class.java)
        val pendingIntentMain = PendingIntent.getActivity(
            context,
            0,
            intentMain,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intentDrink = Intent(context, WaterReminderReceiver::class.java).apply {
            action = "DRINK_WATER"
        }
        val pendingIntentDrink = PendingIntent.getBroadcast(
            context,
            1,
            intentDrink,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "water_reminder")
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
        notificationManager.notify(1, notification)
    }

    private fun getTodayDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }
}
