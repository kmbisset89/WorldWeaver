package net.tactware.worldweaver.ui.dice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.DiceRollResult
import net.tactware.worldweaver.domain.DiceRollSource
import net.tactware.worldweaver.domain.DieSides
import net.tactware.worldweaver.domain.RollMode
import net.tactware.worldweaver.ui.theme.ErrorRed
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.SurfaceCard
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary

@Composable
internal fun DiceScreen(
    viewState: DiceViewState,
    onInteraction: (DiceInteraction) -> Unit,
) {
    LaunchedEffect(Unit) {
        onInteraction(DiceInteraction.ScreenStarted)
    }
    when (viewState) {
        is DiceViewState.Content -> DiceContent(
            state = viewState,
            onInteraction = onInteraction,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DiceContent(
    state: DiceViewState.Content,
    onInteraction: (DiceInteraction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Dice",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Roll digital dice or log the faces on your table",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Color", color = TextSecondary, fontSize = 13.sp)
                DiceColorStyle.entries.forEach { style ->
                    DiceColorSwatchComposeWidget(
                        style = style,
                        selected = state.colorStyle == style,
                        onClick = { onInteraction(DiceInteraction.ColorStyleSelected(style)) },
                    )
                }
            }
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DieSides.entries.forEach { die ->
                    DieFaceComposeWidget(
                        die = die,
                        colorStyle = state.colorStyle,
                        caption = die.label,
                        selected = state.selectedDie == die,
                        size = 56.dp,
                        onClick = { onInteraction(DiceInteraction.DieSelected(die)) },
                    )
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Count", color = TextSecondary, fontSize = 13.sp)
                TextButton(
                    onClick = { onInteraction(DiceInteraction.CountChanged(state.count - 1)) },
                    enabled = state.count > 1,
                ) {
                    Text("−")
                }
                Text(
                    text = state.count.toString(),
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
                TextButton(
                    onClick = { onInteraction(DiceInteraction.CountChanged(state.count + 1)) },
                ) {
                    Text("+")
                }
                OutlinedTextField(
                    value = state.modifierText,
                    onValueChange = { onInteraction(DiceInteraction.ModifierChanged(it)) },
                    label = { Text("Modifier") },
                    singleLine = true,
                    modifier = Modifier.width(120.dp),
                )
                OutlinedTextField(
                    value = state.notationText,
                    onValueChange = { onInteraction(DiceInteraction.NotationChanged(it)) },
                    label = { Text("Notation") },
                    placeholder = { Text("2d6+3") },
                    singleLine = true,
                    modifier = Modifier.width(160.dp),
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RollMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.rollMode == mode,
                        onClick = { onInteraction(DiceInteraction.RollModeSelected(mode)) },
                        enabled = mode == RollMode.Normal || state.advantageEnabled,
                        label = { Text(mode.displayName) },
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DiceEntryMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.entryMode == mode,
                        onClick = { onInteraction(DiceInteraction.EntryModeSelected(mode)) },
                        label = { Text(mode.displayName) },
                    )
                }
            }
        }

        if (state.entryMode == DiceEntryMode.Table) {
            item {
                OutlinedTextField(
                    value = state.tableFacesText,
                    onValueChange = { onInteraction(DiceInteraction.TableFacesChanged(it)) },
                    label = { Text("Table faces") },
                    placeholder = { Text(tableFacesHint(state)) },
                    supportingText = { Text("Comma-separated, one value per die") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            Button(
                onClick = { onInteraction(DiceInteraction.RollSelected) },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
            ) {
                Text(if (state.entryMode == DiceEntryMode.Table) "Log" else "Roll")
            }
        }

        if (state.entryError != null) {
            item {
                Text(
                    text = state.entryError,
                    color = ErrorRed,
                    fontSize = 13.sp,
                )
            }
        }

        if (state.lastResult != null) {
            item {
                LastRollCard(
                    result = state.lastResult,
                    colorStyle = state.colorStyle,
                    rollToken = state.rollToken,
                )
            }
        }

        if (state.history.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "History",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    TextButton(onClick = { onInteraction(DiceInteraction.HistoryCleared) }) {
                        Text("Clear")
                    }
                }
            }
            items(state.history.size) { index ->
                Text(
                    text = formatRoll(state.history[index]),
                    fontSize = 14.sp,
                    color = TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun LastRollCard(
    result: DiceRollResult,
    colorStyle: DiceColorStyle,
    rollToken: Long,
) {
    val die = DieSides.fromSides(result.sides) ?: DieSides.D20
    val animate = result.source == DiceRollSource.Automated
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = if (result.source == DiceRollSource.Manual) "Table roll" else "Last roll",
                fontSize = 13.sp,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                result.faces.forEachIndexed { index, face ->
                    DieFaceComposeWidget(
                        die = die,
                        colorStyle = colorStyle,
                        face = face,
                        size = 76.dp,
                        dimmed = isDiscardedFace(result, face),
                        rollToken = rollToken,
                        staggerIndex = index,
                        animate = animate,
                    )
                }
            }
            Text(
                text = formatRoll(result),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
    }
}

private fun isDiscardedFace(result: DiceRollResult, face: Int): Boolean {
    if (result.mode == RollMode.Normal) {
        return false
    }
    if (result.faces.distinct().size <= 1) {
        return false
    }
    return face != result.keptFaces.single()
}

private fun tableFacesHint(state: DiceViewState.Content): String {
    val expected = when (state.rollMode) {
        RollMode.Normal -> state.count
        RollMode.Advantage, RollMode.Disadvantage -> 2
    }
    return if (expected == 1) "17" else List(expected) { n -> (n + 8).toString() }.joinToString(", ")
}

private fun formatRoll(result: DiceRollResult): String {
    val facesLabel = when (result.mode) {
        RollMode.Normal -> result.keptFaces.joinToString("+")
        RollMode.Advantage -> "${result.faces.joinToString(" / ")}, adv"
        RollMode.Disadvantage -> "${result.faces.joinToString(" / ")}, dis"
    }
    val modifierLabel = when {
        result.modifier > 0 -> "+${result.modifier}"
        result.modifier < 0 -> result.modifier.toString()
        else -> ""
    }
    val sourceLabel = if (result.source == DiceRollSource.Manual) " · table" else ""
    return "${result.total} ($facesLabel$modifierLabel)$sourceLabel"
}
