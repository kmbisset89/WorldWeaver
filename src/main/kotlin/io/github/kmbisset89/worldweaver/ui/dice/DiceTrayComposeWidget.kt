package io.github.kmbisset89.worldweaver.ui.dice

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.DieSides
import io.github.kmbisset89.worldweaver.domain.RollMode
import io.github.kmbisset89.worldweaver.ui.theme.ErrorRed
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.SurfaceCard
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DiceTrayComposeWidget(
    state: DiceViewState.Content,
    onInteraction: (DiceInteraction) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: Dp = 24.dp,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            DiceHeroStageComposeWidget(state = state)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            size = 64.dp,
                            onClick = { onInteraction(DiceInteraction.DieSelected(die)) },
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    DiceColorStyle.entries.forEach { style ->
                        DiceColorSwatchComposeWidget(
                            style = style,
                            selected = state.colorStyle == style,
                            onClick = { onInteraction(DiceInteraction.ColorStyleSelected(style)) },
                        )
                    }
                }
            }
        }

        item {
            ControlsCard(
                state = state,
                onInteraction = onInteraction,
            )
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
                        color = TextPrimary,
                    )
                    TextButton(onClick = { onInteraction(DiceInteraction.HistoryCleared) }) {
                        Text("Clear")
                    }
                }
            }
            items(state.history.size) { index ->
                DiceHistoryRowComposeWidget(
                    result = state.history[index],
                    colorStyle = state.colorStyle,
                )
            }
        }
    }
}

@Composable
private fun ControlsCard(
    state: DiceViewState.Content,
    onInteraction: (DiceInteraction) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.notationText,
                onValueChange = { onInteraction(DiceInteraction.NotationChanged(it)) },
                label = { Text("Notation") },
                placeholder = { Text("2d6+3") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
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
            }
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DiceEntryMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.entryMode == mode,
                        onClick = { onInteraction(DiceInteraction.EntryModeSelected(mode)) },
                        label = { Text(mode.displayName) },
                    )
                }
            }
            if (state.entryMode == DiceEntryMode.Table) {
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
            if (state.entryError != null) {
                Text(
                    text = state.entryError,
                    color = ErrorRed,
                    fontSize = 13.sp,
                )
            }
            Button(
                onClick = { onInteraction(DiceInteraction.RollSelected) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue),
            ) {
                Text(if (state.entryMode == DiceEntryMode.Table) "Log" else "Roll")
            }
        }
    }
}

private fun tableFacesHint(state: DiceViewState.Content): String {
    val expected = when (state.rollMode) {
        RollMode.Normal -> state.count
        RollMode.Advantage, RollMode.Disadvantage -> 2
    }
    return if (expected == 1) "17" else List(expected) { n -> (n + 8).toString() }.joinToString(", ")
}
