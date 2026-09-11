package com.vmeduri.fintrack.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vmeduri.fintrack.R
import com.vmeduri.fintrack.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onLogout: () -> Unit = {}) {
    val viewModel: SettingsViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Local text-field state, seeded from the persisted goals and re-seeded only when the
    // underlying stored value actually changes (not on every keystroke).
    var weeklyText by remember { mutableStateOf(CurrencyUtils.centsToPlainString(uiState.weeklyGoalCents)) }
    var monthlyText by remember { mutableStateOf(CurrencyUtils.centsToPlainString(uiState.monthlyGoalCents)) }
    var foodText by remember {
        mutableStateOf(if (uiState.healthyFoodGoalCents == 0L) "" else CurrencyUtils.centsToPlainString(uiState.healthyFoodGoalCents))
    }
    var careText by remember {
        mutableStateOf(if (uiState.healthyCareGoalCents == 0L) "" else CurrencyUtils.centsToPlainString(uiState.healthyCareGoalCents))
    }
    var seeded by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.weeklyGoalCents, uiState.monthlyGoalCents, uiState.healthyFoodGoalCents, uiState.healthyCareGoalCents) {
        if (!seeded) {
            weeklyText = CurrencyUtils.centsToPlainString(uiState.weeklyGoalCents)
            monthlyText = CurrencyUtils.centsToPlainString(uiState.monthlyGoalCents)
            foodText = if (uiState.healthyFoodGoalCents == 0L) "" else CurrencyUtils.centsToPlainString(uiState.healthyFoodGoalCents)
            careText = if (uiState.healthyCareGoalCents == 0L) "" else CurrencyUtils.centsToPlainString(uiState.healthyCareGoalCents)
            seeded = true
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_settings)) }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Goals", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = weeklyText,
                onValueChange = { weeklyText = it; viewModel.setWeeklyGoal(it) },
                label = { Text("Weekly spend goal (R)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = monthlyText,
                onValueChange = { monthlyText = it; viewModel.setMonthlyGoal(it) },
                label = { Text("Monthly spend goal (R)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = foodText,
                onValueChange = { foodText = it; viewModel.setHealthyFoodGoal(it) },
                label = { Text("Healthy Food monthly goal (R, optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = careText,
                onValueChange = { careText = it; viewModel.setHealthyCareGoal(it) },
                label = { Text("Healthy Care monthly goal (R, optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            Text("Reminders", style = MaterialTheme.typography.titleMedium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remind me if a goal isn't met yet")
                Switch(checked = uiState.remindersEnabled, onCheckedChange = { viewModel.setRemindersEnabled(it) })
            }
            Text(
                "Fires Friday evening for the weekly goal, and on the last day of the month for the " +
                    "monthly goal — only if that goal hasn't been reached yet. Reminder time:",
                style = MaterialTheme.typography.bodySmall
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { viewModel.setReminderHour((uiState.reminderHour - 1).mod(24)) }) {
                    Text("-")
                }
                Text("${uiState.reminderHour.toString().padStart(2, '0')}:00", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = { viewModel.setReminderHour((uiState.reminderHour + 1).mod(24)) }) {
                    Text("+")
                }
            }

            HorizontalDivider()

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Out")
            }
        }
    }
}
