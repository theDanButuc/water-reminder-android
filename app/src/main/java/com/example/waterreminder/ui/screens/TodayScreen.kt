package com.example.waterreminder.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterreminder.R
import com.example.waterreminder.data.db.entity.DrinkType
import com.example.waterreminder.data.db.entity.WaterIntake
import com.example.waterreminder.ui.components.HydrationProgressRing
import com.example.waterreminder.ui.home.WaterViewModel
import com.example.waterreminder.util.DrinkPreset
import com.example.waterreminder.util.defaultPresetsFor

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    totalMl: Int,
    dailyGoal: Int,
    selectedDrinkType: DrinkType,
    todayEntries: List<WaterIntake>,
    streaks: WaterViewModel.StreakData,
    onDrinkTypeSelected: (DrinkType) -> Unit,
    onAddDrink: (amount: Int, presetLabel: String?) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var showCustomDialog by rememberSaveable { mutableStateOf(false) }
    var selectedPreset by rememberSaveable { mutableStateOf<DrinkPreset?>(null) }

    val remaining = (dailyGoal - totalMl).coerceAtLeast(0)
    val presets = defaultPresetsFor(selectedDrinkType)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Circular progress ring
        HydrationProgressRing(consumed = totalMl, goal = dailyGoal)

        // Streak card
        if (streaks.current > 0 || streaks.best > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔥", fontSize = 24.sp)
                        Text(
                            text = stringResource(R.string.streak_current, streaks.current),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = stringResource(R.string.streak_current_label),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🏆", fontSize = 24.sp)
                        Text(
                            text = stringResource(R.string.streak_best, streaks.best),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = stringResource(R.string.streak_best_label),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }

        // Progress text
        Text(
            text = if (remaining > 0) {
                stringResource(R.string.remaining_text, remaining)
            } else {
                stringResource(R.string.goal_completed)
            },
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Drink Type Picker
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.drink_type_label),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(DrinkType.entries) { type ->
                    FilterChip(
                        selected = selectedDrinkType == type,
                        onClick = {
                            onDrinkTypeSelected(type)
                            selectedPreset = null
                        },
                        label = { Text("${type.emoji} ${type.displayName}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }

        // Amount Presets (per drink type)
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.select_amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                presets.forEach { preset ->
                    ElevatedFilterChip(
                        selected = selectedPreset == preset,
                        onClick = {
                            selectedPreset = preset
                            onAddDrink(preset.amountMl, preset.label)
                        },
                        label = { Text(preset.label) },
                        modifier = Modifier.padding(horizontal = 4.dp),
                        colors = FilterChipDefaults.elevatedFilterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
                ElevatedFilterChip(
                    selected = selectedPreset == null && false,
                    onClick = { showCustomDialog = true },
                    label = { Text(stringResource(R.string.custom_amount)) },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )
            }
        }

        // Today's Log
        if (todayEntries.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.todays_log),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                todayEntries.take(10).forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            Text(text = entry.type.emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${entry.displayLabel} ${entry.type.displayName}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (entry.type.hydrationFactor < 1.0f) {
                            Text(
                                text = stringResource(R.string.effective_amount, entry.effectiveAmount),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (todayEntries.size > 10) {
                    Text(
                        text = stringResource(R.string.more_entries, todayEntries.size - 10),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }

    if (showCustomDialog) {
        CustomAmountDialog(
            onDismiss = { showCustomDialog = false },
            onConfirm = { amount ->
                selectedPreset = null
                onAddDrink(amount, null)
                showCustomDialog = false
            }
        )
    }
}

@Composable
private fun CustomAmountDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var textValue by rememberSaveable { mutableStateOf("") }
    val amount = textValue.toIntOrNull()
    val isValid = amount != null && amount in 1..5000

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_amount_title)) },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = { if (it.all(Char::isDigit) && it.length <= 4) textValue = it },
                label = { Text(stringResource(R.string.amount_ml_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { if (isValid) onConfirm(amount!!) }, enabled = isValid) {
                Text(stringResource(R.string.add_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_button)) }
        }
    )
}
