package io.github.kmbisset89.worldweaver.ui.campaigns

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.Campaign
import io.github.kmbisset89.worldweaver.domain.CampaignStatus
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
internal fun CampaignsScreen(
    viewState: CampaignsViewState,
    onInteraction: (CampaignsInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(CampaignsInteraction.ScreenStarted)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        when (viewState) {
            CampaignsViewState.Loading -> {
                CampaignsHeader(
                    subtitle = "Play-throughs for the active world",
                    showCreate = false,
                    onInteraction = onInteraction,
                )
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            is CampaignsViewState.Error -> {
                CampaignsHeader(
                    subtitle = "Play-throughs for the active world",
                    showCreate = false,
                    onInteraction = onInteraction,
                )
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(CampaignsInteraction.RetrySelected) },
                )
            }
            CampaignsViewState.NoActiveWorld -> {
                CampaignsHeader(
                    subtitle = "Select a world first",
                    showCreate = false,
                    onInteraction = onInteraction,
                )
                FeatureEmptyState(
                    icon = Icons.Default.PublicOff,
                    title = "No active world",
                    message = "Create or select a world first so campaigns have a setting.",
                    actionLabel = "Go to Worlds",
                    onAction = { onInteraction(CampaignsInteraction.CreateWorldSelected) },
                )
            }
            is CampaignsViewState.Empty -> {
                CampaignsHeader(
                    subtitle = viewState.worldName,
                    showCreate = true,
                    onInteraction = onInteraction,
                )
                FeatureEmptyState(
                    icon = Icons.Default.Flag,
                    title = "No campaigns yet",
                    message = "Create a campaign to start a play-through.",
                    actionLabel = "New campaign",
                    onAction = { onInteraction(CampaignsInteraction.NewCampaignSelected) },
                )
                viewState.editor?.let { editor ->
                    CampaignEditorDialog(
                        editor = editor,
                        worldName = viewState.worldName,
                        onInteraction = onInteraction,
                    )
                }
            }
            is CampaignsViewState.Content -> {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val compact = maxWidth < AdaptiveListDetailBreakpoint
                    var listOpen by rememberAdaptiveListOpen(compact)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        CampaignsHeader(
                            subtitle = viewState.worldName,
                            showCreate = true,
                            compact = compact,
                            selectedName = viewState.selectedCampaign?.name,
                            onListToggle = { listOpen = !listOpen },
                            onInteraction = onInteraction,
                        )
                        CampaignsContent(
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
private fun CampaignsHeader(
    subtitle: String,
    showCreate: Boolean,
    onInteraction: (CampaignsInteraction) -> Unit,
    compact: Boolean = false,
    selectedName: String? = null,
    onListToggle: () -> Unit = {},
) {
    AdaptiveScreenHeaderComposeWidget(
        title = "Campaigns",
        subtitle = subtitle,
        compact = compact,
        switcherName = selectedName,
        listLabel = "campaigns list",
        onListToggle = onListToggle,
    ) {
        if (showCreate) {
            Button(
                onClick = { onInteraction(CampaignsInteraction.NewCampaignSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("New campaign")
            }
        }
    }
}

@Composable
private fun CampaignsContent(
    state: CampaignsViewState.Content,
    compact: Boolean,
    listOpen: Boolean,
    onListDismissed: () -> Unit,
    onInteraction: (CampaignsInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleCampaigns = if (state.showRetired) {
        state.campaigns
    } else {
        state.campaigns.filter { it.status == CampaignStatus.Active }
    }
    val listInteraction: (CampaignsInteraction) -> Unit = { interaction ->
        if (interaction is CampaignsInteraction.CampaignSelected) {
            onListDismissed()
        }
        onInteraction(interaction)
    }

    AdaptiveListDetailComposeWidget(
        compact = compact,
        listOpen = listOpen,
        hasSelection = state.selectedCampaign != null,
        listPane = { listModifier ->
            Column(modifier = listModifier) {
                TextButton(onClick = { onInteraction(CampaignsInteraction.RetiredVisibilityToggled) }) {
                    Text(
                        if (state.showRetired) {
                            "Hide archived and completed"
                        } else {
                            "Show archived and completed"
                        }
                    )
                }
                if (visibleCampaigns.isEmpty()) {
                    Text(
                        text = "No active campaigns. Show archived and completed to see history.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(visibleCampaigns, key = { it.id }) { campaign ->
                            CampaignRow(
                                campaign = campaign,
                                isSelected = campaign.id == state.selectedCampaign?.id,
                                onInteraction = listInteraction,
                            )
                        }
                    }
                }
            }
        },
        detailPane = { detailModifier ->
            if (state.selectedCampaign != null) {
                CampaignOverviewPane(
                    campaign = state.selectedCampaign,
                    worldDefaultGameSystem = state.worldDefaultGameSystem,
                    partyMembers = state.partyMembers,
                    activeQuests = state.activeQuests,
                    lastSession = state.lastSession,
                    nextSessionHint = state.nextSessionHint,
                    onInteraction = onInteraction,
                    modifier = detailModifier,
                )
            } else {
                Text(
                    text = "Select a campaign to see its overview.",
                    color = TextSecondary,
                    modifier = detailModifier.padding(16.dp)
                )
            }
        },
        onListDismissed = onListDismissed,
        dismissListLabel = "Dismiss campaigns list",
        modifier = modifier,
    )

    state.editor?.let { editor ->
        CampaignEditorDialog(
            editor = editor,
            worldName = state.worldName,
            onInteraction = onInteraction,
        )
    }
    state.pendingDelete?.let { pending ->
        ConfirmDestructiveDialog(
            title = "Delete campaign?",
            message = "Delete “${pending.campaignName}”? The world will not be deleted.",
            confirmLabel = "Delete",
            onConfirm = { onInteraction(CampaignsInteraction.DeleteConfirmed) },
            onDismiss = { onInteraction(CampaignsInteraction.DeleteCancelled) },
        )
    }
}

@Composable
private fun CampaignRow(
    campaign: Campaign,
    isSelected: Boolean,
    onInteraction: (CampaignsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteraction(CampaignsInteraction.CampaignSelected(campaign.id)) },
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
                text = campaign.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = CampaignsViewState.statusLabel(campaign.status),
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
