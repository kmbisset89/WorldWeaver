package io.github.kmbisset89.worldweaver.ui.run

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.ui.advancement.AdvancementPromptComposeWidget
import io.github.kmbisset89.worldweaver.ui.atmosphere.AtmosphereInteraction
import io.github.kmbisset89.worldweaver.ui.atmosphere.AtmosphereViewState
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveListDetailBreakpoint
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun RunScreen(
    viewState: RunViewState,
    atmosphereState: AtmosphereViewState.Content?,
    cameraPreview: StateFlow<ImageBitmap?>,
    onInteraction: (RunInteraction) -> Unit,
    onAtmosphereInteraction: (AtmosphereInteraction) -> Unit,
) {
    DisposableEffect(Unit) {
        onInteraction(RunInteraction.ScreenStarted)
        onDispose {
            onInteraction(RunInteraction.ScreenStopped)
        }
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
            is RunViewState.Content -> RunContent(
                state = state,
                atmosphereState = atmosphereState,
                cameraPreview = cameraPreview,
                onInteraction = onInteraction,
                onAtmosphereInteraction = onAtmosphereInteraction,
            )
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
    atmosphereState: AtmosphereViewState.Content?,
    cameraPreview: StateFlow<ImageBitmap?>,
    onInteraction: (RunInteraction) -> Unit,
    onAtmosphereInteraction: (AtmosphereInteraction) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RunHeader(state = state, onInteraction = onInteraction)
        BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
            val wide = maxWidth >= AdaptiveListDetailBreakpoint
            if (wide) {
                Row(modifier = Modifier.fillMaxSize()) {
                    RunScrollColumn(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentPadding = 16.dp,
                    ) {
                        RunIdentityComposeWidget(state = state)
                        RunScenesComposeWidget(scenes = state.scenes)
                        RunClocksComposeWidget(
                            clocks = state.clocks,
                            clockLabel = state.clockLabel,
                            clockSegmentCount = state.clockSegmentCount,
                            clockError = state.clockError,
                            onInteraction = onInteraction,
                        )
                        RunTimerComposeWidget(
                            minutesText = state.timerMinutesText,
                            timerLabel = state.timerLabel,
                            running = state.timerRunning,
                            onInteraction = onInteraction,
                        )
                        RunRecordingComposeWidget(
                            hasMicrophone = state.hasMicrophone,
                            hasCamera = state.hasCamera,
                            microphones = state.microphones,
                            selectedMicrophoneId = state.selectedMicrophoneId,
                            recordingMode = state.recordingMode,
                            isRecording = state.isRecording,
                            elapsedLabel = state.recordingElapsedLabel,
                            recordings = state.recordings,
                            recordingError = state.recordingError,
                            cameraNeedsPermission = state.cameraNeedsPermission,
                            cameraPreview = cameraPreview,
                            onInteraction = onInteraction,
                        )
                    }
                    RunColumnDivider()
                    RunScrollColumn(
                        modifier = Modifier.weight(1.2f).fillMaxHeight(),
                        contentPadding = 16.dp,
                    ) {
                        RunLookupComposeWidget(
                            query = state.lookupQuery,
                            results = state.lookupResults,
                            peek = state.lookupPeek,
                            onInteraction = onInteraction,
                        )
                        RunNotesComposeWidget(
                            sessionNotes = state.sessionNotes,
                            scratchNotes = state.scratchNotes,
                            recap = state.recap,
                            onInteraction = onInteraction,
                        )
                    }
                    RunColumnDivider()
                    RunScrollColumn(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentPadding = 16.dp,
                    ) {
                        RunPartyComposeWidget(party = state.party, onInteraction = onInteraction)
                        RunAtmosphereHostComposeWidget(
                            atmosphereState = atmosphereState,
                            onAtmosphereInteraction = onAtmosphereInteraction,
                            onPopOut = { onInteraction(RunInteraction.AtmosphereTraySelected) },
                        )
                        RunObjectivesComposeWidget(objectives = state.questObjectives)
                        RunPartyLocationComposeWidget(locations = state.partyLocations)
                        RunCombatComposeWidget(
                            encounter = state.activeEncounter,
                            onInteraction = onInteraction,
                        )
                        RunCloseSessionComposeWidget(state = state, onInteraction = onInteraction)
                    }
                }
            } else {
                RunScrollColumn(modifier = Modifier.fillMaxSize()) {
                    RunIdentityComposeWidget(state = state)
                    RunScenesComposeWidget(scenes = state.scenes)
                    RunClocksComposeWidget(
                        clocks = state.clocks,
                        clockLabel = state.clockLabel,
                        clockSegmentCount = state.clockSegmentCount,
                        clockError = state.clockError,
                        onInteraction = onInteraction,
                    )
                    RunTimerComposeWidget(
                        minutesText = state.timerMinutesText,
                        timerLabel = state.timerLabel,
                        running = state.timerRunning,
                        onInteraction = onInteraction,
                    )
                    RunRecordingComposeWidget(
                        hasMicrophone = state.hasMicrophone,
                        hasCamera = state.hasCamera,
                        microphones = state.microphones,
                        selectedMicrophoneId = state.selectedMicrophoneId,
                        recordingMode = state.recordingMode,
                        isRecording = state.isRecording,
                        elapsedLabel = state.recordingElapsedLabel,
                        recordings = state.recordings,
                        recordingError = state.recordingError,
                        cameraNeedsPermission = state.cameraNeedsPermission,
                        cameraPreview = cameraPreview,
                        onInteraction = onInteraction,
                    )
                    RunLookupComposeWidget(
                        query = state.lookupQuery,
                        results = state.lookupResults,
                        peek = state.lookupPeek,
                        onInteraction = onInteraction,
                    )
                    RunNotesComposeWidget(
                        sessionNotes = state.sessionNotes,
                        scratchNotes = state.scratchNotes,
                        recap = state.recap,
                        onInteraction = onInteraction,
                    )
                    RunPartyComposeWidget(party = state.party, onInteraction = onInteraction)
                    RunAtmosphereHostComposeWidget(
                        atmosphereState = atmosphereState,
                        onAtmosphereInteraction = onAtmosphereInteraction,
                        onPopOut = { onInteraction(RunInteraction.AtmosphereTraySelected) },
                    )
                    RunObjectivesComposeWidget(objectives = state.questObjectives)
                    RunPartyLocationComposeWidget(locations = state.partyLocations)
                    RunCombatComposeWidget(
                        encounter = state.activeEncounter,
                        onInteraction = onInteraction,
                    )
                    RunCloseSessionComposeWidget(state = state, onInteraction = onInteraction)
                }
            }
        }
    }
}

