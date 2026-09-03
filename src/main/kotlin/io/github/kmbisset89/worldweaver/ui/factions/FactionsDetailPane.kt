package io.github.kmbisset89.worldweaver.ui.factions

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
import io.github.kmbisset89.worldweaver.domain.Faction
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun FactionsDetailPane(
    faction: Faction,
    members: List<FactionsViewState.MemberRow>,
    onInteraction: (FactionsInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = faction.name,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        if (faction.description.isNotBlank()) {
            DetailSection("Description", faction.description)
        }
        if (faction.goals.isNotBlank()) {
            DetailSection("Goals", faction.goals)
        }
        if (faction.notes.isNotBlank()) {
            DetailSection("Notes", faction.notes)
        }
        MembersSection(members = members, onInteraction = onInteraction)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = { onInteraction(FactionsInteraction.EditFactionSelected(faction.id)) }) {
                Text("Edit")
            }
            TextButton(onClick = { onInteraction(FactionsInteraction.DeleteFactionSelected(faction.id)) }) {
                Text("Delete")
            }
        }
    }
}

@Composable
private fun MembersSection(
    members: List<FactionsViewState.MemberRow>,
    onInteraction: (FactionsInteraction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Members",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (members.isEmpty()) {
                Text(
                    text = "No members yet. Add people from the Characters screen.",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            } else {
                members.forEach { member ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (member.role.isBlank()) {
                                    member.personName
                                } else {
                                    "${member.personName} · ${member.role}"
                                },
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            if (member.notes.isNotBlank()) {
                                Text(
                                    text = member.notes,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                        TextButton(
                            onClick = {
                                onInteraction(FactionsInteraction.MemberRemoved(member.membershipId))
                            }
                        ) {
                            Text("Remove")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    value: String,
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
                text = value,
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
