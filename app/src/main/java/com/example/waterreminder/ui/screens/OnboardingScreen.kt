package com.example.waterreminder.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.R

@Composable
fun OnboardingScreen(
    onFinished: (goalMl: Int, wakeTime: String, sleepTime: String) -> Unit
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var goalMl by rememberSaveable { mutableIntStateOf(2000) }
    var wakeTime by rememberSaveable { mutableStateOf("07:00") }
    var sleepTime by rememberSaveable { mutableStateOf("23:00") }

    // Must be declared before any conditional composable calls (Compose rules)
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { onFinished(goalMl, wakeTime, sleepTime) }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        LinearProgressIndicator(
            progress = (step + 1) / 4f,
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedContent(
            targetState = step,
            transitionSpec = {
                (slideInHorizontally { it } + fadeIn()) togetherWith
                        (slideOutHorizontally { -it } + fadeOut())
            },
            label = "onboarding_step"
        ) { currentStep ->
            when (currentStep) {
                0 -> WelcomeStep()
                1 -> GoalStep(goalMl = goalMl, onGoalChanged = { goalMl = it })
                2 -> TimesStep(
                    wakeTime = wakeTime,
                    sleepTime = sleepTime,
                    onWakeChanged = { wakeTime = it },
                    onSleepChanged = { sleepTime = it }
                )
                3 -> NotificationStep()
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (step > 0) {
                OutlinedButton(
                    onClick = { step-- },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.back_button))
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            val isLastStep = step == 3

            Button(
                onClick = {
                    if (isLastStep) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            onFinished(goalMl, wakeTime, sleepTime)
                        }
                    } else {
                        step++
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = step != 1 || goalMl in 500..5000
            ) {
                Text(
                    if (isLastStep) stringResource(R.string.get_started_button)
                    else stringResource(R.string.next_button)
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "💧", fontSize = 72.sp)
        Text(
            text = stringResource(R.string.onboarding_welcome_title),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.onboarding_welcome_text),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GoalStep(goalMl: Int, onGoalChanged: (Int) -> Unit) {
    var textValue by rememberSaveable { mutableStateOf(goalMl.toString()) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.onboarding_goal_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.onboarding_goal_text),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = textValue,
            onValueChange = { input ->
                if (input.all(Char::isDigit) && input.length <= 5) {
                    textValue = input
                    input.toIntOrNull()?.let { onGoalChanged(it) }
                }
            },
            label = { Text(stringResource(R.string.daily_goal_label)) },
            suffix = { Text("ml") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            isError = goalMl !in 500..5000,
            supportingText = {
                if (goalMl !in 500..5000) {
                    Text(stringResource(R.string.goal_range_hint))
                }
            }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1500, 2000, 2500, 3000).forEach { preset ->
                OutlinedButton(onClick = {
                    textValue = preset.toString()
                    onGoalChanged(preset)
                }) {
                    Text("${preset}ml")
                }
            }
        }
    }
}

@Composable
private fun TimesStep(
    wakeTime: String,
    sleepTime: String,
    onWakeChanged: (String) -> Unit,
    onSleepChanged: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.onboarding_times_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.onboarding_times_text),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = wakeTime,
            onValueChange = { if (it.length <= 5) onWakeChanged(it) },
            label = { Text(stringResource(R.string.wake_time_label)) },
            placeholder = { Text("07:00") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = sleepTime,
            onValueChange = { if (it.length <= 5) onSleepChanged(it) },
            label = { Text(stringResource(R.string.sleep_time_label)) },
            placeholder = { Text("23:00") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.onboarding_times_hint),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NotificationStep() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "🔔", fontSize = 72.sp)
        Text(
            text = stringResource(R.string.onboarding_notif_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.onboarding_notif_text),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
