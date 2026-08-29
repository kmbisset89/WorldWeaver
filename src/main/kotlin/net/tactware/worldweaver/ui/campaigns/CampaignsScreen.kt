package net.tactware.worldweaver.ui.campaigns

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
import net.tactware.worldweaver.domain.Campaign
import net.tactware.worldweaver.domain.CampaignStatus
import net.tactware.worldweaver.ui.components.ConfirmDestructiveDialog
import net.tactware.worldweaver.ui.components.FeatureEmptyState
import net.tactware.worldweaver.ui.components.FeatureErrorState
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

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
                CampaignsHeader(
                    subtitle = viewState.worldName,
                    showCreate = true,
                    onInteraction = onInteraction,
                )
                CampaignsContent(
                    state = viewState,
                    onInteraction = onInteraction,
                )
            }
        }
    }
}

@Composable
private fun CampaignsHeader(
    subtitle: String,
    showCreate: Boolean,
    onInteraction: (CampaignsInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Campaigns",
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
    onInteraction: (CampaignsInteraction) -> Unit,
) {
    val visibleCampaigns = if (state.showRetired) {
        state.campaigns
    } else {
        state.campaigns.filter { it.status == CampaignStatus.Active }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.width(320.dp).fillMaxHeight()
        ) {
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
                            onInteraction = onInteraction,
                        )
                    }
                }
            }
        }

        if (state.selectedCampaign != null) {
            CampaignOverviewPane(
                campaign = state.selectedCampaign,
                onInteraction = onInteraction,
                modifier = Modifier.weight(1f).fillMaxHeight(),
            )
        } else {
            Text(
                text = "Select a campaign to see its overview.",
                color = TextSecondary,
                modifier = Modifier.weight(1f).padding(16.dp)
            )
        }
    }

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
