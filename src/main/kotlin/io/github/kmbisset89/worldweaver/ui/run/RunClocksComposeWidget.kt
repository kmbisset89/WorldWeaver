package io.github.kmbisset89.worldweaver.ui.run

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.SessionClock
import io.github.kmbisset89.worldweaver.ui.components.ActionIconButtonComposeWidget
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun RunClocksComposeWidget(
    clocks: List<RunViewState.ClockLine>,
    clockLabel: String,
    clockSegmentCount: Int,
    clockError: String?,
    onInteraction: (RunInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    RunCardComposeWidget(title = "Clocks", modifier = modifier) {
        if (clocks.isEmpty()) {
            Text("Track story pressure with segmented clocks.", fontSize = 13.sp, color = TextSecondary)
        }
        clocks.forEach { clock ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(clock.label, color = TextPrimary, fontSize = 13.sp)
                    ActionIconButtonComposeWidget(
                        icon = Icons.Default.Delete,
                        tooltip = "Delete ${clock.label}",
                        tint = ErrorRed,
                        onClick = { onInteraction(RunInteraction.ClockDeleteSelected(clock.id)) },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..clock.segmentCount).forEach { index ->
                        val filled = index <= clock.filledCount
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .border(1.dp, NavyBlue, CircleShape)
                                .background(if (filled) NavyBlue else NavyBlue.copy(alpha = 0f))
                                .semantics {
                                    contentDescription = "${clock.label} segment $index of ${clock.segmentCount}"
                                }
                                .clickable {
                                    val next = if (index == clock.filledCount) {
                                        index - 1
                                    } else {
                                        index
                                    }
                                    onInteraction(RunInteraction.ClockFilledSelected(clock.id, next))
                                },
                        )
                    }
                }
            }
        }
        OutlinedTextField(
            value = clockLabel,
            onValueChange = { onInteraction(RunInteraction.ClockLabelChanged(it)) },
            label = { Text("New clock") },
            isError = clockError != null,
            supportingText = clockError?.let { error -> { Text(error) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(4, 6, 8).forEach { count ->
                FilterChip(
                    selected = clockSegmentCount == count,
                    onClick = { onInteraction(RunInteraction.ClockSegmentCountSelected(count)) },
                    label = { Text("$count") },
                )
            }
        }
        OutlinedButton(
            onClick = { onInteraction(RunInteraction.ClockCreateSelected) },
            enabled = clockSegmentCount in SessionClock.MIN_SEGMENT_COUNT..SessionClock.MAX_SEGMENT_COUNT,
        ) {
            Text("Add clock")
        }
    }
}
