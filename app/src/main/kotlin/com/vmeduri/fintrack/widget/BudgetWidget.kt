package com.vmeduri.fintrack.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.vmeduri.fintrack.MainActivity
import com.vmeduri.fintrack.data.AppDatabase
import com.vmeduri.fintrack.data.GoalsPreferences
import com.vmeduri.fintrack.util.CurrencyUtils
import com.vmeduri.fintrack.util.DateUtils
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * The home screen widget: this week's spend and this month's spend, each against its
 * goal, with how much is left. Tapping it opens the app. It's re-rendered (via
 * [WidgetUpdater]) right after any transaction is added, edited or deleted, and on a
 * light periodic schedule so the numbers stay correct across day/week/month rollovers.
 *
 * Kept deliberately free of the Glance Material3 theming helpers (GlanceTheme etc.) —
 * that's a separate, faster-moving library and not worth the extra version-pairing risk
 * for a two-line widget with its own fixed colour scheme.
 */
class BudgetWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = AppDatabase.getInstance(context).transactionDao()
        val prefs = GoalsPreferences(context)
        val today = LocalDate.now()

        val weekSpent = dao.totalBetween(DateUtils.startOfWeek(today), DateUtils.endOfWeek(today))
        val weekGoal = prefs.weeklyGoalCents.first()
        val monthSpent = dao.totalBetween(DateUtils.startOfMonth(today), DateUtils.endOfMonth(today))
        val monthGoal = prefs.monthlyGoalCents.first()

        val openApp: Action = actionStartActivity(Intent(context, MainActivity::class.java))

        provideContent {
            WidgetContent(weekSpent, weekGoal, monthSpent, monthGoal, openApp)
        }
    }
}

@Composable
private fun WidgetContent(weekSpent: Long, weekGoal: Long, monthSpent: Long, monthGoal: Long, openApp: Action) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF))
            .padding(12.dp)
            .clickable(openApp)
    ) {
        WidgetLine("This week", weekSpent, weekGoal)
        Spacer(modifier = GlanceModifier.height(10.dp))
        WidgetLine("This month", monthSpent, monthGoal)
    }
}

@Composable
private fun WidgetLine(label: String, spent: Long, goal: Long) {
    val reached = goal > 0 && spent >= goal

    Text(
        text = label,
        style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 13.sp)
    )
    Text(
        text = "${CurrencyUtils.formatRands(spent)} / ${CurrencyUtils.formatRands(goal)}",
        style = TextStyle(fontSize = 13.sp)
    )
    Text(
        text = if (reached) "Goal reached" else "${CurrencyUtils.formatRands(goal - spent)} remaining",
        style = TextStyle(fontSize = 12.sp)
    )
}
