package com.example.waterreminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.waterreminder.ui.theme.WaterReminderTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) scheduleNotifications()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        setContent {
            WaterReminderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WaterTrackerScreen(
                        onScheduleNotifications = { checkPermissionAndSchedule() }
                    )
                }
            }
        }
    }

    private fun checkPermissionAndSchedule() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> scheduleNotifications()
                else -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            scheduleNotifications()
        }
    }

    private fun scheduleNotifications() {
        val prefs = getSharedPreferences("WaterReminder", Context.MODE_PRIVATE)
        val startHour = prefs.getInt("startHour", 8)
        val endHour = prefs.getInt("endHour", 22)
        val intervalMinutes = prefs.getInt("intervalMinutes", 60)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, WaterReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            (intervalMinutes * 60 * 1000).toLong(),
            pendingIntent
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "water_reminder",
                "Water Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notificări pentru consumul de apă" }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun WaterTrackerScreen(onScheduleNotifications: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("WaterReminder", Context.MODE_PRIVATE)

    var glassesConsumed by remember { mutableIntStateOf(prefs.getInt("today_${getTodayDate()}", 0)) }
    var startHour by remember { mutableIntStateOf(prefs.getInt("startHour", 8)) }
    var endHour by remember { mutableIntStateOf(prefs.getInt("endHour", 22)) }
    var intervalMinutes by remember { mutableIntStateOf(prefs.getInt("intervalMinutes", 60)) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "💧 Consumul Meu de Apă",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("$glassesConsumed pahare", style = MaterialTheme.typography.displayLarge)
                Text("${glassesConsumed * 250} ml", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        glassesConsumed++
                        prefs.edit().putInt("today_${getTodayDate()}", glassesConsumed).apply()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Am băut un pahar")
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("⚙️ Setări Notificări", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                Text("Interval orar: $startHour:00 - $endHour:00")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Start: $startHour:00", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = startHour.toFloat(),
                            onValueChange = { startHour = it.toInt(); prefs.edit().putInt("startHour", startHour).apply() },
                            valueRange = 0f..23f,
                            steps = 22
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sfârșit: $endHour:00", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = endHour.toFloat(),
                            onValueChange = { endHour = it.toInt(); prefs.edit().putInt("endHour", endHour).apply() },
                            valueRange = 0f..23f,
                            steps = 22
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Frecvență: la fiecare $intervalMinutes minute")
                Slider(
                    value = intervalMinutes.toFloat(),
                    onValueChange = { intervalMinutes = it.toInt(); prefs.edit().putInt("intervalMinutes", intervalMinutes).apply() },
                    valueRange = 15f..240f,
                    steps = 14
                )

                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onScheduleNotifications,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Activează Notificările")
                }
            }
        }
    }
}

fun getTodayDate(): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
}