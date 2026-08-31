package net.tactware.worldweaver.ui.maps

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import net.tactware.worldweaver.core.AppCoroutineScope
import net.tactware.worldweaver.domain.ActiveContextDetails
import net.tactware.worldweaver.domain.BattleMap
import net.tactware.worldweaver.domain.BattleMapDraft
import net.tactware.worldweaver.domain.BattleMapGridGeometry
import net.tactware.worldweaver.domain.BattleMapImageScaler
import net.tactware.worldweaver.domain.BattleMapSituation
import net.tactware.worldweaver.domain.BattleMapSituationDraft
import net.tactware.worldweaver.domain.CalculateGridDistanceUseCase
import net.tactware.worldweaver.domain.CalculateReachableCellsUseCase
import net.tactware.worldweaver.domain.GridDistance
import net.tactware.worldweaver.domain.BattleMapFogEdit
import net.tactware.worldweaver.domain.CreateBattleMapSituationUseCase
import net.tactware.worldweaver.domain.CreateBattleMapUseCase
import net.tactware.worldweaver.domain.DeleteBattleMapSituationUseCase
import net.tactware.worldweaver.domain.DeleteBattleMapUseCase
import net.tactware.worldweaver.domain.Encounter
import net.tactware.worldweaver.domain.EncounterParticipant
import net.tactware.worldweaver.domain.EncounterParticipantSource
import net.tactware.worldweaver.domain.EncounterParticipantVisibilityResolver
import net.tactware.worldweaver.domain.EncounterStatus
import net.tactware.worldweaver.domain.GridCell
import net.tactware.worldweaver.domain.ObserveActiveContextDetailsUseCase
import net.tactware.worldweaver.domain.ObserveBattleMapSituationsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveBattleMapsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveEncountersForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObservePeopleForActiveContextUseCase
import net.tactware.worldweaver.domain.PeopleSnapshot
import net.tactware.worldweaver.domain.PersonAvatarFileStore
import net.tactware.worldweaver.domain.PersonRef
import net.tactware.worldweaver.domain.PlaceEncounterTokenUseCase
import net.tactware.worldweaver.domain.ToggleBattleMapSituationUseCase
import net.tactware.worldweaver.domain.UpdateBattleMapFogUseCase
import ovh.plrapps.mapcompose.ui.state.MapState
import java.io.File
import javax.imageio.ImageIO

