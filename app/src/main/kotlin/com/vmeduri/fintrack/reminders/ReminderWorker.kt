package com.vmeduri.fintrack.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vmeduri.fintrack.data.AppDatabase
import com.vmeduri.fintrack.data.GoalsPreferences
import com.vmeduri.fintrack.util.CurrencyUtils
import com.vmeduri.fintrack.util.DateUtils
import kotlinx.coroutines.flow.first
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Runs once a day at the configured reminder hour. Fires the weekly-goal notification
 * only on Fridays, and the monthly-goal notification only on the last day of the month —
 * and only when the relevant goal hasn't been reached yet, exactly as requested.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val prefs = GoalsPreferences(context)

        if (prefs.remindersEnabled.first()) {
            val dao = AppDatabase.getInstance(context).transactionDao()
            val today = LocalDate.now()

            if (today.dayOfWeek == DayOfWeek.FRIDAY) {
                checkWeeklyGoal(context, today, dao = { start, end -> dao.totalBetween(start, end) }, prefs)
            }

            if (DateUtils.isLastDayOfMonth(today)) {
                checkMonthlyGoal(context, today, dao = { start, end -> dao.totalBetween(start, end) }, prefs)
            }
        }

        // Chain: line up tomorrow's check regardless, so re-enabling reminders later
        // doesn't require reopening the app.
        ReminderScheduler.scheduleNext(context)
        return Result.success()
    }

    private suspend fun checkWeeklyGoal(
        context: Context,
        today: LocalDate,
        dao: suspend (LocalDate, LocalDate) -> Long,
        prefs: GoalsPreferences
    ) {
        val spent = dao(DateUtils.startOfWeek(today), DateUtils.endOfWeek(today))
        val goal = prefs.weeklyGoalCents.first()
        if (spent < goal) {
            val remaining = goal - spent
            NotificationHelper.notify(
                context,
                NotificationHelper.NOTIFICATION_ID_WEEKLY,
                "Weekly spend goal not reached yet",
                "You've spent ${CurrencyUtils.formatRands(spent)} of ${CurrencyUtils.formatRands(goal)} this week — " +
                    "${CurrencyUtils.formatRands(remaining)} to go before Friday."
            )
        }
    }

    private suspend fun checkMonthlyGoal(
        context: Context,
        today: LocalDate,
        dao: suspend (LocalDate, LocalDate) -> Long,
        prefs: GoalsPreferences
    ) {
        val spent = dao(DateUtils.startOfMonth(today), DateUtils.endOfMonth(today))
        val goal = prefs.monthlyGoalCents.first()
        if (spent < goal) {
            val remaining = goal - spent
            NotificationHelper.notify(
                context,
                NotificationHelper.NOTIFICATION_ID_MONTHLY,
                "Monthly spend goal not reached yet",
                "You've spent ${CurrencyUtils.formatRands(spent)} of ${CurrencyUtils.formatRands(goal)} this month — " +
                    "${CurrencyUtils.formatRands(remaining)} left today."
            )
        }
    }
}
