package io.github.kmbisset89.worldweaver.ui.factions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.Faction
import io.github.kmbisset89.worldweaver.ui.components.ConfirmDestructiveDialog
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun FactionsScreen(
    viewState: FactionsViewState,
    onInteraction: (FactionsInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(FactionsInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (viewState) {
            FactionsViewState.Loading -> {
                FactionsHeader(subtitle = "World factions", showCreate = false, onInteraction = onInteraction)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is FactionsViewState.Error -> {
                FactionsHeader(subtitle = "World factions", showCreate = false, onInteraction = onInteraction)
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(FactionsInteraction.RetrySelected) },
                )
            }
            FactionsViewState.NoActiveWorld -> {
                FactionsHeader(subtitle = "Select a world first", showCreate = false, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so factions have a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(FactionsInteraction.CreateWorldSelected) },
                )
            }
            is FactionsViewState.Empty -> {
                FactionsHeader(subtitle = viewState.worldName, showCreate = true, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.AccountBalance,
                    title = "No factions yet",
                    message = "Create the first faction for this world.",
                    actionLabel = "New faction",
                    onAction = { onInteraction(FactionsInteraction.NewFactionSelected) },
                )
                viewState.editor?.let { editor ->
                    FactionEditorDialog(editor = editor, onInteraction = onInteraction)
                }
            }
            is FactionsViewState.Content -> {
                FactionsHeader(subtitle = viewState.worldName, showCreate = true, onInteraction = onInteraction)
                FactionsContent(state = viewState, onInteraction = onInteraction)
            }
        }
    }
}

@Composable
private fun FactionsHeader(
    subtitle: String,
    showCreate: Boolean,
    onInteraction: (FactionsInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Factions",
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
        if (showCreate) {
            Button(
                onClick = { onInteraction(FactionsInteraction.NewFactionSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("New faction")
            }
        }
    }
}

@Composable
private fun FactionsContent(
    state: FactionsViewState.Content,
    onInteraction: (FactionsInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.width(320.dp).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.factions, key = { it.id }) { faction ->
                FactionRow(
                    faction = faction,
                    isSelected = faction.id == state.selectedFaction?.id,
                    onInteraction = onInteraction,
                )
            }
        }
        if (state.selectedFaction != null) {
            FactionsDetailPane(
                faction = state.selectedFaction,
                members = state.members,
                onInteraction = onInteraction,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        } else {
            Text(
                text = "Select a faction to read it.",
                color = TextSecondary,
                modifier = Modifier.weight(1f).padding(16.dp)
            )
        }
    }

    state.editor?.let { editor ->
        FactionEditorDialog(editor = editor, onInteraction = onInteraction)
    }
    state.pendingDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Delete faction?",
            message = "Delete “${pending.factionName}”? This cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = { onInteraction(FactionsInteraction.DeleteConfirmed) },
            onDismiss = { onInteraction(FactionsInteraction.DeleteCancelled) },
        )
    }
    if (state.blockDeleteReason != null) {
        AlertDialog(
            onDismissRequest = { onInteraction(FactionsInteraction.BlockReasonDismissed) },
            title = { Text("Cannot delete") },
            text = { Text(state.blockDeleteReason) },
            confirmButton = {
                TextButton(onClick = { onInteraction(FactionsInteraction.BlockReasonDismissed) }) {
                    Text("OK")
                }
            },
        )
    }
}

@Composable
private fun FactionRow(
    faction: Faction,
    isSelected: Boolean,
    onInteraction: (FactionsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteraction(FactionsInteraction.FactionSelected(faction.id)) },
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
                text = faction.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (faction.description.isNotBlank()) {
                Text(
                    text = faction.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