internal class MapsViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeBattleMaps: ObserveBattleMapsForActiveCampaignUseCase,
    private val observeSituations: ObserveBattleMapSituationsForActiveCampaignUseCase,
    private val createBattleMap: CreateBattleMapUseCase,
    private val deleteBattleMap: DeleteBattleMapUseCase,
    private val createSituation: CreateBattleMapSituationUseCase,
    private val toggleSituation: ToggleBattleMapSituationUseCase,
    private val deleteSituation: DeleteBattleMapSituationUseCase,
    private val mapStateFactory: BattleMapMapStateFactory,
    private val movementOverlay: BattleMapMovementOverlay,
    private val measureOverlay: BattleMapMeasureOverlay,
    private val tokenOverlay: BattleMapTokenOverlay,
    private val calculateReachableCells: CalculateReachableCellsUseCase,
    private val calculateGridDistance: CalculateGridDistanceUseCase,
    private val placeEncounterToken: PlaceEncounterTokenUseCase,
    private val observeEncounters: ObserveEncountersForActiveCampaignUseCase,
    private val observePeople: ObservePeopleForActiveContextUseCase,
    private val avatarFileStore: PersonAvatarFileStore,
    private val imageScaler: BattleMapImageScaler,
    private val updateBattleMapFog: UpdateBattleMapFogUseCase,
    private val visibilityResolver: EncounterParticipantVisibilityResolver =
        EncounterParticipantVisibilityResolver(),
) {
    private val _state = MutableStateFlow<MapsViewState>(MapsViewState.Loading)
    val state: StateFlow<MapsViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<MapsViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<MapsViewEffect> = _effects.asSharedFlow()

    private var dmBinding: BoundViewer? = null
    private var playerBinding: BoundViewer? = null
    val mapState: MapState?
        get() = dmBinding?.mapState
    val playerMapState: MapState?
        get() = playerBinding?.mapState

    private var observeJob: Job? = null
    private var openMakerOnNextLoad = false
    private var selectedMapId: String? = null
    private var latestMaps: List<BattleMap> = emptyList()
    private var latestSituations: List<BattleMapSituation> = emptyList()
    private var latestEncounters: List<Encounter> = emptyList()
    private var latestPeople: PeopleSnapshot = PeopleSnapshot(emptyList(), emptyList())
    private var selectedTokenParticipantId: String? = null
    private var latestWorldName: String = ""
    private var latestCampaignName: String = ""
    private var latestSituationError: String? = null
    private var isSavingSituation: Boolean = false
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

    init {
        observe()
    }

    fun onInteraction(interaction: MapsInteraction) {
        when (interaction) {
            MapsInteraction.ScreenStarted -> Unit
            MapsInteraction.RetrySelected -> observe()
            MapsInteraction.CreateWorldSelected -> _effects.tryEmit(MapsViewEffect.OpenWorlds)
            MapsInteraction.CreateCampaignSelected -> _effects.tryEmit(MapsViewEffect.OpenCampaigns)
            MapsInteraction.ImportSelected -> openMaker()
            is MapsInteraction.MakerNameChanged -> updateMaker { editor ->
                editor.copy(name = interaction.name, nameError = null)
            }
            is MapsInteraction.MakerImageChosen -> applyChosenImage(interaction.path)
            is MapsInteraction.MakerColumnsChanged -> updateMaker { editor ->
                editor.copy(
                    columnsText = interaction.columns.filter { it.isDigit() }.take(4),
                    gridError = null,
                )
            }
            is MapsInteraction.MakerRowsChanged -> updateMaker { editor ->
                editor.copy(
                    rowsText = interaction.rows.filter { it.isDigit() }.take(4),
                    gridError = null,
                )
            }
            is MapsInteraction.MakerUnitNameChanged -> updateMaker { editor ->
                editor.copy(unitNameText = interaction.unitName.trim().take(12), gridError = null)
            }
            is MapsInteraction.MakerUnitsPerTileChanged -> updateMaker { editor ->
                editor.copy(unitsPerTileText = sanitizeDecimal(interaction.unitsPerTile), gridError = null)
            }
            is MapsInteraction.MakerScaleChanged -> updateMaker { editor ->
                editor.copy(scalePercentText = interaction.scalePercent.filter { it.isDigit() }.take(3))
            }
            MapsInteraction.MakerGridToggled -> updateMaker { editor ->
                editor.copy(showGrid = !editor.showGrid)
            }
            MapsInteraction.MakerRenderTilesToggled -> updateMaker { editor ->
                editor.copy(showRenderTiles = !editor.showRenderTiles)
            }
            MapsInteraction.MakerSaved -> saveMaker()
            MapsInteraction.MakerDismissed -> dismissMaker()
            is MapsInteraction.SituationImageChosen -> addSituation(interaction.path)
            is MapsInteraction.SituationToggled -> toggleSituationVisibility(interaction.situationId)
            is MapsInteraction.SituationDeleteSelected -> removeSituation(interaction.situationId)
            is MapsInteraction.MapSelected,
            is MapsInteraction.MapOpened,
            -> selectMap(mapIdFrom(interaction))
            MapsInteraction.PlayerViewSelected -> openPlayerView(null)
            MapsInteraction.PlayerViewClosed -> closePlayerView()
            is MapsInteraction.PlayerViewOpened -> {
                selectMap(interaction.battleMapId)
                openPlayerView(interaction.walkSpeed)
            }
            is MapsInteraction.MapCellSelected -> selectMapCell(interaction.x, interaction.y)
            is MapsInteraction.TokenSelected -> selectToken(interaction.participantId)
            is MapsInteraction.MovementSpeedChanged -> changeMovementSpeed(interaction.speed)
            MapsInteraction.MovementCleared -> clearMovement()
            MapsInteraction.MeasureToggled -> toggleMeasure()
            MapsInteraction.MeasureCleared -> clearMeasure()
            MapsInteraction.FogToggled -> toggleFogPaint()
            MapsInteraction.FogRevealBrushSelected -> setFogRevealBrush(true)
            MapsInteraction.FogHideBrushSelected -> setFogRevealBrush(false)
            MapsInteraction.FogRevealAllSelected -> applyFogEdit(BattleMapFogEdit.RevealAll)
            MapsInteraction.FogHideAllSelected -> applyFogEdit(BattleMapFogEdit.HideAll)
            is MapsInteraction.DeleteMapSelected -> requestDelete(interaction.battleMapId)
            MapsInteraction.DeleteConfirmed -> confirmDelete()
            MapsInteraction.DeleteCancelled -> updatePendingDelete(null)
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = MapsViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                observeActiveContextDetails(),
                observeBattleMaps(),
                observeSituations(),
                combine(observeEncounters(), observePeople()) { encounters, people ->
                    encounters to people
                },
            ) { details, maps, situations, encounterPeople ->
                LoadedSnapshot(
                    details = details,
                    maps = maps,
                    situations = situations,
                    encounters = encounterPeople.first,
                    people = encounterPeople.second,
                )
            }
                .catch { error ->
                    _state.value = MapsViewState.Error(
                        message = error.message ?: "Could not load battle maps",
                        canRetry = true,
                    )
                }
                .collect { snapshot ->
                    applyLoaded(snapshot)
                }
        }
    }

    private fun applyLoaded(snapshot: LoadedSnapshot) {
        val world = snapshot.details.world
        if (world == null) {
            clearLatest()
            shutdownMapState()
            _state.value = MapsViewState.NoActiveWorld
            return
        }
        val campaign = snapshot.details.campaign
        if (campaign == null) {
            clearLatest()
            shutdownMapState()
            latestWorldName = world.name
            _state.value = MapsViewState.NoActiveCampaign
            return
        }
        latestMaps = snapshot.maps
        latestSituations = snapshot.situations
        latestEncounters = snapshot.encounters
        latestPeople = snapshot.people
        latestWorldName = world.name
        latestCampaignName = campaign.name
        syncSelectedToken()
        val current = _state.value
        if (openMakerOnNextLoad) {
            openMakerOnNextLoad = false
            _state.value = MapsViewState.Maker(
                worldName = world.name,
                campaignName = campaign.name,
                editor = emptyMaker(),
            )
            return
        }
        if (current is MapsViewState.Maker) {
            _state.value = current.copy(worldName = world.name, campaignName = campaign.name)
            return
        }
        showLibrary(pendingDelete = pendingDeleteFrom(current))
    }

    private fun showLibrary(pendingDelete: MapsViewState.PendingDelete?) {
        if (latestMaps.isEmpty()) {
            selectedMapId = null
            shutdownMapState()
            _state.value = MapsViewState.Empty(
                worldName = latestWorldName,
                campaignName = latestCampaignName,
            )
            return
        }
        val selected = selectedFrom(latestMaps) ?: latestMaps.first()
        selectedMapId = selected.id
        val situations = situationsFor(selected.id)
        bindViewers(selected, situations)
        _state.value = contentState(
            selected = selected,
            situations = situations,
            pendingDelete = pendingDelete,
        )
    }

    private fun selectMap(battleMapId: String) {
        if (battleMapId.isEmpty()) {
            return
        }
        selectedMapId = battleMapId
        val current = _state.value
        if (current !is MapsViewState.Content) {
            return
        }
        val previousId = current.selectedMap?.id
        val selected = selectedFrom(latestMaps) ?: return
        if (previousId != selected.id) {
            clearMovement(refresh = false)
            clearMeasure(refresh = false)
            fogPaintEnabled = false
            selectedTokenParticipantId = null
            syncSelectedToken()
        }
        val situations = situationsFor(selected.id)
        bindViewers(selected, situations)
        _state.value = current.copy(
            selectedMap = selected,
            situations = situations,
            situationError = latestSituationError,
            isSavingSituation = isSavingSituation,
            playerViewOpen = playerViewOpen,
            movementSpeedText = movementSpeedText,
            movementOrigin = movementOrigin,
            reachableCells = reachableCells,
            measureEnabled = measureEnabled,
            measureOrigin = measureOrigin,
            measureDestination = measureDestination,
            measureDistance = measureDistance,
            fogPaintEnabled = fogPaintEnabled,
            fogRevealBrush = fogRevealBrush,
            tokens = boardTokens(selected.id),
            selectedTokenName = selectedTokenName(),
            unplacedTokenCount = unplacedTokenCount(selected.id),
        )
    }

    private fun openMaker() {
        when (_state.value) {
            is MapsViewState.Empty, is MapsViewState.Content -> {
                _state.value = MapsViewState.Maker(
                    worldName = latestWorldName,
                    campaignName = latestCampaignName,
                    editor = emptyMaker(),
                )
            }
            MapsViewState.Loading, is MapsViewState.Error -> {
                openMakerOnNextLoad = true
            }
            MapsViewState.NoActiveWorld, MapsViewState.NoActiveCampaign, is MapsViewState.Maker -> Unit
        }
    }

    private fun applyChosenImage(path: String) {
        val file = File(path)
        if (!file.isFile) {
            updateMaker { editor ->
                editor.copy(imageError = "Could not read that file")
            }
            return
        }
        val image = try {
            ImageIO.read(file)
        } catch (_: Exception) {
            null
        }
        if (image == null || image.width <= 0 || image.height <= 0) {
            updateMaker { editor ->
                editor.copy(imageError = "That file is not a readable image")
            }
            return
        }
        updateMaker { editor ->
            editor.copy(
                imagePath = path,
                imageWidth = image.width,
                imageHeight = image.height,
                imageError = null,
                name = editor.name.ifBlank { suggestedName(path) },
            )
        }
    }

    private fun saveMaker() {
        val maker = _state.value as? MapsViewState.Maker ?: return
        val editor = maker.editor
        if (editor.isSaving) {
            return
        }
        val name = editor.name.trim()
        if (name.isEmpty()) {
            updateMaker { current -> current.copy(nameError = "Name is required") }
            return
        }
        val path = editor.imagePath
        if (path.isNullOrBlank()) {
            updateMaker { current -> current.copy(imageError = "Choose a PNG image") }
            return
        }
        val imageFile = File(path)
        if (!imageFile.isFile) {
            updateMaker { current -> current.copy(imageError = "Could not read that file") }
            return
        }
        updateMaker { current -> current.copy(isSaving = true, gridError = null) }
        appScope.scope.launch {
            val originalBytes = imageFile.readBytes()
            val scalePercent = editor.scalePercentText.toIntOrNull()?.coerceIn(10, 400) ?: 100
            val scaledBytes = imageScaler.scale(originalBytes, scalePercent)
            if (scaledBytes == null) {
                updateMaker { current ->
                    current.copy(isSaving = false, imageError = "That file is not a readable image")
                }
                return@launch
            }
            val draft = BattleMapDraft(
                name = name,
                imagePng = scaledBytes,
                columns = editor.columnsText.toIntOrNull() ?: 0,
                rows = editor.rowsText.toIntOrNull() ?: 0,
                unitName = editor.unitNameText,
                unitsPerTile = editor.unitsPerTileText.toDoubleOrNull() ?: 0.0,
            )
            when (val result = createBattleMap(draft)) {
                is CreateBattleMapUseCase.Result.Created -> {
                    selectedMapId = result.battleMap.id
                    showLibrary(pendingDelete = null)
                }
                CreateBattleMapUseCase.Result.InvalidName -> {
                    updateMaker { current ->
                        current.copy(isSaving = false, nameError = "Name is required")
                    }
                }
                CreateBattleMapUseCase.Result.InvalidImage -> {
                    updateMaker { current ->
                        current.copy(isSaving = false, imageError = "That file is not a readable image")
                    }
                }
                CreateBattleMapUseCase.Result.InvalidGrid -> {
                    updateMaker { current ->
                        current.copy(
                            isSaving = false,
                            gridError = "Columns and rows must be at least 1 and no larger than the scaled image.",
                        )
                    }
                }
                CreateBattleMapUseCase.Result.NoActiveCampaign -> showLibrary(pendingDelete = null)
            }
        }
    }

    private fun dismissMaker() {
        showLibrary(pendingDelete = null)
    }

    private fun requestDelete(battleMapId: String) {
        val battleMap = latestMaps.firstOrNull { it.id == battleMapId } ?: return
        val current = _state.value
        if (current is MapsViewState.Content) {
            _state.value = current.copy(
                pendingDelete = MapsViewState.PendingDelete(
                    battleMapId = battleMap.id,
                    battleMapName = battleMap.name,
                )
            )
        }
    }

    private fun confirmDelete() {
        val pending = pendingDeleteFrom(_state.value) ?: return
        appScope.scope.launch {
            deleteBattleMap(pending.battleMapId)
            if (selectedMapId == pending.battleMapId) {
                selectedMapId = null
            }
            updatePendingDelete(null)
        }
    }

    private fun addSituation(path: String) {
        val battleMapId = selectedMapId ?: return
        if (isSavingSituation) {
            return
        }
        val file = File(path)
        if (!file.isFile) {
            latestSituationError = "Could not read that file"
            refreshContentSituations()
            return
        }
        isSavingSituation = true
        latestSituationError = null
        refreshContentSituations()
        appScope.scope.launch {
            val draft = BattleMapSituationDraft(
                battleMapId = battleMapId,
                name = suggestedName(path),
                imagePng = file.readBytes(),
            )
            when (createSituation(draft)) {
                is CreateBattleMapSituationUseCase.Result.Created -> {
                    isSavingSituation = false
                    latestSituationError = null
                }
                CreateBattleMapSituationUseCase.Result.InvalidName -> {
                    isSavingSituation = false
                    latestSituationError = "Name is required"
                    refreshContentSituations()
                }
                CreateBattleMapSituationUseCase.Result.InvalidImage -> {
                    isSavingSituation = false
                    latestSituationError = "That file is not a readable image"
                    refreshContentSituations()
                }
                CreateBattleMapSituationUseCase.Result.MapNotFound,
                CreateBattleMapSituationUseCase.Result.NoActiveCampaign,
                -> {
                    isSavingSituation = false
                    latestSituationError = "Could not add a layer to this map"
                    refreshContentSituations()
                }
            }
        }
    }

    private fun toggleSituationVisibility(situationId: String) {
        appScope.scope.launch {
            toggleSituation(situationId)
        }
    }

    private fun removeSituation(situationId: String) {
        appScope.scope.launch {
            deleteSituation(situationId)
        }
    }

    private fun refreshContentSituations() {
        val current = _state.value
        if (current is MapsViewState.Content) {
            _state.value = current.copy(
                situations = situationsFor(current.selectedMap?.id),
                situationError = latestSituationError,
                isSavingSituation = isSavingSituation,
                playerViewOpen = playerViewOpen,
                movementSpeedText = movementSpeedText,
                movementOrigin = movementOrigin,
                reachableCells = reachableCells,
                measureEnabled = measureEnabled,
                measureOrigin = measureOrigin,
                measureDestination = measureDestination,
                measureDistance = measureDistance,
            fogPaintEnabled = fogPaintEnabled,
            fogRevealBrush = fogRevealBrush,
            )
        }
    }

    private fun openPlayerView(walkSpeed: Int?) {
        playerViewOpen = true
        if (walkSpeed != null && walkSpeed > 0) {
            movementSpeedText = walkSpeed.toString()
        }
        val selected = selectedFrom(latestMaps)
        if (selected == null) {
            refreshMovementState()
            return
        }
        if (movementOrigin != null) {
            recomputeMovement(selected)
        }
        bindViewers(selected, situationsFor(selected.id))
        refreshMovementState()
    }

    private fun closePlayerView() {
        playerViewOpen = false
        shutdownPlayerBinding()
        refreshMovementState()
    }

    private fun selectMapCell(x: Double, y: Double) {
        val selected = selectedFrom(latestMaps) ?: return
        val geometry = geometryFor(selected)
        val cell = geometry.cellAtNormalized(x, y) ?: return
        if (fogPaintEnabled) {
            val edit = if (fogRevealBrush) {
                BattleMapFogEdit.Reveal(setOf(cell))
            } else {
                BattleMapFogEdit.Hide(setOf(cell))
            }
            appScope.scope.launch {
                updateBattleMapFog(selected.id, edit)
            }
            return
        }
        if (measureEnabled) {
            applyMeasureClick(selected, cell)
            return
        }
        val encounter = encounterForMap(selected.id)
        val participantId = selectedTokenParticipantId
            ?: encounter?.let { currentTurnParticipant(it)?.id }
        if (encounter != null && participantId != null) {
            appScope.scope.launch {
                val result = placeEncounterToken(
                    encounterId = encounter.id,
                    participantId = participantId,
                    cell = cell,
                    columns = selected.columns,
                    rows = selected.rows,
                )
                if (result is PlaceEncounterTokenUseCase.Result.Placed) {
                    selectedTokenParticipantId = participantId
                    applyTokenMovement(selected, result.participant)
                }
            }
            return
        }
        movementOrigin = cell
        recomputeMovement(selected)
        bindMapOverlays(selected)
        refreshMovementState()
    }

    private fun selectToken(participantId: String) {
        val selected = selectedFrom(latestMaps) ?: return
        val encounter = encounterForMap(selected.id) ?: return
        val participant = encounter.participants.firstOrNull { it.id == participantId } ?: return
        selectedTokenParticipantId = participantId
        applyTokenMovement(selected, participant)
    }

    private fun applyTokenMovement(battleMap: BattleMap, participant: EncounterParticipant) {
        walkSpeedFor(participant)?.let { speed ->
            movementSpeedText = speed.toString()
        }
        movementOrigin = participant.boardCell()
        recomputeMovement(battleMap)
        bindMapOverlays(battleMap)
        refreshMovementState()
    }

    private fun changeMovementSpeed(speed: String) {
        movementSpeedText = speed.filter { it.isDigit() }.take(4)
        val selected = selectedFrom(latestMaps)
        if (selected != null && movementOrigin != null) {
            recomputeMovement(selected)
            bindMapOverlays(selected)
        }
        refreshMovementState()
    }

    private fun clearMovement(refresh: Boolean = true) {
        movementOrigin = null
        reachableCells = emptyList()
        dmBinding?.let { movementOverlay.clear(it.mapState) }
        playerBinding?.let { movementOverlay.clear(it.mapState) }
        if (refresh) {
            refreshMovementState()
        }
    }

    private fun toggleMeasure() {
        measureEnabled = !measureEnabled
        if (measureEnabled) {
            fogPaintEnabled = false
        }
        if (!measureEnabled) {
            clearMeasure()
            return
        }
        val selected = selectedFrom(latestMaps)
        if (selected != null) {
            bindMapOverlays(selected)
        }
        refreshMovementState()
    }

    private fun applyMeasureClick(battleMap: BattleMap, cell: GridCell) {
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
        bindMapOverlays(battleMap)
        refreshMovementState()
    }

    private fun clearMeasure(refresh: Boolean = true) {
        measureOrigin = null
        measureDestination = null
        measureDistance = null
        dmBinding?.let { measureOverlay.clear(it.mapState) }
        if (refresh) {
            val selected = selectedFrom(latestMaps)
            if (selected != null) {
                bindMapOverlays(selected)
            }
            refreshMovementState()
        }
    }

    private fun toggleFogPaint() {
        fogPaintEnabled = !fogPaintEnabled
        if (fogPaintEnabled) {
            measureEnabled = false
            clearMeasure(refresh = false)
        }
        refreshMovementState()
    }

    private fun setFogRevealBrush(reveal: Boolean) {
        fogRevealBrush = reveal
        fogPaintEnabled = true
        measureEnabled = false
        clearMeasure(refresh = false)
        refreshMovementState()
    }

    private fun applyFogEdit(edit: BattleMapFogEdit) {
        val selected = selectedFrom(latestMaps) ?: return
        appScope.scope.launch {
            updateBattleMapFog(selected.id, edit)
        }
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
        )
    }

    private fun refreshMovementState() {
        val current = _state.value
        if (current is MapsViewState.Content) {
            _state.value = current.copy(
                playerViewOpen = playerViewOpen,
                movementSpeedText = movementSpeedText,
                movementOrigin = movementOrigin,
                reachableCells = reachableCells,
                measureEnabled = measureEnabled,
                measureOrigin = measureOrigin,
                measureDestination = measureDestination,
                measureDistance = measureDistance,
            fogPaintEnabled = fogPaintEnabled,
            fogRevealBrush = fogRevealBrush,
                tokens = boardTokens(current.selectedMap?.id),
                selectedTokenName = selectedTokenName(),
                unplacedTokenCount = unplacedTokenCount(current.selectedMap?.id),
            )
        }
    }

    private fun contentState(
        selected: BattleMap,
        situations: List<BattleMapSituation>,
        pendingDelete: MapsViewState.PendingDelete?,
    ): MapsViewState.Content {
        return MapsViewState.Content(
            worldName = latestWorldName,
            campaignName = latestCampaignName,
            maps = latestMaps,
            selectedMap = selected,
            situations = situations,
            situationError = latestSituationError,
            isSavingSituation = isSavingSituation,
            pendingDelete = pendingDelete,
            playerViewOpen = playerViewOpen,
            movementSpeedText = movementSpeedText,
            movementOrigin = movementOrigin,
            reachableCells = reachableCells,
            measureEnabled = measureEnabled,
            measureOrigin = measureOrigin,
            measureDestination = measureDestination,
            measureDistance = measureDistance,
            fogPaintEnabled = fogPaintEnabled,
            fogRevealBrush = fogRevealBrush,
            tokens = boardTokens(selected.id),
            selectedTokenName = selectedTokenName(),
            unplacedTokenCount = unplacedTokenCount(selected.id),
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
        bindMapOverlays(battleMap)
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
        val fog = mapStateFactory.syncFogLayer(
            mapState = binding.mapState,
            battleMap = battleMap,
            opaque = binding == playerBinding,
            layerId = binding.fogLayerId,
        )
        binding.fogLayerId = fog.first
    }

    private fun bindMapOverlays(battleMap: BattleMap) {
        val geometry = geometryFor(battleMap)
        val tokens = boardTokens(battleMap.id)
        dmBinding?.let { binding ->
            movementOverlay.bind(binding.mapState, geometry, movementOrigin, reachableCells)
            measureOverlay.bind(
                mapState = binding.mapState,
                geometry = geometry,
                origin = measureOrigin,
                destination = measureDestination,
                distance = measureDistance,
                unitName = battleMap.unitName,
            )
            tokenOverlay.bind(binding.mapState, geometry, tokens)
        }
        playerBinding?.let { binding ->
            val playerTokens = tokens.filter { token ->
                token.visibleToPlayers && battleMap.isRevealedToPlayers(token.cell)
            }
            val origin = movementOrigin
            val originToken = tokens.firstOrNull { token -> token.cell == origin }
            val playerOrigin = when {
                origin == null -> null
                !battleMap.isRevealedToPlayers(origin) -> null
                originToken != null && !originToken.visibleToPlayers -> null
                else -> origin
            }
            val playerReachable = if (playerOrigin == null) {
                emptyList()
            } else {
                reachableCells.filter { battleMap.isRevealedToPlayers(it) }
            }
            movementOverlay.bind(binding.mapState, geometry, playerOrigin, playerReachable)
            tokenOverlay.bind(binding.mapState, geometry, playerTokens)
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

    private fun shutdownMapState() {
        shutdownBinding(dmBinding)
        dmBinding = null
        shutdownPlayerBinding()
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
        binding.mapState.shutdown()
    }

    private fun mapIdFrom(interaction: MapsInteraction): String {
        return when (interaction) {
            is MapsInteraction.MapSelected -> interaction.battleMapId
            is MapsInteraction.MapOpened -> interaction.battleMapId
            is MapsInteraction.PlayerViewOpened -> interaction.battleMapId
            else -> ""
        }
    }

    private fun suggestedName(path: String): String {
        return File(path).nameWithoutExtension.replace('_', ' ').trim()
    }

    private fun sanitizeDecimal(value: String): String {
        return buildString {
            var dotUsed = false
            for (character in value.trim()) {
                when {
                    character.isDigit() -> append(character)
                    character == '.' && !dotUsed -> {
                        dotUsed = true
                        append(character)
                    }
                }
            }
        }.take(8)
    }

    private fun emptyMaker(): MapsViewState.MakerEditorState {
        return MapsViewState.MakerEditorState(
            name = "",
            imagePath = null,
            imageWidth = 0,
            imageHeight = 0,
            columnsText = "20",
            rowsText = "20",
            unitNameText = "ft",
            unitsPerTileText = "5",
            scalePercentText = "100",
            showGrid = true,
            showRenderTiles = true,
            nameError = null,
            imageError = null,
            gridError = null,
            isSaving = false,
        )
    }

    private fun updateMaker(
        transform: (MapsViewState.MakerEditorState) -> MapsViewState.MakerEditorState,
    ) {
        val current = _state.value
        if (current is MapsViewState.Maker) {
            _state.value = current.copy(editor = transform(current.editor))
        }
    }

    private fun updatePendingDelete(pendingDelete: MapsViewState.PendingDelete?) {
        val current = _state.value
        if (current is MapsViewState.Content) {
            _state.value = current.copy(pendingDelete = pendingDelete)
        }
    }

    private fun selectedFrom(maps: List<BattleMap>): BattleMap? {
        return selectedMapId?.let { id -> maps.firstOrNull { it.id == id } }
    }

    private fun situationsFor(battleMapId: String?): List<BattleMapSituation> {
        if (battleMapId == null) {
            return emptyList()
        }
        return latestSituations
            .filter { it.battleMapId == battleMapId }
            .sortedBy { it.sortIndex }
    }

    private fun pendingDeleteFrom(state: MapsViewState): MapsViewState.PendingDelete? {
        return (state as? MapsViewState.Content)?.pendingDelete
    }

    private fun clearLatest() {
        latestMaps = emptyList()
        latestSituations = emptyList()
        latestEncounters = emptyList()
        latestPeople = PeopleSnapshot(emptyList(), emptyList())
        selectedMapId = null
        selectedTokenParticipantId = null
        latestSituationError = null
        isSavingSituation = false
        playerViewOpen = false
        movementSpeedText = DEFAULT_MOVEMENT_SPEED
        movementOrigin = null
        reachableCells = emptyList()
    }

    private fun syncSelectedToken() {
        val encounter = encounterForMap(selectedMapId) ?: run {
            selectedTokenParticipantId = null
            return
        }
        val stillPresent = encounter.participants.any { it.id == selectedTokenParticipantId }
        if (!stillPresent) {
            selectedTokenParticipantId = currentTurnParticipant(encounter)?.id
                ?: encounter.participants.firstOrNull()?.id
        }
        val selected = selectedFrom(latestMaps)
        val participant = encounter.participants.firstOrNull { it.id == selectedTokenParticipantId }
        if (selected != null && participant?.boardCell() != null) {
            applyTokenMovement(selected, participant)
        }
    }

    private fun encounterForMap(battleMapId: String?): Encounter? {
        if (battleMapId == null) {
            return null
        }
        val attached = latestEncounters.filter { it.battleMapId == battleMapId }
        return attached.firstOrNull { it.status == EncounterStatus.Active }
            ?: attached.maxByOrNull { it.updatedAt }
    }

    private fun currentTurnParticipant(encounter: Encounter): EncounterParticipant? {
        return encounter.initiativeOrder().getOrNull(encounter.currentTurnIndex)
    }

    private fun boardTokens(battleMapId: String?): List<BattleMapBoardToken> {
        val encounter = encounterForMap(battleMapId) ?: return emptyList()
        val currentTurnId = currentTurnParticipant(encounter)?.id
        return encounter.participants.mapNotNull { participant ->
            val cell = participant.boardCell() ?: return@mapNotNull null
            BattleMapBoardToken(
                participantId = participant.id,
                name = participant.name,
                cell = cell,
                avatarPath = avatarPathFor(participant),
                selected = participant.id == selectedTokenParticipantId,
                isCurrentTurn = participant.id == currentTurnId,
                combatState = participant.combatState,
                conditions = participant.conditions,
                visibleToPlayers = visibilityResolver.isVisibleToPlayers(participant, latestPeople),
            )
        }
    }

    private fun selectedTokenName(): String? {
        val encounter = encounterForMap(selectedMapId) ?: return null
        return encounter.participants.firstOrNull { it.id == selectedTokenParticipantId }?.name
    }

    private fun unplacedTokenCount(battleMapId: String?): Int {
        val encounter = encounterForMap(battleMapId) ?: return 0
        return encounter.participants.count { it.boardCell() == null }
    }

    private fun avatarPathFor(participant: EncounterParticipant): String? {
        val sourceId = participant.sourceId ?: return null
        return when (participant.source) {
            EncounterParticipantSource.WorldPerson -> {
                avatarFileStore.pathIfPresent(PersonRef.World(sourceId))
            }
            EncounterParticipantSource.CampaignPerson -> {
                avatarFileStore.pathIfPresent(PersonRef.Campaign(sourceId))
                    ?: latestPeople.campaignPeople
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
                latestPeople.worldPeople.firstOrNull { it.id == sourceId }?.sheet?.walkSpeed
            }
            EncounterParticipantSource.CampaignPerson -> {
                val campaignPerson = latestPeople.campaignPeople.firstOrNull { it.id == sourceId }
                    ?: return null
                campaignPerson.sheet.walkSpeed.takeIf { it > 0 }
                    ?: latestPeople.worldPeople
                        .firstOrNull { it.id == campaignPerson.worldPersonId }
                        ?.sheet
                        ?.walkSpeed
            }
            EncounterParticipantSource.Nameless -> null
        }
    }

    private data class BoundViewer(
        val mapId: String,
        val mapState: MapState,
        val situationLayerIds: MutableMap<String, String> = mutableMapOf(),
        var situationSignature: String? = null,
        var fogLayerId: String? = null,
    )

    private data class LoadedSnapshot(
        val details: ActiveContextDetails,
        val maps: List<BattleMap>,
        val situations: List<BattleMapSituation>,
        val encounters: List<Encounter>,
        val people: PeopleSnapshot,
    )

    private companion object {
        const val DEFAULT_MOVEMENT_SPEED = "30"
    }
}
