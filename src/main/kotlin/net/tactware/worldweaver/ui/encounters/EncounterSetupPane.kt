package net.tactware.worldweaver.ui.encounters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.EncounterDifficulty
import net.tactware.worldweaver.domain.EncounterParticipantSource
import net.tactware.worldweaver.domain.EncounterStatus
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

@Composable
internal fun EncounterSetupPane(
    setup: EncountersViewState.EncounterSetupState,
    encounterStatus: EncounterStatus?,
    startWarning: String?,
    onInteraction: (EncountersInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isCreate = setup.encounterId == null
    val search = setup.rosterSearch.trim()
    val campaignOptions = setup.campaignPersonOptions.filter { option ->
        search.isEmpty() || option.name.contains(search, ignoreCase = true)
    }
    val worldOptions = setup.worldPersonOptions.filter { option ->
        search.isEmpty() || option.name.contains(search, ignoreCase = true)
    }
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (isCreate) "New encounter" else "Setup",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        OutlinedTextField(
            value = setup.name,
            onValueChange = { onInteraction(EncountersInteraction.EditorNameChanged(it)) },
            label = { Text("Name") },
            isError = setup.nameError != null,
            supportingText = setup.nameError?.let { error -> { Text(error) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = setup.notes,
            onValueChange = { onInteraction(EncountersInteraction.EditorNotesChanged(it)) },
            label = { Text("Notes") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Difficulty", fontSize = 13.sp, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            EncounterDifficulty.entries.forEach { difficulty ->
                FilterChip(
                    selected = setup.difficulty == difficulty,
                    onClick = {
                        onInteraction(EncountersInteraction.EditorDifficultySelected(difficulty))
                    },
                    label = { Text(difficulty.displayName) },
                )
            }
        }
        Text("Location", fontSize = 13.sp, color = TextSecondary)
        EncounterChoiceList(
            selectedLabel = setup.locationOptions.firstOrNull { it.id == setup.locationId }?.name
                ?: "None",
            noneSelected = setup.locationId == null,
            onNoneSelected = { onInteraction(EncountersInteraction.EditorLocationSelected(null)) },
            options = setup.locationOptions.map { it.id to it.name },
            selectedId = setup.locationId,
            onSelected = { id -> onInteraction(EncountersInteraction.EditorLocationSelected(id)) },
        )
        Text("Battle map", fontSize = 13.sp, color = TextSecondary)
        EncounterChoiceList(
            selectedLabel = setup.battleMapOptions.firstOrNull { it.id == setup.battleMapId }?.name
                ?: "None",
            noneSelected = setup.battleMapId == null,
            onNoneSelected = { onInteraction(EncountersInteraction.EditorBattleMapSelected(null)) },
            options = setup.battleMapOptions.map { it.id to it.name },
            selectedId = setup.battleMapId,
            onSelected = { id -> onInteraction(EncountersInteraction.EditorBattleMapSelected(id)) },
        )
        Text(
            text = "Roster",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onInteraction(EncountersInteraction.EditorPartyAdded) }) {
                Text("Add party")
            }
            TextButton(
                onClick = {
                    onInteraction(
                        EncountersInteraction.RollAllInitiativeSelected(
                            encounterId = setup.encounterId,
                            overwriteExisting = false,
                        )
                    )
                }
            ) {
                Text("Roll all initiative")
            }
        }
        if (setup.missingInitiativeCount > 0) {
            Text(
                text = "${setup.missingInitiativeCount} combatants still need initiative",
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        OutlinedTextField(
            value = setup.rosterSearch,
            onValueChange = { onInteraction(EncountersInteraction.EditorRosterSearchChanged(it)) },
            label = { Text("Search people") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Campaign PCs", fontSize = 13.sp, color = TextSecondary)
        PersonOptionRow(
            options = campaignOptions,
            onAdded = { id -> onInteraction(EncountersInteraction.EditorCampaignPersonAdded(id)) },
        )
        setup.companionSuggestions.forEach { suggestion ->
            val names = suggestion.companions.joinToString(", ") { it.name }
            TextButton(
                onClick = {
                    onInteraction(
                        EncountersInteraction.EditorOwnerCompanionsAdded(
                            ownerPersonId = suggestion.ownerPersonId,
                            ownerSource = suggestion.ownerSource,
                        )
                    )
                }
            ) {
                Text("Add companions of ${suggestion.ownerName} ($names)")
            }
        }
        Text("World NPCs", fontSize = 13.sp, color = TextSecondary)
        PersonOptionRow(
            options = worldOptions,
            onAdded = { id -> onInteraction(EncountersInteraction.EditorWorldPersonAdded(id)) },
        )
        Text("Nameless combatants", fontSize = 13.sp, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = setup.namelessName,
                onValueChange = { onInteraction(EncountersInteraction.EditorNamelessNameChanged(it)) },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.weight(2f)
            )
            OutlinedTextField(
                value = setup.namelessGroupCount,
                onValueChange = {
                    onInteraction(EncountersInteraction.EditorNamelessGroupCountChanged(it))
                },
                label = { Text("Count") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
        FilterChip(
            selected = setup.namelessAsSwarm,
            onClick = { onInteraction(EncountersInteraction.EditorNamelessSwarmToggled) },
            label = { Text("Swarm (shared HP)") },
        )
        TextButton(onClick = { onInteraction(EncountersInteraction.EditorNamelessAdded) }) {
            Text(if (setup.namelessAsSwarm) "Add swarm" else "Add combatants")
        }
        Text("Participants", fontSize = 13.sp, color = TextSecondary)
        setup.participants.forEachIndexed { index, participant ->
            val group = if (participant.groupCount > 1) {
                " ×${participant.groupCount}"
            } else {
                ""
            }
            val source = when (participant.source) {
                EncounterParticipantSource.Nameless -> "nameless"
                EncounterParticipantSource.WorldPerson -> "world"
                EncounterParticipantSource.CampaignPerson -> "campaign"
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = participant.initiativeRoll?.toString().orEmpty(),
                    onValueChange = { roll ->
                        onInteraction(
                            EncountersInteraction.EditorParticipantInitiativeChanged(index, roll)
                        )
                    },
                    label = { Text("${participant.name}$group ($source) d20") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = {
                        onInteraction(EncountersInteraction.EditorParticipantRemoved(index))
                    }
                ) {
                    Text("Remove")
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = { onInteraction(EncountersInteraction.EditorSaved) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text(if (isCreate) "Create" else "Save")
            }
            if (isCreate) {
                TextButton(onClick = { onInteraction(EncountersInteraction.EditorDismissed) }) {
                    Text("Cancel")
                }
            }
            val canStart = setup.encounterId != null && encounterStatus != EncounterStatus.Active
            if (canStart) {
                Button(
                    onClick = {
                        onInteraction(
                            EncountersInteraction.StartEncounterSelected(setup.encounterId)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                ) {
                    Text("Start")
                }
            }
            if (setup.battleMapId != null) {
                TextButton(
                    onClick = {
                        onInteraction(EncountersInteraction.OpenMapSelected(setup.battleMapId))
                    }
                ) {
                    Text("Open map")
                }
            }
            if (setup.encounterId != null) {
                TextButton(
                    onClick = {
                        onInteraction(
                            EncountersInteraction.DeleteEncounterSelected(setup.encounterId)
                        )
                    }
                ) {
                    Text("Delete")
                }
            }
        }
        if (startWarning != null) {
            Text(text = startWarning, fontSize = 13.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun EncounterChoiceList(
    selectedLabel: String,
    noneSelected: Boolean,
    onNoneSelected: () -> Unit,
    options: List<Pair<String, String>>,
    selectedId: String?,
    onSelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Selected: $selectedLabel",
            fontSize = 13.sp,
            color = TextPrimary
        )
        Column(
            modifier = Modifier.heightIn(max = 160.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(
                selected = noneSelected,
                onClick = onNoneSelected,
                label = { Text("None") },
            )
            options.forEach { (id, name) ->
                FilterChip(
                    selected = selectedId == id,
                    onClick = { onSelected(id) },
                    label = { Text(name) },
                )
            }
        }
    }
}

@Composable
private fun PersonOptionRow(
    options: List<EncountersViewState.PersonOption>,
    onAdded: (String) -> Unit,
) {
    Column(
        modifier = Modifier.heightIn(max = 160.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (options.isEmpty()) {
            Text("None", fontSize = 13.sp, color = TextSecondary)
        } else {
            options.forEach { option ->
                FilterChip(
                    selected = option.alreadyAdded,
                    enabled = !option.alreadyAdded,
                    onClick = { onAdded(option.id) },
                    label = { Text(option.name) },
                )
            }
        }
    }
}
