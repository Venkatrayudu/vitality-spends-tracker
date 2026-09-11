package com.vmeduri.fintrack.widget

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * A light periodic nudge so the widget's numbers roll over correctly at day/week/month
 * boundaries even if no transaction is added right at midnight. 15 minutes is
 * WorkManager's minimum period for periodic work, and it's batched by the OS so the
 * battery cost is negligible.
 */
class WidgetRefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        WidgetUpdater.update(applicationContext)
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "widget_periodic_refresh"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
