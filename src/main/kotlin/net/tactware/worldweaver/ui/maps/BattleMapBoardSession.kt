package net.tactware.worldweaver.ui.maps

import kotlinx.coroutines.launch
import net.tactware.worldweaver.core.AppCoroutineScope
import net.tactware.worldweaver.domain.BattleMap
import net.tactware.worldweaver.domain.BattleMapGridGeometry
import net.tactware.worldweaver.domain.BattleMapSituation
import net.tactware.worldweaver.domain.CalculateGridDistanceUseCase
import net.tactware.worldweaver.domain.CalculateReachableCellsUseCase
import net.tactware.worldweaver.domain.GridDistance
import net.tactware.worldweaver.domain.Encounter
import net.tactware.worldweaver.domain.EncounterParticipant
import net.tactware.worldweaver.domain.EncounterParticipantSource
import net.tactware.worldweaver.domain.EncounterParticipantVisibilityResolver
import net.tactware.worldweaver.domain.GridCell
import net.tactware.worldweaver.domain.OccupiedBoardCellsCalculator
import net.tactware.worldweaver.domain.CreatureSizeResolver
import net.tactware.worldweaver.domain.PeopleSnapshot
import net.tactware.worldweaver.domain.PersonAvatarFileStore
import net.tactware.worldweaver.domain.PersonRef
import net.tactware.worldweaver.domain.BattleMapFogEdit
import net.tactware.worldweaver.domain.BattleMapTerrainEdit
import net.tactware.worldweaver.domain.DeleteBattleMapItemUseCase
import net.tactware.worldweaver.domain.PlaceBattleMapItemUseCase
import net.tactware.worldweaver.domain.PlaceEncounterTokenUseCase
import net.tactware.worldweaver.domain.UpdateBattleMapFogUseCase
import net.tactware.worldweaver.domain.UpdateBattleMapTerrainUseCase
import ovh.plrapps.mapcompose.ui.state.MapState

