package com.vmeduri.fintrack.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vmeduri.fintrack.data.GoalsPreferences
import com.vmeduri.fintrack.reminders.ReminderScheduler
import com.vmeduri.fintrack.util.CurrencyUtils
import com.vmeduri.fintrack.widget.WidgetUpdater
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val weeklyGoalCents: Long = GoalsPreferences.DEFAULT_WEEKLY_GOAL_CENTS,
    val monthlyGoalCents: Long = GoalsPreferences.DEFAULT_MONTHLY_GOAL_CENTS,
    val healthyFoodGoalCents: Long = 0,
    val healthyCareGoalCents: Long = 0,
    val remindersEnabled: Boolean = true,
    val reminderHour: Int = GoalsPreferences.DEFAULT_REMINDER_HOUR
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = GoalsPreferences(application)

    // kotlinx.coroutines' combine() only has typed overloads up to 5 flows, and the
    // 4 goal amounts are Long while the 2 reminder settings are Boolean/Int, so this
    // combines in two typed stages rather than one 6-argument (or mixed-type vararg) call.
    private val goalAmounts = combine(
        prefs.weeklyGoalCents,
        prefs.monthlyGoalCents,
        prefs.healthyFoodGoalCents,
        prefs.healthyCareGoalCents
    ) { weekly, monthly, food, care -> listOf(weekly, monthly, food, care) }

    private val reminderSettings = combine(
        prefs.remindersEnabled,
        prefs.reminderHour
    ) { enabled, hour -> enabled to hour }

    val uiState: StateFlow<SettingsUiState> = combine(goalAmounts, reminderSettings) { goals, reminders ->
        SettingsUiState(
            weeklyGoalCents = goals[0],
            monthlyGoalCents = goals[1],
            healthyFoodGoalCents = goals[2],
            healthyCareGoalCents = goals[3],
            remindersEnabled = reminders.first,
            reminderHour = reminders.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun setWeeklyGoal(rands: String) {
        val cents = CurrencyUtils.parseRandsToCents(rands) ?: return
        viewModelScope.launch {
            prefs.setWeeklyGoalCents(cents)
            WidgetUpdater.update(getApplication())
        }
    }

    fun setMonthlyGoal(rands: String) {
        val cents = CurrencyUtils.parseRandsToCents(rands) ?: return
        viewModelScope.launch {
            prefs.setMonthlyGoalCents(cents)
            WidgetUpdater.update(getApplication())
        }
    }

    /** Blank input clears the (optional) goal back to "not set". */
    fun setHealthyFoodGoal(rands: String) {
        val cents = if (rands.isBlank()) 0L else (CurrencyUtils.parseRandsToCents(rands) ?: return)
        viewModelScope.launch {
            prefs.setHealthyFoodGoalCents(cents)
            WidgetUpdater.update(getApplication())
        }
    }

    fun setHealthyCareGoal(rands: String) {
        val cents = if (rands.isBlank()) 0L else (CurrencyUtils.parseRandsToCents(rands) ?: return)
        viewModelScope.launch {
            prefs.setHealthyCareGoalCents(cents)
            WidgetUpdater.update(getApplication())
        }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setRemindersEnabled(enabled)
            ReminderScheduler.scheduleNext(getApplication())
        }
    }

    fun setReminderHour(hour: Int) {
        viewModelScope.launch {
            prefs.setReminderHour(hour)
            ReminderScheduler.scheduleNext(getApplication())
        }
    }
}
