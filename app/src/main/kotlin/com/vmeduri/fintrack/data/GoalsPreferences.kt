package com.vmeduri.fintrack.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * User-editable goals and reminder settings, backed by Jetpack DataStore (the modern,
 * async-safe replacement for SharedPreferences). Everything here lives only on the
 * device — there's no account and no server.
 */
class GoalsPreferences(private val context: Context) {

    companion object {
        val WEEKLY_GOAL_CENTS = longPreferencesKey("weekly_goal_cents")
        val MONTHLY_GOAL_CENTS = longPreferencesKey("monthly_goal_cents")
        val HEALTHY_FOOD_GOAL_CENTS = longPreferencesKey("healthy_food_goal_cents")
        val HEALTHY_CARE_GOAL_CENTS = longPreferencesKey("healthy_care_goal_cents")
        val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
        val REMINDER_HOUR = intPreferencesKey("reminder_hour")

        // R4,500 / R29,000, as requested. Change these anytime from the Settings screen.
        const val DEFAULT_WEEKLY_GOAL_CENTS = 450_000L
        const val DEFAULT_MONTHLY_GOAL_CENTS = 2_900_000L
        const val DEFAULT_REMINDER_HOUR = 18 // 18:00 = 6pm
    }

    val weeklyGoalCents: Flow<Long> =
        context.dataStore.data.map { it[WEEKLY_GOAL_CENTS] ?: DEFAULT_WEEKLY_GOAL_CENTS }

    val monthlyGoalCents: Flow<Long> =
        context.dataStore.data.map { it[MONTHLY_GOAL_CENTS] ?: DEFAULT_MONTHLY_GOAL_CENTS }

    /** 0 means "no goal set" — the dashboard just shows the running total in that case. */
    val healthyFoodGoalCents: Flow<Long> =
        context.dataStore.data.map { it[HEALTHY_FOOD_GOAL_CENTS] ?: 0L }

    val healthyCareGoalCents: Flow<Long> =
        context.dataStore.data.map { it[HEALTHY_CARE_GOAL_CENTS] ?: 0L }

    val remindersEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[REMINDERS_ENABLED] ?: true }

    val reminderHour: Flow<Int> =
        context.dataStore.data.map { it[REMINDER_HOUR] ?: DEFAULT_REMINDER_HOUR }

    suspend fun setWeeklyGoalCents(value: Long) {
        context.dataStore.edit { it[WEEKLY_GOAL_CENTS] = value }
    }

    suspend fun setMonthlyGoalCents(value: Long) {
        context.dataStore.edit { it[MONTHLY_GOAL_CENTS] = value }
    }

    suspend fun setHealthyFoodGoalCents(value: Long) {
        context.dataStore.edit { it[HEALTHY_FOOD_GOAL_CENTS] = value }
    }

    suspend fun setHealthyCareGoalCents(value: Long) {
        context.dataStore.edit { it[HEALTHY_CARE_GOAL_CENTS] = value }
    }

    suspend fun setRemindersEnabled(value: Boolean) {
        context.dataStore.edit { it[REMINDERS_ENABLED] = value }
    }

    suspend fun setReminderHour(value: Int) {
        context.dataStore.edit { it[REMINDER_HOUR] = value.coerceIn(0, 23) }
    }
}
