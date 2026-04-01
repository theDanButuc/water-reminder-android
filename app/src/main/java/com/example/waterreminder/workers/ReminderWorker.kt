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
import com.example.waterreminder.data.db.AppDatabase
import com.example.waterreminder.data.preferences.UserPreferences
import kotlinx.coroutines.flow.first
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

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

        if (isOutsideActiveHours(prefs)) return Result.success()

        val goal = prefs.dailyGoalMl.first()
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayIntake = AppDatabase.getDatabase(context)
            .waterIntakeDao()
            .getIntakeFrom(todayStart)
            .first()
        val totalMl = todayIntake.sumOf { it.effectiveAmount }

        if (goal > 0 && totalMl >= goal) return Result.success()

        val progressPct = if (goal > 0) (totalMl * 100 / goal) else 0
        val remainingMl = goal - totalMl

        val message = when {
            progressPct < 25 -> context.getString(R.string.notif_msg_start)
            progressPct < 50 -> context.getString(R.string.notif_msg_quarter, progressPct)
            progressPct < 75 -> context.getString(R.string.notif_msg_half, remainingMl)
            else -> context.getString(R.string.notif_msg_almost, remainingMl)
        }

        showNotification(message)
        return Result.success()
    }

    private suspend fun isOutsideActiveHours(prefs: UserPreferences): Boolean {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        val now = LocalTime.now()

        val wake = runCatching {
            LocalTime.parse(prefs.wakeTime.first(), formatter)
        }.getOrDefault(LocalTime.of(7, 0))

        val sleep = runCatching {
            LocalTime.parse(prefs.sleepTime.first(), formatter)
        }.getOrDefault(LocalTime.of(23, 0))

        return if (wake <= sleep) {
            now < wake || now > sleep
        } else {
            now < sleep && now > wake
        }
    }

    private fun showNotification(message: String) {
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingContentIntent = PendingIntent.getActivity(
            context, 0, contentIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_new)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(message)
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
