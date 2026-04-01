package com.example.waterreminder.workers

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun schedule(context: Context, intervalHours: Int = 2) {
        if (intervalHours <= 0) {
            cancel(context)
            return
        }

        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            intervalHours.toLong(), TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            ReminderWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(ReminderWorker.WORK_NAME)
    }
}
