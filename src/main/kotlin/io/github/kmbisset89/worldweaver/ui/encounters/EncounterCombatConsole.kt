package io.github.kmbisset89.worldweaver.ui.encounters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.kmbisset89.worldweaver.domain.EncounterTurnDirection
import io.github.kmbisset89.worldweaver.ui.maps.BattleMapBoardHudComposeWidget
import io.github.kmbisset89.worldweaver.ui.maps.BattleMapBoardHudInteraction
import io.github.kmbisset89.worldweaver.ui.maps.BattleMapBoardHudState
import io.github.kmbisset89.worldweaver.ui.maps.BattleMapItemOverlay
import io.github.kmbisset89.worldweaver.ui.maps.BattleMapTokenOverlay
import io.github.kmbisset89.worldweaver.ui.maps.TerrainPaintKind
import io.github.kmbisset89.worldweaver.ui.theme.NavyBlue
import io.github.kmbisset89.worldweaver.ui.theme.TextPrimary
import io.github.kmbisset89.worldweaver.ui.theme.TextSecondary
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
            ) {
                if (state.battleMap != null && mapState != null) {
                    val hudState = BattleMapBoardHudState.fromEncounter(state)
                    if (hudState != null) {
                        BattleMapBoardHudComposeWidget(
                            state = hudState,
                            mapState = mapState,
                            onInteraction = { interaction ->
                                dispatchEncounterHud(interaction, onInteraction)
                            },
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
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
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

private fun dispatchEncounterHud(
    interaction: BattleMapBoardHudInteraction,
    onInteraction: (EncountersInteraction) -> Unit,
) {
    when (interaction) {
        BattleMapBoardHudInteraction.MoveToolSelected ->
            onInteraction(EncountersInteraction.BoardToolCleared)
        BattleMapBoardHudInteraction.MeasureToolSelected ->
            onInteraction(EncountersInteraction.MeasureToggled)
        BattleMapBoardHudInteraction.FogToolSelected ->
            onInteraction(EncountersInteraction.FogToggled)
        BattleMapBoardHudInteraction.TerrainToolSelected ->
            onInteraction(EncountersInteraction.TerrainPaintSelected(TerrainPaintKind.Blocked))
        BattleMapBoardHudInteraction.ItemToolSelected ->
            onInteraction(EncountersInteraction.ItemDropToggled)
        BattleMapBoardHudInteraction.LayersToolSelected,
        BattleMapBoardHudInteraction.AddLayerSelected,
        BattleMapBoardHudInteraction.ExportSelected,
        BattleMapBoardHudInteraction.DeleteSelected,
        is BattleMapBoardHudInteraction.SituationToggled,
        is BattleMapBoardHudInteraction.SituationDeleteSelected,
        -> Unit
        is BattleMapBoardHudInteraction.MovementSpeedChanged ->
            onInteraction(EncountersInteraction.MovementSpeedChanged(interaction.speed))
        BattleMapBoardHudInteraction.MovementCleared ->
            onInteraction(EncountersInteraction.MovementCleared)
        BattleMapBoardHudInteraction.MeasureCleared ->
            onInteraction(EncountersInteraction.MeasureCleared)
        BattleMapBoardHudInteraction.FogHideBrushSelected ->
            onInteraction(EncountersInteraction.FogHideBrushSelected)
        BattleMapBoardHudInteraction.FogRevealBrushSelected ->
            onInteraction(EncountersInteraction.FogRevealBrushSelected)
        BattleMapBoardHudInteraction.FogHideAllSelected ->
            onInteraction(EncountersInteraction.FogHideAllSelected)
        BattleMapBoardHudInteraction.FogRevealAllSelected ->
            onInteraction(EncountersInteraction.FogRevealAllSelected)
        is BattleMapBoardHudInteraction.TerrainPaintSelected ->
            onInteraction(EncountersInteraction.TerrainPaintSelected(interaction.kind))
        is BattleMapBoardHudInteraction.ItemNameChanged ->
            onInteraction(EncountersInteraction.ItemNameChanged(interaction.name))
        BattleMapBoardHudInteraction.ItemRemoved ->
            onInteraction(EncountersInteraction.ItemRemoved)
        BattleMapBoardHudInteraction.PlayerViewSelected ->
            onInteraction(EncountersInteraction.PlayerViewSelected)
    }
}