@Composable
private fun RunScrollColumn(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 0.dp,
    content: @Composable () -> Unit,
) {
    val scrollState = rememberScrollState()
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = contentPadding, end = contentPadding + 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            content()
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
        )
    }
}

@Composable
private fun RunColumnDivider() {
    VerticalDivider(
        modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp, vertical = 8.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
    )
}

@Composable
private fun RunHeader(
    state: RunViewState.Content,
    onInteraction: (RunInteraction) -> Unit,
) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RunIdentityComposeWidget(state: RunViewState.Content) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
        state.inWorldDateLabel?.let { label ->
            Text(
                text = "Session $label",
                fontSize = 14.sp,
                color = TextPrimary,
            )
        }
        state.calendarTodayLabel?.let { label ->
            Text(
                text = "Today · $label",
                fontSize = 14.sp,
                color = TextSecondary,
            )
        }
        if (state.observanceNames.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.observanceNames.forEach { name ->
                    FilterChip(
                        selected = false,
                        onClick = {},
                        label = { Text(name) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RunScenesComposeWidget(scenes: List<RunViewState.SceneLine>) {
    if (scenes.isEmpty()) {
        return
    }
    RunCardComposeWidget(title = "Scenes") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            scenes.forEach { scene ->
                Text(text = scene.title, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                if (scene.notes.isNotBlank()) {
                    Text(text = scene.notes, fontSize = 13.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun RunPartyComposeWidget(
    party: List<RunViewState.PartyMember>,
    onInteraction: (RunInteraction) -> Unit,
) {
    RunCardComposeWidget(title = "Party") {
        if (party.isEmpty()) {
            Text("No player characters in this campaign.", fontSize = 13.sp, color = TextSecondary)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                party.forEach { member ->
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

@Composable
private fun RunObjectivesComposeWidget(objectives: List<RunViewState.QuestObjectiveLine>) {
    RunCardComposeWidget(title = "Objectives") {
        if (objectives.isEmpty()) {
            Text("No active quest objectives.", fontSize = 13.sp, color = TextSecondary)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                objectives.forEach { line ->
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

@Composable
private fun RunPartyLocationComposeWidget(locations: List<String>) {
    if (locations.isEmpty()) {
        return
    }
    RunCardComposeWidget(title = "Party location") {
        Text(locations.joinToString(", "), fontSize = 13.sp, color = TextPrimary)
    }
}

@Composable
private fun RunCombatComposeWidget(
    encounter: RunViewState.EncounterLine?,
    onInteraction: (RunInteraction) -> Unit,
) {
    if (encounter == null) {
        return
    }
    RunCardComposeWidget(title = "Combat") {
        Text(
            text = "${encounter.name} · ${encounter.status}",
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
        encounter.roundLabel?.let { label ->
            Text(text = label, fontSize = 13.sp, color = TextSecondary)
        }
        Text(
            text = "Open tracker",
            fontSize = 13.sp,
            color = NavyBlue,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable {
                onInteraction(RunInteraction.OpenEncountersSelected)
            }.padding(top = 4.dp),
        )
    }
}

@Composable
private fun RunCloseSessionComposeWidget(
    state: RunViewState.Content,
    onInteraction: (RunInteraction) -> Unit,
) {
    RunCardComposeWidget(title = "Close session") {
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
