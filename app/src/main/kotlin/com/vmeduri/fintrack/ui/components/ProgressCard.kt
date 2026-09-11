package com.vmeduri.fintrack.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.vmeduri.fintrack.util.CurrencyUtils

/**
 * A goal card used on the dashboard. When [goalCents] is 0 it's treated as "no goal set"
 * (used for the Healthy Food / Healthy Care totals, which are optional) and just shows
 * the running total with no progress bar.
 */
@Composable
fun ProgressCard(
    title: String,
    spentCents: Long,
    goalCents: Long,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val goalIsSet = goalCents > 0
    val progress = if (goalIsSet) (spentCents.toFloat() / goalCents.toFloat()).coerceIn(0f, 1f) else 0f
    val reached = goalIsSet && spentCents >= goalCents

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (goalIsSet) {
                    "${CurrencyUtils.formatRands(spentCents)} of ${CurrencyUtils.formatRands(goalCents)}"
                } else {
                    CurrencyUtils.formatRands(spentCents)
                },
                style = MaterialTheme.typography.headlineSmall
            )

            if (goalIsSet) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                @Suppress("DEPRECATION")
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (reached) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(4.dp))
                val remaining = goalCents - spentCents
                Text(
                    text = if (reached) "Goal reached" else "${CurrencyUtils.formatRands(remaining)} remaining",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (reached) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
