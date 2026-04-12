package com.example.waterreminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.waterreminder.data.db.AppDatabase
import com.example.waterreminder.data.preferences.UserPreferences
import com.example.waterreminder.data.repository.WaterRepository
import com.example.waterreminder.workers.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class WaterReminderApplication : android.app.Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { WaterRepository(database.waterIntakeDao()) }
    val userPreferences by lazy { UserPreferences(this) }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        // Observe interval changes and keep the WorkManager schedule in sync.
        // This also handles the initial schedule on app start.
        appScope.launch {
            // distinctUntilChanged ensures WorkManager is only rescheduled when the
            // interval actually changes, not on every app start emission.
            userPreferences.notificationIntervalMin
                .distinctUntilChanged()
                .collect { intervalMin ->
                    ReminderScheduler.schedule(this@WaterReminderApplication, intervalMin)
                }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("water_reminder_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
