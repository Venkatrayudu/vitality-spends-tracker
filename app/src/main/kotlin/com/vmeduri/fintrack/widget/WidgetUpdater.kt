package com.vmeduri.fintrack.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

/** Call after any write to the transactions table or the goals, so the widget stays live. */
object WidgetUpdater {
    suspend fun update(context: Context) {
        BudgetWidget().updateAll(context)
    }
}
