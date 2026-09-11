package com.vmeduri.fintrack

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.vmeduri.fintrack.reminders.NotificationHelper
import com.vmeduri.fintrack.reminders.ReminderScheduler
import com.vmeduri.fintrack.widget.WidgetRefreshWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FinTrackApp : Application() {

    /** A long-lived scope for app-wide background work that must outlive any one screen. */
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        Firebase.initialize(this)
        
        NotificationHelper.ensureChannel(this)
        WidgetRefreshWorker.schedule(this)
        applicationScope.launch {
            // Idempotent: safe to call on every app start, it just replaces the
            // pending check with a freshly-calculated one.
            ReminderScheduler.scheduleNext(this@FinTrackApp)
        }
    }
}

