package net.tactware.worldweaver.ui.characters

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.AbilityScores
import net.tactware.worldweaver.domain.CompanionKind
import net.tactware.worldweaver.domain.FifthEditionReference
import net.tactware.worldweaver.domain.FifthEditionSheet
import net.tactware.worldweaver.domain.RelationshipType
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.voice.VoiceClipComposeWidget
import net.tactware.worldweaver.ui.voice.chooseWavPath
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

@Composable
internal fun CharacterDetailPane(
    selected: CharactersViewState.SelectedPerson,
    relationshipEditor: CharactersViewState.RelationshipEditorState?,
    companionEditor: CharactersViewState.CompanionEditorState?,
    onInteraction: (CharactersInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PersonAvatarComposeWidget(
                name = selected.name,
                avatarPath = selected.avatarPath,
                size = 72.dp,
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = selected.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = {
                            choosePngPath("Choose avatar")?.let { path ->
                                onInteraction(CharactersInteraction.AvatarImageChosen(path))
                            }
                        }
                    ) {
                        Text("Set avatar")
                    }
                    if (selected.avatarPath != null) {
                        TextButton(
                            onClick = { onInteraction(CharactersInteraction.AvatarRemoved) }
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
        Text(
            text = detailSubtitle(selected),
            fontSize = 13.sp,
            color = TextSecondary
        )
        if (selected.description.isNotBlank()) {
            Text(text = selected.description, fontSize = 14.sp, color = TextPrimary)
        }
        VoiceClipComposeWidget(
            hasClip = selected.voiceClipPath != null,
            isRecording = selected.isRecordingVoice,
            isPlaying = selected.isPlayingVoice,
            onAttachSelected = {
                chooseWavPath("Choose voice clip")?.let { path ->
                    onInteraction(CharactersInteraction.VoiceClipAttached(path))
                }
            },
            onRecordToggled = {
                onInteraction(CharactersInteraction.VoiceClipRecordToggled)
            },
            onPlayToggled = {
                onInteraction(CharactersInteraction.VoiceClipPlayToggled)
            },
            onRemoveSelected = {
                onInteraction(CharactersInteraction.VoiceClipRemoved)
            },
        )
        AbilitySection(sheet = selected.sheet)
        CombatSection(sheet = selected.sheet)
        ClassSection(sheet = selected.sheet)
        if (selected.isWorldReference) {
            OverlaySection(selected = selected, onInteraction = onInteraction)
        }
        InventorySection(sheet = selected.sheet)
        SpellSection(sheet = selected.sheet)
        FeatureSection(sheet = selected.sheet)
        if (selected.sheet.notes.isNotBlank()) {
            DetailSection("Notes", selected.sheet.notes)
        }
        CompanionSection(
            companions = selected.companions,
            editor = companionEditor,
            onInteraction = onInteraction,
        )
        RelationshipSection(
            relationships = selected.relationships,
            editor = relationshipEditor,
            onInteraction = onInteraction,
        )
        AttachedLoreSection(
            attachedLore = selected.attachedLore,
            onInteraction = onInteraction,
        )
        AttachedQuestSection(
            attachedQuests = selected.attachedQuests,
            onInteraction = onInteraction,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(
                onClick = { onInteraction(CharactersInteraction.EditPersonSelected(selected.key)) }
            ) {
                Text("Edit")
            }
            if (selected.canAddToCampaign) {
                TextButton(
                    onClick = {
                        onInteraction(CharactersInteraction.AddToCampaignSelected(selected.key.id))
                    }
                ) {
                    Text("Add to campaign")
                }
            }
            TextButton(
                onClick = { onInteraction(CharactersInteraction.DeletePersonSelected(selected.key)) }
            ) {
                Text("Delete")
            }
        }
    }
}

@Composable
private fun AbilitySection(sheet: FifthEditionSheet) {
    val scores = sheet.abilityScores
    DetailSection(
        title = "Ability scores",
        value = listOf(
            scoreLine("STR", scores.strength, scores),
            scoreLine("DEX", scores.dexterity, scores),
            scoreLine("CON", scores.constitution, scores),
            scoreLine("INT", scores.intelligence, scores),
            scoreLine("WIS", scores.wisdom, scores),
            scoreLine("CHA", scores.charisma, scores),
        ).joinToString("   "),
    )
}

@Composable
private fun CombatSection(sheet: FifthEditionSheet) {
    val death = "Death saves ${sheet.deathSaves.successes}S / ${sheet.deathSaves.failures}F"
    DetailSection(
        title = "Combat",
        value = "HP ${sheet.hitPoints}/${sheet.maxHitPoints}  ·  Temp ${sheet.temporaryHitPoints}  ·  AC ${sheet.armorClass}  ·  Speed ${sheet.walkSpeed}  ·  $death",
    )
}

@Composable
private fun ClassSection(sheet: FifthEditionSheet) {
    val race = sheet.race.ifBlank { "No race" }
    val classes = if (sheet.classLevels.isEmpty()) {
        "No classes"
    } else {
        sheet.classLevels.joinToString(", ") { level ->
            val subclass = if (level.subclass.isBlank()) "" else " (${level.subclass})"
            "${level.className}$subclass ${level.level}"
        }
    }
    DetailSection("Race and classes", "$race · Level ${sheet.totalLevel()}\n$classes")
}

@Composable
private fun InventorySection(sheet: FifthEditionSheet) {
    val value = if (sheet.items.isEmpty()) {
        "No items yet."
    } else {
        sheet.items.joinToString("\n") { item ->
            val notes = if (item.notes.isBlank()) "" else " — ${item.notes}"
            "${item.name} ×${item.quantity}$notes"
        }
    }
    DetailSection("Inventory", value)
}

@Composable
private fun SpellSection(sheet: FifthEditionSheet) {
    val value = if (sheet.spells.isEmpty()) {
        "No spells yet."
    } else {
        sheet.spells.joinToString("\n") { spell ->
            val prepared = if (spell.prepared) "prepared" else "known"
            "${spell.name} (Lv ${spell.level}, $prepared)"
        }
    }
    DetailSection("Spells", value)
}

@Composable
private fun FeatureSection(sheet: FifthEditionSheet) {
    val value = if (sheet.features.isEmpty()) {
        "No features yet."
    } else {
        sheet.features.joinToString("\n") { feature ->
            if (feature.description.isBlank()) feature.name else "${feature.name}: ${feature.description}"
        }
    }
    DetailSection("Features", value)
}

@Composable
private fun OverlaySection(
    selected: CharactersViewState.SelectedPerson,
    onInteraction: (CharactersInteraction) -> Unit,
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
                text = "Campaign overlay",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = "World-source sheet stays on the library record. Only current HP and notes are campaign-specific.",
                fontSize = 12.sp,
                color = TextSecondary
            )
            OutlinedTextField(
                value = selected.overlayHitPoints,
                onValueChange = { onInteraction(CharactersInteraction.OverlayHitPointsChanged(it)) },
                label = { Text("Current HP") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = selected.overlayNotes,
                onValueChange = { onInteraction(CharactersInteraction.OverlayNotesChanged(it)) },
                label = { Text("Campaign notes") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = { onInteraction(CharactersInteraction.OverlaySaved) }) {
                Text("Save overlay")
            }
        }
    }
}

@Composable
private fun CompanionSection(
    companions: List<CharactersViewState.CompanionRow>,
    editor: CharactersViewState.CompanionEditorState?,
    onInteraction: (CharactersInteraction) -> Unit,
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
                text = "Companions",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (companions.isEmpty()) {
                Text("No familiars or animal companions yet.", fontSize = 13.sp, color = TextSecondary)
            } else {
                companions.forEach { companion ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    onInteraction(CharactersInteraction.PersonSelected(companion.key))
                                }
                        ) {
                            Text(
                                text = companion.name,
                                fontSize = 13.sp,
                                color = NavyBlue
                            )
                            Text(
                                text = companion.kind.displayName,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        TextButton(
                            onClick = {
                                onInteraction(CharactersInteraction.CompanionDeleted(companion.id))
                            }
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }
            if (editor == null) {
                TextButton(onClick = { onInteraction(CharactersInteraction.CompanionEditorOpened) }) {
                    Text("Add companion")
                }
            } else {
                Text("Kind", fontSize = 12.sp, color = TextSecondary)
                CompanionKind.entries.forEach { kind ->
                    FilterChip(
                        selected = editor.kind == kind,
                        onClick = {
                            onInteraction(CharactersInteraction.CompanionKindSelected(kind))
                        },
                        label = { Text(kind.displayName) },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = editor.useExisting,
                        onClick = {
                            onInteraction(CharactersInteraction.CompanionUseExistingChanged(true))
                        },
                        label = { Text("Link existing") },
                    )
                    FilterChip(
                        selected = !editor.useExisting,
                        onClick = {
                            onInteraction(CharactersInteraction.CompanionUseExistingChanged(false))
                        },
                        label = { Text("Create new") },
                    )
                }
                if (editor.useExisting) {
                    editor.targets.forEach { target ->
                        FilterChip(
                            selected = editor.existingKey == target.key,
                            onClick = {
                                onInteraction(
                                    CharactersInteraction.CompanionTargetSelected(target.key)
                                )
                            },
                            label = { Text(target.name) },
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = editor.newName,
                        onValueChange = {
                            onInteraction(CharactersInteraction.CompanionNameChanged(it))
                        },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editor.newCreature,
                        onValueChange = {
                            onInteraction(CharactersInteraction.CompanionCreatureChanged(it))
                        },
                        label = { Text("Creature") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    val creatures = when (editor.kind) {
                        CompanionKind.Familiar -> FifthEditionReference.familiars
                        CompanionKind.AnimalCompanion -> FifthEditionReference.animalCompanions
                    }
                    creatures.forEach { creature ->
                        FilterChip(
                            selected = editor.newCreature == creature,
                            onClick = {
                                onInteraction(
                                    CharactersInteraction.CompanionCreatureChanged(creature)
                                )
                            },
                            label = { Text(creature) },
                        )
                    }
                }
                if (editor.error != null) {
                    Text(editor.error, fontSize = 12.sp, color = TextSecondary)
                }
                Row {
                    TextButton(onClick = { onInteraction(CharactersInteraction.CompanionSaved) }) {
                        Text("Save companion")
                    }
                    TextButton(
                        onClick = { onInteraction(CharactersInteraction.CompanionEditorDismissed) }
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun RelationshipSection(
    relationships: List<CharactersViewState.RelationshipRow>,
    editor: CharactersViewState.RelationshipEditorState?,
    onInteraction: (CharactersInteraction) -> Unit,
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
                text = "Relationships",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (relationships.isEmpty()) {
                Text("No relationships yet.", fontSize = 13.sp, color = TextSecondary)
            } else {
                relationships.forEach { relationship ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${relationship.label} · ${relationship.type.displayName}",
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            if (relationship.factionLean.isNotBlank()) {
                                Text(
                                    text = "Faction: ${relationship.factionLean}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                            if (relationship.description.isNotBlank()) {
                                Text(
                                    text = relationship.description,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        TextButton(
                            onClick = {
                                onInteraction(CharactersInteraction.RelationshipDeleted(relationship.id))
                            }
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }
            if (editor == null) {
                TextButton(onClick = { onInteraction(CharactersInteraction.RelationshipEditorOpened) }) {
                    Text("Add relationship")
                }
            } else {
                Text("Related person", fontSize = 12.sp, color = TextSecondary)
                editor.targets.forEach { target ->
                    FilterChip(
                        selected = editor.target == target.key,
                        onClick = {
                            onInteraction(CharactersInteraction.RelationshipTargetSelected(target.key))
                        },
                        label = { Text(target.name) },
                    )
                }
                if (editor.targetError != null) {
                    Text(editor.targetError, fontSize = 12.sp, color = TextSecondary)
                }
                Text("Type", fontSize = 12.sp, color = TextSecondary)
                RelationshipType.entries.forEach { type ->
                    FilterChip(
                        selected = editor.type == type,
                        onClick = {
                            onInteraction(CharactersInteraction.RelationshipTypeSelected(type))
                        },
                        label = { Text(type.displayName) },
                    )
                }
                OutlinedTextField(
                    value = editor.description,
                    onValueChange = {
                        onInteraction(CharactersInteraction.RelationshipDescriptionChanged(it))
                    },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.factionLean,
                    onValueChange = {
                        onInteraction(CharactersInteraction.RelationshipFactionChanged(it))
                    },
                    label = { Text("Faction lean") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row {
                    TextButton(onClick = { onInteraction(CharactersInteraction.RelationshipSaved) }) {
                        Text("Save relationship")
                    }
                    TextButton(onClick = { onInteraction(CharactersInteraction.RelationshipEditorDismissed) }) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachedQuestSection(
    attachedQuests: List<CharactersViewState.AttachedQuest>,
    onInteraction: (CharactersInteraction) -> Unit,
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
                    text = "No quests are linked to this person.",
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
                                onInteraction(CharactersInteraction.AttachedQuestSelected(quest.questId))
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachedLoreSection(
    attachedLore: List<CharactersViewState.AttachedLore>,
    onInteraction: (CharactersInteraction) -> Unit,
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
                    text = "No lore is attached to this person.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                attachedLore.forEach { lore ->
                    Text(
                        text = lore.title,
                        fontSize = 13.sp,
                        color = NavyBlue,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clickable {
                                onInteraction(CharactersInteraction.AttachedLoreSelected(lore.loreId))
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
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
                text = value,
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

private fun detailSubtitle(selected: CharactersViewState.SelectedPerson): String {
    val membership = if (selected.isWorldReference) {
        "Campaign reference"
    } else {
        selected.key.membership.displayName
    }
    return "${selected.kind.displayName} · $membership"
}

private fun scoreLine(label: String, score: Int, scores: AbilityScores): String {
    val modifier = scores.modifierFor(score)
    val sign = if (modifier >= 0) "+" else ""
    return "$label $score ($sign$modifier)"
}

private fun choosePngPath(title: String): String? {
    val dialog = java.awt.FileDialog(null as java.awt.Frame?, title, java.awt.FileDialog.LOAD)
    dialog.filenameFilter = java.io.FilenameFilter { _, name ->
        name.lowercase().endsWith(".png")
    }
    dialog.isVisible = true
    val fileName = dialog.file ?: return null
    val directory = dialog.directory ?: return null
    return java.io.File(directory, fileName).absolutePath
}
