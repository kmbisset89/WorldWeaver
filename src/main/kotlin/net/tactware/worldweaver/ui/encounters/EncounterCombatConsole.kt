package net.tactware.worldweaver.ui.encounters

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.tactware.worldweaver.domain.EncounterTurnDirection
import net.tactware.worldweaver.ui.maps.BattleMapItemOverlay
import net.tactware.worldweaver.ui.maps.BattleMapTokenOverlay
import net.tactware.worldweaver.ui.maps.BattleMapViewerComposeWidget
import net.tactware.worldweaver.ui.maps.TerrainPaintKind
import net.tactware.worldweaver.ui.theme.NavyBlue
import net.tactware.worldweaver.ui.theme.TextPrimary
import net.tactware.worldweaver.ui.theme.TextSecondary
import ovh.plrapps.mapcompose.ui.state.MapState

@Composable
internal fun EncounterCombatConsole(
    state: EncountersViewState.Running,
    mapState: MapState?,
    onInteraction: (EncountersInteraction) -> Unit,
) {
    val currentName = state.initiativeOrder
        .firstOrNull { it.id == state.currentTurnParticipantId }
        ?.name
        ?: "—"
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CombatBar(state = state, currentName = currentName, onInteraction = onInteraction)
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EncounterCombatTracker(
                encounterId = state.encounter.id,
                initiativeOrder = state.initiativeOrder,
                currentTurnParticipantId = state.currentTurnParticipantId,
                selectedParticipantId = state.selectedParticipantId,
                combatAmount = state.combatAmount,
                availableConditions = state.availableConditions,
                deathSaves = state.deathSaves,
                onInteraction = onInteraction,
                modifier = Modifier.width(300.dp).fillMaxHeight(),
            )
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.battleMap != null && mapState != null) {
                    CombatBoardChrome(state = state, onInteraction = onInteraction)
                    BattleMapViewerComposeWidget(
                        mapState = mapState,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        onMapTapped = { x, y ->
                            onInteraction(EncountersInteraction.MapCellSelected(x, y))
                        },
                        onMarkerClicked = { markerId ->
                            BattleMapTokenOverlay.participantIdFrom(markerId)?.let { participantId ->
                                onInteraction(EncountersInteraction.TokenSelected(participantId))
                            }
                            BattleMapItemOverlay.itemIdFrom(markerId)?.let { itemId ->
                                onInteraction(EncountersInteraction.ItemSelected(itemId))
                            }
                        },
                    )
                } else {
                    Text(
                        text = "Theater of the mind — attach a battle map in setup to place tokens here.",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CombatBar(
    state: EncountersViewState.Running,
    currentName: String,
    onInteraction: (EncountersInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = state.encounter.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Round ${state.encounter.currentRound} · $currentName",
                fontSize = 14.sp,
                color = NavyBlue
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { onInteraction(EncountersInteraction.LibrarySelected) }) {
                Text("Library")
            }
            TextButton(
                onClick = {
                    onInteraction(
                        EncountersInteraction.RollAllInitiativeSelected(
                            encounterId = state.encounter.id,
                            overwriteExisting = false,
                        )
                    )
                }
            ) {
                Text("Roll all")
            }
            TextButton(
                onClick = {
                    onInteraction(
                        EncountersInteraction.TurnAdvanced(
                            state.encounter.id,
                            EncounterTurnDirection.Previous,
                        )
                    )
                }
            ) {
                Text("Prev")
            }
            Button(
                onClick = {
                    onInteraction(
                        EncountersInteraction.TurnAdvanced(
                            state.encounter.id,
                            EncounterTurnDirection.Next,
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyBlue)
            ) {
                Text("Next turn")
            }
            if (state.battleMap != null) {
                TextButton(onClick = { onInteraction(EncountersInteraction.PlayerViewSelected) }) {
                    Text(if (state.playerViewOpen) "Player view open" else "Player view")
                }
            }
            TextButton(
                onClick = {
                    onInteraction(EncountersInteraction.EndEncounterSelected(state.encounter.id))
                }
            ) {
                Text("End")
            }
        }
    }
}

@Composable
private fun CombatBoardChrome(
    state: EncountersViewState.Running,
    onInteraction: (EncountersInteraction) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = state.movementSpeedText,
            onValueChange = { onInteraction(EncountersInteraction.MovementSpeedChanged(it)) },
            label = { Text("Speed") },
            singleLine = true,
            modifier = Modifier.width(100.dp)
        )
        val tokenLabel = when {
            state.selectedTokenName != null && state.unplacedTokenCount > 0 -> {
                "Place ${state.selectedTokenName} · ${state.unplacedTokenCount} unplaced"
            }
            state.selectedTokenName != null -> "Move ${state.selectedTokenName}"
            state.tokens.isNotEmpty() -> "${state.tokens.size} on the board"
            else -> "Click a combatant, then a cell to place them"
        }
        Text(text = tokenLabel, fontSize = 13.sp, color = TextSecondary)
        val measureLabel = when {
            state.measureEnabled && state.measureDistance != null -> {
                val unitName = state.battleMap?.unitName ?: "ft"
                val distance = state.measureDistance
                "${distance.squares} squares · ${distance.unitsLabel()} $unitName"
            }
            state.measureEnabled && state.measureOrigin != null -> "Click a second cell to measure"
            state.measureEnabled -> "Click a cell to start measuring"
            else -> null
        }
        if (measureLabel != null) {
            Text(text = measureLabel, fontSize = 13.sp, color = TextSecondary)
        }
        FilterChip(
            selected = state.measureEnabled,
            onClick = { onInteraction(EncountersInteraction.MeasureToggled) },
            label = { Text("Measure") },
        )
        if (state.measureOrigin != null) {
            TextButton(onClick = { onInteraction(EncountersInteraction.MeasureCleared) }) {
                Text("Clear measure")
            }
        }
        if (state.movementOrigin != null) {
            TextButton(onClick = { onInteraction(EncountersInteraction.MovementCleared) }) {
                Text("Clear range")
            }
        }
        FilterChip(
            selected = state.fogPaintEnabled,
            onClick = { onInteraction(EncountersInteraction.FogToggled) },
            label = { Text("Fog") },
        )
        if (state.fogPaintEnabled) {
            FilterChip(
                selected = !state.fogRevealBrush,
                onClick = { onInteraction(EncountersInteraction.FogHideBrushSelected) },
                label = { Text("Hide") },
            )
            FilterChip(
                selected = state.fogRevealBrush,
                onClick = { onInteraction(EncountersInteraction.FogRevealBrushSelected) },
                label = { Text("Reveal") },
            )
            TextButton(onClick = { onInteraction(EncountersInteraction.FogHideAllSelected) }) {
                Text("Hide all")
            }
            TextButton(onClick = { onInteraction(EncountersInteraction.FogRevealAllSelected) }) {
                Text("Reveal all")
            }
        }
        FilterChip(
            selected = state.terrainPaint == TerrainPaintKind.Blocked,
            onClick = { onInteraction(EncountersInteraction.TerrainPaintSelected(TerrainPaintKind.Blocked)) },
            label = { Text("Blocked") },
        )
        FilterChip(
            selected = state.terrainPaint == TerrainPaintKind.Difficult,
            onClick = { onInteraction(EncountersInteraction.TerrainPaintSelected(TerrainPaintKind.Difficult)) },
            label = { Text("Difficult") },
        )
        FilterChip(
            selected = state.terrainPaint == TerrainPaintKind.Clear,
            onClick = { onInteraction(EncountersInteraction.TerrainPaintSelected(TerrainPaintKind.Clear)) },
            label = { Text("Clear terrain") },
        )
        if (state.terrainPaint != null) {
            Text(
                text = "Click cells to paint ${state.terrainPaint.name.lowercase()} terrain",
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }
        FilterChip(
            selected = state.itemDropEnabled,
            onClick = { onInteraction(EncountersInteraction.ItemDropToggled) },
            label = { Text("Item") },
        )
        if (state.itemDropEnabled) {
            OutlinedTextField(
                value = state.itemNameText,
                onValueChange = { onInteraction(EncountersInteraction.ItemNameChanged(it)) },
                label = { Text("Item name") },
                singleLine = true,
                modifier = Modifier.width(160.dp),
            )
            Text(
                text = if (state.itemNameText.isBlank()) {
                    "Name the item, then click a cell"
                } else {
                    "Click a cell to drop ${state.itemNameText.trim()}"
                },
                fontSize = 13.sp,
                color = TextSecondary,
            )
        }
        if (state.selectedItemName != null) {
            Text(text = state.selectedItemName, fontSize = 13.sp, color = TextSecondary)
            TextButton(onClick = { onInteraction(EncountersInteraction.ItemRemoved) }) {
                Text("Remove")
            }
        }
    }
}
