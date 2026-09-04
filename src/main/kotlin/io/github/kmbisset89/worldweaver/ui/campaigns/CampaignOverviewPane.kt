package io.github.kmbisset89.worldweaver.ui.campaigns

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
import io.github.kmbisset89.worldweaver.domain.Campaign
import io.github.kmbisset89.worldweaver.domain.CampaignStatus
import io.github.kmbisset89.worldweaver.domain.GameSystem
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun CampaignOverviewPane(
    campaign: Campaign,
    worldDefaultGameSystem: GameSystem,
    partyMembers: List<CampaignsViewState.PartyMember>,
    activeQuests: List<CampaignsViewState.OverviewQuest>,
    lastSession: CampaignsViewState.OverviewSession?,
    nextSessionHint: String,
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
                campaign.resolvedGameSystem(worldDefaultGameSystem).displayName +
                " · ${campaign.levelingMode.displayName}",
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

        PartySection(partyMembers = partyMembers, onInteraction = onInteraction)
        OverviewSection(
            title = "Active quests",
            emptyMessage = "There are no quests yet.",
            items = activeQuests.map { it.title },
            actionLabel = "Open Quests",
            onAction = { onInteraction(CampaignsInteraction.OpenQuestsSelected) },
        )
        OverviewSection(
            title = "Last session",
            emptyMessage = "There are no sessions yet.",
            items = lastSession?.let { session ->
                listOfNotNull(
                    session.name,
                    session.dateLabel,
                    session.recap.takeIf { it.isNotBlank() },
                )
            }.orEmpty(),
            actionLabel = "Open Sessions",
            onAction = { onInteraction(CampaignsInteraction.OpenSessionsSelected) },
        )
        OverviewSection(
            title = "Next session",
            emptyMessage = nextSessionHint,
            items = emptyList(),
            actionLabel = "Open Sessions",
            onAction = { onInteraction(CampaignsInteraction.OpenSessionsSelected) },
        )
    }
}

@Composable
private fun PartySection(
    partyMembers: List<CampaignsViewState.PartyMember>,
    onInteraction: (CampaignsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Party",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (partyMembers.isEmpty()) {
                Text(
                    text = "There are no PCs yet.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                partyMembers.forEach { member ->
                    Text(
                        text = member.name,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    if (member.summary.isNotBlank()) {
                        Text(
                            text = member.summary,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = {
                        onInteraction(CampaignsInteraction.CreatePlayerCharacterSelected)
                    }
                ) {
                    Text("Add PC")
                }
                TextButton(onClick = { onInteraction(CampaignsInteraction.OpenCharactersSelected) }) {
                    Text("Open Characters")
                }
            }
        }
    }
}

@Composable
private fun OverviewSection(
    title: String,
    emptyMessage: String,
    items: List<String>,
    actionLabel: String,
    onAction: () -> Unit,
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
            if (items.isEmpty()) {
                Text(
                    text = emptyMessage,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                items.forEach { item ->
                    Text(
                        text = item,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
            TextButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}
