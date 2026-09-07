package io.github.kmbisset89.worldweaver.ui.quests

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.Quest
import io.github.kmbisset89.worldweaver.domain.QuestStatus
import io.github.kmbisset89.worldweaver.ui.advancement.AdvancementPromptComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveListDetailBreakpoint
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveListDetailComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.AdaptiveScreenHeaderComposeWidget
import io.github.kmbisset89.worldweaver.ui.components.ConfirmDestructiveDialog
import io.github.kmbisset89.worldweaver.ui.components.FeatureEmptyState
import io.github.kmbisset89.worldweaver.ui.components.FeatureErrorState
import io.github.kmbisset89.worldweaver.ui.components.rememberAdaptiveListOpen
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

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
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val compact = maxWidth < AdaptiveListDetailBreakpoint
                    var listOpen by rememberAdaptiveListOpen(compact)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        QuestsHeader(
                            subtitle = "${viewState.campaignName} · ${viewState.worldName}",
                            showCreate = true,
                            compact = compact,
                            selectedName = viewState.selectedQuest?.title,
                            onListToggle = { listOpen = !listOpen },
                            onInteraction = onInteraction,
                        )
                        QuestsContent(
                            state = viewState,
                            compact = compact,
                            listOpen = listOpen,
                            onListDismissed = { listOpen = false },
                            onInteraction = onInteraction,
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestsHeader(
    subtitle: String,
    showCreate: Boolean,
    onInteraction: (QuestsInteraction) -> Unit,
    compact: Boolean = false,
    selectedName: String? = null,
    onListToggle: () -> Unit = {},
) {
    AdaptiveScreenHeaderComposeWidget(
        title = "Quests",
        subtitle = subtitle,
        compact = compact,
        switcherName = selectedName,
        listLabel = "quests list",
        onListToggle = onListToggle,
    ) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestsContent(
    state: QuestsViewState.Content,
    compact: Boolean,
    listOpen: Boolean,
    onListDismissed: () -> Unit,
    onInteraction: (QuestsInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listInteraction: (QuestsInteraction) -> Unit = { interaction ->
        if (interaction is QuestsInteraction.QuestSelected) {
            onListDismissed()
        }
        onInteraction(interaction)
    }
    AdaptiveListDetailComposeWidget(
        compact = compact,
        listOpen = listOpen,
        hasSelection = state.selectedQuest != null,
        listPane = { listModifier ->
            Column(
                modifier = listModifier,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
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
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(),
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
                                onInteraction = listInteraction,
                            )
                        }
                    }
                }
            }
        },
        detailPane = { detailModifier ->
            if (state.selectedQuest != null) {
                QuestDetailPane(
                    quest = state.selectedQuest,
                    locationName = state.locationName,
                    links = state.links,
                    wikilinkBacklinks = state.wikilinkBacklinks,
                    onInteraction = onInteraction,
                    modifier = detailModifier,
                )
            } else {
                Text(
                    text = "Select a quest to read it.",
                    color = TextSecondary,
                    modifier = detailModifier.padding(16.dp)
                )
            }
        },
        onListDismissed = onListDismissed,
        dismissListLabel = "Dismiss quests list",
        modifier = modifier,
    )

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
    state.advancementPrompt?.let { prompt ->
        AdvancementPromptComposeWidget(
            prompt = prompt,
            onDismiss = { onInteraction(QuestsInteraction.AdvancementDismissed) },
            onAwardLevel = { onInteraction(QuestsInteraction.AwardLevelConfirmed) },
            onAmountChanged = { onInteraction(QuestsInteraction.AwardExperienceAmountChanged(it)) },
            onAwardExperience = { onInteraction(QuestsInteraction.AwardExperienceConfirmed) },
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
