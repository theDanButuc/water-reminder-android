package com.example.waterreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class WaterReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val builder = NotificationCompat.Builder(context, "water_reminder")
            .setSmallIcon(R.drawable.ic_water) // iconița ta
            .setContentTitle("💧 Time to drink water!")
            .setContentText("Nu uita să bei apă pentru a rămâne hidratat!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(1001, builder.build())
    }
}