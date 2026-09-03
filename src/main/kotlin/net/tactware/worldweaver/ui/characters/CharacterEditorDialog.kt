package net.tactware.worldweaver.ui.characters

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
import net.tactware.worldweaver.domain.CreatureSize
import net.tactware.worldweaver.domain.FifthEditionPickerCatalog
import net.tactware.worldweaver.domain.PersonKind

@Composable
internal fun CharacterEditorDialog(
    editor: CharactersViewState.CharacterEditorState,
    pickerCatalog: FifthEditionPickerCatalog,
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
                                    onInteraction(CharactersInteraction.EditorMembershipSelected(membership))
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
                    kindOptions(editor).forEach { kind ->
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
                    onValueChange = { onInteraction(CharactersInteraction.EditorDescriptionChanged(it)) },
                    label = { Text("Description") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                if (editor.isWorldReference) {
                    OverlayEditor(editor = editor, onInteraction = onInteraction)
                } else {
                    SheetEditor(
                        editor = editor,
                        pickerCatalog = pickerCatalog,
                        onInteraction = onInteraction,
                    )
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
private fun OverlayEditor(
    editor: CharactersViewState.CharacterEditorState,
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
private fun SheetEditor(
    editor: CharactersViewState.CharacterEditorState,
    pickerCatalog: FifthEditionPickerCatalog,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    Text("Race")
    OutlinedTextField(
        value = editor.race,
        onValueChange = { onInteraction(CharactersInteraction.EditorRaceChanged(it)) },
        label = { Text("Race") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    pickerCatalog.races.forEach { race ->
        FilterChip(
            selected = editor.race == race,
            onClick = { onInteraction(CharactersInteraction.EditorRaceChanged(race)) },
            label = { Text(race) },
        )
    }
    Text("Classes")
    editor.classLevels.forEachIndexed { index, level ->
        ClassLevelEditor(
            index = index,
            level = level,
            pickerCatalog = pickerCatalog,
            onInteraction = onInteraction,
        )
    }
    TextButton(onClick = { onInteraction(CharactersInteraction.EditorClassLevelAdded) }) {
        Text("Add class")
    }
    Text("Ability scores")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ScoreField("STR", editor.strength) {
            onInteraction(CharactersInteraction.EditorStrengthChanged(it))
        }
        ScoreField("DEX", editor.dexterity) {
            onInteraction(CharactersInteraction.EditorDexterityChanged(it))
        }
        ScoreField("CON", editor.constitution) {
            onInteraction(CharactersInteraction.EditorConstitutionChanged(it))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ScoreField("INT", editor.intelligence) {
            onInteraction(CharactersInteraction.EditorIntelligenceChanged(it))
        }
        ScoreField("WIS", editor.wisdom) {
            onInteraction(CharactersInteraction.EditorWisdomChanged(it))
        }
        ScoreField("CHA", editor.charisma) {
            onInteraction(CharactersInteraction.EditorCharismaChanged(it))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ScoreField("HP", editor.hitPoints) {
            onInteraction(CharactersInteraction.EditorHitPointsChanged(it))
        }
        ScoreField("Max HP", editor.maxHitPoints) {
            onInteraction(CharactersInteraction.EditorMaxHitPointsChanged(it))
        }
        ScoreField("Temp", editor.temporaryHitPoints) {
            onInteraction(CharactersInteraction.EditorTemporaryHitPointsChanged(it))
        }
        ScoreField("AC", editor.armorClass) {
            onInteraction(CharactersInteraction.EditorArmorClassChanged(it))
        }
        ScoreField("Speed", editor.walkSpeed) {
            onInteraction(CharactersInteraction.EditorWalkSpeedChanged(it))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ScoreField("Death S", editor.deathSuccesses) {
            onInteraction(CharactersInteraction.EditorDeathSuccessesChanged(it))
        }
        ScoreField("Death F", editor.deathFailures) {
            onInteraction(CharactersInteraction.EditorDeathFailuresChanged(it))
        }
    }
    Text("Size")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CreatureSize.entries.forEach { size ->
            FilterChip(
                selected = editor.creatureSize == size,
                onClick = { onInteraction(CharactersInteraction.EditorCreatureSizeSelected(size)) },
                label = { Text(size.displayName) },
            )
        }
    }
    OutlinedTextField(
        value = editor.concentratingSpell,
        onValueChange = { onInteraction(CharactersInteraction.EditorConcentratingSpellChanged(it)) },
        label = { Text("Concentrating on") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Text("Skill proficiency")
    editor.skills.chunked(3).forEach { row ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            row.forEach { skill ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f),
                ) {
                    Checkbox(
                        checked = skill.proficient,
                        onCheckedChange = {
                            onInteraction(CharactersInteraction.EditorSkillProficiencyToggled(skill.name))
                        },
                    )
                    Text("${skill.name} (${skill.ability})")
                }
            }
        }
    }
    Text("Spell slots")
    editor.spellSlots.forEachIndexed { index, slot ->
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = slot.levelText,
                onValueChange = {
                    onInteraction(CharactersInteraction.EditorSpellSlotLevelChanged(index, it))
                },
                label = { Text("Lvl") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = slot.maximumText,
                onValueChange = {
                    onInteraction(CharactersInteraction.EditorSpellSlotMaximumChanged(index, it))
                },
                label = { Text("Max") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = slot.usedText,
                onValueChange = {
                    onInteraction(CharactersInteraction.EditorSpellSlotUsedChanged(index, it))
                },
                label = { Text("Used") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { onInteraction(CharactersInteraction.EditorSpellSlotRemoved(index)) }) {
                Text("Remove")
            }
        }
    }
    TextButton(onClick = { onInteraction(CharactersInteraction.EditorSpellSlotAdded) }) {
        Text("Add spell slot")
    }
    Text("Inventory")
    editor.items.forEachIndexed { index, item ->
        OutlinedTextField(
            value = item.name,
            onValueChange = { onInteraction(CharactersInteraction.EditorItemNameChanged(index, it)) },
            label = { Text("Item") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = item.quantityText,
                onValueChange = {
                    onInteraction(CharactersInteraction.EditorItemQuantityChanged(index, it))
                },
                label = { Text("Qty") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = item.notes,
                onValueChange = {
                    onInteraction(CharactersInteraction.EditorItemNotesChanged(index, it))
                },
                label = { Text("Notes") },
                singleLine = true,
                modifier = Modifier.weight(2f)
            )
        }
        TextButton(onClick = { onInteraction(CharactersInteraction.EditorItemRemoved(index)) }) {
            Text("Remove item")
        }
    }
    TextButton(onClick = { onInteraction(CharactersInteraction.EditorItemAdded) }) {
        Text("Add item")
    }
    Text("Features")
    editor.features.forEachIndexed { index, feature ->
        OutlinedTextField(
            value = feature.name,
            onValueChange = {
                onInteraction(CharactersInteraction.EditorFeatureNameChanged(index, it))
            },
            label = { Text("Feature") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = feature.description,
            onValueChange = {
                onInteraction(CharactersInteraction.EditorFeatureDescriptionChanged(index, it))
            },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = { onInteraction(CharactersInteraction.EditorFeatureRemoved(index)) }) {
            Text("Remove feature")
        }
    }
    TextButton(onClick = { onInteraction(CharactersInteraction.EditorFeatureAdded) }) {
        Text("Add feature")
    }
    Text("Spells")
    editor.spells.forEachIndexed { index, spell ->
        OutlinedTextField(
            value = spell.name,
            onValueChange = {
                onInteraction(CharactersInteraction.EditorSpellNameChanged(index, it))
            },
            label = { Text("Spell") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        pickerCatalog.spells.forEach { name ->
            FilterChip(
                selected = spell.name == name,
                onClick = { onInteraction(CharactersInteraction.EditorSpellNameChanged(index, name)) },
                label = { Text(name) },
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = spell.levelText,
                onValueChange = {
                    onInteraction(CharactersInteraction.EditorSpellLevelChanged(index, it))
                },
                label = { Text("Level") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = spell.prepared,
                    onCheckedChange = {
                        onInteraction(CharactersInteraction.EditorSpellPreparedChanged(index, it))
                    }
                )
                Text("Prepared")
            }
        }
        TextButton(onClick = { onInteraction(CharactersInteraction.EditorSpellRemoved(index)) }) {
            Text("Remove spell")
        }
    }
    TextButton(onClick = { onInteraction(CharactersInteraction.EditorSpellAdded) }) {
        Text("Add spell")
    }
    OutlinedTextField(
        value = editor.notes,
        onValueChange = { onInteraction(CharactersInteraction.EditorNotesChanged(it)) },
        label = { Text("Notes") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ClassLevelEditor(
    index: Int,
    level: CharactersViewState.ClassLevelEditor,
    pickerCatalog: FifthEditionPickerCatalog,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    OutlinedTextField(
        value = level.className,
        onValueChange = { onInteraction(CharactersInteraction.EditorClassNameChanged(index, it)) },
        label = { Text("Class") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    pickerCatalog.classes.forEach { className ->
        FilterChip(
            selected = level.className == className,
            onClick = { onInteraction(CharactersInteraction.EditorClassNameChanged(index, className)) },
            label = { Text(className) },
        )
    }
    val subclasses = pickerCatalog.subclassesFor(level.className)
    if (subclasses.isNotEmpty()) {
        Text("Subclass")
        subclasses.forEach { subclass ->
            FilterChip(
                selected = level.subclass == subclass,
                onClick = { onInteraction(CharactersInteraction.EditorSubclassChanged(index, subclass)) },
                label = { Text(subclass) },
            )
        }
    }
    OutlinedTextField(
        value = level.subclass,
        onValueChange = { onInteraction(CharactersInteraction.EditorSubclassChanged(index, it)) },
        label = { Text("Subclass") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = level.levelText,
        onValueChange = { onInteraction(CharactersInteraction.EditorClassLevelChanged(index, it)) },
        label = { Text("Level") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    TextButton(onClick = { onInteraction(CharactersInteraction.EditorClassLevelRemoved(index)) }) {
        Text("Remove class")
    }
}

@Composable
private fun ScoreField(
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

private fun kindOptions(editor: CharactersViewState.CharacterEditorState): List<PersonKind> {
    return when (editor.membership) {
        PersonMembership.WorldLibrary -> listOf(PersonKind.Npc, PersonKind.Monster)
        PersonMembership.ThisCampaign -> listOf(
            PersonKind.PlayerCharacter,
            PersonKind.Npc,
            PersonKind.Monster,
        )
    }
}
