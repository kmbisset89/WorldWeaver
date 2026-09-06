package io.github.kmbisset89.worldweaver.ui.encounters

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.Encounter
import io.github.kmbisset89.worldweaver.domain.EncounterStatus
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveListDetailBreakpoint
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveListDetailComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveScreenHeaderComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.ConfirmDestructiveDialog
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.components.rememberAdaptiveListOpen
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary
import ovh.plrapps.mapcompose.ui.state.MapState

@Composable
internal fun EncountersScreen(
    viewState: EncountersViewState,
    mapState: MapState?,
    onInteraction: (EncountersInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(EncountersInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (viewState) {
            EncountersViewState.Loading -> {
                EncountersHeader(
                    subtitle = "Campaign encounters",
                    showCreate = false,
                    onInteraction = onInteraction,
                )
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is EncountersViewState.Error -> {
                EncountersHeader(
                    subtitle = "Campaign encounters",
                    showCreate = false,
                    onInteraction = onInteraction,
                )
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(EncountersInteraction.RetrySelected) },
                )
            }
            EncountersViewState.NoActiveWorld -> {
                EncountersHeader(
                    subtitle = "Select a world first",
                    showCreate = false,
                    onInteraction = onInteraction,
                )
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so encounters have a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(EncountersInteraction.CreateWorldSelected) },
                )
            }
            EncountersViewState.NoActiveCampaign -> {
                EncountersHeader(
                    subtitle = "Select a campaign first",
                    showCreate = false,
                    onInteraction = onInteraction,
                )
                FeatureEmptyState(
                    icon = Icons.Default.Flag,
                    title = "No active campaign",
                    message = "Create or select a campaign to run encounters.",
                    actionLabel = "Go to Campaigns",
                    onAction = { onInteraction(EncountersInteraction.CreateCampaignSelected) },
                )
            }
            is EncountersViewState.Empty -> {
                EncountersHeader(
                    subtitle = "${viewState.campaignName} · ${viewState.worldName}",
                    showCreate = true,
                    onInteraction = onInteraction,
                )
                if (viewState.setup != null) {
                    EncounterSetupPane(
                        setup = viewState.setup,
                        encounterStatus = null,
                        startWarning = null,
                        onInteraction = onInteraction,
                    )
                } else {
                    FeatureEmptyState(
                        icon = Icons.Default.Security,
                        title = "No encounters yet",
                        message = "Create the first encounter for this campaign.",
                        actionLabel = "New encounter",
                        onAction = { onInteraction(EncountersInteraction.NewEncounterSelected) },
                    )
                }
            }
            is EncountersViewState.Content -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val compact = maxWidth < AdaptiveListDetailBreakpoint
                    var listOpen by rememberAdaptiveListOpen(compact)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        EncountersHeader(
                            subtitle = "${viewState.campaignName} · ${viewState.worldName}",
                            showCreate = true,
                            compact = compact,
                            selectedName = viewState.selectedEncounter?.name,
                            onListToggle = { listOpen = !listOpen },
                            onInteraction = onInteraction,
                        )
                        EncountersLibrary(
                            state = viewState,
                            compact = compact,
                            listOpen = listOpen,
                            onListDismissed = { listOpen = false },
                            onInteraction = onInteraction,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        )
                    }
                }
            }
            is EncountersViewState.Running -> {
                EncounterCombatConsole(
                    state = viewState,
                    mapState = mapState,
                    onInteraction = onInteraction,
                )
                viewState.pendingEnd?.let { pending ->
                    EndEncounterDialog(pending = pending, onInteraction = onInteraction)
                }
            }
        }
    }
}

