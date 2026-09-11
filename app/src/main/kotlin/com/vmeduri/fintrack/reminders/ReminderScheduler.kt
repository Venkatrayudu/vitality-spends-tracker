package com.vmeduri.fintrack.reminders

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.vmeduri.fintrack.data.GoalsPreferences
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Schedules a single [ReminderWorker] run for the next occurrence of the user's chosen
 * reminder hour (default 18:00 / 6pm). The worker checks whether today is a day that
 * matters (Friday, or the last day of the month) and, when it runs, re-calls this
 * function to schedule the next day's check — so one call at app startup keeps the
 * chain going indefinitely without needing an exact daily alarm.
 */
object ReminderScheduler {
    private const val WORK_NAME = "daily_goal_check"

    suspend fun scheduleNext(context: Context) {
        val prefs = GoalsPreferences(context)
        val hour = prefs.reminderHour.first()
        val delayMillis = millisUntilNextHour(hour)

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

    private fun millisUntilNextHour(hour: Int): Long {
        val now = LocalDateTime.now()
        var target = now.withHour(hour.coerceIn(0, 23)).withMinute(0).withSecond(0).withNano(0)
        if (!target.isAfter(now)) {
            target = target.plusDays(1)
        }
        return Duration.between(now, target).toMillis()
    }
}
