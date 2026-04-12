package com.example.waterreminder.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.waterreminder.R
import com.example.waterreminder.ui.home.WaterViewModel
import kotlinx.coroutines.launch

@Composable
fun PermissionWrapper(viewModel: WaterViewModel) {
    val context = LocalContext.current

    var hasNotificationPermission by remember { mutableStateOf(checkNotificationPermission(context)) }

    // Re-check permission whenever the app comes back to foreground
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission = checkNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

    if (hasNotificationPermission) {
        MainScreen(viewModel)
    } else {
        PermissionRequestScreen(
            title = stringResource(R.string.notification_permission_title),
            text = stringResource(R.string.notification_permission_text),
            buttonText = stringResource(R.string.grant_permission),
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    hasNotificationPermission = true
                }
            }
        )
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: WaterViewModel) {
    val tabs = listOf(
        stringResource(R.string.tab_today),
        stringResource(R.string.tab_week),
        stringResource(R.string.tab_month)
    )
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSettings by remember { mutableStateOf(false) }

    val todayIntake by viewModel.todayIntake.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val wakeTime by viewModel.wakeTime.collectAsState()
    val sleepTime by viewModel.sleepTime.collectAsState()
    val selectedDrinkType by viewModel.selectedDrinkType.collectAsState()
    val todayEntries by viewModel.todayEntries.collectAsState()
    val streaks by viewModel.streaks.collectAsState()
    val weekIntake by viewModel.weekIntake.collectAsState()
    val monthIntake by viewModel.monthIntake.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()

    val goalReachedMessage = stringResource(R.string.goal_reached_congrats)

    LaunchedEffect(Unit) {
        viewModel.goalReachedEvent.collect {
            snackbarHostState.showSnackbar(goalReachedMessage)
        }
    }

    if (showSettings) {
        SettingsScreen(
            currentGoalMl = dailyGoal,
            currentWakeTime = wakeTime,
            currentSleepTime = sleepTime,
            onBack = { showSettings = false },
            onSave = { goal, wake, sleep ->
                viewModel.saveSettings(goal, wake, sleep)
                showSettings = false
            },
            onResetToday = {
                viewModel.resetTodayData()
                showSettings = false
            },
            onRestartOnboarding = {
                viewModel.resetOnboarding()
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> TodayScreen(
                        totalMl = todayIntake,
                        dailyGoal = dailyGoal,
                        selectedDrinkType = selectedDrinkType,
                        todayEntries = todayEntries,
                        streaks = streaks,
                        onDrinkTypeSelected = { viewModel.setDrinkType(it) },
                        onAddDrink = { amount, label -> viewModel.addWaterIntake(amount, label) },
                        snackbarHostState = snackbarHostState
                    )
                    1 -> StatisticsScreen(
                        title = stringResource(R.string.week_progress_title),
                        data = weekIntake,
                        goalMl = dailyGoal
                    )
                    2 -> StatisticsScreen(
                        title = stringResource(R.string.month_title, currentMonth),
                        data = monthIntake,
                        goalMl = dailyGoal
                    )
                }
            }
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
