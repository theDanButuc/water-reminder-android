package com.example.waterreminder.workers

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.waterreminder.MainActivity
import com.example.waterreminder.R
import com.example.waterreminder.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "water_reminder_periodic"
        private const val NOTIFICATION_ID = 456
        private const val CHANNEL_ID = "water_reminder_channel"
    }

    override suspend fun doWork(): Result {
        val prefs = UserPreferences(context)

        if (isQuietHours(prefs)) return Result.success()

        showNotification()
        return Result.success()
    }

    private suspend fun isQuietHours(prefs: UserPreferences): Boolean {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val now = LocalTime.now()

        val startStr = prefs.quietHoursStart.first()
        val endStr = prefs.quietHoursEnd.first()

        val start = LocalTime.parse(startStr, formatter)
        val end = LocalTime.parse(endStr, formatter)

        return if (start <= end) {
            now in start..end
        } else {
            now >= start || now <= end
        }
    }

    private fun showNotification() {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingContentIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_new)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingContentIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // Notification permission not granted
        }
    }
}
