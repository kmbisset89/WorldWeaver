package io.github.kmbisset89.worldweaver.ui.maps

import io.github.kmbisset89.worldweaver.domain.BattleMap
import io.github.kmbisset89.worldweaver.domain.BattleMapSituation
import io.github.kmbisset89.worldweaver.domain.Encounter
import io.github.kmbisset89.worldweaver.domain.EncounterDifficulty
import io.github.kmbisset89.worldweaver.domain.EncounterStatus
import io.github.kmbisset89.worldweaver.domain.GridCell
import io.github.kmbisset89.worldweaver.domain.GridDistance
import io.github.kmbisset89.worldweaver.ui.encounters.EncountersViewState
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

internal class BattleMapBoardHudStateTest {
    @Test
    fun fromMapsReturnsNullWithoutSelectedMap() {
        val state = mapsContent(selected = null)
        assertNull(BattleMapBoardHudState.fromMaps(state))
    }

    @Test
    fun clearedToolsSelectMove() {
        val hud = requireNotNull(BattleMapBoardHudState.fromMaps(mapsContent()))
        assertEquals(BattleMapBoardHudState.Tool.Move, hud.activeTool)
        assertTrue(hud.showLayersTool)
        assertTrue(hud.showExport)
        assertTrue(hud.showDelete)
        assertEquals("Cave", hud.mapName)
        assertEquals("20×20 · 5 ft", hud.gridSubtitle)
        assertEquals("Click a cell to show range", hud.statusText)
    }

    @Test
    fun measureToolWinsWhenEnabled() {
        val hud = requireNotNull(
            BattleMapBoardHudState.fromMaps(
                mapsContent(
                    measureEnabled = true,
                    fogPaintEnabled = true,
                ),
            ),
        )
        assertEquals(BattleMapBoardHudState.Tool.Measure, hud.activeTool)
        assertEquals("Click a cell to start measuring", hud.statusText)
    }

    @Test
    fun measureDistanceBecomesStatus() {
        val distance = GridDistance(
            squares = 4,
            units = 20.0,
            path = listOf(GridCell(0, 0), GridCell(1, 0)),
        )
        val hud = requireNotNull(
            BattleMapBoardHudState.fromMaps(
                mapsContent(
                    measureEnabled = true,
                    measureOrigin = GridCell(0, 0),
                    measureDistance = distance,
                ),
            ),
        )
        assertEquals("4 squares · 20 ft", hud.statusText)
        assertTrue(hud.canClearMeasure)
    }

    @Test
    fun fogToolAndRevealStatus() {
        val hud = requireNotNull(
            BattleMapBoardHudState.fromMaps(
                mapsContent(fogPaintEnabled = true, fogRevealBrush = true),
            ),
        )
        assertEquals(BattleMapBoardHudState.Tool.Fog, hud.activeTool)
        assertEquals("Click cells to reveal them on Player View", hud.statusText)
    }

    @Test
    fun terrainToolUsesPaintKind() {
        val hud = requireNotNull(
            BattleMapBoardHudState.fromMaps(
                mapsContent(terrainPaint = TerrainPaintKind.Difficult),
            ),
        )
        assertEquals(BattleMapBoardHudState.Tool.Terrain, hud.activeTool)
        assertEquals("Click cells to paint difficult terrain", hud.statusText)
    }

    @Test
    fun itemDropUsesNamedStatus() {
        val hud = requireNotNull(
            BattleMapBoardHudState.fromMaps(
                mapsContent(itemDropEnabled = true, itemNameText = "Lantern"),
            ),
        )
        assertEquals(BattleMapBoardHudState.Tool.Item, hud.activeTool)
        assertEquals("Click a cell to drop Lantern", hud.statusText)
    }

    @Test
    fun layersPanelTakesPriorityAndHidesPaintTools() {
        val situation = BattleMapSituation(
            id = "sit-1",
            battleMapId = "map-1",
            name = "Fire",
            visible = true,
            sortIndex = 0,
            createdAt = NOW,
            updatedAt = NOW,
        )
        val hud = requireNotNull(
            BattleMapBoardHudState.fromMaps(
                mapsContent(
                    layersPanelOpen = true,
                    measureEnabled = true,
                    situations = listOf(situation),
                ),
            ),
        )
        assertEquals(BattleMapBoardHudState.Tool.Layers, hud.activeTool)
        assertEquals("Toggle situation layers on this map", hud.statusText)
        assertEquals(listOf(situation), hud.situations)
    }

    @Test
    fun fromEncounterOmitsMapOnlyActions() {
        val hud = requireNotNull(BattleMapBoardHudState.fromEncounter(runningEncounter()))
        assertEquals(BattleMapBoardHudState.Tool.Move, hud.activeTool)
        assertFalse(hud.showLayersTool)
        assertFalse(hud.showExport)
        assertFalse(hud.showDelete)
        assertEquals("Click a combatant, then a cell to place them · Click a cell to show range", hud.statusText)
    }

