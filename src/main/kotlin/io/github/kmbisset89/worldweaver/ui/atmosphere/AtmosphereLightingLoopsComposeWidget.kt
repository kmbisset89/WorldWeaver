package io.github.kmbisset89.worldweaver.ui.atmosphere

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.AtmosphereLightingLoop
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun AtmosphereLightingLoopsComposeWidget(
    playingLoop: AtmosphereLightingLoop?,
    hasSelectedLights: Boolean,
    onInteraction: (AtmosphereInteraction) -> Unit,
) {
    Text(text = "Live effects", fontSize = 13.sp, color = TextSecondary)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Oscillates between colors until you tap it again, fire a one-shot, or change the look.",
        fontSize = 13.sp,
        color = TextSecondary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AtmosphereLightingLoop.entries.forEach { loop ->
            val selected = playingLoop == loop
            if (selected) {
                Button(
                    onClick = { onInteraction(AtmosphereInteraction.LightingLoopSelected(loop)) },
                    enabled = hasSelectedLights,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                ) {
                    Text("Stop ${loop.displayName}")
                }
            } else {
                OutlinedButton(
                    onClick = { onInteraction(AtmosphereInteraction.LightingLoopSelected(loop)) },
                    enabled = hasSelectedLights,
                ) {
                    Text(loop.displayName)
                }
            }
        }
    }
    if (playingLoop != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Looping ${playingLoop.displayName.lowercase()}…",
            fontSize = 13.sp,
            color = TextPrimary,
        )
    }
}
