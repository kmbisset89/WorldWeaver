package io.github.kmbisset89.worldweaver.ui.worldmap

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
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.ActiveContextDetails
import io.github.kmbisset89.worldweaver.domain.CreateWorldMapUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteWorldMapUseCase
import io.github.kmbisset89.worldweaver.domain.Location
import io.github.kmbisset89.worldweaver.domain.LocationType
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLocationsForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldMapsForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateLocationMapAnchorUseCase
import io.github.kmbisset89.worldweaver.domain.WorldMap
import io.github.kmbisset89.worldweaver.domain.WorldMapDraft
import ovh.plrapps.mapcompose.ui.state.MapState
import java.io.File

internal class WorldMapViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeLocations: ObserveLocationsForActiveWorldUseCase,
    private val observeWorldMaps: ObserveWorldMapsForActiveWorldUseCase,
    private val createWorldMap: CreateWorldMapUseCase,
    private val deleteWorldMap: DeleteWorldMapUseCase,
    private val updateLocationMapAnchor: UpdateLocationMapAnchorUseCase,
    private val mapStateFactory: WorldMapMapStateFactory,
    private val pinOverlay: WorldMapPinOverlay,
) {
    private val _state = MutableStateFlow<WorldMapViewState>(WorldMapViewState.Loading)
    val state: StateFlow<WorldMapViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<WorldMapViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<WorldMapViewEffect> = _effects.asSharedFlow()

    private var boundMap: BoundMap? = null
    val mapState: MapState?
        get() = boundMap?.mapState

    private var observeJob: Job? = null
    private var focusedLocationId: String? = null
    private var selectedLocationId: String? = null
    private var placingLocationId: String? = null
    private var importError: String? = null
    private var pendingDelete: WorldMapViewState.PendingDelete? = null
    private var latestWorldName: String = ""
    private var latestLocations: List<Location> = emptyList()
    private var latestWorldMaps: List<WorldMap> = emptyList()

    init {
        observe()
    }

    fun onInteraction(interaction: WorldMapInteraction) {
        when (interaction) {
            WorldMapInteraction.ScreenStarted -> observe()
            is WorldMapInteraction.MapOpened -> openMap(interaction.locationId)
            WorldMapInteraction.RetrySelected -> observe()
            WorldMapInteraction.CreateWorldSelected -> {
                _effects.tryEmit(WorldMapViewEffect.OpenWorlds)
            }
            WorldMapInteraction.BackToLocationsSelected -> {
                _effects.tryEmit(WorldMapViewEffect.OpenLocations(focusedLocationId))
            }
            is WorldMapInteraction.ImageChosen -> importImage(interaction.path)
            WorldMapInteraction.DeleteMapSelected -> requestDelete()
            WorldMapInteraction.DeleteConfirmed -> confirmDelete()
            WorldMapInteraction.DeleteCancelled -> {
                pendingDelete = null
                render()
            }
            is WorldMapInteraction.BreadcrumbSelected -> openMap(interaction.locationId)
            is WorldMapInteraction.PinSelected -> onPinSelected(interaction.locationId)
            is WorldMapInteraction.PlacePinSelected -> {
                placingLocationId = interaction.locationId
                selectedLocationId = interaction.locationId
                render()
            }
            WorldMapInteraction.PlacePinCancelled -> {
                placingLocationId = null
                render()
            }
            is WorldMapInteraction.ClearPinSelected -> clearPin(interaction.locationId)
            is WorldMapInteraction.MapTapped -> onMapTapped(interaction.x, interaction.y)
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = WorldMapViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                observeActiveContextDetails(),
                observeLocations(),
                observeWorldMaps(),
            ) { details, locations, worldMaps ->
                Loaded(details, locations, worldMaps)
            }
                .catch { error ->
                    _state.value = WorldMapViewState.Error(
                        message = error.message ?: "Could not load the world map",
                        canRetry = true,
                    )
                }
                .collect { loaded ->
                    applyLoaded(loaded.details, loaded.locations, loaded.worldMaps)
                }
        }
    }

    private fun applyLoaded(
        details: ActiveContextDetails,
        locations: List<Location>,
        worldMaps: List<WorldMap>,
    ) {
        latestLocations = locations
        latestWorldMaps = worldMaps
        val world = details.world
        if (world == null) {
            shutdownBinding()
            latestWorldName = ""
            _state.value = WorldMapViewState.NoActiveWorld
            return
        }
        latestWorldName = world.name
        render()
    }

    private fun openMap(locationId: String?) {
        focusedLocationId = locationId
        selectedLocationId = locationId
        placingLocationId = null
        importError = null
        pendingDelete = null
        render()
    }

    private fun render() {
        val worldName = latestWorldName
        if (_state.value is WorldMapViewState.NoActiveWorld && worldName.isEmpty()) {
            return
        }
        val worldMap = latestWorldMaps.firstOrNull { it.locationId == focusedLocationId }
        if (worldMap == null) {
            shutdownBinding()
            val location = focusedLocationId?.let { id -> latestLocations.firstOrNull { it.id == id } }
            _state.value = WorldMapViewState.Empty(
                worldName = worldName,
                locationId = focusedLocationId,
                locationName = location?.name,
                importError = importError,
            )
            return
        }
        bindMap(worldMap)
        val children = childLocations()
        val pins = children.mapNotNull { child ->
            val x = child.mapAnchorX ?: return@mapNotNull null
            val y = child.mapAnchorY ?: return@mapNotNull null
            WorldMapViewState.Pin(
                locationId = child.id,
                name = child.name,
                x = x,
                y = y,
                hasMap = latestWorldMaps.any { it.locationId == child.id },
            )
        }
        val unplaced = children.filter { !it.isPlacedOnParentMap }.map { child ->
            WorldMapViewState.UnplacedChild(locationId = child.id, name = child.name)
        }
        val selected = selectedLocationId?.let { id -> latestLocations.firstOrNull { it.id == id } }
        boundMap?.mapState?.let { mapState ->
            pinOverlay.bind(mapState, pins, selectedLocationId)
        }
        _state.value = WorldMapViewState.Content(
            worldName = worldName,
            locationId = focusedLocationId,
            title = mapTitle(),
            breadcrumbs = breadcrumbs(),
            pins = pins,
            unplacedChildren = unplaced,
            selectedLocationId = selectedLocationId,
            selectedLocationName = selected?.name,
            selectedHasMap = selected != null && latestWorldMaps.any { it.locationId == selected.id },
            placingLocationId = placingLocationId,
            importError = importError,
            pendingDelete = pendingDelete,
        )
    }

    private fun mapTitle(): String {
        val location = focusedLocationId?.let { id -> latestLocations.firstOrNull { it.id == id } }
        return location?.name ?: latestWorldName
    }

    private fun breadcrumbs(): List<WorldMapViewState.Breadcrumb> {
        val crumbs = mutableListOf(
            WorldMapViewState.Breadcrumb(
                locationId = null,
                name = latestWorldName,
                hasMap = latestWorldMaps.any { it.locationId == null },
            )
        )
        val focused = focusedLocationId?.let { id -> latestLocations.firstOrNull { it.id == id } } ?: return crumbs
        pathTo(focused).forEach { location ->
            crumbs += WorldMapViewState.Breadcrumb(
                locationId = location.id,
                name = location.name,
                hasMap = latestWorldMaps.any { it.locationId == location.id },
            )
        }
        return crumbs
    }

    private fun pathTo(location: Location): List<Location> {
        val byId = latestLocations.associateBy { it.id }
        val path = mutableListOf<Location>()
        var current: Location? = location
        val seen = mutableSetOf<String>()
        while (current != null && seen.add(current.id)) {
            path.add(0, current)
            current = current.parentLocationId?.let(byId::get)
        }
        return path
    }

    private fun childLocations(): List<Location> {
        val focusedId = focusedLocationId
        return if (focusedId == null) {
            latestLocations.filter { it.type == LocationType.Continent }
        } else {
            latestLocations.filter { it.parentLocationId == focusedId }
        }
    }

    private fun onPinSelected(locationId: String) {
        val hasMap = latestWorldMaps.any { it.locationId == locationId }
        if (hasMap) {
            openMap(locationId)
            return
        }
        selectedLocationId = locationId
        placingLocationId = null
        render()
    }

    private fun onMapTapped(x: Double, y: Double) {
        val locationId = placingLocationId ?: return
        val clampedX = x.coerceIn(0.0, 1.0)
        val clampedY = y.coerceIn(0.0, 1.0)
        appScope.scope.launch {
            when (updateLocationMapAnchor(locationId, clampedX, clampedY)) {
                UpdateLocationMapAnchorUseCase.Result.Updated -> {
                    placingLocationId = null
                }
                UpdateLocationMapAnchorUseCase.Result.InvalidAnchor,
                UpdateLocationMapAnchorUseCase.Result.NotFound,
                -> Unit
            }
        }
    }

    private fun clearPin(locationId: String) {
        appScope.scope.launch {
            updateLocationMapAnchor(locationId, null, null)
        }
    }

    private fun importImage(path: String) {
        val file = File(path)
        if (!file.isFile) {
            importError = "Choose a PNG image."
            render()
            return
        }
        appScope.scope.launch {
            val bytes = runCatching { file.readBytes() }.getOrNull()
            if (bytes == null) {
                importError = "Could not read that image."
                render()
                return@launch
            }
            when (val result = createWorldMap(WorldMapDraft(locationId = focusedLocationId, imagePng = bytes))) {
                is CreateWorldMapUseCase.Result.Created -> {
                    importError = null
                }
                CreateWorldMapUseCase.Result.InvalidImage -> {
                    importError = "That file is not a readable image."
                    render()
                }
                CreateWorldMapUseCase.Result.NoActiveWorld -> {
                    _state.value = WorldMapViewState.NoActiveWorld
                }
                CreateWorldMapUseCase.Result.LocationNotFound -> {
                    importError = "That location is no longer in this world."
                    render()
                }
            }
        }
    }

    private fun requestDelete() {
        val worldMap = latestWorldMaps.firstOrNull { it.locationId == focusedLocationId } ?: return
        pendingDelete = WorldMapViewState.PendingDelete(
            worldMapId = worldMap.id,
            title = mapTitle(),
        )
        render()
    }

    private fun confirmDelete() {
        val pending = pendingDelete ?: return
        pendingDelete = null
        appScope.scope.launch {
            deleteWorldMap(pending.worldMapId)
        }
    }

    private fun bindMap(worldMap: WorldMap) {
        val existing = boundMap
        if (existing != null && existing.mapId == worldMap.id) {
            return
        }
        shutdownBinding()
        val mapState = mapStateFactory.create(worldMap)
        boundMap = BoundMap(mapId = worldMap.id, mapState = mapState)
    }

    private fun shutdownBinding() {
        boundMap?.let { bound ->
            pinOverlay.clear(bound.mapState)
            bound.mapState.shutdown()
        }
        boundMap = null
    }

    private data class Loaded(
        val details: ActiveContextDetails,
        val locations: List<Location>,
        val worldMaps: List<WorldMap>,
    )

    private data class BoundMap(
        val mapId: String,
        val mapState: MapState,
    )
}
