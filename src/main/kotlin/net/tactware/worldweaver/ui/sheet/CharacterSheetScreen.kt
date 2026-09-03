package net.tactware.worldweaver.ui.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.DeathSaves
import net.tactware.worldweaver.ui.characters.PersonAvatarComposeWidget
import net.tactware.worldweaver.ui.components.FeatureErrorState
import net.tactware.worldweaver.ui.theme.ErrorRed
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

@Composable
internal fun CharacterSheetScreen(
    viewState: CharacterSheetViewState,
    onInteraction: (CharacterSheetInteraction) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        CharacterSheetBody(
            viewState = viewState,
            onInteraction = onInteraction,
        )
    }
}

@Composable
private fun CharacterSheetBody(
    viewState: CharacterSheetViewState,
    onInteraction: (CharacterSheetInteraction) -> Unit,
) {
    when (viewState) {
        CharacterSheetViewState.Hidden -> Unit
        CharacterSheetViewState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is CharacterSheetViewState.Error -> {
            Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                FeatureErrorState(
                    message = viewState.message,
                    canRetry = viewState.canRetry,
                    onRetry = { onInteraction(CharacterSheetInteraction.RetrySelected) },
                )
            }
        }
        is CharacterSheetViewState.Unavailable -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = viewState.message,
                    fontSize = 16.sp,
                    color = TextPrimary,
                )
                OutlinedButton(
                    onClick = { onInteraction(CharacterSheetInteraction.SheetDismissed) },
                ) {
                    Text("Close")
                }
            }
        }
        is CharacterSheetViewState.Content -> {
            CharacterSheetContent(
                state = viewState,
                onInteraction = onInteraction,
            )
        }
    }
}

@Composable
private fun CharacterSheetContent(
    state: CharacterSheetViewState.Content,
    onInteraction: (CharacterSheetInteraction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SheetHeader(state = state)
            AbilityStrip(
                scores = state.abilityScores,
                proficiencyBonus = state.proficiencyBonus,
                initiativeBonus = state.initiativeBonus,
            )
            CombatVitals(
                vitals = state.vitals,
                onInteraction = onInteraction,
            )
            when (val body = state.body) {
                is CharacterSheetViewState.SheetBody.FifthEdition -> {
                    FifthEditionBody(body = body)
                }
                is CharacterSheetViewState.SheetBody.Pathfinder -> {
                    PathfinderBody(body = body)
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            OutlinedButton(
                onClick = { onInteraction(CharacterSheetInteraction.SheetDismissed) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ),
            ) {
                Text("Close")
            }
            Button(
                onClick = { onInteraction(CharacterSheetInteraction.EditSelected) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text("Edit sheet")
            }
        }
    }
}

@Composable
private fun SheetHeader(state: CharacterSheetViewState.Content) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PersonAvatarComposeWidget(
            name = state.name,
            avatarPath = state.avatarPath,
            size = 72.dp,
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = state.name,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = state.identityLine,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        SystemBadge(label = state.systemBadge)
    }
}

@Composable
private fun SystemBadge(label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun AbilityStrip(
    scores: List<CharacterSheetViewState.AbilityScoreTile>,
    proficiencyBonus: Int?,
    initiativeBonus: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        scores.forEach { tile ->
            AbilityTile(tile = tile, modifier = Modifier.weight(1f))
        }
        DerivedTile(
            label = "INIT",
            value = signed(initiativeBonus),
            modifier = Modifier.weight(1f),
        )
        if (proficiencyBonus != null) {
            DerivedTile(
                label = "PROF",
                value = signed(proficiencyBonus),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AbilityTile(
    tile: CharacterSheetViewState.AbilityScoreTile,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = tile.label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
            Text(
                text = tile.score.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Text(
                text = signed(tile.modifier),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyBlue,
            )
        }
    }
}

@Composable
private fun DerivedTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyBlue,
            )
        }
    }
}

