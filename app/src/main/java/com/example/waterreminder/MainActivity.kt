
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
 import androidx.compose.foundation.ExperimentalFoundationApi
 import androidx.compose.foundation.layout.*
 import androidx.compose.foundation.pager.HorizontalPager
 import androidx.compose.foundation.pager.rememberPagerState
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
 import kotlinx.coroutines.launch

 class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaterReminderTheme {
                PermissionWrapper()
            }
        }
    }
 }

 @Composable
 fun PermissionWrapper() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasNotificationPermission by remember { mutableStateOf(checkNotificationPermission(context)) }
    var hasExactAlarmPermission by remember { mutableStateOf(checkExactAlarmPermission(context)) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

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
        hasNotificationPermission && hasExactAlarmPermission -> {
            LaunchedEffect(Unit) {
                NotificationScheduler.scheduleInitialAlarm(context)
            }
            MainScreen()
        }
        !hasNotificationPermission -> {
            PermissionRequestScreen(
                title = "Notification Permission Needed",
                text = "To remind you to drink water, please grant the notification permission. It's essential for the app to work correctly.",
                buttonText = "Grant Permission",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        hasNotificationPermission = true
                    }
                }
            )
        }
        !hasExactAlarmPermission -> {
            PermissionRequestScreen(
                title = "Precise Alarms Required",
                text = "For timely reminders, the app needs permission to schedule precise alarms. Please enable this permission in the next screen to ensure you get notified on the hour.",
                buttonText = "Open Settings",
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    } else {
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

 private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
 }

 private fun checkExactAlarmPermission(context: Context): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }
 }

 @OptIn(ExperimentalFoundationApi::class)
 @Composable
 fun MainScreen() {
    val tabs = listOf("Today", "Week", "Month")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    // State is lifted to the common parent, MainScreen.
    var glasses by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f)
        ) { page ->
            when (page) {
                0 -> TodayScreen(
                    glasses = glasses,
                    onDrinkGlassClick = { glasses++ } // Pass state and event down
                )
                1 -> StatisticsChart(period = "Week")
                2 -> StatisticsChart(period = "Month")
            }
        }
    }
 }

 @Composable
 fun TodayScreen(glasses: Int, onDrinkGlassClick: () -> Unit) {
    val mlPerGlass = 250

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        StatisticsToday(glasses = glasses, mlPerGlass = mlPerGlass)
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onDrinkGlassClick) { // Use the provided event handler
            Text(text = "I drank a glass")
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
 }


 @Composable
 fun StatisticsToday(glasses: Int, mlPerGlass: Int) {
    val totalMl = glasses * mlPerGlass
    val dailyGoal = 2000

    ProgressCard(consumed = totalMl, goal = dailyGoal)
 }

 @Composable
 fun ProgressCard(consumed: Int, goal: Int) {
    val progress = (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
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
        PermissionRequestScreen(
            title = "Permission Required",
            text = "This is a preview of how the permission request screen will look.",
            buttonText = "Grant",
            onClick = {}
        )
    }
 }
