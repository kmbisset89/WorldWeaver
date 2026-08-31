package net.tactware.worldweaver.ui.characters

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.ui.components.ConfirmDestructiveDialog
import net.tactware.worldweaver.ui.components.FeatureEmptyState
import net.tactware.worldweaver.ui.components.FeatureErrorState
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

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
                    onInteraction = onInteraction,
                )
                FeatureEmptyState(
                    icon = Icons.Default.Groups,
                    title = "No people yet",
                    message = "Add an NPC to the world library, create a campaign PC, or generate a random NPC.",
                    actionLabel = "New person",
                    onAction = { onInteraction(CharactersInteraction.NewPersonSelected) },
                )
                viewState.editor?.let { editor ->
                    CharacterEditorDialog(editor = editor, onInteraction = onInteraction)
                }
                viewState.wizard?.let { wizard ->
                    CharacterCreationWizardDialog(wizard = wizard, onInteraction = onInteraction)
                }
                viewState.generator?.let { generator ->
                    RandomNpcDialog(generator = generator, onInteraction = onInteraction)
                }
            }
            is CharactersViewState.Content -> {
                CharactersHeader(
                    subtitle = viewState.worldName,
                    showActions = true,
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
                OutlinedButton(
                    onClick = { onInteraction(CharactersInteraction.RandomNpcSelected) }
                ) {
                    Text("Random NPC")
                }
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
                companionEditor = state.companionEditor,
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
        CharacterEditorDialog(editor = editor, onInteraction = onInteraction)
    }
    state.wizard?.let { wizard ->
        CharacterCreationWizardDialog(wizard = wizard, onInteraction = onInteraction)
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
