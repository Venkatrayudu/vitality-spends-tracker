package com.vmeduri.fintrack.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vmeduri.fintrack.data.AppDatabase
import com.vmeduri.fintrack.data.Category
import com.vmeduri.fintrack.data.GoalsPreferences
import com.vmeduri.fintrack.util.DateUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

data class DashboardUiState(
    val weekSpentCents: Long = 0,
    val weekGoalCents: Long = GoalsPreferences.DEFAULT_WEEKLY_GOAL_CENTS,
    val monthSpentCents: Long = 0,
    val monthGoalCents: Long = GoalsPreferences.DEFAULT_MONTHLY_GOAL_CENTS,
    val healthyFoodSpentCents: Long = 0,
    val healthyFoodGoalCents: Long = 0,
    val healthyCareSpentCents: Long = 0,
    val healthyCareGoalCents: Long = 0
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).transactionDao()
    private val prefs = GoalsPreferences(application)

    // Computed once when the ViewModel is created. If the app is left open across a
    // day/week/month boundary the numbers refresh next time the screen is reopened.
    private val today: LocalDate = LocalDate.now()
    private val weekStart = DateUtils.startOfWeek(today)
    private val weekEnd = DateUtils.endOfWeek(today)
    private val monthStart = DateUtils.startOfMonth(today)
    private val monthEnd = DateUtils.endOfMonth(today)

    val uiState: StateFlow<DashboardUiState> = combine(
        dao.observeTotalBetween(weekStart, weekEnd),
        prefs.weeklyGoalCents,
        dao.observeTotalBetween(monthStart, monthEnd),
        prefs.monthlyGoalCents,
        dao.observeTotalBetweenForCategory(monthStart, monthEnd, Category.HEALTHY_FOOD),
        prefs.healthyFoodGoalCents,
        dao.observeTotalBetweenForCategory(monthStart, monthEnd, Category.HEALTHY_CARE),
        prefs.healthyCareGoalCents
    ) { values ->
        DashboardUiState(
            weekSpentCents = values[0] as Long,
            weekGoalCents = values[1] as Long,
            monthSpentCents = values[2] as Long,
            monthGoalCents = values[3] as Long,
            healthyFoodSpentCents = values[4] as Long,
            healthyFoodGoalCents = values[5] as Long,
            healthyCareSpentCents = values[6] as Long,
            healthyCareGoalCents = values[7] as Long
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())
}