@Composable
private fun CombatVitals(
    vitals: CharacterSheetViewState.Vitals,
    onInteraction: (CharacterSheetInteraction) -> Unit,
) {
    val fraction = if (vitals.maxHitPoints <= 0) {
        0f
    } else {
        vitals.hitPoints.toFloat() / vitals.maxHitPoints.toFloat()
    }
    val bloodied = fraction <= 0.5f && vitals.hitPoints > 0
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "HP ${vitals.hitPoints}/${vitals.maxHitPoints}" +
                        if (vitals.temporaryHitPoints > 0) {
                            "  ·  Temp ${vitals.temporaryHitPoints}"
                        } else {
                            ""
                        },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (bloodied || vitals.hitPoints <= 0) ErrorRed else TextPrimary,
                )
                Text(
                    text = "AC ${vitals.armorClass}  ·  Speed ${vitals.speed}",
                    fontSize = 14.sp,
                    color = TextPrimary,
                )
            }
            LinearProgressIndicator(
                progress = { fraction.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = if (bloodied || vitals.hitPoints <= 0) ErrorRed else NavyBlue,
                trackColor = TextSecondary.copy(alpha = 0.2f),
            )
            if (vitals.usesOverlayHitPoints) {
                Text(
                    text = "Current HP is the campaign overlay. Combat uses the tracker’s snapshot.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
            } else {
                Text(
                    text = "Sheet HP — mid-round damage stays on the combat tracker.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
            }
            vitals.fifthEdition?.let { fifth ->
                DeathSaveBoxes(
                    deathSaves = fifth.deathSaves,
                    writable = fifth.writable,
                    onInteraction = onInteraction,
                )
            }
            vitals.pathfinder?.let { pf ->
                PathfinderConditionRow(
                    vitals = pf,
                    onInteraction = onInteraction,
                )
            }
        }
    }
}

@Composable
private fun DeathSaveBoxes(
    deathSaves: DeathSaves,
    writable: Boolean,
    onInteraction: (CharacterSheetInteraction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Death saves", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CountBoxes(
                label = "Successes",
                selected = deathSaves.successes,
                enabled = writable,
                onSelected = { count ->
                    onInteraction(CharacterSheetInteraction.DeathSaveSuccessesSelected(count))
                },
            )
            CountBoxes(
                label = "Failures",
                selected = deathSaves.failures,
                enabled = writable,
                onSelected = { count ->
                    onInteraction(CharacterSheetInteraction.DeathSaveFailuresSelected(count))
                },
            )
        }
    }
}

@Composable
private fun PathfinderConditionRow(
    vitals: CharacterSheetViewState.PathfinderVitals,
    onInteraction: (CharacterSheetInteraction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "Perception ${signed(vitals.perception)}",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            CountBoxes(
                label = "Dying",
                selected = vitals.dying.coerceIn(0, DeathSaves.LIMIT + 1),
                limit = 4,
                enabled = vitals.writable,
                onSelected = { count ->
                    onInteraction(CharacterSheetInteraction.DyingSelected(count))
                },
            )
            CountBoxes(
                label = "Wounded",
                selected = vitals.wounded.coerceIn(0, DeathSaves.LIMIT + 1),
                limit = 4,
                enabled = vitals.writable,
                onSelected = { count ->
                    onInteraction(CharacterSheetInteraction.WoundedSelected(count))
                },
            )
        }
    }
}

@Composable
private fun CountBoxes(
    label: String,
    selected: Int,
    enabled: Boolean,
    onSelected: (Int) -> Unit,
    limit: Int = DeathSaves.LIMIT,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (0..limit).forEach { count ->
                FilterChip(
                    selected = selected == count,
                    enabled = enabled,
                    onClick = { onSelected(count) },
                    label = { Text(count.toString()) },
                )
            }
        }
    }
}

