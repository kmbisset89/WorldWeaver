package io.github.kmbisset89.worldweaver.ui.run

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.advancement.AdvancementPromptComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun RunScreen(
    viewState: RunViewState,
    onInteraction: (RunInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(RunInteraction.ScreenStarted)
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (val state = viewState) {
            RunViewState.Loading -> {
                Text(
                    text = "Tonight",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is RunViewState.Error -> {
                Text(
                    text = "Tonight",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                FeatureErrorState(
                    message = state.message,
                    canRetry = state.canRetry,
                    onRetry = { onInteraction(RunInteraction.RetrySelected) },
                )
            }
            RunViewState.NoActiveWorld -> FeatureEmptyState(
                icon = Icons.Default.PublicOff,
                title = "No active world",
                message = "Choose a world before running tonight.",
                actionLabel = "Open worlds",
                onAction = { onInteraction(RunInteraction.CreateWorldSelected) },
            )
            RunViewState.NoActiveCampaign -> FeatureEmptyState(
                icon = Icons.Default.Flag,
                title = "No active campaign",
                message = "Set a campaign active to open tonight's run.",
                actionLabel = "Open campaigns",
                onAction = { onInteraction(RunInteraction.CreateCampaignSelected) },
            )
            is RunViewState.NoActiveSession -> FeatureEmptyState(
                icon = Icons.Default.Event,
                title = "No active session",
                message = "Create or select a session in ${state.campaignName} to run tonight.",
                actionLabel = "Open sessions",
                onAction = { onInteraction(RunInteraction.OpenSessionsSelected) },
            )
            is RunViewState.Content -> RunContent(state = state, onInteraction = onInteraction)
        }
        val prompt = (viewState as? RunViewState.Content)?.advancementPrompt
        if (prompt != null) {
            AdvancementPromptComposeWidget(
                prompt = prompt,
                onDismiss = { onInteraction(RunInteraction.AdvancementDismissed) },
                onAwardLevel = { onInteraction(RunInteraction.AwardLevelConfirmed) },
                onAmountChanged = { onInteraction(RunInteraction.AwardExperienceAmountChanged(it)) },
                onAwardExperience = { onInteraction(RunInteraction.AwardExperienceConfirmed) },
            )
        }
    }
}

@Composable
private fun RunContent(
    state: RunViewState.Content,
    onInteraction: (RunInteraction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = state.sessionName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = "${state.campaignName} · ${state.worldName}",
                fontSize = 14.sp,
                color = TextSecondary,
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                state.inWorldDateLabel?.let { label ->
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = { Text("Session $label") },
                    )
                }
                state.calendarTodayLabel?.let { label ->
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text("Today $label") },
                    )
                }
                state.observanceNames.forEach { name ->
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text(name) },
                    )
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onInteraction(RunInteraction.DiceTraySelected) }) {
                    Text("Dice tray")
                }
                OutlinedButton(onClick = { onInteraction(RunInteraction.OpenEncountersSelected) }) {
                    Text("Encounters")
                }
                OutlinedButton(onClick = { onInteraction(RunInteraction.OpenMapsSelected) }) {
                    Text("Maps")
                }
                if (state.activeEncounter?.hasMap == true) {
                    Button(
                        onClick = { onInteraction(RunInteraction.PlayerViewSelected) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    ) {
                        Text("Player view")
                    }
                }
            }
        }
        item {
            RunCard(title = "Party") {
                if (state.party.isEmpty()) {
                    Text("No player characters in this campaign.", fontSize = 13.sp, color = TextSecondary)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.party.forEach { member ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onInteraction(
                                            RunInteraction.PersonPeeked(
                                                membership = member.membership,
                                                personId = member.personId,
                                            )
                                        )
                                    },
                            ) {
                                Text(
                                    text = member.name,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                )
                                Text(
                                    text = "HP ${member.hitPoints}/${member.maxHitPoints}  ·  AC ${member.armorClass}",
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                )
                                if (member.concentratingSpell.isNotBlank()) {
                                    Text(
                                        text = "Concentrating: ${member.concentratingSpell}",
                                        fontSize = 13.sp,
                                        color = TextPrimary,
                                    )
                                }
                                if (member.spellSlotsLabel.isNotBlank()) {
                                    Text(
                                        text = member.spellSlotsLabel,
                                        fontSize = 13.sp,
                                        color = TextSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        if (state.activeEncounter != null) {
            item {
                val encounter = state.activeEncounter
                RunCard(title = "Combat") {
                    Text(
                        text = "${encounter.name} · ${encounter.status}",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )
                    encounter.roundLabel?.let { label ->
                        Text(text = label, fontSize = 13.sp, color = TextSecondary)
                    }
                    TextButtonLike("Open tracker") {
                        onInteraction(RunInteraction.OpenEncountersSelected)
                    }
                }
            }
        }
        item {
            RunCard(title = "Objectives") {
                if (state.questObjectives.isEmpty()) {
                    Text("No active quest objectives.", fontSize = 13.sp, color = TextSecondary)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        state.questObjectives.forEach { line ->
                            Text(
                                text = "${line.questTitle}: ${line.objectiveTitle} (${line.status})",
                                fontSize = 13.sp,
                                color = TextPrimary,
                            )
                        }
                    }
                }
            }
        }
        if (state.partyLocations.isNotEmpty()) {
            item {
                RunCard(title = "Party location") {
                    Text(state.partyLocations.joinToString(", "), fontSize = 13.sp, color = TextPrimary)
                }
            }
        }
        if (state.scenes.isNotEmpty()) {
            item {
                RunCard(title = "Scenes") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.scenes.forEach { scene ->
                            Text(text = scene.title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            if (scene.notes.isNotBlank()) {
                                Text(text = scene.notes, fontSize = 13.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            }
        }
        if (state.sessionNotes.isNotBlank()) {
            item {
                RunCard(title = "Session notes") {
                    Text(text = state.sessionNotes, fontSize = 13.sp, color = TextPrimary)
                }
            }
        }
        if (state.recap.isNotBlank()) {
            item {
                RunCard(title = "What changed") {
                    Text(text = state.recap, fontSize = 13.sp, color = TextPrimary)
                }
            }
        }
        item {
            RunCard(title = "Close session") {
                OutlinedTextField(
                    value = state.whyItMatters,
                    onValueChange = { onInteraction(RunInteraction.WhyItMattersChanged(it)) },
                    label = { Text("Why it matters next week") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                state.closeError?.let { error ->
                    Text(text = error, fontSize = 13.sp, color = TextSecondary)
                }
                Button(
                    onClick = { onInteraction(RunInteraction.CloseSessionSelected) },
                    enabled = !state.isClosing,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(if (state.isClosing) "Closing…" else "Close session")
                }
            }
        }
    }
}

@Composable
private fun RunCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
            )
            content()
        }
    }
}

@Composable
private fun TextButtonLike(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        fontSize = 13.sp,
        color = NavyBlue,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clickable(onClick = onClick).padding(top = 4.dp),
    )
}
