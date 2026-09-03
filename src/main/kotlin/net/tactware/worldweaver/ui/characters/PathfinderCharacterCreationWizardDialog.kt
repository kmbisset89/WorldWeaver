package net.tactware.worldweaver.ui.characters

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
import net.tactware.worldweaver.domain.Pathfinder2ESkillRank
import net.tactware.worldweaver.domain.PersonKind

@Composable
internal fun PathfinderCharacterCreationWizardDialog(
    wizard: CharactersViewState.PathfinderWizardState,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    val isLast = wizard.step == CharactersViewState.PathfinderCreationStep.Review
    AlertDialog(
        onDismissRequest = { onInteraction(CharactersInteraction.WizardDismissed) },
        title = {
            Text("${pathfinderWizardTitle(wizard.kind)} · ${pathfinderStepTitle(wizard.step)}")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Step ${pathfinderStepIndex(wizard.step)} of 5")
                when (wizard.step) {
                    CharactersViewState.PathfinderCreationStep.Identity -> {
                        PathfinderIdentityStep(wizard = wizard, onInteraction = onInteraction)
                    }
                    CharactersViewState.PathfinderCreationStep.AncestryAndClass -> {
                        PathfinderAncestryFields(
                            ancestry = wizard.ancestry,
                            heritage = wizard.heritage,
                            background = wizard.background,
                            className = wizard.className,
                            subclass = wizard.subclass,
                            levelText = wizard.levelText,
                            onInteraction = onInteraction,
                        )
                    }
                    CharactersViewState.PathfinderCreationStep.Attributes -> {
                        PathfinderAttributesStep(wizard = wizard, onInteraction = onInteraction)
                    }
                    CharactersViewState.PathfinderCreationStep.Skills -> {
                        PathfinderSkillsStep(wizard = wizard, onInteraction = onInteraction)
                    }
                    CharactersViewState.PathfinderCreationStep.Review -> {
                        PathfinderReviewStep(wizard = wizard)
                    }
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
                if (wizard.step != CharactersViewState.PathfinderCreationStep.Identity) {
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
private fun PathfinderIdentityStep(
    wizard: CharactersViewState.PathfinderWizardState,
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
        pathfinderKindOptions(wizard.membership).forEach { kind ->
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
private fun PathfinderAttributesStep(
    wizard: CharactersViewState.PathfinderWizardState,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    Text("Attributes")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PathfinderScoreField("STR", wizard.strength) {
            onInteraction(CharactersInteraction.WizardStrengthChanged(it))
        }
        PathfinderScoreField("DEX", wizard.dexterity) {
            onInteraction(CharactersInteraction.WizardDexterityChanged(it))
        }
        PathfinderScoreField("CON", wizard.constitution) {
            onInteraction(CharactersInteraction.WizardConstitutionChanged(it))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PathfinderScoreField("INT", wizard.intelligence) {
            onInteraction(CharactersInteraction.WizardIntelligenceChanged(it))
        }
        PathfinderScoreField("WIS", wizard.wisdom) {
            onInteraction(CharactersInteraction.WizardWisdomChanged(it))
        }
        PathfinderScoreField("CHA", wizard.charisma) {
            onInteraction(CharactersInteraction.WizardCharismaChanged(it))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PathfinderScoreField("HP", wizard.hitPoints) {
            onInteraction(CharactersInteraction.WizardHitPointsChanged(it))
        }
        PathfinderScoreField("Max HP", wizard.maxHitPoints) {
            onInteraction(CharactersInteraction.WizardMaxHitPointsChanged(it))
        }
        PathfinderScoreField("AC", wizard.armorClass) {
            onInteraction(CharactersInteraction.WizardArmorClassChanged(it))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PathfinderScoreField("Perception", wizard.perception) {
            onInteraction(CharactersInteraction.PathfinderPerceptionChanged(it))
        }
        PathfinderScoreField("Speed", wizard.landSpeed) {
            onInteraction(CharactersInteraction.PathfinderLandSpeedChanged(it))
        }
    }
}

@Composable
private fun PathfinderSkillsStep(
    wizard: CharactersViewState.PathfinderWizardState,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    Text("Skills")
    wizard.skills.forEachIndexed { index, skill ->
        PathfinderSkillRow(index = index, skill = skill, onInteraction = onInteraction)
    }
    TextButton(onClick = { onInteraction(CharactersInteraction.PathfinderSkillAdded) }) {
        Text("Add skill")
    }
}

@Composable
private fun PathfinderReviewStep(wizard: CharactersViewState.PathfinderWizardState) {
    Text("Name: ${wizard.name.ifBlank { "(unnamed)" }}")
    Text("Type: ${wizard.kind.displayName}")
    Text("Membership: ${wizard.membership.displayName}")
    Text("Ancestry: ${wizard.ancestry.ifBlank { "—" }}")
    Text("Heritage: ${wizard.heritage.ifBlank { "—" }}")
    Text("Background: ${wizard.background.ifBlank { "—" }}")
    val classLabel = listOfNotNull(
        wizard.className.takeIf { it.isNotBlank() },
        wizard.subclass.takeIf { it.isNotBlank() }?.let { "($it)" },
        "Lv ${wizard.levelText.ifBlank { "1" }}",
    ).joinToString(" ")
    Text("Class: ${classLabel.ifBlank { "—" }}")
    Text(
        "STR ${wizard.strength}  DEX ${wizard.dexterity}  CON ${wizard.constitution}  " +
            "INT ${wizard.intelligence}  WIS ${wizard.wisdom}  CHA ${wizard.charisma}",
    )
    Text("HP ${wizard.hitPoints}/${wizard.maxHitPoints}  ·  AC ${wizard.armorClass}")
    Text("Perception ${wizard.perception}  ·  Speed ${wizard.landSpeed}")
    val trained = wizard.skills.filter { it.rank != Pathfinder2ESkillRank.Untrained }
    if (trained.isEmpty()) {
        Text("Skills: all untrained")
    } else {
        Text(
            "Skills: " + trained.joinToString(", ") { skill ->
                "${skill.name} (${skill.rank.name})"
            },
        )
    }
}

private fun pathfinderWizardTitle(kind: PersonKind): String {
    return if (kind == PersonKind.PlayerCharacter) "New PC" else "New person"
}

private fun pathfinderStepTitle(step: CharactersViewState.PathfinderCreationStep): String {
    return when (step) {
        CharactersViewState.PathfinderCreationStep.Identity -> "Identity"
        CharactersViewState.PathfinderCreationStep.AncestryAndClass -> "Ancestry and class"
        CharactersViewState.PathfinderCreationStep.Attributes -> "Attributes"
        CharactersViewState.PathfinderCreationStep.Skills -> "Skills"
        CharactersViewState.PathfinderCreationStep.Review -> "Review"
    }
}

private fun pathfinderStepIndex(step: CharactersViewState.PathfinderCreationStep): Int {
    return step.ordinal + 1
}
