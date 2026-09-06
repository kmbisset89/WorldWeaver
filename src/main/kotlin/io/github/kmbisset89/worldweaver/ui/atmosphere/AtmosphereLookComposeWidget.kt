package io.github.kmbisset89.worldweaver.ui.atmosphere

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.AtmosphereLightingEffect
import io.github.kmbisset89.worldweaver.domain.AtmosphereLightingLoop
import io.github.kmbisset89.worldweaver.domain.AtmosphereMood
import io.github.kmbisset89.worldweaver.domain.GoveeLightingPreset
import io.github.kmbisset89.worldweaver.domain.LightingTransitionCalculator
import io.github.kmbisset89.worldweaver.ui.components.ActionIconButtonComposeWidget
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

/**
 * Color, brightness, and power used by selected Hue and Govee lights when a mapping has no Hue scene.
 */
@Composable
internal fun AtmosphereLookComposeWidget(
    colorHex: String,
    brightness: String,
    powerOn: Boolean,
    transitionMs: Int,
    playingEffect: AtmosphereLightingEffect?,
    playingLoop: AtmosphereLightingLoop?,
    moods: List<AtmosphereMood>,
    draftMoodName: String,
    lookError: String?,
    moodError: String?,
    hasSelectedLights: Boolean,
    onInteraction: (AtmosphereInteraction) -> Unit,
    compact: Boolean = false,
) {
    Text(
        text = if (compact) {
            "Applies to the selected Hue and Govee lights, using the transition below."
        } else {
            "Moods, the color picker, brightness, transition, and power apply to the Hue and Govee lights selected above. Skip this if you are using a Hue scene instead."
        },
        fontSize = 13.sp,
        color = TextSecondary,
    )
    if (!hasSelectedLights) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select Hue or Govee lights above to apply this look.",
            fontSize = 13.sp,
            color = TextSecondary,
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(text = "Moods", fontSize = 13.sp, color = TextSecondary)
    Spacer(modifier = Modifier.height(8.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        GoveeLightingPreset.entries.forEach { preset ->
            MoodChip(
                name = preset.displayName,
                selected = preset.matchesLook(colorHex, brightness, powerOn),
                onSelected = {
                    onInteraction(
                        AtmosphereInteraction.LookPresetSelected(
                            colorHex = preset.colorHex,
                            brightness = preset.brightness,
                            powerOn = preset.powerOn,
                        ),
                    )
                },
            )
        }
        moods.forEach { mood ->
            MoodChip(
                name = mood.name,
                selected = mood.matchesLook(colorHex, brightness, powerOn),
                onSelected = {
                    onInteraction(
                        AtmosphereInteraction.LookPresetSelected(
                            colorHex = mood.colorHex,
                            brightness = mood.brightness,
                            powerOn = mood.powerOn,
                        ),
                    )
                },
                onDelete = {
                    onInteraction(AtmosphereInteraction.LookMoodDeleteSelected(mood.id))
                },
            )
        }
    }
    if (!compact) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = draftMoodName,
                onValueChange = { onInteraction(AtmosphereInteraction.LookMoodNameChanged(it)) },
                modifier = Modifier.weight(1f),
                label = { Text("Mood name") },
                placeholder = { Text("Temple") },
                singleLine = true,
            )
            ActionIconButtonComposeWidget(
                icon = Icons.Default.Save,
                tooltip = "Save mood",
                filled = true,
                enabled = draftMoodName.isNotBlank(),
                onClick = { onInteraction(AtmosphereInteraction.LookMoodSaveSelected) },
            )
        }
        moodError?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, fontSize = 13.sp, color = TextPrimary)
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    AtmosphereColorPickerComposeWidget(
        colorHex = colorHex,
        onColorHexChanged = { onInteraction(AtmosphereInteraction.LookColorHexChanged(it)) },
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "Power", fontSize = 13.sp, color = TextPrimary)
        Switch(
            checked = powerOn,
            onCheckedChange = { onInteraction(AtmosphereInteraction.LookPowerChanged(it)) },
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    val brightnessValue = brightness.toIntOrNull()?.coerceIn(1, 100) ?: 80
    Text(
        text = "Brightness $brightnessValue",
        fontSize = 13.sp,
        color = TextSecondary,
    )
    Slider(
        value = brightnessValue.toFloat(),
        onValueChange = { next ->
            onInteraction(
                AtmosphereInteraction.LookBrightnessChanged(next.toInt().coerceIn(1, 100).toString()),
            )
        },
        valueRange = 1f..100f,
    )
    if (!compact) {
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = colorHex,
            onValueChange = { onInteraction(AtmosphereInteraction.LookColorHexChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Color") },
            placeholder = { Text("#E39B5A") },
            singleLine = true,
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Transition ${transitionLabel(transitionMs)}",
        fontSize = 13.sp,
        color = TextSecondary,
    )
    Slider(
        value = transitionMs.toFloat(),
        onValueChange = { next ->
            onInteraction(
                AtmosphereInteraction.LookTransitionChanged(
                    next.toInt().coerceIn(0, LightingTransitionCalculator.MAX_DURATION_MS),
                ),
            )
        },
        valueRange = 0f..LightingTransitionCalculator.MAX_DURATION_MS.toFloat(),
    )
    Spacer(modifier = Modifier.height(4.dp))
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TRANSITION_PRESETS.forEach { (name, duration) ->
            FilterChip(
                selected = transitionMs == duration,
                onClick = { onInteraction(AtmosphereInteraction.LookTransitionChanged(duration)) },
                label = { Text(name) },
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    AtmosphereLightingEffectsComposeWidget(
        playingEffect = playingEffect,
        hasSelectedLights = hasSelectedLights,
        onInteraction = onInteraction,
    )
    Spacer(modifier = Modifier.height(12.dp))
    AtmosphereLightingLoopsComposeWidget(
        playingLoop = playingLoop,
        hasSelectedLights = hasSelectedLights,
        onInteraction = onInteraction,
    )
    lookError?.let { error ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = error, fontSize = 13.sp, color = TextPrimary)
    }
}

@Composable
private fun MoodChip(
    name: String,
    selected: Boolean,
    onSelected: () -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        FilterChip(
            selected = selected,
            onClick = onSelected,
            label = { Text(name) },
        )
        if (onDelete != null) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete $name",
                tint = ErrorRed,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onDelete),
            )
        }
    }
}

private fun transitionLabel(durationMs: Int): String {
    return when (durationMs) {
        0 -> "instant"
        else -> {
            val tenths = (durationMs + 50) / 100
            val whole = tenths / 10
            val fraction = tenths % 10
            if (fraction == 0) "${whole}s" else "$whole.${fraction}s"
        }
    }
}

private val TRANSITION_PRESETS = listOf(
    "Instant" to 0,
    "Soft" to LightingTransitionCalculator.DEFAULT_DURATION_MS,
    "Slow" to 1_200,
    "Dramatic" to 2_500,
)

