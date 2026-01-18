
 package com.example.waterreminder

 import android.app.AlarmManager
 import android.app.PendingIntent
 import android.content.Context
 import android.content.Intent
 import java.util.Calendar

 object NotificationScheduler {

    const val NOTIFICATION_ID = 101
    private const val START_HOUR = 8
    private const val END_HOUR = 22

    fun scheduleInitialAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Use a specific request code for the alarm to avoid conflicts
        val requestCode = 0
        val pendingIntent = createPendingIntent(context, requestCode)

        // Check if the alarm is already scheduled
        val isAlarmUp = PendingIntent.getBroadcast(context, requestCode,
            Intent(context, WaterReminderReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) != null

        if (isAlarmUp) {
            // Alarm is already running, no need to schedule again
            return
        }

        val nextAlarmTime = calculateNextAlarmTime()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextAlarmTime.timeInMillis,
            pendingIntent
        )
    }

    fun scheduleNextAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = createPendingIntent(context, 0)
        val nextAlarmTime = calculateNextAlarmTime()

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextAlarmTime.timeInMillis,
            pendingIntent
        )
    }

    fun showNotificationIfNeeded(context: Context) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour in START_HOUR..END_HOUR) {
            WaterReminderReceiver().showNotification(context)
        }
    }

    private fun calculateNextAlarmTime(): Calendar {
        val now = Calendar.getInstance()
        val nextAlarm = Calendar.getInstance()
        nextAlarm.timeInMillis = now.timeInMillis

        // Set to the beginning of the next hour
        nextAlarm.add(Calendar.HOUR_OF_DAY, 1)
        nextAlarm.set(Calendar.MINUTE, 0)
        nextAlarm.set(Calendar.SECOND, 0)
        nextAlarm.set(Calendar.MILLISECOND, 0)

        val nextAlarmHour = nextAlarm.get(Calendar.HOUR_OF_DAY)

        when {
            // If the next hour is after the end time, schedule for the next day's start time
            nextAlarmHour > END_HOUR -> {
                nextAlarm.add(Calendar.DAY_OF_YEAR, 1)
                nextAlarm.set(Calendar.HOUR_OF_DAY, START_HOUR)
            }
            // If the next hour is before the start time, schedule for this day's start time
            nextAlarmHour < START_HOUR -> {
                nextAlarm.set(Calendar.HOUR_OF_DAY, START_HOUR)
            }
        }
        return nextAlarm
    }

    private fun createPendingIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = "SHOW_NOTIFICATION" // Define a clear action
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
 }
