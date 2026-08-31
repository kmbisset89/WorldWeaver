package net.tactware.worldweaver.ui.quests

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
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.Quest
import net.tactware.worldweaver.domain.QuestStatus
import net.tactware.worldweaver.ui.components.ConfirmDestructiveDialog
import net.tactware.worldweaver.ui.components.FeatureEmptyState
import net.tactware.worldweaver.ui.components.FeatureErrorState
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

@Composable
internal fun QuestsScreen(
    viewState: QuestsViewState,
    onInteraction: (QuestsInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(QuestsInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (viewState) {
            QuestsViewState.Loading -> {
                QuestsHeader(subtitle = "Campaign quests", showCreate = false, onInteraction = onInteraction)
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is QuestsViewState.Error -> {
                QuestsHeader(subtitle = "Campaign quests", showCreate = false, onInteraction = onInteraction)
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(QuestsInteraction.RetrySelected) },
                )
            }
            QuestsViewState.NoActiveWorld -> {
                QuestsHeader(subtitle = "Select a world first", showCreate = false, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so quests have a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(QuestsInteraction.CreateWorldSelected) },
                )
            }
            QuestsViewState.NoActiveCampaign -> {
                QuestsHeader(subtitle = "Select a campaign first", showCreate = false, onInteraction = onInteraction)
                FeatureEmptyState(
                    icon = Icons.Default.Flag,
                    title = "No active campaign",
                    message = "Create or select a campaign to track quests.",
                    actionLabel = "Go to Campaigns",
                    onAction = { onInteraction(QuestsInteraction.CreateCampaignSelected) },
                )
            }
            is QuestsViewState.Empty -> {
                QuestsHeader(
                    subtitle = "${viewState.campaignName} · ${viewState.worldName}",
                    showCreate = true,
                    onInteraction = onInteraction,
                )
                FeatureEmptyState(
                    icon = Icons.Default.Flag,
                    title = "No quests yet",
                    message = "Create the first quest for this campaign.",
                    actionLabel = "New quest",
                    onAction = { onInteraction(QuestsInteraction.NewQuestSelected) },
                )
                viewState.editor?.let { editor ->
                    QuestEditorDialog(editor = editor, onInteraction = onInteraction)
                }
            }
            is QuestsViewState.Content -> {
                QuestsHeader(
                    subtitle = "${viewState.campaignName} · ${viewState.worldName}",
                    showCreate = true,
                    onInteraction = onInteraction,
                )
                QuestsContent(state = viewState, onInteraction = onInteraction)
            }
        }
    }
}

@Composable
private fun QuestsHeader(
    subtitle: String,
    showCreate: Boolean,
    onInteraction: (QuestsInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Quests",
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
                onClick = { onInteraction(QuestsInteraction.NewQuestSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("New quest")
            }
        }
    }
}

@Composable
private fun QuestsContent(
    state: QuestsViewState.Content,
    onInteraction: (QuestsInteraction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = state.statusFilter == null,
                onClick = { onInteraction(QuestsInteraction.StatusFilterSelected(null)) },
                label = { Text("All") },
            )
            QuestStatus.entries.forEach { status ->
                FilterChip(
                    selected = state.statusFilter == status,
                    onClick = { onInteraction(QuestsInteraction.StatusFilterSelected(status)) },
                    label = { Text(status.displayName) },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.width(320.dp).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.quests.isEmpty()) {
                    item {
                        Text(
                            text = "No quests in this filter.",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                } else {
                    items(state.quests, key = { it.id }) { quest ->
                        QuestRow(
                            quest = quest,
                            isSelected = quest.id == state.selectedQuest?.id,
                            onInteraction = onInteraction,
                        )
                    }
                }
            }

            if (state.selectedQuest != null) {
                QuestDetailPane(
                    quest = state.selectedQuest,
                    locationName = state.locationName,
                    links = state.links,
                    onInteraction = onInteraction,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            } else {
                Text(
                    text = "Select a quest to read it.",
                    color = TextSecondary,
                    modifier = Modifier.weight(1f).padding(16.dp)
                )
            }
        }
    }

    state.editor?.let { editor ->
        QuestEditorDialog(editor = editor, onInteraction = onInteraction)
    }
    state.pendingDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Delete quest?",
            message = "Delete “${pending.questTitle}”? Objectives and links will be removed.",
            confirmLabel = "Delete",
            onConfirm = { onInteraction(QuestsInteraction.DeleteConfirmed) },
            onDismiss = { onInteraction(QuestsInteraction.DeleteCancelled) },
        )
    }
}

@Composable
private fun QuestRow(
    quest: Quest,
    isSelected: Boolean,
    onInteraction: (QuestsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteraction(QuestsInteraction.QuestSelected(quest.id)) },
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
                text = quest.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = quest.status.displayName,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
