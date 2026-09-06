package io.github.kmbisset89.worldweaver.ui.run

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary

@Composable
internal fun RunTimerComposeWidget(
    minutesText: String,
    timerLabel: String,
    running: Boolean,
    onInteraction: (RunInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    RunCardComposeWidget(title = "Timer", modifier = modifier) {
        Text(
            text = timerLabel,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 15).forEach { minutes ->
                FilterChip(
                    selected = minutesText == minutes.toString() && !running,
                    onClick = { onInteraction(RunInteraction.TimerPresetSelected(minutes)) },
                    label = { Text("${minutes}m") },
                )
            }
        }
        OutlinedTextField(
            value = minutesText,
            onValueChange = { onInteraction(RunInteraction.TimerMinutesChanged(it)) },
            label = { Text("Minutes") },
            singleLine = true,
            enabled = !running,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (running) {
                OutlinedButton(onClick = { onInteraction(RunInteraction.TimerPauseSelected) }) {
                    Text("Pause")
                }
            } else {
                Button(
                    onClick = { onInteraction(RunInteraction.TimerStartSelected) },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                ) {
                    Text("Start")
                }
            }
            OutlinedButton(onClick = { onInteraction(RunInteraction.TimerResetSelected) }) {
                Text("Reset")
            }
        }
    }
}
