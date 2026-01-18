
 package com.example.waterreminder

 import android.app.AlarmManager
 import android.app.PendingIntent
 import android.content.BroadcastReceiver
 import android.content.Context
 import android.content.Intent
 import android.os.SystemClock
 import androidx.core.app.NotificationCompat
 import androidx.core.app.NotificationManagerCompat

 object NotificationScheduler {

    private const val ALARM_REQUEST_CODE = 123
    private const val NOTIFICATION_ID = 456

    fun scheduleInitialAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Porneste la fiecare ora
        val interval = AlarmManager.INTERVAL_HOUR
        val firstTrigger = SystemClock.elapsedRealtime() + interval

        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            firstTrigger,
            interval,
            pendingIntent
        )
    }

    fun showNotification(context: Context) {
        val notification = NotificationCompat.Builder(context, "water_reminder_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Asigura-te ca aceasta iconita exista
            .setContentTitle("Time for Water!")
            .setContentText("Don't forget to stay hydrated. Drink a glass of water.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            // channelId-ul trebuie sa fie acelasi cu cel creat in Application class
            notify(NOTIFICATION_ID, notification)
        }
    }
 }

 class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationScheduler.showNotification(context)
    }
 }
