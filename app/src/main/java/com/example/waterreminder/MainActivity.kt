package com.example.waterreminder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.waterreminder.ui.home.WaterViewModel
import com.example.waterreminder.ui.home.WaterViewModelFactory
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
                PermissionWrapper(viewModel)
            }
        }
    }
}
