package com.example.waterreminder.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    object PreferenceKeys {
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
        val WAKE_TIME = stringPreferencesKey("wake_time")
        val SLEEP_TIME = stringPreferencesKey("sleep_time")
        val NOTIFICATION_INTERVAL_MIN = intPreferencesKey("notif_interval_min")
        val UNIT_ML = booleanPreferencesKey("unit_ml")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val LAST_SELECTED_DRINK_TYPE = stringPreferencesKey("last_drink_type")
    }

    val dailyGoalMl: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DAILY_GOAL_ML] ?: 2000
    }

    val wakeTime: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.WAKE_TIME] ?: "07:00"
    }

    val sleepTime: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.SLEEP_TIME] ?: "23:00"
    }

    val notificationIntervalMin: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.NOTIFICATION_INTERVAL_MIN] ?: 120
    }

    val unitMl: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.UNIT_ML] ?: true
    }

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.ONBOARDING_DONE] ?: false
    }

    val lastSelectedDrinkType: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.LAST_SELECTED_DRINK_TYPE] ?: "WATER"
    }

    suspend fun setDailyGoalMl(goalMl: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.DAILY_GOAL_ML] = goalMl
        }
    }

    suspend fun setWakeTime(time: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.WAKE_TIME] = time
        }
    }

    suspend fun setSleepTime(time: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.SLEEP_TIME] = time
        }
    }

    suspend fun setNotificationIntervalMin(minutes: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.NOTIFICATION_INTERVAL_MIN] = minutes
        }
    }

    suspend fun setUnitMl(isMl: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.UNIT_ML] = isMl
        }
    }

    suspend fun setOnboardingDone(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.ONBOARDING_DONE] = done
        }
    }

    suspend fun setLastSelectedDrinkType(type: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.LAST_SELECTED_DRINK_TYPE] = type
        }
    }

    /** Computes notification interval from goal + wake/sleep window. */
    suspend fun computeAndSaveInterval() {
        val goal = dailyGoalMl.first()
        val wake = wakeTime.first()
        val sleep = sleepTime.first()

        val wakeMinutes = timeToMinutes(wake)
        val sleepMinutes = timeToMinutes(sleep)
        val awakeMinutes = if (sleepMinutes > wakeMinutes) sleepMinutes - wakeMinutes else (24 * 60) - wakeMinutes + sleepMinutes

        val glasses = (goal / 250.0).coerceAtLeast(1.0)
        val intervalMin = (awakeMinutes / glasses).toInt().coerceIn(30, 240)

        setNotificationIntervalMin(intervalMin)
    }

    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return if (parts.size == 2) {
            (parts[0].toIntOrNull() ?: 7) * 60 + (parts[1].toIntOrNull() ?: 0)
        } else 7 * 60
    }
}