@Composable
private fun EncountersHeader(
    subtitle: String,
    showCreate: Boolean,
    onInteraction: (EncountersInteraction) -> Unit,
    compact: Boolean = false,
    selectedName: String? = null,
    onListToggle: () -> Unit = {},
) {
    AdaptiveScreenHeaderComposeWidget(
        title = "Encounters",
        subtitle = subtitle,
        compact = compact,
        switcherName = selectedName,
        listLabel = "encounters list",
        onListToggle = onListToggle,
    ) {
        if (showCreate) {
            Button(
                onClick = { onInteraction(EncountersInteraction.NewEncounterSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("New encounter")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EncountersLibrary(
    state: EncountersViewState.Content,
    compact: Boolean,
    listOpen: Boolean,
    onListDismissed: () -> Unit,
    onInteraction: (EncountersInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listInteraction: (EncountersInteraction) -> Unit = { interaction ->
        if (interaction is EncountersInteraction.EncounterSelected) {
            onListDismissed()
        }
        onInteraction(interaction)
    }
    AdaptiveListDetailComposeWidget(
        compact = compact,
        listOpen = listOpen,
        hasSelection = state.setup != null,
        listPane = { listModifier ->
            Column(
                modifier = listModifier,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    FilterChip(
                        selected = state.statusFilter == null,
                        onClick = { onInteraction(EncountersInteraction.StatusFilterSelected(null)) },
                        label = { Text("All") },
                    )
                    EncounterStatus.entries.forEach { status ->
                        FilterChip(
                            selected = state.statusFilter == status,
                            onClick = { onInteraction(EncountersInteraction.StatusFilterSelected(status)) },
                            label = { Text(status.displayName) },
                        )
                    }
                }
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (state.encounters.isEmpty()) {
                        item {
                            Text(
                                text = "No encounters in this filter.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        items(state.encounters, key = { it.id }) { encounter ->
                            EncounterRow(
                                encounter = encounter,
                                isSelected = encounter.id == state.selectedEncounter?.id &&
                                    state.setup?.encounterId != null,
                                onInteraction = listInteraction,
                            )
                        }
                    }
                }
            }
        },
        detailPane = { detailModifier ->
            val setup = state.setup
            if (setup != null) {
                EncounterSetupPane(
                    setup = setup,
                    encounterStatus = state.selectedEncounter?.status,
                    startWarning = state.startWarning,
                    onInteraction = onInteraction,
                    modifier = detailModifier,
                )
            } else {
                Text(
                    text = "Select an encounter to set it up, or start a new one.",
                    color = TextSecondary,
                    modifier = detailModifier.padding(16.dp)
                )
            }
        },
        onListDismissed = onListDismissed,
        dismissListLabel = "Dismiss encounters list",
        modifier = modifier,
    )

    state.pendingDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Delete encounter?",
            message = "Delete “${pending.encounterName}”? Participants will be removed.",
            confirmLabel = "Delete",
            onConfirm = { onInteraction(EncountersInteraction.DeleteConfirmed) },
            onDismiss = { onInteraction(EncountersInteraction.DeleteCancelled) },
        )
    }
}

@Composable
private fun EncounterRow(
    encounter: Encounter,
    isSelected: Boolean,
    onInteraction: (EncountersInteraction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteraction(EncountersInteraction.EncounterSelected(encounter.id)) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) {
                        Modifier.background(NavyBlue.copy(alpha = 0.08f))
                    } else {
                        Modifier
                    }
                )
                .padding(14.dp)
        ) {
            Text(
                text = encounter.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "${encounter.status.displayName} · ${encounter.participants.size} combatants",
                fontSize = 12.sp,
                color = if (encounter.status == EncounterStatus.Active) NavyBlue else TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun EndEncounterDialog(
    pending: EncountersViewState.PendingEnd,
    onInteraction: (EncountersInteraction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onInteraction(EncountersInteraction.EndCancelled) },
        title = { Text("End ${pending.encounterName}?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Write a short outcome. It is stored on the encounter and appended to the most recently updated session if one exists.")
                OutlinedTextField(
                    value = pending.outcomeNote,
                    onValueChange = {
                        onInteraction(EncountersInteraction.EndOutcomeChanged(it))
                    },
                    label = { Text("Outcome") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onInteraction(EncountersInteraction.EndConfirmed) }) {
                Text("End encounter")
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(EncountersInteraction.EndCancelled) }) {
                Text("Cancel")
            }
        },
    )
}
