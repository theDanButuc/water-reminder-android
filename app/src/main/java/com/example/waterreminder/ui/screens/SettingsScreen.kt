package com.example.waterreminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentGoalMl: Int,
    currentWakeTime: String,
    currentSleepTime: String,
    onBack: () -> Unit,
    onSave: (goalMl: Int, wakeTime: String, sleepTime: String) -> Unit,
    onResetToday: () -> Unit,
    onRestartOnboarding: () -> Unit
) {
    var goalText by rememberSaveable { mutableStateOf(currentGoalMl.toString()) }
    var wakeTime by rememberSaveable { mutableStateOf(currentWakeTime) }
    var sleepTime by rememberSaveable { mutableStateOf(currentSleepTime) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    var showOnboardingDialog by rememberSaveable { mutableStateOf(false) }

    val goalMl = goalText.toIntOrNull() ?: 0
    val isGoalValid = goalMl in 500..5000
    val canSave = isGoalValid && wakeTime.length == 5 && sleepTime.length == 5

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hydration Goal
            SectionHeader(stringResource(R.string.settings_goal_section))

            OutlinedTextField(
                value = goalText,
                onValueChange = { if (it.all(Char::isDigit) && it.length <= 5) goalText = it },
                label = { Text(stringResource(R.string.daily_goal_label)) },
                suffix = { Text("ml") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = goalText.isNotEmpty() && !isGoalValid,
                supportingText = {
                    if (goalText.isNotEmpty() && !isGoalValid) {
                        Text(stringResource(R.string.goal_range_hint))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1500, 2000, 2500, 3000).forEach { preset ->
                    OutlinedButton(onClick = { goalText = preset.toString() }) {
                        Text("${preset}ml")
                    }
                }
            }

            Divider()

            // Schedule
            SectionHeader(stringResource(R.string.settings_schedule_section))

            OutlinedTextField(
                value = wakeTime,
                onValueChange = { if (it.length <= 5) wakeTime = it },
                label = { Text(stringResource(R.string.wake_time_label)) },
                placeholder = { Text("07:00") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = sleepTime,
                onValueChange = { if (it.length <= 5) sleepTime = it },
                label = { Text(stringResource(R.string.sleep_time_label)) },
                placeholder = { Text("23:00") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.settings_interval_note),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { onSave(goalMl, wakeTime, sleepTime) },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save_button))
            }

            Divider()

            // Danger zone
            SectionHeader(stringResource(R.string.settings_danger_section))

            OutlinedButton(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.settings_reset_today))
            }

            OutlinedButton(
                onClick = { showOnboardingDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_restart_onboarding))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.settings_reset_today_title)) },
            text = { Text(stringResource(R.string.settings_reset_today_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onResetToday()
                        showResetDialog = false
                    }
                ) {
                    Text(
                        stringResource(R.string.settings_reset_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    if (showOnboardingDialog) {
        AlertDialog(
            onDismissRequest = { showOnboardingDialog = false },
            title = { Text(stringResource(R.string.settings_restart_onboarding_title)) },
            text = { Text(stringResource(R.string.settings_restart_onboarding_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRestartOnboarding()
                        showOnboardingDialog = false
                    }
                ) {
                    Text(stringResource(R.string.confirm_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { showOnboardingDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp)
    )
}
