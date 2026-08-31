package net.tactware.worldweaver.ui.quests

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.Quest
import net.tactware.worldweaver.domain.QuestLinkKind
import net.tactware.worldweaver.domain.QuestObjectiveStatus
import net.tactware.worldweaver.domain.QuestStatus
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

@Composable
internal fun QuestDetailPane(
    quest: Quest,
    locationName: String?,
    links: List<QuestsViewState.QuestLinkRow>,
    onInteraction: (QuestsInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = quest.title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = quest.status.displayName,
            fontSize = 13.sp,
            color = TextSecondary
        )
        if (quest.summary.isNotBlank()) {
            Text(text = quest.summary, fontSize = 14.sp, color = TextPrimary)
        }
        if (locationName != null && quest.locationId != null) {
            Text(
                text = "Location: $locationName",
                fontSize = 13.sp,
                color = NavyBlue,
                modifier = Modifier.clickable {
                    onInteraction(QuestsInteraction.LinkedLocationSelected(quest.locationId))
                }
            )
        }
        ObjectivesSection(quest = quest, onInteraction = onInteraction)
        LinksSection(links = links, onInteraction = onInteraction)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (quest.status == QuestStatus.Active) {
                TextButton(
                    onClick = {
                        onInteraction(
                            QuestsInteraction.QuestStatusSelected(quest.id, QuestStatus.Completed)
                        )
                    }
                ) {
                    Text("Complete")
                }
            } else {
                TextButton(
                    onClick = {
                        onInteraction(
                            QuestsInteraction.QuestStatusSelected(quest.id, QuestStatus.Active)
                        )
                    }
                ) {
                    Text("Reopen")
                }
            }
            TextButton(onClick = { onInteraction(QuestsInteraction.EditQuestSelected(quest.id)) }) {
                Text("Edit")
            }
            TextButton(onClick = { onInteraction(QuestsInteraction.DeleteQuestSelected(quest.id)) }) {
                Text("Delete")
            }
        }
    }
}

@Composable
private fun ObjectivesSection(
    quest: Quest,
    onInteraction: (QuestsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Objectives",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (quest.objectives.isEmpty()) {
                Text(
                    text = "No objectives yet.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                quest.objectives.forEach { objective ->
                    Text(
                        text = objective.title,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuestObjectiveStatus.entries.forEach { status ->
                            FilterChip(
                                selected = objective.status == status,
                                onClick = {
                                    onInteraction(
                                        QuestsInteraction.ObjectiveStatusSelected(
                                            questId = quest.id,
                                            objectiveId = objective.id,
                                            status = status,
                                        )
                                    )
                                },
                                label = { Text(status.displayName) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LinksSection(
    links: List<QuestsViewState.QuestLinkRow>,
    onInteraction: (QuestsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                text = "Links",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (links.isEmpty()) {
                Text(
                    text = "No lore, people, or sessions are linked yet.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                links.forEach { link ->
                    val prefix = when (link.kind) {
                        QuestLinkKind.LORE -> "Lore"
                        QuestLinkKind.WORLD_PERSON -> "World person"
                        QuestLinkKind.CAMPAIGN_PERSON -> "Campaign person"
                        QuestLinkKind.SESSION -> "Session"
                    }
                    Text(
                        text = "$prefix: ${link.label}",
                        fontSize = 13.sp,
                        color = if (link.missing) TextSecondary else NavyBlue,
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .then(
                                if (link.missing) {
                                    Modifier
                                } else {
                                    Modifier.clickable {
                                        when (link.kind) {
                                            QuestLinkKind.LORE -> {
                                                onInteraction(QuestsInteraction.LinkedLoreSelected(link.targetId))
                                            }
                                            QuestLinkKind.WORLD_PERSON -> {
                                                onInteraction(
                                                    QuestsInteraction.LinkedPersonSelected(
                                                        personId = link.targetId,
                                                        worldOwned = true,
                                                    )
                                                )
                                            }
                                            QuestLinkKind.CAMPAIGN_PERSON -> {
                                                onInteraction(
                                                    QuestsInteraction.LinkedPersonSelected(
                                                        personId = link.targetId,
                                                        worldOwned = false,
                                                    )
                                                )
                                            }
                                            QuestLinkKind.SESSION -> {
                                                onInteraction(
                                                    QuestsInteraction.LinkedSessionSelected(link.targetId)
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                    )
                }
            }
        }
    }
}
