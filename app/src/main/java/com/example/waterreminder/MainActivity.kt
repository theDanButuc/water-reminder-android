
 package com.example.waterreminder

 import android.Manifest
 import android.app.AlarmManager
 import android.content.Context
 import android.content.Intent
 import android.content.pm.PackageManager
 import android.net.Uri
 import android.os.Build
 import android.os.Bundle
 import android.provider.Settings
 import androidx.activity.ComponentActivity
 import androidx.activity.compose.rememberLauncherForActivityResult
 import androidx.activity.compose.setContent
 import androidx.activity.result.contract.ActivityResultContracts
 import androidx.compose.foundation.layout.*
 import androidx.compose.material3.*
 import androidx.compose.runtime.*
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.platform.LocalContext
 import androidx.compose.ui.platform.LocalLifecycleOwner
 import androidx.compose.ui.text.font.FontWeight
 import androidx.compose.ui.text.style.TextAlign
 import androidx.compose.ui.tooling.preview.Preview
 import androidx.compose.ui.unit.dp
 import androidx.compose.ui.unit.sp
 import androidx.core.content.ContextCompat
 import androidx.lifecycle.Lifecycle
 import androidx.lifecycle.LifecycleEventObserver
 import com.example.waterreminder.ui.theme.WaterReminderTheme
 import com.google.accompanist.pager.*

 class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaterReminderTheme {
                // The new entry point that handles permissions first
                PermissionWrapper()
            }
        }
    }
 }

 @Composable
 fun PermissionWrapper() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // State holders for permissions
    var hasNotificationPermission by remember { mutableStateOf(checkNotificationPermission(context)) }
    var hasExactAlarmPermission by remember { mutableStateOf(checkExactAlarmPermission(context)) }

    // Launcher for the standard notification permission request
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

    // This effect observes the lifecycle to re-check the exact alarm permission
    // when the user returns to the app from the settings screen.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasExactAlarmPermission = checkExactAlarmPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    when {
        // If both permissions are granted, show the main app screen
        hasNotificationPermission && hasExactAlarmPermission -> {
            LaunchedEffect(Unit) {
                // Schedule the alarm only after all permissions are granted
                NotificationScheduler.scheduleInitialAlarm(context)
            }
            MainScreen()
        }

        // If notification permission is missing, show its request screen
        !hasNotificationPermission -> {
            PermissionRequestScreen(
                title = "Notification Permission Needed",
                text = "To remind you to drink water, please grant the notification permission. It's essential for the app to work correctly.",
                buttonText = "Grant Permission",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        // Automatically granted on older versions
                        hasNotificationPermission = true
                    }
                }
            )
        }

        // If exact alarm permission is missing, show its request screen
        !hasExactAlarmPermission -> {
            PermissionRequestScreen(
                title = "Precise Alarms Required",
                text = "For timely reminders, the app needs permission to schedule precise alarms. Please enable this permission in the next screen to ensure you get notified on the hour.",
                buttonText = "Open Settings",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            // Takes the user directly to the app's permission page
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
                        // Not a special permission on older versions
                        hasExactAlarmPermission = true
                    }
                }
            )
        }
    }
 }

 @Composable
 fun PermissionRequestScreen(title: String, text: String, buttonText: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = text, textAlign = TextAlign.Center, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = buttonText, fontSize = 16.sp)
        }
    }
 }

 // Helper function to check for notification permission
 private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true // Granted by default on older Android versions
    }
 }

 // Helper function to check for exact alarm permission
 private fun checkExactAlarmPermission(context: Context): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true // Not a special permission on older Android versions
    }
 }


 // --- The rest of the app's UI remains the same ---

 @OptIn(ExperimentalPagerApi::class)
 @Composable
 fun MainScreen() {
    val pagerState = rememberPagerState()
    val tabs = listOf("Today", "Week", "Month")
    // TODO: The pager is not swipeable, and tab clicks are not implemented yet.

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = { /* TODO: Implement click listener */ }
                )
            }
        }

        HorizontalPager(
            count = tabs.size,
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) { page ->
            when (page) {
                0 -> StatisticsScreen(period = "Today")
                1 -> StatisticsScreen(period = "Week")
                2 -> StatisticsScreen(period = "Month")
            }
        }
    }
 }

 @Composable
 fun StatisticsScreen(period: String) {
    var glasses by remember { mutableStateOf(0) }
    val mlPerGlass = 250 // 250 ml per glass

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // The content of the screen is now at the top
        if (period == "Today") {
            StatisticsToday(glasses = glasses, mlPerGlass = mlPerGlass)
        } else {
            StatisticsChart(period = period)
        }

        Spacer(modifier = Modifier.weight(1f)) // Pushes the button to the bottom

        Button(onClick = { glasses++ }) {
            Text(text = "I drank a glass")
        }

        Spacer(modifier = Modifier.height(16.dp)) // Some padding from the bottom edge
    }
 }

 @Composable
 fun StatisticsToday(glasses: Int, mlPerGlass: Int) {
    val totalMl = glasses * mlPerGlass
    val dailyGoal = 2000 // 2000 ml daily goal

    ProgressCard(consumed = totalMl, goal = dailyGoal)
 }

 @Composable
 fun ProgressCard(consumed: Int, goal: Int) {
    val progress = (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp), // Add padding to the top
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "💧 Daily Progress",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$consumed ml / $goal ml",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
 }


 @Composable
 fun StatisticsChart(period: String) {
    // Placeholder for week/month charts
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "$period statistics chart will be here.", fontSize = 18.sp)
    }
 }

 @Preview(showBackground = true)
 @Composable
 fun DefaultPreview() {
    WaterReminderTheme {
        // Preview the permission screen for easy UI checks
        PermissionRequestScreen(
            title = "Permission Required",
            text = "This is a preview of how the permission request screen will look.",
            buttonText = "Grant",
            onClick = {}
        )
    }
 }
