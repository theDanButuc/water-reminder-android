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
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferences(private val context: Context) {

    object PreferenceKeys {
        val DAILY_GOAL_ML = intPreferencesKey("daily_goal_ml")
        val DEFAULT_CUP_ML = intPreferencesKey("default_cup_ml")
        val NOTIFICATION_INTERVAL_H = intPreferencesKey("notif_interval")
        val QUIET_HOURS_START = stringPreferencesKey("quiet_start")
        val QUIET_HOURS_END = stringPreferencesKey("quiet_end")
        val WAKE_TIME = stringPreferencesKey("wake_time")
        val SLEEP_TIME = stringPreferencesKey("sleep_time")
        val UNIT_ML = booleanPreferencesKey("unit_ml")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }

    val dailyGoalMl: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DAILY_GOAL_ML] ?: 2000
    }

    val defaultCupMl: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.DEFAULT_CUP_ML] ?: 250
    }

    val notificationIntervalH: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.NOTIFICATION_INTERVAL_H] ?: 2
    }

    val quietHoursStart: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.QUIET_HOURS_START] ?: "22:00"
    }

    val quietHoursEnd: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.QUIET_HOURS_END] ?: "07:00"
    }

    val unitMl: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferenceKeys.UNIT_ML] ?: true
    }

    suspend fun setDailyGoalMl(goalMl: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.DAILY_GOAL_ML] = goalMl
        }
    }

    suspend fun setDefaultCupMl(cupMl: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.DEFAULT_CUP_ML] = cupMl
        }
    }

    suspend fun setNotificationIntervalH(hours: Int) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.NOTIFICATION_INTERVAL_H] = hours
        }
    }

    suspend fun setQuietHours(start: String, end: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferenceKeys.QUIET_HOURS_START] = start
            prefs[PreferenceKeys.QUIET_HOURS_END] = end
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
}