internal class BattleMapBoardSession(
    private val appScope: AppCoroutineScope,
    private val mapStateFactory: BattleMapMapStateFactory,
    private val movementOverlay: BattleMapMovementOverlay,
    private val measureOverlay: BattleMapMeasureOverlay,
    private val tokenOverlay: BattleMapTokenOverlay,
    private val itemOverlay: BattleMapItemOverlay,
    private val calculateReachableCells: CalculateReachableCellsUseCase,
    private val calculateGridDistance: CalculateGridDistanceUseCase,
    private val placeEncounterToken: PlaceEncounterTokenUseCase,
    private val updateBattleMapFog: UpdateBattleMapFogUseCase,
    private val updateBattleMapTerrain: UpdateBattleMapTerrainUseCase,
    private val placeBattleMapItem: PlaceBattleMapItemUseCase,
    private val deleteBattleMapItem: DeleteBattleMapItemUseCase,
    private val avatarFileStore: PersonAvatarFileStore,
    private val visibilityResolver: EncounterParticipantVisibilityResolver =
        EncounterParticipantVisibilityResolver(),
) {
    private var dmBinding: BoundViewer? = null
    private var playerBinding: BoundViewer? = null
    val mapState: MapState?
        get() = dmBinding?.mapState
    val playerMapState: MapState?
        get() = playerBinding?.mapState

    private var battleMap: BattleMap? = null
    private var situations: List<BattleMapSituation> = emptyList()
    private var encounter: Encounter? = null
    private var people: PeopleSnapshot = PeopleSnapshot(emptyList(), emptyList())
    private var selectedTokenParticipantId: String? = null
    private var playerViewOpen = false
    private var movementSpeedText = DEFAULT_MOVEMENT_SPEED
    private var movementOrigin: GridCell? = null
    private var reachableCells: List<GridCell> = emptyList()
    private var measureEnabled = false
    private var measureOrigin: GridCell? = null
    private var measureDestination: GridCell? = null
    private var measureDistance: GridDistance? = null
    private var fogPaintEnabled = false
    private var fogRevealBrush = false
    private var terrainPaint: TerrainPaintKind? = null
    private var itemDropEnabled = false
    private var itemNameText = ""
    private var selectedItemId: String? = null

    fun snapshot(): BattleMapBoardSnapshot {
        return BattleMapBoardSnapshot(
            tokens = boardTokens(),
            selectedTokenParticipantId = selectedTokenParticipantId,
            selectedTokenName = encounter?.participants
                ?.firstOrNull { it.id == selectedTokenParticipantId }
                ?.name,
            unplacedTokenCount = encounter?.participants?.count { it.boardCell() == null } ?: 0,
            movementSpeedText = movementSpeedText,
            movementOrigin = movementOrigin,
            reachableCells = reachableCells,
            measureEnabled = measureEnabled,
            measureOrigin = measureOrigin,
            measureDestination = measureDestination,
            measureDistance = measureDistance,
            fogPaintEnabled = fogPaintEnabled,
            fogRevealBrush = fogRevealBrush,
            terrainPaint = terrainPaint,
            itemDropEnabled = itemDropEnabled,
            itemNameText = itemNameText,
            selectedItemId = selectedItemId,
            selectedItemName = battleMap?.items?.firstOrNull { it.id == selectedItemId }?.name,
            playerViewOpen = playerViewOpen,
        )
    }

    fun sync(
        battleMap: BattleMap?,
        situations: List<BattleMapSituation>,
        encounter: Encounter?,
        people: PeopleSnapshot,
    ): BattleMapBoardSnapshot {
        val previousMapId = this.battleMap?.id
        this.battleMap = battleMap
        this.situations = situations
        this.encounter = encounter
        this.people = people
        if (previousMapId != battleMap?.id) {
            clearMovement(refreshOverlays = false)
            clearMeasure(refreshOverlays = false)
            fogPaintEnabled = false
            terrainPaint = null
            itemDropEnabled = false
            selectedItemId = null
            selectedTokenParticipantId = null
        }
        syncSelectedToken()
        if (battleMap == null) {
            shutdown()
            return snapshot()
        }
        bindViewers(battleMap, situations)
        return snapshot()
    }

    fun selectToken(participantId: String): BattleMapBoardSnapshot {
        val map = battleMap ?: return snapshot()
        val participant = encounter?.participants?.firstOrNull { it.id == participantId }
            ?: return snapshot()
        selectedTokenParticipantId = participantId
        applyTokenMovement(map, participant)
        return snapshot()
    }

    fun selectParticipant(participantId: String?): BattleMapBoardSnapshot {
        if (participantId == null) {
            selectedTokenParticipantId = null
            bindMapOverlays()
            return snapshot()
        }
        return selectToken(participantId)
    }

    fun selectCell(x: Double, y: Double): BattleMapBoardSnapshot {
        val map = battleMap ?: return snapshot()
        val geometry = geometryFor(map)
        val cell = geometry.cellAtNormalized(x, y) ?: return snapshot()
        if (fogPaintEnabled) {
            val edit = if (fogRevealBrush) {
                BattleMapFogEdit.Reveal(setOf(cell))
            } else {
                BattleMapFogEdit.Hide(setOf(cell))
            }
            appScope.scope.launch {
                updateBattleMapFog(map.id, edit)
            }
            return snapshot()
        }
        val terrain = terrainPaint
        if (terrain != null) {
            val edit = when (terrain) {
                TerrainPaintKind.Blocked -> BattleMapTerrainEdit.SetBlocked(setOf(cell))
                TerrainPaintKind.Difficult -> BattleMapTerrainEdit.SetDifficult(setOf(cell))
                TerrainPaintKind.Clear -> BattleMapTerrainEdit.Clear(setOf(cell))
            }
            appScope.scope.launch {
                updateBattleMapTerrain(map.id, edit)
            }
            return snapshot()
        }
        if (itemDropEnabled) {
            appScope.scope.launch {
                val result = placeBattleMapItem(map.id, itemNameText, cell)
                if (result is PlaceBattleMapItemUseCase.Result.Placed) {
                    selectedItemId = result.item.id
                }
            }
            return snapshot()
        }
        if (measureEnabled) {
            applyMeasureClick(map, cell)
            return snapshot()
        }
        val currentEncounter = encounter
        val participantId = selectedTokenParticipantId
            ?: currentEncounter?.let { currentTurnParticipant(it)?.id }
        if (currentEncounter != null && participantId != null) {
            val participant = currentEncounter.participants.firstOrNull { it.id == participantId }
            val span = participant?.let { CreatureSizeResolver().resolve(it, people).span } ?: 1
            appScope.scope.launch {
                val result = placeEncounterToken(
                    encounterId = currentEncounter.id,
                    participantId = participantId,
                    cell = cell,
                    columns = map.columns,
                    rows = map.rows,
                    span = span,
                )
                if (result is PlaceEncounterTokenUseCase.Result.Placed) {
                    selectedTokenParticipantId = participantId
                    val latestMap = battleMap ?: return@launch
                    applyTokenMovement(latestMap, result.participant)
                }
            }
            return snapshot()
        }
        movementOrigin = cell
        recomputeMovement(map)
        bindMapOverlays()
        return snapshot()
    }

    fun changeMovementSpeed(speed: String): BattleMapBoardSnapshot {
        movementSpeedText = speed.filter { it.isDigit() }.take(4)
        val map = battleMap
        if (map != null && movementOrigin != null) {
            recomputeMovement(map)
            bindMapOverlays()
        }
        return snapshot()
    }

    fun clearMovement(): BattleMapBoardSnapshot {
        clearMovement(refreshOverlays = true)
        return snapshot()
    }

    fun toggleMeasure(): BattleMapBoardSnapshot {
        measureEnabled = !measureEnabled
        if (measureEnabled) {
            fogPaintEnabled = false
            terrainPaint = null
            itemDropEnabled = false
        }
        if (!measureEnabled) {
            clearMeasure(refreshOverlays = true)
        } else {
            bindMapOverlays()
        }
        return snapshot()
    }

    fun clearMeasure(): BattleMapBoardSnapshot {
        clearMeasure(refreshOverlays = true)
        return snapshot()
    }

    fun toggleFogPaint(): BattleMapBoardSnapshot {
        fogPaintEnabled = !fogPaintEnabled
        if (fogPaintEnabled) {
            terrainPaint = null
            itemDropEnabled = false
            measureEnabled = false
            clearMeasure(refreshOverlays = false)
        }
        bindMapOverlays()
        return snapshot()
    }

    fun setFogRevealBrush(reveal: Boolean): BattleMapBoardSnapshot {
        fogRevealBrush = reveal
        fogPaintEnabled = true
        terrainPaint = null
        itemDropEnabled = false
        measureEnabled = false
        clearMeasure(refreshOverlays = false)
        bindMapOverlays()
        return snapshot()
    }

    fun setTerrainPaint(kind: TerrainPaintKind?): BattleMapBoardSnapshot {
        terrainPaint = if (terrainPaint == kind) null else kind
        if (terrainPaint != null) {
            fogPaintEnabled = false
            itemDropEnabled = false
            measureEnabled = false
            clearMeasure(refreshOverlays = false)
        }
        bindMapOverlays()
        return snapshot()
    }

    fun toggleItemDrop(): BattleMapBoardSnapshot {
        itemDropEnabled = !itemDropEnabled
        if (itemDropEnabled) {
            fogPaintEnabled = false
            terrainPaint = null
            measureEnabled = false
            clearMeasure(refreshOverlays = false)
        }
        bindMapOverlays()
        return snapshot()
    }

    fun changeItemName(name: String): BattleMapBoardSnapshot {
        itemNameText = name.take(80)
        return snapshot()
    }

    fun selectItem(itemId: String): BattleMapBoardSnapshot {
        val map = battleMap ?: return snapshot()
        if (map.items.none { it.id == itemId }) {
            return snapshot()
        }
        selectedItemId = itemId
        bindMapOverlays()
        return snapshot()
    }

    fun removeSelectedItem(): BattleMapBoardSnapshot {
        val map = battleMap ?: return snapshot()
        val itemId = selectedItemId ?: return snapshot()
        appScope.scope.launch {
            deleteBattleMapItem(map.id, itemId)
            selectedItemId = null
        }
        return snapshot()
    }

    fun applyFogEdit(edit: BattleMapFogEdit): BattleMapBoardSnapshot {
        val map = battleMap ?: return snapshot()
        appScope.scope.launch {
            updateBattleMapFog(map.id, edit)
        }
        return snapshot()
    }

    fun openPlayerView(walkSpeed: Int?): BattleMapBoardSnapshot {
        playerViewOpen = true
        if (walkSpeed != null && walkSpeed > 0) {
            movementSpeedText = walkSpeed.toString()
        }
        val map = battleMap
        if (map != null) {
            if (movementOrigin != null) {
                recomputeMovement(map)
            }
            bindViewers(map, situations)
        }
        return snapshot()
    }

    fun closePlayerView(): BattleMapBoardSnapshot {
        playerViewOpen = false
        shutdownPlayerBinding()
        return snapshot()
    }

    fun shutdown() {
        shutdownBinding(dmBinding)
        dmBinding = null
        shutdownPlayerBinding()
    }

    private fun clearMovement(refreshOverlays: Boolean) {
        movementOrigin = null
        reachableCells = emptyList()
        dmBinding?.let { movementOverlay.clear(it.mapState) }
        playerBinding?.let { movementOverlay.clear(it.mapState) }
        if (refreshOverlays) {
            bindMapOverlays()
        }
    }

    private fun applyMeasureClick(battleMap: BattleMap, cell: GridCell) {
        if (cell in battleMap.blockedCells) {
            return
        }
        if (measureOrigin == null || measureDestination != null) {
            measureOrigin = cell
            measureDestination = null
            measureDistance = null
        } else {
            measureDestination = cell
            measureDistance = calculateGridDistance(
                from = measureOrigin ?: cell,
                to = cell,
                unitsPerTile = battleMap.unitsPerTile,
            )
        }
        bindMapOverlays()
    }

    private fun clearMeasure(refreshOverlays: Boolean) {
        measureOrigin = null
        measureDestination = null
        measureDistance = null
        dmBinding?.let { measureOverlay.clear(it.mapState) }
        if (refreshOverlays) {
            bindMapOverlays()
        }
    }

    private fun syncSelectedToken() {
        val current = encounter ?: run {
            selectedTokenParticipantId = null
            return
        }
        val stillPresent = current.participants.any { it.id == selectedTokenParticipantId }
        if (!stillPresent) {
            selectedTokenParticipantId = currentTurnParticipant(current)?.id
                ?: current.participants.firstOrNull()?.id
        }
        val map = battleMap
        val participant = current.participants.firstOrNull { it.id == selectedTokenParticipantId }
        if (map != null && participant?.boardCell() != null) {
            applyTokenMovement(map, participant)
        }
    }

    private fun applyTokenMovement(battleMap: BattleMap, participant: EncounterParticipant) {
        walkSpeedFor(participant)?.let { speed ->
            movementSpeedText = speed.toString()
        }
        movementOrigin = participant.boardCell()
        recomputeMovement(battleMap)
        bindMapOverlays()
    }

    private fun recomputeMovement(battleMap: BattleMap) {
        val origin = movementOrigin ?: run {
            reachableCells = emptyList()
            return
        }
        val walkSpeed = movementSpeedText.toIntOrNull()?.coerceAtLeast(0) ?: 0
        reachableCells = calculateReachableCells(
            origin = origin,
            walkSpeed = walkSpeed,
            unitsPerTile = battleMap.unitsPerTile,
            columns = battleMap.columns,
            rows = battleMap.rows,
            blockedCells = battleMap.blockedCells,
            difficultCells = battleMap.difficultCells,
            occupiedCells = encounter?.let { current ->
                OccupiedBoardCellsCalculator().occupiedCells(
                    encounter = current,
                    people = people,
                    exceptParticipantId = selectedTokenParticipantId,
                )
            }.orEmpty(),
        )
    }

    private fun bindViewers(battleMap: BattleMap, situations: List<BattleMapSituation>) {
        dmBinding = ensureBinding(dmBinding, battleMap)
        syncBinding(dmBinding, battleMap, situations)
        if (playerViewOpen) {
            playerBinding = ensureBinding(playerBinding, battleMap)
            syncBinding(playerBinding, battleMap, situations)
        } else {
            shutdownPlayerBinding()
        }
        bindMapOverlays()
    }

    private fun ensureBinding(existing: BoundViewer?, battleMap: BattleMap): BoundViewer {
        if (existing != null && existing.mapId == battleMap.id) {
            return existing
        }
        existing?.let { shutdownBinding(it) }
        return BoundViewer(
            mapId = battleMap.id,
            mapState = mapStateFactory.create(battleMap),
        )
    }

    private fun syncBinding(
        binding: BoundViewer?,
        battleMap: BattleMap,
        situations: List<BattleMapSituation>,
    ) {
        if (binding == null) {
            return
        }
        binding.situationSignature = mapStateFactory.syncSituationLayers(
            mapState = binding.mapState,
            battleMap = battleMap,
            situations = situations,
            layerIds = binding.situationLayerIds,
            currentSignature = binding.situationSignature,
        )
        binding.terrainLayerId = mapStateFactory.syncTerrainLayer(
            mapState = binding.mapState,
            battleMap = battleMap,
            layerId = binding.terrainLayerId,
        )
        val fog = mapStateFactory.syncFogLayer(
            mapState = binding.mapState,
            battleMap = battleMap,
            opaque = binding == playerBinding,
            layerId = binding.fogLayerId,
        )
        binding.fogLayerId = fog.first
    }

    private fun bindMapOverlays() {
        val map = battleMap ?: return
        if (selectedItemId != null && map.items.none { it.id == selectedItemId }) {
            selectedItemId = null
        }
        val geometry = geometryFor(map)
        val tokens = boardTokens()
        dmBinding?.let { binding ->
            movementOverlay.bind(binding.mapState, geometry, movementOrigin, reachableCells)
            measureOverlay.bind(
                mapState = binding.mapState,
                geometry = geometry,
                origin = measureOrigin,
                destination = measureDestination,
                distance = measureDistance,
                unitName = map.unitName,
            )
            tokenOverlay.bind(binding.mapState, geometry, tokens)
            itemOverlay.bind(binding.mapState, geometry, map.items, selectedItemId)
        }
        playerBinding?.let { binding ->
            val playerTokens = tokens.filter { token ->
                token.visibleToPlayers && map.isRevealedToPlayers(token.cell)
            }
            val origin = movementOrigin
            val originToken = tokens.firstOrNull { token -> token.cell == origin }
            val playerOrigin = when {
                origin == null -> null
                !map.isRevealedToPlayers(origin) -> null
                originToken != null && !originToken.visibleToPlayers -> null
                else -> origin
            }
            val playerReachable = if (playerOrigin == null) {
                emptyList()
            } else {
                reachableCells.filter { map.isRevealedToPlayers(it) }
            }
            movementOverlay.bind(binding.mapState, geometry, playerOrigin, playerReachable)
            tokenOverlay.bind(binding.mapState, geometry, playerTokens)
            itemOverlay.bind(
                mapState = binding.mapState,
                geometry = geometry,
                items = map.items.filter { map.isRevealedToPlayers(it.cell) },
                selectedItemId = null,
            )
        }
    }

    private fun geometryFor(battleMap: BattleMap): BattleMapGridGeometry {
        return BattleMapGridGeometry(
            imageWidth = battleMap.originalWidth,
            imageHeight = battleMap.originalHeight,
            columns = battleMap.columns,
            rows = battleMap.rows,
        )
    }

    private fun shutdownPlayerBinding() {
        shutdownBinding(playerBinding)
        playerBinding = null
    }

    private fun shutdownBinding(binding: BoundViewer?) {
        if (binding == null) {
            return
        }
        movementOverlay.clear(binding.mapState)
        measureOverlay.clear(binding.mapState)
        tokenOverlay.clear(binding.mapState)
        itemOverlay.clear(binding.mapState)
        binding.mapState.shutdown()
    }

    private fun currentTurnParticipant(encounter: Encounter): EncounterParticipant? {
        return encounter.initiativeOrder().getOrNull(encounter.currentTurnIndex)
    }

    private fun boardTokens(): List<BattleMapBoardToken> {
        val current = encounter ?: return emptyList()
        val currentTurnId = currentTurnParticipant(current)?.id
        return current.participants.mapNotNull { participant ->
            val cell = participant.boardCell() ?: return@mapNotNull null
            BattleMapBoardToken(
                participantId = participant.id,
                name = participant.name,
                cell = cell,
                span = CreatureSizeResolver().resolve(participant, people).span,
                avatarPath = avatarPathFor(participant),
                selected = participant.id == selectedTokenParticipantId,
                isCurrentTurn = participant.id == currentTurnId,
                combatState = participant.combatState,
                conditions = participant.conditions,
                visibleToPlayers = visibilityResolver.isVisibleToPlayers(participant, people),
            )
        }
    }

    private fun avatarPathFor(participant: EncounterParticipant): String? {
        val sourceId = participant.sourceId ?: return null
        return when (participant.source) {
            EncounterParticipantSource.WorldPerson -> {
                avatarFileStore.pathIfPresent(PersonRef.World(sourceId))
            }
            EncounterParticipantSource.CampaignPerson -> {
                avatarFileStore.pathIfPresent(PersonRef.Campaign(sourceId))
                    ?: people.campaignPeople
                        .firstOrNull { it.id == sourceId }
                        ?.worldPersonId
                        ?.let { worldId -> avatarFileStore.pathIfPresent(PersonRef.World(worldId)) }
            }
            EncounterParticipantSource.Nameless -> null
        }
    }

    private fun walkSpeedFor(participant: EncounterParticipant): Int? {
        val sourceId = participant.sourceId ?: return null
        return when (participant.source) {
            EncounterParticipantSource.WorldPerson -> {
                people.worldPeople.firstOrNull { it.id == sourceId }?.sheet?.movementSpeed()
            }
            EncounterParticipantSource.CampaignPerson -> {
                val campaignPerson = people.campaignPeople.firstOrNull { it.id == sourceId }
                    ?: return null
                campaignPerson.sheet.movementSpeed().takeIf { it > 0 }
                    ?: people.worldPeople
                        .firstOrNull { it.id == campaignPerson.worldPersonId }
                        ?.sheet
                        ?.movementSpeed()
            }
            EncounterParticipantSource.Nameless -> null
        }
    }

    private data class BoundViewer(
        val mapId: String,
        val mapState: MapState,
        val situationLayerIds: MutableMap<String, String> = mutableMapOf(),
        var situationSignature: String? = null,
        var terrainLayerId: String? = null,
        var fogLayerId: String? = null,
    )

    private companion object {
        const val DEFAULT_MOVEMENT_SPEED = "30"
    }
}
