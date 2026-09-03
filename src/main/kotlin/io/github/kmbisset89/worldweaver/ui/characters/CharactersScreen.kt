package io.github.kmbisset89.worldweaver.ui.characters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.SrdMonsterEntry
import io.github.kmbisset89.worldweaver.ui.components.ConfirmDestructiveDialog
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun CharactersScreen(
    viewState: CharactersViewState,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(CharactersInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (viewState) {
            CharactersViewState.Loading -> {
                CharactersHeader(
                    subtitle = "People of the setting",
                    showActions = false,
                    onInteraction = onInteraction,
                )
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is CharactersViewState.Error -> {
                CharactersHeader(
                    subtitle = "People of the setting",
                    showActions = false,
                    onInteraction = onInteraction,
                )
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(CharactersInteraction.RetrySelected) },
                )
            }
            CharactersViewState.NoActiveWorld -> {
                CharactersHeader(
                    subtitle = "Select a world first",
                    showActions = false,
                    onInteraction = onInteraction,
                )
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so people have a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(CharactersInteraction.CreateWorldSelected) },
                )
            }
            is CharactersViewState.Empty -> {
                CharactersHeader(
                    subtitle = viewState.worldName,
                    showActions = true,
                    canCreatePlayerCharacter = viewState.campaignName != null,
                    canImportSrdMonster = viewState.worldGameSystemIsFifthEdition &&
                        viewState.pickerCatalog.monsters.isNotEmpty(),
                    canGenerateRandomNpc = viewState.worldGameSystemIsFifthEdition,
                    onInteraction = onInteraction,
                )
                FeatureEmptyState(
                    icon = Icons.Default.Groups,
                    title = "No people yet",
                    message = if (viewState.campaignName != null) {
                        "Create a campaign PC, add an NPC to the world library, or generate a random NPC."
                    } else {
                        "Add an NPC to the world library, or select a campaign to create a PC."
                    },
                    actionLabel = if (viewState.campaignName != null) "New PC" else "New person",
                    onAction = {
                        onInteraction(
                            if (viewState.campaignName != null) {
                                CharactersInteraction.NewPlayerCharacterSelected
                            } else {
                                CharactersInteraction.NewPersonSelected
                            }
                        )
                    },
                )
                viewState.editor?.let { editor ->
                    CharacterEditorDialog(
                        editor = editor,
                        pickerCatalog = viewState.pickerCatalog,
                        onInteraction = onInteraction,
                    )
                }
                viewState.pathfinderEditor?.let { editor ->
                    PathfinderCharacterEditorDialog(
                        editor = editor,
                        onInteraction = onInteraction,
                    )
                }
                viewState.wizard?.let { wizard ->
                    CharacterCreationWizardDialog(
                        wizard = wizard,
                        pickerCatalog = viewState.pickerCatalog,
                        onInteraction = onInteraction,
                    )
                }
                viewState.pathfinderWizard?.let { wizard ->
                    PathfinderCharacterCreationWizardDialog(
                        wizard = wizard,
                        onInteraction = onInteraction,
                    )
                }
                viewState.generator?.let { generator ->
                    RandomNpcDialog(generator = generator, onInteraction = onInteraction)
                }
                viewState.srdMonsterPicker?.let { monsters ->
                    SrdMonsterPickerDialog(monsters = monsters, onInteraction = onInteraction)
                }
            }
            is CharactersViewState.Content -> {
                CharactersHeader(
                    subtitle = viewState.worldName,
                    showActions = true,
                    canCreatePlayerCharacter = viewState.campaignName != null,
                    canImportSrdMonster = viewState.worldGameSystemIsFifthEdition &&
                        viewState.pickerCatalog.monsters.isNotEmpty(),
                    canGenerateRandomNpc = viewState.worldGameSystemIsFifthEdition,
                    onInteraction = onInteraction,
                )
                CharactersContent(state = viewState, onInteraction = onInteraction)
            }
        }
    }
}

@Composable
private fun CharactersHeader(
    subtitle: String,
    showActions: Boolean,
    canCreatePlayerCharacter: Boolean = false,
    canImportSrdMonster: Boolean = false,
    canGenerateRandomNpc: Boolean = false,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Characters",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
        if (showActions) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (canImportSrdMonster) {
                    OutlinedButton(
                        onClick = { onInteraction(CharactersInteraction.SrdMonsterImportOpened) }
                    ) {
                        Text("Add SRD monster")
                    }
                }
                if (canGenerateRandomNpc) {
                    OutlinedButton(
                        onClick = { onInteraction(CharactersInteraction.RandomNpcSelected) }
                    ) {
                        Text("Random NPC")
                    }
                }
                if (canCreatePlayerCharacter) {
                    Button(
                        onClick = {
                            onInteraction(CharactersInteraction.NewPlayerCharacterSelected)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text("New PC")
                    }
                    OutlinedButton(
                        onClick = { onInteraction(CharactersInteraction.NewPersonSelected) }
                    ) {
                        Text("New person")
                    }
                } else {
                    Button(
                        onClick = { onInteraction(CharactersInteraction.NewPersonSelected) },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text("New person")
                    }
                }
            }
        }
    }
}

