package io.github.kmbisset89.worldweaver.ui.maps

import io.github.kmbisset89.worldweaver.domain.BattleMap
import io.github.kmbisset89.worldweaver.domain.BattleMapFogEdit
import io.github.kmbisset89.worldweaver.domain.BattleMapGridGeometry
import io.github.kmbisset89.worldweaver.domain.BattleMapSituation
import io.github.kmbisset89.worldweaver.domain.BattleMapTerrainEdit
import io.github.kmbisset89.worldweaver.domain.CreatureSizeResolver
import io.github.kmbisset89.worldweaver.domain.Encounter
import io.github.kmbisset89.worldweaver.domain.EncounterParticipant
import io.github.kmbisset89.worldweaver.domain.EncounterParticipantSource
import io.github.kmbisset89.worldweaver.domain.EncounterParticipantVisibilityResolver
import io.github.kmbisset89.worldweaver.domain.GridCell
import io.github.kmbisset89.worldweaver.domain.GridDistance
import io.github.kmbisset89.worldweaver.domain.OccupiedBoardCellsCalculator
import io.github.kmbisset89.worldweaver.domain.PeopleSnapshot
import io.github.kmbisset89.worldweaver.domain.PersonAvatarFileStore
import io.github.kmbisset89.worldweaver.domain.PersonRef
import ovh.plrapps.mapcompose.ui.state.MapState

