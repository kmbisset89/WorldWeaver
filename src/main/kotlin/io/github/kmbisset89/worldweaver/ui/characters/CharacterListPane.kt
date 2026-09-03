package io.github.kmbisset89.worldweaver.ui.characters

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.PersonKind
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@Composable
internal fun CharacterListPane(
    people: List<CharactersViewState.PersonRow>,
    selectedKey: CharactersViewState.PersonKey?,
    searchQuery: String,
    kindFilter: PersonKind?,
    membershipFilter: PersonMembership?,
    onInteraction: (CharactersInteraction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(320.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { onInteraction(CharactersInteraction.SearchQueryChanged(it)) },
            label = { Text("Search people") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Text("Type", fontSize = 12.sp, color = TextSecondary)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FilterChip(
                selected = kindFilter == null,
                onClick = { onInteraction(CharactersInteraction.KindFilterSelected(null)) },
                label = { Text("All types") },
            )
            PersonKind.entries.forEach { kind ->
                FilterChip(
                    selected = kindFilter == kind,
                    onClick = { onInteraction(CharactersInteraction.KindFilterSelected(kind)) },
                    label = { Text(kind.displayName) },
                )
            }
        }
        Text("Membership", fontSize = 12.sp, color = TextSecondary)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            FilterChip(
                selected = membershipFilter == null,
                onClick = { onInteraction(CharactersInteraction.MembershipFilterSelected(null)) },
                label = { Text("All membership") },
            )
            PersonMembership.entries.forEach { membership ->
                FilterChip(
                    selected = membershipFilter == membership,
                    onClick = {
                        onInteraction(CharactersInteraction.MembershipFilterSelected(membership))
                    },
                    label = { Text(membership.displayName) },
                )
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (people.isEmpty()) {
                item {
                    Text(
                        text = "No people match these filters.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                items(
                    people,
                    key = { row -> "${row.key.membership.name}:${row.key.id}" },
                ) { row ->
                    PersonRow(
                        row = row,
                        isSelected = row.key == selectedKey,
                        onInteraction = onInteraction,
                    )
                }
            }
        }
    }
}

@Composable
private fun PersonRow(
    row: CharactersViewState.PersonRow,
    isSelected: Boolean,
    onInteraction: (CharactersInteraction) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onInteraction(CharactersInteraction.PersonSelected(row.key)) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) {
                        Modifier.background(NavyBlue.copy(alpha = 0.08f))
                    } else {
                        Modifier
                    }
                )
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PersonAvatarComposeWidget(
                name = row.name,
                avatarPath = row.avatarPath,
                size = 36.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = row.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = row.subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