@Composable
private fun CharactersContent(
    state: CharactersViewState.Content,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    if (state.blockDeleteReason != null) {
        Card(
            modifier = Modifier.fillMaxWidth().clickable {
                onInteraction(CharactersInteraction.BlockReasonDismissed)
            },
            colors = CardDefaults.cardColors(containerColor = SurfaceCard)
        ) {
            Text(
                text = state.blockDeleteReason,
                modifier = Modifier.padding(16.dp),
                color = TextPrimary,
                fontSize = 13.sp
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CharacterListPane(
            people = state.people,
            selectedKey = state.selected?.key,
            searchQuery = state.searchQuery,
            kindFilter = state.kindFilter,
            membershipFilter = state.membershipFilter,
            onInteraction = onInteraction,
            modifier = Modifier.fillMaxHeight(),
        )
        if (state.selected != null) {
            CharacterDetailPane(
                selected = state.selected,
                relationshipEditor = state.relationshipEditor,
                membershipEditor = state.membershipEditor,
                companionEditor = state.companionEditor,
                pickerCatalog = state.pickerCatalog,
                onInteraction = onInteraction,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        } else {
            Text(
                text = "Select a person to see their details.",
                color = TextSecondary,
                modifier = Modifier.weight(1f).padding(16.dp)
            )
        }
    }

    state.editor?.let { editor ->
        CharacterEditorDialog(
            editor = editor,
            pickerCatalog = state.pickerCatalog,
            onInteraction = onInteraction,
        )
    }
    state.pathfinderEditor?.let { editor ->
        PathfinderCharacterEditorDialog(
            editor = editor,
            onInteraction = onInteraction,
        )
    }
    state.wizard?.let { wizard ->
        CharacterCreationWizardDialog(
            wizard = wizard,
            pickerCatalog = state.pickerCatalog,
            onInteraction = onInteraction,
        )
    }
    state.pathfinderWizard?.let { wizard ->
        PathfinderCharacterCreationWizardDialog(
            wizard = wizard,
            onInteraction = onInteraction,
        )
    }
    state.srdMonsterPicker?.let { monsters ->
        SrdMonsterPickerDialog(monsters = monsters, onInteraction = onInteraction)
    }
    state.generator?.let { generator ->
        RandomNpcDialog(generator = generator, onInteraction = onInteraction)
    }
    state.pendingDelete?.let { pending ->
        val message = if (pending.key.membership == PersonMembership.WorldLibrary) {
            "Delete “${pending.name}”? Campaigns that still reference this person will block the delete."
        } else {
            "Delete “${pending.name}” from this campaign?"
        }
        ConfirmDestructiveDialog(
            title = "Delete person?",
            message = message,
            confirmLabel = "Delete",
            onConfirm = { onInteraction(CharactersInteraction.DeleteConfirmed) },
            onDismiss = { onInteraction(CharactersInteraction.DeleteCancelled) },
        )
    }
}

@Composable
private fun SrdMonsterPickerDialog(
    monsters: List<SrdMonsterEntry>,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onInteraction(CharactersInteraction.SrdMonsterImportDismissed) },
        title = { Text("Add SRD monster") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Adds a world-library monster with name, HP, AC, and walk speed from the imported catalog.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )
                monsters.forEach { monster ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            onInteraction(CharactersInteraction.SrdMonsterSelected(monster.name))
                        },
                        label = { Text(monsterSubtitle(monster)) },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { onInteraction(CharactersInteraction.SrdMonsterImportDismissed) }) {
                Text("Cancel")
            }
        },
    )
}

private fun monsterSubtitle(monster: SrdMonsterEntry): String {
    val details = listOfNotNull(
        monster.creatureType.takeIf { it.isNotBlank() },
        monster.challengeRating.takeIf { it.isNotBlank() }?.let { "CR $it" },
        "HP ${monster.hitPoints}",
        "AC ${monster.armorClass}",
    )
    return "${monster.name} · ${details.joinToString(" · ")}"
}
