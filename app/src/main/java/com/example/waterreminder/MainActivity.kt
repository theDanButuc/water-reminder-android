
 package com.example.waterreminder

 import android.Manifest
 import android.app.AlarmManager
 import android.app.NotificationChannel
 import android.app.NotificationManager
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
 import androidx.activity.viewModels
 import androidx.compose.animation.core.animateFloatAsState
 import androidx.compose.animation.core.tween
 import androidx.compose.foundation.Canvas
 import androidx.compose.foundation.ExperimentalFoundationApi
 import androidx.compose.foundation.layout.*
 import androidx.compose.foundation.pager.HorizontalPager
 import androidx.compose.foundation.pager.rememberPagerState
 import androidx.compose.material3.*
 import androidx.compose.runtime.*
 import androidx.compose.ui.Alignment
 import androidx.compose.ui.Modifier
 import androidx.compose.ui.geometry.CornerRadius
 import androidx.compose.ui.geometry.Offset
 import androidx.compose.ui.geometry.Size
 import androidx.compose.ui.graphics.Color
 import androidx.compose.ui.graphics.Path
 import androidx.compose.ui.graphics.drawscope.clipPath
 import androidx.compose.ui.platform.LocalContext
 import androidx.compose.ui.platform.LocalLifecycleOwner
 import androidx.compose.ui.text.* 
 import androidx.compose.ui.text.font.FontWeight
 import androidx.compose.ui.text.style.TextAlign
 import androidx.compose.ui.tooling.preview.Preview
 import androidx.compose.ui.unit.dp
 import androidx.compose.ui.unit.sp
 import androidx.core.content.ContextCompat
 import androidx.lifecycle.Lifecycle
 import androidx.lifecycle.LifecycleEventObserver
 import androidx.lifecycle.ViewModel
 import androidx.lifecycle.ViewModelProvider
 import com.example.waterreminder.data.AppDatabase
 import com.example.waterreminder.data.WaterIntake
 import com.example.waterreminder.data.WaterRepository
 import com.example.waterreminder.ui.theme.WaterReminderTheme
 import kotlinx.coroutines.flow.SharingStarted
 import kotlinx.coroutines.flow.StateFlow
 import kotlinx.coroutines.flow.map
 import kotlinx.coroutines.flow.stateIn
 import kotlinx.coroutines.launch
 import java.text.SimpleDateFormat
 import java.util.*
 import kotlin.math.sin

 class MainActivity : ComponentActivity() {

    private val viewModel: WaterViewModel by viewModels {
        WaterViewModelFactory((application as WaterReminderApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaterReminderTheme {
                PermissionWrapper(viewModel)
            }
        }
    }
 }

 class WaterReminderApplication : android.app.Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { WaterRepository(database.waterIntakeDao()) }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Water Reminder"
            val descriptionText = "Channel for water reminder notifications"
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

 @Composable
 fun PermissionWrapper(viewModel: WaterViewModel) {
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
            MainScreen(viewModel)
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

 @OptIn(ExperimentalFoundationApi::class, ExperimentalTextApi::class)
 @Composable
 fun MainScreen(viewModel: WaterViewModel) {
    val tabs = listOf("Today", "Week", "Month")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    val todayIntake by viewModel.todayIntake.collectAsState()
    val weekIntake by viewModel.weekIntake.collectAsState()
    val monthIntake by viewModel.monthIntake.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()

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
                    totalMl = todayIntake,
                    onDrinkGlassClick = { viewModel.addWaterIntake(250) }
                )
                1 -> StatisticsScreen(
                    title = "This Week's Progress",
                    data = weekIntake
                )
                2 -> StatisticsScreen(
                    title = "Month of $currentMonth",
                    data = monthIntake
                )
            }
        }
    }
 }

 @Composable
 fun TodayScreen(totalMl: Int, onDrinkGlassClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        AnimatedWaterBottle(consumed = totalMl, goal = 2000)

        Button(onClick = onDrinkGlassClick) {
            Text(text = "I drank a glass (250ml)", fontSize = 16.sp)
        }
    }
 }

 @Composable
 fun AnimatedWaterBottle(consumed: Int, goal: Int) {
    val progress = if (goal > 0) (consumed.toFloat() / goal.toFloat()).coerceIn(0f, 1f) else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = androidx.compose.animation.core.LinearEasing),
        label = ""
    )

    Box(contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(200.dp, 300.dp)) { 
            val bottleWidth = size.width * 0.6f
            val bottleHeight = size.height * 0.9f
            val cornerRadius = CornerRadius(x = 30f, y = 30f)

            val bottlePath = Path().apply {
                moveTo(center.x - bottleWidth / 2, center.y + bottleHeight / 2)
                lineTo(center.x - bottleWidth / 2, center.y - bottleHeight / 2 + cornerRadius.y)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(Offset(center.x - bottleWidth/2, center.y - bottleHeight/2), Size(cornerRadius.x, cornerRadius.y)),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )
                lineTo(center.x + bottleWidth / 2 - cornerRadius.x, center.y - bottleHeight / 2)
                 arcTo(
                    rect = androidx.compose.ui.geometry.Rect(Offset(center.x + bottleWidth/2 - cornerRadius.x, center.y - bottleHeight/2), Size(cornerRadius.x, cornerRadius.y)),
                    startAngleDegrees = 90f,
                    sweepAngleDegrees = -90f,
                    forceMoveTo = false
                )
                lineTo(center.x + bottleWidth / 2, center.y + bottleHeight / 2)
                close()
            }

            // Water Animation
            clipPath(bottlePath) {
                val waterHeight = bottleHeight * animatedProgress
                val waveHeight = 10f
                val waveLength = 150f

                val waterPath = Path().apply {
                    moveTo(center.x - bottleWidth, center.y + bottleHeight / 2)
                    lineTo(center.x - bottleWidth, center.y + bottleHeight / 2 - waterHeight)

                    if (waterHeight > 0) {
                        for (i in 0..size.width.toInt() step 2) {
                            val x = i.toFloat()
                            val y = (sin((x + animatedProgress * size.width) * 2 * Math.PI / waveLength) * waveHeight).toFloat() + (center.y + bottleHeight/2 - waterHeight)
                            lineTo(x, y)
                        }
                    }
                    lineTo(center.x + bottleWidth, center.y + bottleHeight / 2)
                    close()
                }

                drawPath(waterPath, color = Color(0xFF89CFF0))
            }
            // Draw Bottle Outline
            drawPath(bottlePath, color = Color.Gray, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
             Text(
                text = "$consumed ml / $goal ml",
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
 }

 @OptIn(ExperimentalTextApi::class)
 @Composable
 fun StatisticsScreen(title: String, data: List<ChartData>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        if (data.isNotEmpty()) {
            BarChart(data = data)
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                 Text("No data available for this period yet.")
            }
        }
    }
 }

 @OptIn(ExperimentalTextApi::class)
 @Composable
 fun BarChart(data: List<ChartData>) {
    val maxIntake = data.maxOfOrNull { it.value } ?: 0
    val barColor = MaterialTheme.colorScheme.primary
    val textMeasurer = rememberTextMeasurer()
    val textColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(250.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = size.width / (data.size * 2)
                val spaceBetweenBars = barWidth

                data.forEachIndexed { index, chartData ->
                    val barHeight = if (maxIntake > 0) (chartData.value / maxIntake.toFloat()) * size.height * 0.8f else 0f
                    val startX = (index * (barWidth + spaceBetweenBars)) + spaceBetweenBars / 2

                    drawRect(
                        color = barColor,
                        topLeft = Offset(x = startX, y = size.height - barHeight),
                        size = Size(width = barWidth, height = barHeight)
                    )

                    val textLayoutResult = textMeasurer.measure(
                        text = AnnotatedString(chartData.label),
                        style = TextStyle(fontSize = 12.sp, color = textColor)
                    )

                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = startX + barWidth / 2 - textLayoutResult.size.width / 2,
                            y = size.height + 4.dp.toPx() - textLayoutResult.size.height
                        )
                    )
                }
            }
        }
    }
 }


 @Preview(showBackground = true)
 @Composable
 fun DefaultPreview() {
    WaterReminderTheme {
        AnimatedWaterBottle(consumed = 1250, goal = 2000)
    }
 }

 data class ChartData(val label: String, val value: Int)

 class WaterViewModel(private val repository: WaterRepository) : ViewModel() {

    private val coroutineScope = kotlinx.coroutines.MainScope()

    private val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val weekStart = Calendar.getInstance().apply {
        firstDayOfWeek = Calendar.MONDAY
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private val monthStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val currentMonth: StateFlow<String> = kotlinx.coroutines.flow.MutableStateFlow(
        SimpleDateFormat("MMMM", Locale.getDefault()).format(Date(monthStart))
    ).stateIn(coroutineScope, SharingStarted.Lazily, "")

    val todayIntake: StateFlow<Int> = repository.getTodayIntake(todayStart)
        .map { list -> list.sumOf { it.amount } }
        .stateIn(coroutineScope, SharingStarted.Lazily, 0)

    val weekIntake: StateFlow<List<ChartData>> = repository.getWeekIntake(weekStart)
        .map { transformToWeeklyChartData(it) }
        .stateIn(coroutineScope, SharingStarted.Lazily, emptyList())

    val monthIntake: StateFlow<List<ChartData>> = repository.getMonthIntake(monthStart)
        .map { transformToMonthlyChartData(it) }
        .stateIn(coroutineScope, SharingStarted.Lazily, emptyList())

    fun addWaterIntake(amount: Int) {
        coroutineScope.launch {
            repository.addWaterIntake(amount)
        }
    }

    private fun transformToWeeklyChartData(intakeList: List<WaterIntake>): List<ChartData> {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        val dayFormat = SimpleDateFormat("E", Locale.getDefault()) // "Mon", "Tue"

        val dayNames = (0..6).map {
            calendar.time = Date(weekStart)
            calendar.add(Calendar.DAY_OF_WEEK, it)
            dayFormat.format(calendar.time)
        }

        val weeklyData = dayNames.associateWith { 0 }.toMutableMap()

        intakeList.forEach { intake ->
            val day = dayFormat.format(Date(intake.timestamp))
            if (weeklyData.containsKey(day)) {
                weeklyData[day] = (weeklyData[day] ?: 0) + intake.amount
            }
        }
        return dayNames.map { ChartData(it, weeklyData[it] ?: 0) }
    }

    private fun transformToMonthlyChartData(intakeList: List<WaterIntake>): List<ChartData> {
        val calendar = Calendar.getInstance()
        calendar.time = Date(monthStart)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val monthlyData = (1..maxDays).associate {
            it.toString() to 0
        }.toMutableMap()
        
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())

        intakeList.forEach { intake ->
            val dayOfMonth = dayFormat.format(Date(intake.timestamp))
            monthlyData[dayOfMonth] = (monthlyData[dayOfMonth] ?: 0) + intake.amount
        }
        return monthlyData.map { ChartData(it.key, it.value) }
    }
 }

 class WaterViewModelFactory(private val repository: WaterRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
 }
