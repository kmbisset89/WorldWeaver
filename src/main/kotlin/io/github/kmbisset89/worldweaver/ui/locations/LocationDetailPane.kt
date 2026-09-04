package io.github.kmbisset89.worldweaver.ui.locations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.Location
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.voice.VoiceClipComposeWidget
import io.github.kmbisset89.worldweaver.ui.voice.chooseWavPath
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun LocationDetailPane(
    location: Location,
    breadcrumbs: List<Location>,
    overlay: LocationsViewState.OverlayState?,
    campaignName: String?,
    attachedLore: List<LocationsViewState.AttachedLore>,
    attachedQuests: List<LocationsViewState.AttachedQuest>,
    voiceClipPath: String?,
    isRecordingVoice: Boolean,
    isPlayingVoice: Boolean,
    selectedLocationHasMap: Boolean,
    onInteraction: (LocationsInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (breadcrumbs.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                breadcrumbs.forEachIndexed { index, crumb ->
                    if (index > 0) {
                        Text(">", color = TextSecondary, fontSize = 13.sp)
                    }
                    Text(
                        text = crumb.name,
                        fontSize = 13.sp,
                        color = if (crumb.id == location.id) TextPrimary else NavyBlue,
                        modifier = Modifier.clickable {
                            onInteraction(LocationsInteraction.BreadcrumbSelected(crumb.id))
                        }
                    )
                }
            }
        }
        Text(
            text = location.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = location.type.displayName,
            fontSize = 13.sp,
            color = TextSecondary
        )
        if (location.description.isNotBlank()) {
            Text(text = location.description, fontSize = 14.sp, color = TextPrimary)
        }
        MetadataSection("Climate", location.climate)
        MetadataSection("Terrain", location.terrain)
        MetadataSection("Government", location.government)
        MetadataSection(
            title = "Landmarks",
            value = if (location.landmarks.isEmpty()) {
                ""
            } else {
                location.landmarks.joinToString("\n")
            },
        )
        MetadataSection("History", location.history)
        MetadataSection("World notes", location.notes)
        VoiceClipComposeWidget(
            hasClip = voiceClipPath != null,
            isRecording = isRecordingVoice,
            isPlaying = isPlayingVoice,
            onAttachSelected = {
                chooseWavPath("Choose voice clip")?.let { path ->
                    onInteraction(LocationsInteraction.VoiceClipAttached(path))
                }
            },
            onRecordToggled = {
                onInteraction(LocationsInteraction.VoiceClipRecordToggled)
            },
            onPlayToggled = {
                onInteraction(LocationsInteraction.VoiceClipPlayToggled)
            },
            onRemoveSelected = {
                onInteraction(LocationsInteraction.VoiceClipRemoved)
            },
        )
        AttachedLoreSection(attachedLore = attachedLore, onInteraction = onInteraction)
        AttachedQuestSection(attachedQuests = attachedQuests, onInteraction = onInteraction)

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(
                onClick = { onInteraction(LocationsInteraction.OpenLocationMapSelected(location.id)) }
            ) {
                Text(if (selectedLocationHasMap) "Open map" else "Add map")
            }
            TextButton(
                onClick = { onInteraction(LocationsInteraction.EditLocationSelected(location.id)) }
            ) {
                Text("Edit")
            }
            TextButton(
                onClick = { onInteraction(LocationsInteraction.DeleteLocationSelected(location.id)) }
            ) {
                Text("Delete")
            }
        }

        if (overlay != null) {
            OverlayCard(overlay = overlay, onInteraction = onInteraction)
        } else if (campaignName == null) {
            MetadataSection(
                title = "Campaign overlay",
                value = "Set an active campaign to mark party presence and campaign notes.",
            )
        }
    }
}

@Composable
private fun AttachedLoreSection(
    attachedLore: List<LocationsViewState.AttachedLore>,
    onInteraction: (LocationsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Attached lore",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (attachedLore.isEmpty()) {
                Text(
                    text = "No lore is attached to this location yet.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                attachedLore.forEach { entry ->
                    Text(
                        text = entry.title,
                        fontSize = 13.sp,
                        color = NavyBlue,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clickable {
                                onInteraction(LocationsInteraction.AttachedLoreSelected(entry.loreId))
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachedQuestSection(
    attachedQuests: List<LocationsViewState.AttachedQuest>,
    onInteraction: (LocationsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Attached quests",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (attachedQuests.isEmpty()) {
                Text(
                    text = "No quests are linked to this location yet.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                attachedQuests.forEach { quest ->
                    Text(
                        text = quest.title,
                        fontSize = 13.sp,
                        color = NavyBlue,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clickable {
                                onInteraction(LocationsInteraction.AttachedQuestSelected(quest.questId))
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun OverlayCard(
    overlay: LocationsViewState.OverlayState,
    onInteraction: (LocationsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Campaign overlay · ${overlay.campaignName}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "These notes stay on this campaign. They do not change the world location.",
                fontSize = 12.sp,
                color = TextSecondary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = overlay.hasPartyPresence,
                    onCheckedChange = { checked ->
                        onInteraction(LocationsInteraction.OverlayPartyPresenceChanged(checked))
                    }
                )
                Text("Party is here", color = TextPrimary, fontSize = 14.sp)
            }
            OutlinedTextField(
                value = overlay.notes,
                onValueChange = { onInteraction(LocationsInteraction.OverlayNotesChanged(it)) },
                label = { Text("Campaign notes") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { onInteraction(LocationsInteraction.OverlaySaved) }) {
                Text("Save campaign notes")
            }
        }
    }
}

@Composable
private fun MetadataSection(
    title: String,
    value: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = value.ifBlank { "None yet." },
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
