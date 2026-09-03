package io.github.kmbisset89.worldweaver.ui.characters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.kmbisset89.worldweaver.domain.CompanionKind
import io.github.kmbisset89.worldweaver.domain.FifthEditionPickerCatalog
import io.github.kmbisset89.worldweaver.domain.PersonKind

@Composable
internal fun CharacterCreationWizardDialog(
    wizard: CharactersViewState.CreationWizardState,
    pickerCatalog: FifthEditionPickerCatalog,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    val isLast = wizard.step == CharactersViewState.CreationStep.Review
    AlertDialog(
        onDismissRequest = { onInteraction(CharactersInteraction.WizardDismissed) },
        title = {
            Text("${wizardTitle(wizard.kind)} · ${stepTitle(wizard.step)}")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Step ${stepIndex(wizard.step)} of 5")
                when (wizard.step) {
                    CharactersViewState.CreationStep.Identity -> IdentityStep(
                        wizard = wizard,
                        onInteraction = onInteraction,
                    )
                    CharactersViewState.CreationStep.RaceAndClass -> RaceAndClassStep(
                        wizard = wizard,
                        pickerCatalog = pickerCatalog,
                        onInteraction = onInteraction,
                    )
                    CharactersViewState.CreationStep.Abilities -> AbilitiesStep(
                        wizard = wizard,
                        onInteraction = onInteraction,
                    )
                    CharactersViewState.CreationStep.Companions -> CompanionsStep(
                        wizard = wizard,
                        pickerCatalog = pickerCatalog,
                        onInteraction = onInteraction,
                    )
                    CharactersViewState.CreationStep.Review -> ReviewStep(wizard = wizard)
                }
            }
        },
        confirmButton = {
            if (isLast) {
                TextButton(onClick = { onInteraction(CharactersInteraction.WizardSaved) }) {
                    Text("Create")
                }
            } else {
                TextButton(onClick = { onInteraction(CharactersInteraction.WizardNextSelected) }) {
                    Text("Next")
                }
            }
        },
        dismissButton = {
            Row {
                if (wizard.step != CharactersViewState.CreationStep.Identity) {
                    TextButton(onClick = { onInteraction(CharactersInteraction.WizardBackSelected) }) {
                        Text("Back")
                    }
                }
                TextButton(onClick = { onInteraction(CharactersInteraction.WizardDismissed) }) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
private fun IdentityStep(
    wizard: CharactersViewState.CreationWizardState,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    if (wizard.canChangeMembership) {
        Text("Membership")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PersonMembership.entries.forEach { membership ->
                FilterChip(
                    selected = wizard.membership == membership,
                    onClick = {
                        onInteraction(CharactersInteraction.WizardMembershipSelected(membership))
                    },
                    label = { Text(membership.displayName) },
                )
            }
        }
    }
    if (wizard.membershipError != null) {
        Text(wizard.membershipError)
    }
    Text("Type")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        wizardKindOptions(wizard).forEach { kind ->
            FilterChip(
                selected = wizard.kind == kind,
                onClick = { onInteraction(CharactersInteraction.WizardKindSelected(kind)) },
                label = { Text(kind.displayName) },
            )
        }
    }
    OutlinedTextField(
        value = wizard.name,
        onValueChange = { onInteraction(CharactersInteraction.WizardNameChanged(it)) },
        label = { Text("Name") },
        isError = wizard.nameError != null,
        supportingText = wizard.nameError?.let { error -> { Text(error) } },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    OutlinedTextField(
        value = wizard.description,
        onValueChange = { onInteraction(CharactersInteraction.WizardDescriptionChanged(it)) },
        label = { Text("Description") },
        minLines = 2,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RaceAndClassStep(
    wizard: CharactersViewState.CreationWizardState,
    pickerCatalog: FifthEditionPickerCatalog,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    Text("Race")
    OutlinedTextField(
        value = wizard.race,
        onValueChange = { onInteraction(CharactersInteraction.WizardRaceChanged(it)) },
        label = { Text("Race") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    pickerCatalog.races.forEach { race ->
        FilterChip(
            selected = wizard.race == race,
            onClick = { onInteraction(CharactersInteraction.WizardRaceChanged(race)) },
            label = { Text(race) },
        )
    }
    Text("Classes")
    wizard.classLevels.forEachIndexed { index, level ->
        OutlinedTextField(
            value = level.className,
            onValueChange = {
                onInteraction(CharactersInteraction.WizardClassNameChanged(index, it))
            },
            label = { Text("Class") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        pickerCatalog.classes.forEach { className ->
            FilterChip(
                selected = level.className == className,
                onClick = {
                    onInteraction(CharactersInteraction.WizardClassNameChanged(index, className))
                },
                label = { Text(className) },
            )
        }
        val subclasses = pickerCatalog.subclassesFor(level.className)
        if (subclasses.isNotEmpty()) {
            Text("Subclass")
            subclasses.forEach { subclass ->
                FilterChip(
                    selected = level.subclass == subclass,
                    onClick = {
                        onInteraction(CharactersInteraction.WizardSubclassChanged(index, subclass))
                    },
                    label = { Text(subclass) },
                )
            }
        }
        OutlinedTextField(
            value = level.subclass,
            onValueChange = {
                onInteraction(CharactersInteraction.WizardSubclassChanged(index, it))
            },
            label = { Text("Subclass") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = level.levelText,
            onValueChange = {
                onInteraction(CharactersInteraction.WizardClassLevelChanged(index, it))
            },
            label = { Text("Level") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(onClick = { onInteraction(CharactersInteraction.WizardClassLevelRemoved(index)) }) {
            Text("Remove class")
        }
    }
    TextButton(onClick = { onInteraction(CharactersInteraction.WizardClassLevelAdded) }) {
        Text("Add class")
    }
}

@Composable
private fun AbilitiesStep(
    wizard: CharactersViewState.CreationWizardState,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    Text("Ability scores")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WizardScoreField("STR", wizard.strength) {
            onInteraction(CharactersInteraction.WizardStrengthChanged(it))
        }
        WizardScoreField("DEX", wizard.dexterity) {
            onInteraction(CharactersInteraction.WizardDexterityChanged(it))
        }
        WizardScoreField("CON", wizard.constitution) {
            onInteraction(CharactersInteraction.WizardConstitutionChanged(it))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WizardScoreField("INT", wizard.intelligence) {
            onInteraction(CharactersInteraction.WizardIntelligenceChanged(it))
        }
        WizardScoreField("WIS", wizard.wisdom) {
            onInteraction(CharactersInteraction.WizardWisdomChanged(it))
        }
        WizardScoreField("CHA", wizard.charisma) {
            onInteraction(CharactersInteraction.WizardCharismaChanged(it))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        WizardScoreField("HP", wizard.hitPoints) {
            onInteraction(CharactersInteraction.WizardHitPointsChanged(it))
        }
        WizardScoreField("Max HP", wizard.maxHitPoints) {
            onInteraction(CharactersInteraction.WizardMaxHitPointsChanged(it))
        }
        WizardScoreField("AC", wizard.armorClass) {
            onInteraction(CharactersInteraction.WizardArmorClassChanged(it))
        }
        WizardScoreField("Speed", wizard.walkSpeed) {
            onInteraction(CharactersInteraction.WizardWalkSpeedChanged(it))
        }
    }
}

@Composable
private fun CompanionsStep(
    wizard: CharactersViewState.CreationWizardState,
    pickerCatalog: FifthEditionPickerCatalog,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    Text("Add a familiar or animal companion, or skip this step.")
    if (wizard.companionError != null) {
        Text(wizard.companionError)
    }
    wizard.companions.forEachIndexed { index, draft ->
        Text("Companion ${index + 1}")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompanionKind.entries.forEach { kind ->
                FilterChip(
                    selected = draft.kind == kind,
                    onClick = {
                        onInteraction(
                            CharactersInteraction.WizardCompanionKindSelected(index, kind)
                        )
                    },
                    label = { Text(kind.displayName) },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = draft.useExisting,
                onClick = {
                    onInteraction(
                        CharactersInteraction.WizardCompanionUseExistingChanged(index, true)
                    )
                },
                label = { Text("Link existing") },
            )
            FilterChip(
                selected = !draft.useExisting,
                onClick = {
                    onInteraction(
                        CharactersInteraction.WizardCompanionUseExistingChanged(index, false)
                    )
                },
                label = { Text("Create new") },
            )
        }
        if (draft.useExisting) {
            wizard.companionTargets.forEach { target ->
                FilterChip(
                    selected = draft.existingKey == target.key,
                    onClick = {
                        onInteraction(
                            CharactersInteraction.WizardCompanionTargetSelected(index, target.key)
                        )
                    },
                    label = { Text(target.name) },
                )
            }
        } else {
            OutlinedTextField(
                value = draft.newName,
                onValueChange = {
                    onInteraction(CharactersInteraction.WizardCompanionNameChanged(index, it))
                },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = draft.newCreature,
                onValueChange = {
                    onInteraction(CharactersInteraction.WizardCompanionCreatureChanged(index, it))
                },
                label = { Text("Creature") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            creatureOptions(draft.kind, pickerCatalog).forEach { creature ->
                FilterChip(
                    selected = draft.newCreature == creature,
                    onClick = {
                        onInteraction(
                            CharactersInteraction.WizardCompanionCreatureChanged(index, creature)
                        )
                    },
                    label = { Text(creature) },
                )
            }
        }
        TextButton(onClick = { onInteraction(CharactersInteraction.WizardCompanionRemoved(index)) }) {
            Text("Remove companion")
        }
    }
    TextButton(onClick = { onInteraction(CharactersInteraction.WizardCompanionAdded) }) {
        Text("Add companion")
    }
}

@Composable
private fun ReviewStep(wizard: CharactersViewState.CreationWizardState) {
    Text("${wizard.kind.displayName} · ${wizard.membership.displayName}")
    Text(wizard.name.ifBlank { "Unnamed" })
    if (wizard.description.isNotBlank()) {
        Text(wizard.description)
    }
    val classes = if (wizard.classLevels.isEmpty()) {
        "No classes"
    } else {
        wizard.classLevels.joinToString(", ") { level ->
            val subclass = if (level.subclass.isBlank()) "" else " (${level.subclass})"
            "${level.className.ifBlank { "Class" }}$subclass ${level.levelText}"
        }
    }
    Text("${wizard.race.ifBlank { "No race" }} · $classes")
    Text(
        "STR ${wizard.strength}  DEX ${wizard.dexterity}  CON ${wizard.constitution}  " +
            "INT ${wizard.intelligence}  WIS ${wizard.wisdom}  CHA ${wizard.charisma}"
    )
    Text("HP ${wizard.hitPoints}/${wizard.maxHitPoints}  ·  AC ${wizard.armorClass}  ·  Speed ${wizard.walkSpeed}")
    if (wizard.companions.isEmpty()) {
        Text("No companions")
    } else {
        wizard.companions.forEach { draft ->
            val detail = if (draft.useExisting) {
                wizard.companionTargets.firstOrNull { it.key == draft.existingKey }?.name
                    ?: "Existing person"
            } else {
                listOf(draft.newName, draft.newCreature)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                    .ifBlank { "New companion" }
            }
            Text("${draft.kind.displayName}: $detail")
        }
    }
}

@Composable
private fun WizardScoreField(
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

private fun wizardTitle(kind: PersonKind): String {
    return if (kind == PersonKind.PlayerCharacter) "New PC" else "New person"
}

private fun stepTitle(step: CharactersViewState.CreationStep): String {
    return when (step) {
        CharactersViewState.CreationStep.Identity -> "Identity"
        CharactersViewState.CreationStep.RaceAndClass -> "Race and class"
        CharactersViewState.CreationStep.Abilities -> "Abilities"
        CharactersViewState.CreationStep.Companions -> "Companions"
        CharactersViewState.CreationStep.Review -> "Review"
    }
}

private fun stepIndex(step: CharactersViewState.CreationStep): Int {
    return CharactersViewState.CreationStep.entries.indexOf(step) + 1
}

private fun wizardKindOptions(
    wizard: CharactersViewState.CreationWizardState,
): List<PersonKind> {
    return when (wizard.membership) {
        PersonMembership.WorldLibrary -> listOf(PersonKind.Npc, PersonKind.Monster)
        PersonMembership.ThisCampaign -> listOf(
            PersonKind.PlayerCharacter,
            PersonKind.Npc,
            PersonKind.Monster,
        )
    }
}

private fun creatureOptions(
    kind: CompanionKind,
    pickerCatalog: FifthEditionPickerCatalog,
): List<String> {
    return when (kind) {
        CompanionKind.Familiar -> pickerCatalog.familiars
        CompanionKind.AnimalCompanion -> pickerCatalog.animalCompanions
    }
}