@Composable
private fun FifthEditionBody(body: CharacterSheetViewState.SheetBody.FifthEdition) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetCard(title = "Skills", caption = body.skillsCaption) {
                body.skills.forEach { skill ->
                    val mark = if (skill.proficient) "prof" else skill.ability
                    SheetLine(
                        primary = skill.name,
                        secondary = "$mark ${signed(skill.modifier)}",
                    )
                }
            }
            if (body.concentratingSpell.isNotBlank() || body.spellSlots.isNotEmpty()) {
                SheetCard(title = "Concentration and slots") {
                    if (body.concentratingSpell.isNotBlank()) {
                        SheetLine(primary = "Concentrating", secondary = body.concentratingSpell)
                    }
                    if (body.spellSlots.isEmpty()) {
                        EmptyLine("No spell slots stored.")
                    } else {
                        body.spellSlots.forEach { slot ->
                            SheetLine(
                                primary = "Level ${slot.level}",
                                secondary = "${slot.remaining}/${slot.maximum}",
                            )
                        }
                    }
                }
            }
            SheetCard(title = "Features") {
                if (body.features.isEmpty()) {
                    EmptyLine("No features yet.")
                } else {
                    body.features.forEach { feature ->
                        NamedBlock(item = feature)
                    }
                }
            }
            if (body.notes.isNotBlank()) {
                SheetCard(title = "Notes") {
                    Text(text = body.notes, fontSize = 13.sp, color = TextPrimary)
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetCard(title = "Spells") {
                if (body.spells.isEmpty()) {
                    EmptyLine("No spells yet.")
                } else {
                    body.spells.forEach { group ->
                        Text(
                            text = group.heading,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        group.spells.forEach { spell ->
                            val mark = if (spell.prepared) "prepared" else "known"
                            SheetLine(primary = spell.name, secondary = mark)
                        }
                    }
                }
            }
            SheetCard(title = "Inventory") {
                if (body.items.isEmpty()) {
                    EmptyLine("No items yet.")
                } else {
                    body.items.forEach { item ->
                        val extra = if (item.notes.isBlank()) "" else " — ${item.notes}"
                        SheetLine(
                            primary = item.name,
                            secondary = "×${item.quantity}$extra",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PathfinderBody(body: CharacterSheetViewState.SheetBody.Pathfinder) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetCard(title = "Skills") {
                if (body.skills.isEmpty()) {
                    EmptyLine("No skills yet.")
                } else {
                    body.skills.forEach { skill ->
                        SheetLine(primary = skill.name, secondary = skill.rank)
                    }
                }
            }
            SheetCard(title = "Feats") {
                if (body.feats.isEmpty()) {
                    EmptyLine("No feats yet.")
                } else {
                    body.feats.forEach { feat ->
                        NamedBlock(item = feat)
                    }
                }
            }
            if (body.notes.isNotBlank()) {
                SheetCard(title = "Notes") {
                    Text(text = body.notes, fontSize = 13.sp, color = TextPrimary)
                }
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SheetCard(title = "Spells") {
                if (body.spells.isEmpty()) {
                    EmptyLine("No spells yet.")
                } else {
                    body.spells.forEach { group ->
                        Text(
                            text = group.heading,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        group.spells.forEach { spell ->
                            val mark = if (spell.prepared) "prepared" else "known"
                            SheetLine(primary = spell.name, secondary = mark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetCard(
    title: String,
    caption: String? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            if (caption != null) {
                Text(text = caption, fontSize = 12.sp, color = TextSecondary)
            }
            content()
        }
    }
}

@Composable
private fun SheetLine(
    primary: String,
    secondary: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = primary, fontSize = 13.sp, color = TextPrimary, modifier = Modifier.weight(1f))
        Text(text = secondary, fontSize = 13.sp, color = TextSecondary)
    }
}

@Composable
private fun NamedBlock(item: CharacterSheetViewState.NamedText) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = item.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        if (item.description.isNotBlank()) {
            Text(text = item.description, fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun EmptyLine(text: String) {
    Text(text = text, fontSize = 13.sp, color = TextSecondary)
}

private fun signed(value: Int): String {
    return if (value >= 0) "+$value" else value.toString()
}
