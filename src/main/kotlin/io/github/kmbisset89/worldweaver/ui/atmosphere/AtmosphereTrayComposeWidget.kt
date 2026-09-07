package io.github.kmbisset89.worldweaver.ui.atmosphere

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun AtmosphereTrayComposeWidget(
    state: AtmosphereViewState.Content,
    onInteraction: (AtmosphereInteraction) -> Unit,
    contentPadding: Dp = 16.dp,
    showLook: Boolean = false,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AtmosphereMusicComposeWidget(
            tracks = state.musicTracks,
            playingTrackId = state.playingTrackId,
            volume = state.musicVolume,
            loopEnabled = state.musicLoopEnabled,
            musicError = state.musicError,
            showLibraryActions = false,
            onInteraction = onInteraction,
        )
        when {
            !state.isConfigured -> Text(
                text = "Connect Home Assistant, Philips Hue, or Govee lighting on the Atmosphere screen.",
                fontSize = 13.sp,
                color = TextSecondary,
            )
            state.scenes.isNotEmpty() -> FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.scenes.forEach { scene ->
                    val selected = state.lastActivatedSceneId == scene.id
                    if (selected) {
                        Button(
                            onClick = { onInteraction(AtmosphereInteraction.SceneActivateSelected(scene.id)) },
                            enabled = !state.isActivating,
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        ) {
                            Text(scene.name)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onInteraction(AtmosphereInteraction.SceneActivateSelected(scene.id)) },
                            enabled = !state.isActivating,
                        ) {
                            Text(scene.name)
                        }
                    }
                }
            }
            !showLook -> Text(
                text = "Add named scenes on the Atmosphere screen, then trigger them here.",
                fontSize = 13.sp,
                color = TextSecondary,
            )
            !state.hasLookLights -> Text(
                text = "Load Hue or Govee lights on the Atmosphere screen to control color here.",
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }
        state.activationError?.let { message ->
            Text(text = message, fontSize = 13.sp, color = TextPrimary)
        }
        if (state.isActivating) {
            Text(text = "Activating…", fontSize = 13.sp, color = TextSecondary)
        }
        if (!showLook && state.hasSelectedLookLights) {
            AtmosphereLightingEffectsComposeWidget(
                playingEffect = state.playingEffect,
                hasSelectedLights = true,
                onInteraction = onInteraction,
            )
            AtmosphereLightingLoopsComposeWidget(
                playingLoop = state.playingLoop,
                hasSelectedLights = true,
                onInteraction = onInteraction,
            )
        }
        if (showLook && state.hasLookLights) {
            if (state.hueLights.isNotEmpty()) {
                Text(text = "Hue lights", fontSize = 13.sp, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.hueLights.forEach { light ->
                        FilterChip(
                            selected = light.id in state.selectedHueLightIds,
                            onClick = { onInteraction(AtmosphereInteraction.HueLightToggled(light.id)) },
                            label = { Text(light.name) },
                        )
                    }
                }
            }
            if (state.goveeDevices.isNotEmpty()) {
                Text(text = "Govee lights", fontSize = 13.sp, color = TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.goveeDevices.forEach { device ->
                        FilterChip(
                            selected = device.deviceId in state.selectedGoveeDeviceIds,
                            onClick = { onInteraction(AtmosphereInteraction.GoveeDeviceToggled(device.deviceId)) },
                            label = { Text(device.displayName) },
                        )
                    }
                }
            }
            AtmosphereLookComposeWidget(
                colorHex = state.draftLookColorHex,
                brightness = state.draftLookBrightness,
                powerOn = state.draftLookPowerOn,
                transitionMs = state.draftLookTransitionMs,
                playingEffect = state.playingEffect,
                playingLoop = state.playingLoop,
                moods = state.moods,
                draftMoodName = state.draftMoodName,
                lookError = state.lookError,
                moodError = state.moodError,
                hasSelectedLights = state.hasSelectedLookLights,
                onInteraction = onInteraction,
                compact = true,
            )
        }
    }
}
