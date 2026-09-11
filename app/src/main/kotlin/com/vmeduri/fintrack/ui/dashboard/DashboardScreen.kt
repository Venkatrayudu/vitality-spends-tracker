package com.vmeduri.fintrack.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vmeduri.fintrack.R
import com.vmeduri.fintrack.ui.components.ProgressCard

@Composable
fun DashboardScreen(onAddClick: () -> Unit) {
    val viewModel: DashboardViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_transaction))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProgressCard(
                title = "This week",
                spentCents = uiState.weekSpentCents,
                goalCents = uiState.weekGoalCents,
                subtitle = "Monday to Sunday"
            )
            ProgressCard(
                title = "This month",
                spentCents = uiState.monthSpentCents,
                goalCents = uiState.monthGoalCents,
                subtitle = "Calendar month"
            )
            ProgressCard(
                title = "Healthy Food (this month)",
                spentCents = uiState.healthyFoodSpentCents,
                goalCents = uiState.healthyFoodGoalCents
            )
            ProgressCard(
                title = "Healthy Care (this month)",
                spentCents = uiState.healthyCareSpentCents,
                goalCents = uiState.healthyCareGoalCents
            )
        }
    }
}
