package com.example.waterreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.waterreminder.ui.home.WaterViewModel
import com.example.waterreminder.ui.home.WaterViewModelFactory
import com.example.waterreminder.ui.screens.OnboardingScreen
import com.example.waterreminder.ui.screens.PermissionWrapper
import com.example.waterreminder.ui.theme.WaterReminderTheme

class MainActivity : ComponentActivity() {

    private val viewModel: WaterViewModel by viewModels {
        val app = application as WaterReminderApplication
        WaterViewModelFactory(app.repository, app.userPreferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WaterReminderTheme {
                val onboardingDone by viewModel.onboardingDone.collectAsState()
                if (onboardingDone) {
                    PermissionWrapper(viewModel)
                } else {
                    OnboardingScreen(
                        onFinished = { goalMl, wakeTime, sleepTime ->
                            viewModel.completeOnboarding(goalMl, wakeTime, sleepTime)
                        }
                    )
                }
            }
        }
    }
}