internal class BattleMapBoardSession(
    private val mapStateFactory: BattleMapMapStateFactory,
    private val movementOverlay: BattleMapMovementOverlay,
    private val measureOverlay: BattleMapMeasureOverlay,
    private val tokenOverlay: BattleMapTokenOverlay,
    private val itemOverlay: BattleMapItemOverlay,
    private val avatarFileStore: PersonAvatarFileStore,
    private val visibilityResolver: EncounterParticipantVisibilityResolver =
        EncounterParticipantVisibilityResolver(),
    private val occupiedCellsCalculator: OccupiedBoardCellsCalculator = OccupiedBoardCellsCalculator(),
    private val sizeResolver: CreatureSizeResolver = CreatureSizeResolver(),
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
    ): BattleMapBoardOutcome {
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
        val movementWork = syncSelectedToken()
        if (battleMap == null) {
            shutdown()
            return outcome()
        }
        bindViewers(battleMap, situations)
        return outcome(movementWork)
    }

    fun selectToken(participantId: String): BattleMapBoardOutcome {
        val map = battleMap ?: return outcome()
        val participant = encounter?.participants?.firstOrNull { it.id == participantId }
            ?: return outcome()
        selectedTokenParticipantId = participantId
        return outcome(applyTokenMovement(map, participant))
    }

    fun selectParticipant(participantId: String?): BattleMapBoardOutcome {
        if (participantId == null) {
            selectedTokenParticipantId = null
            bindMapOverlays()
            return outcome()
        }
        return selectToken(participantId)
    }

    fun selectCell(x: Double, y: Double): BattleMapBoardOutcome {
        val map = battleMap ?: return outcome()
        val geometry = geometryFor(map)
        val cell = geometry.cellAtNormalized(x, y) ?: return outcome()
        if (fogPaintEnabled) {
            val edit = if (fogRevealBrush) {
                BattleMapFogEdit.Reveal(setOf(cell))
            } else {
                BattleMapFogEdit.Hide(setOf(cell))
            }
            return outcome(BattleMapBoardWork.UpdateFog(map.id, edit))
        }
        val terrain = terrainPaint
        if (terrain != null) {
            val edit = when (terrain) {
                TerrainPaintKind.Blocked -> BattleMapTerrainEdit.SetBlocked(setOf(cell))
                TerrainPaintKind.Difficult -> BattleMapTerrainEdit.SetDifficult(setOf(cell))
                TerrainPaintKind.Clear -> BattleMapTerrainEdit.Clear(setOf(cell))
            }
            return outcome(BattleMapBoardWork.UpdateTerrain(map.id, edit))
        }
        if (itemDropEnabled) {
            return outcome(
                BattleMapBoardWork.PlaceItem(
                    mapId = map.id,
                    name = itemNameText,
                    cell = cell,
                ),
            )
        }
        if (measureEnabled) {
            return applyMeasureClick(map, cell)
        }
        val currentEncounter = encounter
        val participantId = selectedTokenParticipantId
            ?: currentEncounter?.let { currentTurnParticipant(it)?.id }
        if (currentEncounter != null && participantId != null) {
            val participant = currentEncounter.participants.firstOrNull { it.id == participantId }
            val span = participant?.let { sizeResolver.resolve(it, people).span } ?: 1
            return outcome(
                BattleMapBoardWork.PlaceToken(
                    encounterId = currentEncounter.id,
                    participantId = participantId,
                    cell = cell,
                    columns = map.columns,
                    rows = map.rows,
                    span = span,
                ),
            )
        }
        movementOrigin = cell
        bindMapOverlays()
        return outcome(movementWork(map))
    }

    fun changeMovementSpeed(speed: String): BattleMapBoardOutcome {
        movementSpeedText = speed.filter { it.isDigit() }.take(4)
        val map = battleMap
        if (map != null && movementOrigin != null) {
            bindMapOverlays()
            return outcome(movementWork(map))
        }
        return outcome()
    }

    fun clearMovement(): BattleMapBoardOutcome {
        clearMovement(refreshOverlays = true)
        return outcome()
    }

    fun clearBoardTools(): BattleMapBoardOutcome {
        measureEnabled = false
        fogPaintEnabled = false
        terrainPaint = null
        itemDropEnabled = false
        clearMeasure(refreshOverlays = false)
        bindMapOverlays()
        return outcome()
    }

    fun toggleMeasure(): BattleMapBoardOutcome {
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
        return outcome()
    }

    fun clearMeasure(): BattleMapBoardOutcome {
        clearMeasure(refreshOverlays = true)
        return outcome()
    }

    fun toggleFogPaint(): BattleMapBoardOutcome {
        fogPaintEnabled = !fogPaintEnabled
        if (fogPaintEnabled) {
            terrainPaint = null
            itemDropEnabled = false
            measureEnabled = false
            clearMeasure(refreshOverlays = false)
        }
        bindMapOverlays()
        return outcome()
    }

    fun setFogRevealBrush(reveal: Boolean): BattleMapBoardOutcome {
        fogRevealBrush = reveal
        fogPaintEnabled = true
        terrainPaint = null
        itemDropEnabled = false
        measureEnabled = false
        clearMeasure(refreshOverlays = false)
        bindMapOverlays()
        return outcome()
    }

    fun setTerrainPaint(kind: TerrainPaintKind?): BattleMapBoardOutcome {
        terrainPaint = if (terrainPaint == kind) null else kind
        if (terrainPaint != null) {
            fogPaintEnabled = false
            itemDropEnabled = false
            measureEnabled = false
            clearMeasure(refreshOverlays = false)
        }
        bindMapOverlays()
        return outcome()
    }

    fun toggleItemDrop(): BattleMapBoardOutcome {
        itemDropEnabled = !itemDropEnabled
        if (itemDropEnabled) {
            fogPaintEnabled = false
            terrainPaint = null
            measureEnabled = false
            clearMeasure(refreshOverlays = false)
        }
        bindMapOverlays()
        return outcome()
    }

    fun changeItemName(name: String): BattleMapBoardOutcome {
        itemNameText = name.take(80)
        return outcome()
    }

    fun selectItem(itemId: String): BattleMapBoardOutcome {
        val map = battleMap ?: return outcome()
        if (map.items.none { it.id == itemId }) {
            return outcome()
        }
        selectedItemId = itemId
        bindMapOverlays()
        return outcome()
    }

    fun selectPlacedItem(itemId: String): BattleMapBoardOutcome {
        selectedItemId = itemId
        return outcome()
    }

    fun removeSelectedItem(): BattleMapBoardOutcome {
        val map = battleMap ?: return outcome()
        val itemId = selectedItemId ?: return outcome()
        return outcome(BattleMapBoardWork.DeleteItem(map.id, itemId))
    }

    fun clearSelectedItem(): BattleMapBoardOutcome {
        selectedItemId = null
        bindMapOverlays()
        return outcome()
    }

    fun applyFogEdit(edit: BattleMapFogEdit): BattleMapBoardOutcome {
        val map = battleMap ?: return outcome()
        return outcome(BattleMapBoardWork.UpdateFog(map.id, edit))
    }

    fun applyReachableCells(cells: List<GridCell>): BattleMapBoardSnapshot {
        reachableCells = cells
        bindMapOverlays()
        return snapshot()
    }

    fun applyMeasureDistance(distance: GridDistance): BattleMapBoardSnapshot {
        measureDistance = distance
        bindMapOverlays()
        return snapshot()
    }

    fun tokenPlaced(participant: EncounterParticipant): BattleMapBoardOutcome {
        val map = battleMap ?: return outcome()
        selectedTokenParticipantId = participant.id
        return outcome(applyTokenMovement(map, participant))
    }

    fun openPlayerView(walkSpeed: Int?): BattleMapBoardOutcome {
        playerViewOpen = true
        if (walkSpeed != null && walkSpeed > 0) {
            movementSpeedText = walkSpeed.toString()
        }
        val map = battleMap
        if (map != null) {
            bindViewers(map, situations)
            return outcome(if (movementOrigin != null) movementWork(map) else null)
        }
        return outcome()
    }

    fun closePlayerView(): BattleMapBoardOutcome {
        playerViewOpen = false
        shutdownPlayerBinding()
        return outcome()
    }

    fun shutdown() {
        shutdownBinding(dmBinding)
        dmBinding = null
        shutdownPlayerBinding()
    }

    private fun outcome(work: BattleMapBoardWork? = null): BattleMapBoardOutcome {
        return BattleMapBoardOutcome(snapshot = snapshot(), work = work)
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

    private fun applyMeasureClick(battleMap: BattleMap, cell: GridCell): BattleMapBoardOutcome {
        if (cell in battleMap.blockedCells) {
            return outcome()
        }
        if (measureOrigin == null || measureDestination != null) {
            measureOrigin = cell
            measureDestination = null
            measureDistance = null
            bindMapOverlays()
            return outcome()
        }
        measureDestination = cell
        bindMapOverlays()
        val origin = measureOrigin ?: cell
        return outcome(
            BattleMapBoardWork.ComputeMeasureDistance(
                from = origin,
                to = cell,
                unitsPerTile = battleMap.unitsPerTile,
            ),
        )
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

    private fun syncSelectedToken(): BattleMapBoardWork.ComputeReachableCells? {
        val current = encounter ?: run {
            selectedTokenParticipantId = null
            return null
        }
        val stillPresent = current.participants.any { it.id == selectedTokenParticipantId }
        if (!stillPresent) {
            selectedTokenParticipantId = currentTurnParticipant(current)?.id
                ?: current.participants.firstOrNull()?.id
        }
        val map = battleMap
        val participant = current.participants.firstOrNull { it.id == selectedTokenParticipantId }
        if (map != null && participant?.boardCell() != null) {
            return applyTokenMovement(map, participant)
        }
        return null
    }

    private fun applyTokenMovement(
        battleMap: BattleMap,
        participant: EncounterParticipant,
    ): BattleMapBoardWork.ComputeReachableCells? {
        walkSpeedFor(participant)?.let { speed ->
            movementSpeedText = speed.toString()
        }
        movementOrigin = participant.boardCell()
        return movementWork(battleMap)
    }

    private fun movementWork(battleMap: BattleMap): BattleMapBoardWork.ComputeReachableCells? {
        val origin = movementOrigin ?: run {
            reachableCells = emptyList()
            return null
        }
        val walkSpeed = movementSpeedText.toIntOrNull()?.coerceAtLeast(0) ?: 0
        return BattleMapBoardWork.ComputeReachableCells(
            origin = origin,
            walkSpeed = walkSpeed,
            unitsPerTile = battleMap.unitsPerTile,
            columns = battleMap.columns,
            rows = battleMap.rows,
            blockedCells = battleMap.blockedCells,
            difficultCells = battleMap.difficultCells,
            occupiedCells = encounter?.let { current ->
                occupiedCellsCalculator.occupiedCells(
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
                span = sizeResolver.resolve(participant, people).span,
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
