package com.example.waterreminder.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.waterreminder.data.db.entity.DrinkType
import com.example.waterreminder.data.db.entity.WaterIntake
import com.example.waterreminder.data.preferences.UserPreferences
import com.example.waterreminder.data.repository.WaterRepository
import com.example.waterreminder.ui.components.ChartData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WaterViewModel(
    private val repository: WaterRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

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

    val currentMonth: StateFlow<String> = MutableStateFlow(
        SimpleDateFormat("MMMM", Locale.getDefault()).format(Date(monthStart))
    ).stateIn(viewModelScope, SharingStarted.Lazily, "")

    val onboardingDone: StateFlow<Boolean> = userPreferences.onboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val dailyGoal: StateFlow<Int> = userPreferences.dailyGoalMl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)

    private val _selectedDrinkType = MutableStateFlow(DrinkType.WATER)
    val selectedDrinkType: StateFlow<DrinkType> = _selectedDrinkType

    val todayEntries: StateFlow<List<WaterIntake>> = repository.getTodayIntake(todayStart)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayIntake: StateFlow<Int> = repository.getTodayIntake(todayStart)
        .map { list -> list.sumOf { it.effectiveAmount } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _goalReachedEvent = MutableSharedFlow<Unit>()
    val goalReachedEvent = _goalReachedEvent.asSharedFlow()

    private var goalAlreadyReachedToday = false

    val weekIntake: StateFlow<List<ChartData>> = repository.getWeekIntake(weekStart)
        .map { transformToWeeklyChartData(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthIntake: StateFlow<List<ChartData>> = repository.getMonthIntake(monthStart)
        .map { transformToMonthlyChartData(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Restore last selected drink type from prefs
        viewModelScope.launch {
            val lastType = runCatching {
                DrinkType.valueOf(userPreferences.lastSelectedDrinkType.first())
            }.getOrDefault(DrinkType.WATER)
            _selectedDrinkType.value = lastType
        }

        // Watch for goal reached
        viewModelScope.launch {
            combine(todayIntake, dailyGoal) { intake, goal -> intake to goal }
                .collect { (intake, goal) ->
                    if (intake >= goal && !goalAlreadyReachedToday) {
                        goalAlreadyReachedToday = true
                        _goalReachedEvent.emit(Unit)
                    }
                }
        }
    }

    fun setDrinkType(type: DrinkType) {
        _selectedDrinkType.value = type
        viewModelScope.launch {
            userPreferences.setLastSelectedDrinkType(type.name)
        }
    }

    val wakeTime: StateFlow<String> = userPreferences.wakeTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "07:00")

    val sleepTime: StateFlow<String> = userPreferences.sleepTime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "23:00")

    fun completeOnboarding(goalMl: Int, wakeTime: String, sleepTime: String) {
        viewModelScope.launch {
            userPreferences.setDailyGoalMl(goalMl)
            userPreferences.setWakeTime(wakeTime)
            userPreferences.setSleepTime(sleepTime)
            userPreferences.computeAndSaveInterval()
            userPreferences.setOnboardingDone(true)
        }
    }

    fun saveSettings(goalMl: Int, wakeTime: String, sleepTime: String) {
        viewModelScope.launch {
            userPreferences.setDailyGoalMl(goalMl)
            userPreferences.setWakeTime(wakeTime)
            userPreferences.setSleepTime(sleepTime)
            userPreferences.computeAndSaveInterval()
        }
    }

    fun resetTodayData() {
        viewModelScope.launch {
            repository.deleteTodayIntake(todayStart)
            goalAlreadyReachedToday = false
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            userPreferences.setOnboardingDone(false)
        }
    }

    fun addWaterIntake(amount: Int, presetLabel: String? = null) {
        viewModelScope.launch {
            repository.addWaterIntake(amount, _selectedDrinkType.value, presetLabel)
        }
    }

    private fun transformToWeeklyChartData(intakeList: List<WaterIntake>): List<ChartData> {
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        val dayFormat = SimpleDateFormat("E", Locale.getDefault())

        val dayNames = (0..6).map {
            calendar.time = Date(weekStart)
            calendar.add(Calendar.DAY_OF_WEEK, it)
            dayFormat.format(calendar.time)
        }

        val weeklyData = dayNames.associateWith { 0 }.toMutableMap()
        intakeList.forEach { intake ->
            val day = dayFormat.format(Date(intake.timestamp))
            if (weeklyData.containsKey(day)) {
                weeklyData[day] = (weeklyData[day] ?: 0) + intake.effectiveAmount
            }
        }
        return dayNames.map { ChartData(it, weeklyData[it] ?: 0) }
    }

    private fun transformToMonthlyChartData(intakeList: List<WaterIntake>): List<ChartData> {
        val calendar = Calendar.getInstance()
        calendar.time = Date(monthStart)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val monthlyData = (1..maxDays).associate { it.toString() to 0 }.toMutableMap()
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())

        intakeList.forEach { intake ->
            val dayOfMonth = dayFormat.format(Date(intake.timestamp))
            monthlyData[dayOfMonth] = (monthlyData[dayOfMonth] ?: 0) + intake.effectiveAmount
        }
        return monthlyData.map { ChartData(it.key, it.value) }
    }
}

class WaterViewModelFactory(
    private val repository: WaterRepository,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WaterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WaterViewModel(repository, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
