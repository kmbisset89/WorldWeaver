package net.tactware.worldweaver.ui.campaigns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.Campaign
import net.tactware.worldweaver.domain.CampaignStatus
import net.tactware.worldweaver.domain.GameSystem
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

@Composable
internal fun CampaignOverviewPane(
    campaign: Campaign,
    onInteraction: (CampaignsInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = campaign.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "${CampaignsViewState.statusLabel(campaign.status)} · " +
                (campaign.gameSystem ?: GameSystem.FifthEdition).displayName,
            fontSize = 13.sp,
            color = TextSecondary
        )
        if (campaign.description.isNotBlank()) {
            Text(text = campaign.description, fontSize = 14.sp, color = TextPrimary)
        }
        if (campaign.notes.isNotBlank()) {
            Text(text = campaign.notes, fontSize = 13.sp, color = TextSecondary)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = { onInteraction(CampaignsInteraction.EditCampaignSelected(campaign.id)) }) {
                Text("Edit")
            }
            TextButton(
                onClick = {
                    onInteraction(
                        CampaignsInteraction.StatusSelected(campaign.id, CampaignStatus.Archived)
                    )
                }
            ) {
                Text("Archive")
            }
            TextButton(
                onClick = {
                    onInteraction(
                        CampaignsInteraction.StatusSelected(campaign.id, CampaignStatus.Completed)
                    )
                }
            ) {
                Text("Complete")
            }
            if (campaign.status != CampaignStatus.Active) {
                TextButton(
                    onClick = {
                        onInteraction(
                            CampaignsInteraction.StatusSelected(campaign.id, CampaignStatus.Active)
                        )
                    }
                ) {
                    Text("Reopen")
                }
            }
            TextButton(onClick = { onInteraction(CampaignsInteraction.DeleteCampaignSelected(campaign.id)) }) {
                Text("Delete")
            }
        }

        OverviewSection(
            title = "Party",
            emptyMessage = "There are no PCs yet.",
        )
        OverviewSection(
            title = "Active quests",
            emptyMessage = "There are no quests yet.",
        )
        OverviewSection(
            title = "Last session",
            emptyMessage = "There are no sessions yet.",
        )
        OverviewSection(
            title = "Next session",
            emptyMessage = "No next-session hint yet. Add a session when you are ready to run.",
        )
    }
}

@Composable
private fun OverviewSection(
    title: String,
    emptyMessage: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = emptyMessage,
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
