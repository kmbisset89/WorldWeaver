package io.github.kmbisset89.worldweaver.ui.atmosphere

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.AtmosphereMusicTrack
import io.github.kmbisset89.worldweaver.ui.components.ActionIconButtonComposeWidget
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun AtmosphereMusicComposeWidget(
    tracks: List<AtmosphereMusicTrack>,
    playingTrackId: String?,
    volume: Int,
    loopEnabled: Boolean,
    musicError: String?,
    showLibraryActions: Boolean,
    onInteraction: (AtmosphereInteraction) -> Unit,
    onAddFiles: (() -> Unit)? = null,
) {
    val playing = tracks.firstOrNull { it.id == playingTrackId }
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = when {
                playing == null -> "No music playing"
                !playing.fileIsPresent() -> "Missing ${playing.displayName}"
                else -> "Playing ${playing.displayName}"
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
        )
        musicError?.let { message ->
            Text(text = message, fontSize = 13.sp, color = TextPrimary)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                onClick = { onInteraction(AtmosphereInteraction.MusicStopSelected) },
                enabled = playingTrackId != null,
            ) {
                Text("Stop")
            }
            FilterChip(
                selected = loopEnabled,
                onClick = { onInteraction(AtmosphereInteraction.MusicLoopToggled) },
                label = { Text("Loop") },
            )
            if (showLibraryActions && onAddFiles != null) {
                Button(
                    onClick = onAddFiles,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                ) {
                    Text("Add files")
                }
            }
        }
        Text(
            text = "Volume $volume",
            fontSize = 13.sp,
            color = TextSecondary,
        )
        Slider(
            value = volume.toFloat(),
            onValueChange = { next ->
                onInteraction(AtmosphereInteraction.MusicVolumeChanged(next.toInt().coerceIn(0, 100)))
            },
            valueRange = 0f..100f,
        )
        if (tracks.isEmpty()) {
            Text(
                text = if (showLibraryActions) {
                    "Link music files on this computer for table ambience."
                } else {
                    "Link music files on the Atmosphere screen."
                },
                fontSize = 13.sp,
                color = TextSecondary,
            )
        } else if (showLibraryActions) {
            tracks.forEach { track ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.displayName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        )
                        Text(
                            text = if (track.fileIsPresent()) track.path else "Missing file",
                            fontSize = 12.sp,
                            color = TextSecondary,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedButton(
                            onClick = {
                                onInteraction(AtmosphereInteraction.MusicTrackPlaySelected(track.id))
                            },
                        ) {
                            Text(if (track.id == playingTrackId) "Restart" else "Play")
                        }
                        ActionIconButtonComposeWidget(
                            icon = Icons.Default.Delete,
                            tooltip = "Remove",
                            tint = ErrorRed,
                            onClick = {
                                onInteraction(AtmosphereInteraction.MusicTrackDeleteSelected(track.id))
                            },
                        )
                    }
                }
            }
        } else {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tracks.forEach { track ->
                    val selected = track.id == playingTrackId
                    if (selected) {
                        Button(
                            onClick = {
                                onInteraction(AtmosphereInteraction.MusicTrackPlaySelected(track.id))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                        ) {
                            Text(track.displayName)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                onInteraction(AtmosphereInteraction.MusicTrackPlaySelected(track.id))
                            },
                        ) {
                            Text(track.displayName)
                        }
                    }
                }
            }
        }
        if (showLibraryActions) {
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
