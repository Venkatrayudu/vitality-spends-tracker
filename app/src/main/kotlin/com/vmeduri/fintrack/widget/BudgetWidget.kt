package com.vmeduri.fintrack.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
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
import androidx.glance.material3.GlanceTheme
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

        provideContent {
            GlanceTheme {
                WidgetContent(weekSpent, weekGoal, monthSpent, monthGoal)
            }
        }
    }
}

@Composable
private fun WidgetContent(weekSpent: Long, weekGoal: Long, monthSpent: Long, monthGoal: Long) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        WidgetLine("This week", weekSpent, weekGoal)
        Spacer(modifier = GlanceModifier.height(10.dp))
        WidgetLine("This month", monthSpent, monthGoal)
    }
}

@Composable
private fun WidgetLine(label: String, spent: Long, goal: Long) {
    val reached = goal > 0 && spent >= goal
    val statusColor = if (reached) GlanceTheme.colors.primary else GlanceTheme.colors.onBackground

    Text(
        text = label,
        style = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = GlanceTheme.colors.onBackground
        )
    )
    Text(
        text = "${CurrencyUtils.formatRands(spent)} / ${CurrencyUtils.formatRands(goal)}",
        style = TextStyle(fontSize = 13.sp, color = GlanceTheme.colors.onBackground)
    )
    Text(
        text = if (reached) "Goal reached" else "${CurrencyUtils.formatRands(goal - spent)} remaining",
        style = TextStyle(fontSize = 12.sp, color = statusColor)
    )
}
