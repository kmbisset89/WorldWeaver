package io.github.kmbisset89.worldweaver.ui.maps

import io.github.kmbisset89.worldweaver.domain.BattleMap
import io.github.kmbisset89.worldweaver.domain.BattleMapSituation
import io.github.kmbisset89.worldweaver.domain.GridCell
import io.github.kmbisset89.worldweaver.domain.GridDistance
import io.github.kmbisset89.worldweaver.ui.encounters.EncountersViewState

internal data class BattleMapBoardHudState(
    val mapName: String,
    val gridSubtitle: String,
    val activeTool: Tool,
    val movementSpeedText: String,
    val canClearRange: Boolean,
    val canClearMeasure: Boolean,
    val fogRevealBrush: Boolean,
    val terrainPaint: TerrainPaintKind?,
    val itemNameText: String,
    val selectedItemName: String?,
    val situations: List<BattleMapSituation>,
    val situationError: String?,
    val isSavingSituation: Boolean,
    val showLayersTool: Boolean,
    val showExport: Boolean,
    val showDelete: Boolean,
    val playerViewOpen: Boolean,
    val statusText: String,
) {
    enum class Tool {
        Move,
        Measure,
        Fog,
        Terrain,
        Item,
        Layers,
    }

    companion object {
        fun fromMaps(state: MapsViewState.Content): BattleMapBoardHudState? {
            val selected = state.selectedMap ?: return null
            return BattleMapBoardHudState(
                mapName = selected.name,
                gridSubtitle = gridSubtitle(selected),
                activeTool = toolFor(
                    layersPanelOpen = state.layersPanelOpen,
                    measureEnabled = state.measureEnabled,
                    fogPaintEnabled = state.fogPaintEnabled,
                    terrainPaint = state.terrainPaint,
                    itemDropEnabled = state.itemDropEnabled,
                ),
                movementSpeedText = state.movementSpeedText,
                canClearRange = state.movementOrigin != null,
                canClearMeasure = state.measureOrigin != null,
                fogRevealBrush = state.fogRevealBrush,
                terrainPaint = state.terrainPaint,
                itemNameText = state.itemNameText,
                selectedItemName = state.selectedItemName,
                situations = state.situations,
                situationError = state.situationError,
                isSavingSituation = state.isSavingSituation,
                showLayersTool = true,
                showExport = true,
                showDelete = true,
                playerViewOpen = state.playerViewOpen,
                statusText = statusText(
                    unitName = selected.unitName,
                    unitsPerTile = selected.unitsPerTile,
                    movementSpeedText = state.movementSpeedText,
                    movementOrigin = state.movementOrigin,
                    reachableCount = state.reachableCells.size,
                    measureEnabled = state.measureEnabled,
                    measureOrigin = state.measureOrigin,
                    measureDistance = state.measureDistance,
                    fogPaintEnabled = state.fogPaintEnabled,
                    fogRevealBrush = state.fogRevealBrush,
                    terrainPaint = state.terrainPaint,
                    itemDropEnabled = state.itemDropEnabled,
                    itemNameText = state.itemNameText,
                    selectedTokenName = state.selectedTokenName,
                    unplacedTokenCount = state.unplacedTokenCount,
                    tokenCount = state.tokens.size,
                    layersPanelOpen = state.layersPanelOpen,
                ),
            )
        }

        fun fromEncounter(state: EncountersViewState.Running): BattleMapBoardHudState? {
            val selected = state.battleMap ?: return null
            return BattleMapBoardHudState(
                mapName = selected.name,
                gridSubtitle = gridSubtitle(selected),
                activeTool = toolFor(
                    layersPanelOpen = false,
                    measureEnabled = state.measureEnabled,
                    fogPaintEnabled = state.fogPaintEnabled,
                    terrainPaint = state.terrainPaint,
                    itemDropEnabled = state.itemDropEnabled,
                ),
                movementSpeedText = state.movementSpeedText,
                canClearRange = state.movementOrigin != null,
                canClearMeasure = state.measureOrigin != null,
                fogRevealBrush = state.fogRevealBrush,
                terrainPaint = state.terrainPaint,
                itemNameText = state.itemNameText,
                selectedItemName = state.selectedItemName,
                situations = emptyList(),
                situationError = null,
                isSavingSituation = false,
                showLayersTool = false,
                showExport = false,
                showDelete = false,
                playerViewOpen = state.playerViewOpen,
                statusText = statusText(
                    unitName = selected.unitName,
                    unitsPerTile = selected.unitsPerTile,
                    movementSpeedText = state.movementSpeedText,
                    movementOrigin = state.movementOrigin,
                    reachableCount = state.reachableCells.size,
                    measureEnabled = state.measureEnabled,
                    measureOrigin = state.measureOrigin,
                    measureDistance = state.measureDistance,
                    fogPaintEnabled = state.fogPaintEnabled,
                    fogRevealBrush = state.fogRevealBrush,
                    terrainPaint = state.terrainPaint,
                    itemDropEnabled = state.itemDropEnabled,
                    itemNameText = state.itemNameText,
                    selectedTokenName = state.selectedTokenName,
                    unplacedTokenCount = state.unplacedTokenCount,
                    tokenCount = state.tokens.size,
                    layersPanelOpen = false,
                    emptyTokenHint = "Click a combatant, then a cell to place them",
                ),
            )
        }

        private fun toolFor(
            layersPanelOpen: Boolean,
            measureEnabled: Boolean,
            fogPaintEnabled: Boolean,
            terrainPaint: TerrainPaintKind?,
            itemDropEnabled: Boolean,
        ): Tool {
            return when {
                layersPanelOpen -> Tool.Layers
                measureEnabled -> Tool.Measure
                fogPaintEnabled -> Tool.Fog
                terrainPaint != null -> Tool.Terrain
                itemDropEnabled -> Tool.Item
                else -> Tool.Move
            }
        }

        private fun gridSubtitle(map: BattleMap): String {
            return "${map.columns}×${map.rows} · ${formatUnits(map.unitsPerTile)} ${map.unitName}"
        }

        private fun statusText(
            unitName: String,
            unitsPerTile: Double,
            movementSpeedText: String,
            movementOrigin: GridCell?,
            reachableCount: Int,
            measureEnabled: Boolean,
            measureOrigin: GridCell?,
            measureDistance: GridDistance?,
            fogPaintEnabled: Boolean,
            fogRevealBrush: Boolean,
            terrainPaint: TerrainPaintKind?,
            itemDropEnabled: Boolean,
            itemNameText: String,
            selectedTokenName: String?,
            unplacedTokenCount: Int,
            tokenCount: Int,
            layersPanelOpen: Boolean,
            emptyTokenHint: String? = null,
        ): String {
            if (layersPanelOpen) {
                return "Toggle situation layers on this map"
            }
            if (measureEnabled) {
                if (measureDistance != null) {
                    return "${measureDistance.squares} squares · ${measureDistance.unitsLabel()} $unitName"
                }
                return if (measureOrigin != null) {
                    "Click a second cell to measure"
                } else {
                    "Click a cell to start measuring"
                }
            }
            if (fogPaintEnabled) {
                return if (fogRevealBrush) {
                    "Click cells to reveal them on Player View"
                } else {
                    "Click cells to hide them from Player View"
                }
            }
            if (terrainPaint != null) {
                return "Click cells to paint ${terrainPaint.name.lowercase()} terrain"
            }
            if (itemDropEnabled) {
                return if (itemNameText.isBlank()) {
                    "Name the item, then click a cell"
                } else {
                    "Click a cell to drop ${itemNameText.trim()}"
                }
            }
            val tokenLabel = when {
                selectedTokenName != null && unplacedTokenCount > 0 -> {
                    "Place $selectedTokenName · $unplacedTokenCount unplaced"
                }
                selectedTokenName != null -> "Move $selectedTokenName"
                tokenCount > 0 -> "$tokenCount on the board"
                emptyTokenHint != null -> emptyTokenHint
                else -> null
            }
            val rangeLabel = when {
                movementOrigin == null -> "Click a cell to show range"
                else -> {
                    val squares = if (unitsPerTile > 0.0) {
                        (movementSpeedText.toIntOrNull() ?: 0) / unitsPerTile
                    } else {
                        0.0
                    }
                    val squareCount = kotlin.math.floor(squares).toInt()
                    "$reachableCount cells · $squareCount squares · $movementSpeedText $unitName"
                }
            }
            return if (tokenLabel != null) {
                "$tokenLabel · $rangeLabel"
            } else {
                rangeLabel
            }
        }

        private fun formatUnits(value: Double): String {
            return if (value == value.toLong().toDouble()) {
                value.toLong().toString()
            } else {
                value.toString()
            }
        }
    }
}
