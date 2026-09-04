package io.github.kmbisset89.worldweaver.ui.characters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmbisset89.worldweaver.domain.Pathfinder2EReference
import io.github.kmbisset89.worldweaver.domain.Pathfinder2ESkillRank
import io.github.kmbisset89.worldweaver.domain.PersonKind

@Composable
internal fun PathfinderCharacterEditorDialog(
    editor: CharactersViewState.PathfinderEditorState,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    val isCreate = editor.personId == null
    AlertDialog(
        onDismissRequest = { onInteraction(CharactersInteraction.EditorDismissed) },
        title = {
            Text(if (isCreate) "New person" else "Edit person")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (editor.canChangeMembership) {
                    Text("Membership")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PersonMembership.entries.forEach { membership ->
                            FilterChip(
                                selected = editor.membership == membership,
                                onClick = {
                                    onInteraction(
                                        CharactersInteraction.EditorMembershipSelected(membership),
                                    )
                                },
                                label = { Text(membership.displayName) },
                            )
                        }
                    }
                }
                if (editor.membershipError != null) {
                    Text(editor.membershipError)
                }
                Text("Type")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pathfinderKindOptions(editor.membership).forEach { kind ->
                        FilterChip(
                            selected = editor.kind == kind,
                            onClick = { onInteraction(CharactersInteraction.EditorKindSelected(kind)) },
                            label = { Text(kind.displayName) },
                        )
                    }
                }
                OutlinedTextField(
                    value = editor.name,
                    onValueChange = { onInteraction(CharactersInteraction.EditorNameChanged(it)) },
                    label = { Text("Name") },
                    isError = editor.nameError != null,
                    supportingText = editor.nameError?.let { error -> { Text(error) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = editor.description,
                    onValueChange = {
                        onInteraction(CharactersInteraction.EditorDescriptionChanged(it))
                    },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                if (editor.isWorldReference) {
                    PathfinderOverlayEditor(editor = editor, onInteraction = onInteraction)
                } else {
                    PathfinderSheetEditor(editor = editor, onInteraction = onInteraction)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onInteraction(CharactersInteraction.EditorSaved) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = { onInteraction(CharactersInteraction.EditorDismissed) }) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PathfinderOverlayEditor(
    editor: CharactersViewState.PathfinderEditorState,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    Text("World-source fields stay on the library record.")
    OutlinedTextField(
        value = editor.overlayHitPoints,
        onValueChange = { onInteraction(CharactersInteraction.EditorOverlayHitPointsChanged(it)) },
        label = { Text("Current HP") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = editor.overlayNotes,
        onValueChange = { onInteraction(CharactersInteraction.EditorOverlayNotesChanged(it)) },
        label = { Text("Campaign notes") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PathfinderSheetEditor(
    editor: CharactersViewState.PathfinderEditorState,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    PathfinderAncestryFields(
        ancestry = editor.ancestry,
        heritage = editor.heritage,
        background = editor.background,
        className = editor.className,
        subclass = editor.subclass,
        levelText = editor.levelText,
        onInteraction = onInteraction,
    )
    if (editor.showExperience) {
        PathfinderScoreField("XP", editor.currentXpText) {
            onInteraction(CharactersInteraction.EditorExperienceChanged(it))
        }
    }
    Text("Attributes")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PathfinderScoreField("STR", editor.strength) {
            onInteraction(CharactersInteraction.EditorStrengthChanged(it))
        }
        PathfinderScoreField("DEX", editor.dexterity) {
            onInteraction(CharactersInteraction.EditorDexterityChanged(it))
        }
        PathfinderScoreField("CON", editor.constitution) {
            onInteraction(CharactersInteraction.EditorConstitutionChanged(it))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PathfinderScoreField("INT", editor.intelligence) {
            onInteraction(CharactersInteraction.EditorIntelligenceChanged(it))
        }
        PathfinderScoreField("WIS", editor.wisdom) {
            onInteraction(CharactersInteraction.EditorWisdomChanged(it))
        }
        PathfinderScoreField("CHA", editor.charisma) {
            onInteraction(CharactersInteraction.EditorCharismaChanged(it))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PathfinderScoreField("HP", editor.hitPoints) {
            onInteraction(CharactersInteraction.EditorHitPointsChanged(it))
        }
        PathfinderScoreField("Max HP", editor.maxHitPoints) {
            onInteraction(CharactersInteraction.EditorMaxHitPointsChanged(it))
        }
        PathfinderScoreField("Temp", editor.temporaryHitPoints) {
            onInteraction(CharactersInteraction.EditorTemporaryHitPointsChanged(it))
        }
        PathfinderScoreField("AC", editor.armorClass) {
            onInteraction(CharactersInteraction.EditorArmorClassChanged(it))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PathfinderScoreField("Perception", editor.perception) {
            onInteraction(CharactersInteraction.PathfinderPerceptionChanged(it))
        }
        PathfinderScoreField("Speed", editor.landSpeed) {
            onInteraction(CharactersInteraction.PathfinderLandSpeedChanged(it))
        }
        PathfinderScoreField("Dying", editor.dying) {
            onInteraction(CharactersInteraction.PathfinderDyingChanged(it))
        }
        PathfinderScoreField("Wounded", editor.wounded) {
            onInteraction(CharactersInteraction.PathfinderWoundedChanged(it))
        }
    }
    Text("Skills")
    editor.skills.forEachIndexed { index, skill ->
        PathfinderSkillRow(index = index, skill = skill, onInteraction = onInteraction)
    }
    TextButton(onClick = { onInteraction(CharactersInteraction.PathfinderSkillAdded) }) {
        Text("Add skill")
    }
    Text("Feats")
    editor.feats.forEachIndexed { index, feat ->
        OutlinedTextField(
            value = feat.name,
            onValueChange = {
                onInteraction(CharactersInteraction.PathfinderFeatNameChanged(index, it))
            },
            label = { Text("Feat") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Pathfinder2EReference.feats.forEach { name ->
            FilterChip(
                selected = feat.name == name,
                onClick = {
                    onInteraction(CharactersInteraction.PathfinderFeatNameChanged(index, name))
                },
                label = { Text(name) },
            )
        }
        OutlinedTextField(
            value = feat.type,
            onValueChange = {
                onInteraction(CharactersInteraction.PathfinderFeatTypeChanged(index, it))
            },
            label = { Text("Type") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = feat.description,
            onValueChange = {
                onInteraction(CharactersInteraction.PathfinderFeatDescriptionChanged(index, it))
            },
            label = { Text("Description") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = { onInteraction(CharactersInteraction.PathfinderFeatRemoved(index)) }) {
            Text("Remove feat")
        }
    }
    TextButton(onClick = { onInteraction(CharactersInteraction.PathfinderFeatAdded) }) {
        Text("Add feat")
    }
    Text("Spells")
    editor.spells.forEachIndexed { index, spell ->
        OutlinedTextField(
            value = spell.name,
            onValueChange = {
                onInteraction(CharactersInteraction.PathfinderSpellNameChanged(index, it))
            },
            label = { Text("Spell") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Pathfinder2EReference.spells.forEach { name ->
            FilterChip(
                selected = spell.name == name,
                onClick = {
                    onInteraction(CharactersInteraction.PathfinderSpellNameChanged(index, name))
                },
                label = { Text(name) },
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PathfinderScoreField("Rank", spell.rankText) {
                onInteraction(CharactersInteraction.PathfinderSpellRankChanged(index, it))
            }
            Checkbox(
                checked = spell.prepared,
                onCheckedChange = {
                    onInteraction(CharactersInteraction.PathfinderSpellPreparedChanged(index, it))
                },
            )
            Text("Prepared")
        }
        TextButton(onClick = { onInteraction(CharactersInteraction.PathfinderSpellRemoved(index)) }) {
            Text("Remove spell")
        }
    }
    TextButton(onClick = { onInteraction(CharactersInteraction.PathfinderSpellAdded) }) {
        Text("Add spell")
    }
    OutlinedTextField(
        value = editor.notes,
        onValueChange = { onInteraction(CharactersInteraction.EditorNotesChanged(it)) },
        label = { Text("Notes") },
        minLines = 3,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
internal fun PathfinderAncestryFields(
    ancestry: String,
    heritage: String,
    background: String,
    className: String,
    subclass: String,
    levelText: String,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    Text("Ancestry")
    OutlinedTextField(
        value = ancestry,
        onValueChange = { onInteraction(CharactersInteraction.PathfinderAncestryChanged(it)) },
        label = { Text("Ancestry") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Pathfinder2EReference.ancestries.forEach { option ->
        FilterChip(
            selected = ancestry == option,
            onClick = { onInteraction(CharactersInteraction.PathfinderAncestryChanged(option)) },
            label = { Text(option) },
        )
    }
    Text("Heritage")
    OutlinedTextField(
        value = heritage,
        onValueChange = { onInteraction(CharactersInteraction.PathfinderHeritageChanged(it)) },
        label = { Text("Heritage") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Pathfinder2EReference.heritagesFor(ancestry).forEach { option ->
        FilterChip(
            selected = heritage == option,
            onClick = { onInteraction(CharactersInteraction.PathfinderHeritageChanged(option)) },
            label = { Text(option) },
        )
    }
    Text("Background")
    OutlinedTextField(
        value = background,
        onValueChange = { onInteraction(CharactersInteraction.PathfinderBackgroundChanged(it)) },
        label = { Text("Background") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Pathfinder2EReference.backgrounds.forEach { option ->
        FilterChip(
            selected = background == option,
            onClick = { onInteraction(CharactersInteraction.PathfinderBackgroundChanged(option)) },
            label = { Text(option) },
        )
    }
    Text("Class")
    OutlinedTextField(
        value = className,
        onValueChange = { onInteraction(CharactersInteraction.PathfinderClassChanged(it)) },
        label = { Text("Class") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Pathfinder2EReference.classes.forEach { option ->
        FilterChip(
            selected = className == option,
            onClick = { onInteraction(CharactersInteraction.PathfinderClassChanged(option)) },
            label = { Text(option) },
        )
    }
    Text("Class path")
    OutlinedTextField(
        value = subclass,
        onValueChange = { onInteraction(CharactersInteraction.PathfinderSubclassChanged(it)) },
        label = { Text("Class path") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Pathfinder2EReference.subclassesFor(className).forEach { option ->
        FilterChip(
            selected = subclass == option,
            onClick = { onInteraction(CharactersInteraction.PathfinderSubclassChanged(option)) },
            label = { Text(option) },
        )
    }
    PathfinderScoreField("Level", levelText) {
        onInteraction(CharactersInteraction.PathfinderLevelChanged(it))
    }
}

@Composable
internal fun PathfinderSkillRow(
    index: Int,
    skill: CharactersViewState.PathfinderSkillEditor,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    OutlinedTextField(
        value = skill.name,
        onValueChange = {
            onInteraction(CharactersInteraction.PathfinderSkillNameChanged(index, it))
        },
        label = { Text("Skill") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Pathfinder2EReference.skills.forEach { name ->
        FilterChip(
            selected = skill.name == name,
            onClick = { onInteraction(CharactersInteraction.PathfinderSkillNameChanged(index, name)) },
            label = { Text(name) },
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Pathfinder2ESkillRank.entries.forEach { rank ->
            FilterChip(
                selected = skill.rank == rank,
                onClick = {
                    onInteraction(CharactersInteraction.PathfinderSkillRankChanged(index, rank))
                },
                label = { Text(rank.name) },
            )
        }
    }
    TextButton(onClick = { onInteraction(CharactersInteraction.PathfinderSkillRemoved(index)) }) {
        Text("Remove skill")
    }
}

@Composable
internal fun PathfinderScoreField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(0.3f)
    )
}

internal fun pathfinderKindOptions(membership: PersonMembership): List<PersonKind> {
    return when (membership) {
        PersonMembership.WorldLibrary -> listOf(PersonKind.Npc, PersonKind.Monster)
        PersonMembership.ThisCampaign -> listOf(
            PersonKind.PlayerCharacter,
            PersonKind.Npc,
            PersonKind.Monster,
        )
    }
}