    @Test
    fun fromEncounterReturnsNullWithoutBattleMap() {
        assertNull(BattleMapBoardHudState.fromEncounter(runningEncounter(battleMap = null)))
    }

    @Test
    fun fromEncounterClearedToolsSelectMove() {
        val hud = requireNotNull(
            BattleMapBoardHudState.fromEncounter(
                runningEncounter(fogPaintEnabled = true).let { running ->
                    running.copy(
                        fogPaintEnabled = false,
                        measureEnabled = false,
                        terrainPaint = null,
                        itemDropEnabled = false,
                    )
                },
            ),
        )
        assertEquals(BattleMapBoardHudState.Tool.Move, hud.activeTool)
    }

    private fun mapsContent(
        selected: BattleMap? = sampleMap(),
        measureEnabled: Boolean = false,
        measureOrigin: GridCell? = null,
        measureDistance: GridDistance? = null,
        fogPaintEnabled: Boolean = false,
        fogRevealBrush: Boolean = false,
        terrainPaint: TerrainPaintKind? = null,
        itemDropEnabled: Boolean = false,
        itemNameText: String = "",
        layersPanelOpen: Boolean = false,
        situations: List<BattleMapSituation> = emptyList(),
    ): MapsViewState.Content {
        return MapsViewState.Content(
            worldName = "World",
            campaignName = "Campaign",
            maps = listOfNotNull(selected),
            selectedMap = selected,
            situations = situations,
            situationError = null,
            isSavingSituation = false,
            pendingDelete = null,
            playerViewOpen = false,
            movementSpeedText = "30",
            movementOrigin = null,
            reachableCells = emptyList(),
            measureEnabled = measureEnabled,
            measureOrigin = measureOrigin,
            measureDestination = null,
            measureDistance = measureDistance,
            fogPaintEnabled = fogPaintEnabled,
            fogRevealBrush = fogRevealBrush,
            terrainPaint = terrainPaint,
            itemDropEnabled = itemDropEnabled,
            itemNameText = itemNameText,
            selectedItemId = null,
            selectedItemName = null,
            tokens = emptyList(),
            selectedTokenName = null,
            unplacedTokenCount = 0,
            starterCatalogAvailable = true,
            layersPanelOpen = layersPanelOpen,
        )
    }

    private fun runningEncounter(
        battleMap: BattleMap? = sampleMap(),
        fogPaintEnabled: Boolean = false,
        measureEnabled: Boolean = false,
        terrainPaint: TerrainPaintKind? = null,
        itemDropEnabled: Boolean = false,
    ): EncountersViewState.Running {
        return EncountersViewState.Running(
            worldName = "World",
            campaignName = "Campaign",
            encounter = Encounter(
                id = "enc-1",
                campaignId = "campaign-1",
                name = "Ambush",
                locationId = null,
                battleMapId = battleMap?.id,
                difficulty = EncounterDifficulty.Medium,
                notes = "",
                outcomeNote = "",
                status = EncounterStatus.Active,
                currentRound = 1,
                currentTurnIndex = 0,
                participants = emptyList(),
                createdAt = NOW,
                updatedAt = NOW,
            ),
            locationName = null,
            battleMapName = battleMap?.name,
            battleMap = battleMap,
            initiativeOrder = emptyList(),
            currentTurnParticipantId = null,
            selectedParticipantId = null,
            combatAmount = "",
            availableConditions = emptyList(),
            deathSaves = null,
            tokens = emptyList(),
            selectedTokenName = null,
            unplacedTokenCount = 0,
            movementSpeedText = "30",
            movementOrigin = null,
            reachableCells = emptyList(),
            measureEnabled = measureEnabled,
            measureOrigin = null,
            measureDestination = null,
            measureDistance = null,
            fogPaintEnabled = fogPaintEnabled,
            fogRevealBrush = false,
            terrainPaint = terrainPaint,
            itemDropEnabled = itemDropEnabled,
            itemNameText = "",
            selectedItemId = null,
            selectedItemName = null,
            playerViewOpen = false,
            pendingEnd = null,
        )
    }

    private fun sampleMap(): BattleMap {
        return BattleMap(
            id = "map-1",
            campaignId = "campaign-1",
            name = "Cave",
            originalWidth = 1024,
            originalHeight = 1024,
            tileSizePx = 256,
            minZoom = 0,
            maxZoom = 0,
            createdAt = NOW,
            updatedAt = NOW,
        )
    }

    private companion object {
        val NOW: Instant = Instant.parse("2026-09-05T12:00:00Z")
    }
}
