package io.github.kmbisset89.worldweaver.ui.atmosphere

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.AtmosphereLightingEffect
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun AtmosphereLightingEffectsComposeWidget(
    playingEffect: AtmosphereLightingEffect?,
    hasSelectedLights: Boolean,
    onInteraction: (AtmosphereInteraction) -> Unit,
) {
    Text(text = "One-shot effects", fontSize = 13.sp, color = TextSecondary)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Flashes over the current look, then restores it.",
        fontSize = 13.sp,
        color = TextSecondary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AtmosphereLightingEffect.entries.forEach { effect ->
            OutlinedButton(
                onClick = { onInteraction(AtmosphereInteraction.LightingEffectSelected(effect)) },
                enabled = hasSelectedLights,
            ) {
                Text(if (playingEffect == effect) "Playing…" else effect.displayName)
            }
        }
    }
    if (playingEffect != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Playing ${playingEffect.displayName.lowercase()}…",
            fontSize = 13.sp,
            color = TextPrimary,
        )
    }
}
