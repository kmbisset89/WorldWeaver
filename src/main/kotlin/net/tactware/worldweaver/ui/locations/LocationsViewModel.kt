package net.tactware.worldweaver.ui.locations

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
import net.tactware.worldweaver.domain.ClearVoiceClipUseCase
import net.tactware.worldweaver.domain.CreateLocationUseCase
import net.tactware.worldweaver.domain.DeleteLocationUseCase
import net.tactware.worldweaver.domain.Location
import net.tactware.worldweaver.domain.LocationDraft
import net.tactware.worldweaver.domain.LocationOverlay
import net.tactware.worldweaver.domain.LocationType
import net.tactware.worldweaver.domain.Lore
import net.tactware.worldweaver.domain.ObserveActiveContextDetailsUseCase
import net.tactware.worldweaver.domain.ObserveLocationOverlaysForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveLocationsForActiveWorldUseCase
import net.tactware.worldweaver.domain.ObserveLoreForActiveWorldUseCase
import net.tactware.worldweaver.domain.ObserveQuestsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.Quest
import net.tactware.worldweaver.domain.SetVoiceClipUseCase
import net.tactware.worldweaver.domain.UpdateLocationOverlayUseCase
import net.tactware.worldweaver.domain.UpdateLocationUseCase
import net.tactware.worldweaver.domain.VoiceClipFileStore
import net.tactware.worldweaver.domain.VoiceClipPlayer
import net.tactware.worldweaver.domain.VoiceClipRecorder
import net.tactware.worldweaver.domain.VoiceClipRef

internal class LocationsViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeLocations: ObserveLocationsForActiveWorldUseCase,
    private val observeOverlays: ObserveLocationOverlaysForActiveCampaignUseCase,
    private val observeLore: ObserveLoreForActiveWorldUseCase,
    private val observeQuests: ObserveQuestsForActiveCampaignUseCase,
    private val createLocation: CreateLocationUseCase,
    private val updateLocation: UpdateLocationUseCase,
    private val deleteLocation: DeleteLocationUseCase,
    private val updateLocationOverlay: UpdateLocationOverlayUseCase,
    private val setVoiceClip: SetVoiceClipUseCase,
    private val clearVoiceClip: ClearVoiceClipUseCase,
    private val voiceClipFileStore: VoiceClipFileStore,
    private val voiceClipRecorder: VoiceClipRecorder,
    private val voiceClipPlayer: VoiceClipPlayer,
) {
    private val _state = MutableStateFlow<LocationsViewState>(LocationsViewState.Loading)
    val state: StateFlow<LocationsViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<LocationsViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<LocationsViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var openCreateOnNextLoad = false
    private var searchQuery = ""
    private var typeFilter: LocationType? = null
    private var selectedLocationId: String? = null
    private var overlayNotesDraft: String? = null
    private var latestLocations: List<Location> = emptyList()
    private var latestOverlays: List<LocationOverlay> = emptyList()
    private var latestLore: List<Lore> = emptyList()
    private var latestQuests: List<Quest> = emptyList()
    private var latestCampaignName: String? = null
    private var latestWorldName: String = ""
    private var isRecordingVoice = false
    private var isPlayingVoice = false

    init {
        observe()
    }

    fun onInteraction(interaction: LocationsInteraction) {
        when (interaction) {
            LocationsInteraction.ScreenStarted -> Unit
            LocationsInteraction.RetrySelected -> observe()
            LocationsInteraction.CreateWorldSelected -> {
                _effects.tryEmit(LocationsViewEffect.OpenWorlds)
            }
            LocationsInteraction.NewLocationSelected -> openCreateEditor()
            is LocationsInteraction.LocationSelected,
            is LocationsInteraction.LocationOpened,
            -> selectLocation(locationIdFrom(interaction))
            is LocationsInteraction.BreadcrumbSelected -> selectLocation(interaction.locationId)
            is LocationsInteraction.EditLocationSelected -> openEditEditor(interaction.locationId)
            is LocationsInteraction.DeleteLocationSelected -> requestDelete(interaction.locationId)
            LocationsInteraction.DeleteConfirmed -> confirmDelete()
            LocationsInteraction.DeleteCancelled -> updateContentOverlays(pendingDelete = null)
            LocationsInteraction.BlockReasonDismissed -> updateContentOverlays(blockDeleteReason = null)
            is LocationsInteraction.SearchQueryChanged -> {
                searchQuery = interaction.query
                refreshContent()
            }
            is LocationsInteraction.TypeFilterSelected -> {
                typeFilter = interaction.type
                refreshContent()
            }
            is LocationsInteraction.EditorNameChanged -> updateEditor { editor ->
                editor?.copy(name = interaction.name, nameError = null)
            }
            is LocationsInteraction.EditorTypeSelected -> updateEditor { editor ->
                editor?.let { changeEditorType(it, interaction.type) }
            }
            is LocationsInteraction.EditorParentSelected -> updateEditor { editor ->
                editor?.copy(parentLocationId = interaction.parentLocationId, parentError = null)
            }
            is LocationsInteraction.EditorDescriptionChanged -> updateEditor { editor ->
                editor?.copy(description = interaction.description)
            }
            is LocationsInteraction.EditorClimateChanged -> updateEditor { editor ->
                editor?.copy(climate = interaction.climate)
            }
            is LocationsInteraction.EditorTerrainChanged -> updateEditor { editor ->
                editor?.copy(terrain = interaction.terrain)
            }
            is LocationsInteraction.EditorGovernmentChanged -> updateEditor { editor ->
                editor?.copy(government = interaction.government)
            }
            is LocationsInteraction.EditorLandmarksChanged -> updateEditor { editor ->
                editor?.copy(landmarksText = interaction.landmarks)
            }
            is LocationsInteraction.EditorHistoryChanged -> updateEditor { editor ->
                editor?.copy(history = interaction.history)
            }
            is LocationsInteraction.EditorNotesChanged -> updateEditor { editor ->
                editor?.copy(notes = interaction.notes)
            }
            LocationsInteraction.EditorSaved -> saveEditor()
            LocationsInteraction.EditorDismissed -> updateEditor { null }
            is LocationsInteraction.OverlayPartyPresenceChanged -> {
                persistOverlay(hasPartyPresence = interaction.hasPartyPresence)
            }
            is LocationsInteraction.OverlayNotesChanged -> {
                overlayNotesDraft = interaction.notes
                refreshContent()
            }
            LocationsInteraction.OverlaySaved -> persistOverlay(hasPartyPresence = null)
            is LocationsInteraction.AttachedLoreSelected -> {
                _effects.tryEmit(LocationsViewEffect.OpenLore(interaction.loreId))
            }
            is LocationsInteraction.AttachedQuestSelected -> {
                _effects.tryEmit(LocationsViewEffect.OpenQuest(interaction.questId))
            }
            is LocationsInteraction.VoiceClipAttached -> saveVoiceClip(interaction.path)
            LocationsInteraction.VoiceClipRecordToggled -> toggleVoiceRecord()
            LocationsInteraction.VoiceClipPlayToggled -> toggleVoicePlay()
            LocationsInteraction.VoiceClipRemoved -> removeVoiceClip()
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = LocationsViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                observeActiveContextDetails(),
                observeLocations(),
                observeOverlays(),
                observeLore(),
                observeQuests(),
            ) { details, locations, overlays, lore, quests ->
                LoadedSnapshot(details, locations, overlays, lore, quests)
            }
                .catch { error ->
                    _state.value = LocationsViewState.Error(
                        message = error.message ?: "Could not load locations",
                        canRetry = true,
                    )
                }
                .collect { snapshot ->
                    applyLoaded(
                        snapshot.details,
                        snapshot.locations,
                        snapshot.overlays,
                        snapshot.lore,
                        snapshot.quests,
                    )
                }
        }
    }

    private fun applyLoaded(
        details: ActiveContextDetails,
        locations: List<Location>,
        overlays: List<LocationOverlay>,
        lore: List<Lore>,
        quests: List<Quest>,
    ) {
        val world = details.world
        if (world == null) {
            latestLocations = emptyList()
            latestLore = emptyList()
            selectedLocationId = null
            overlayNotesDraft = null
            _state.value = LocationsViewState.NoActiveWorld
            return
        }
        latestLocations = locations
        latestOverlays = overlays
        latestLore = lore
        latestQuests = quests
        latestCampaignName = details.campaign?.name
        latestWorldName = world.name
        val current = _state.value
        val editor = if (openCreateOnNextLoad) {
            openCreateOnNextLoad = false
            createEditor(locations, selectedFrom(locations))
        } else {
            editorFrom(current)?.let { refreshEditorParents(it, locations) }
        }
        if (locations.isEmpty()) {
            selectedLocationId = null
            overlayNotesDraft = null
            _state.value = LocationsViewState.Empty(
                worldName = world.name,
                editor = editor,
            )
            return
        }
        val matched = selectedFrom(locations)
        val selected = matched ?: locations.first()
        if (matched != null || selectedLocationId == null) {
            if (selectedLocationId != selected.id) {
                selectedLocationId = selected.id
                overlayNotesDraft = null
            }
        }
        _state.value = contentState(
            worldName = world.name,
            campaignName = details.campaign?.name,
            locations = locations,
            selected = selected,
            overlays = overlays,
            editor = editor,
            pendingDelete = pendingDeleteFrom(current),
            blockDeleteReason = blockReasonFrom(current),
        )
    }

    private fun refreshContent() {
        val current = _state.value
        if (current !is LocationsViewState.Content) {
            return
        }
        val selected = selectedFrom(latestLocations) ?: latestLocations.firstOrNull()
        _state.value = contentState(
            worldName = latestWorldName,
            campaignName = latestCampaignName,
            locations = latestLocations,
            selected = selected,
            overlays = latestOverlays,
            editor = current.editor,
            pendingDelete = current.pendingDelete,
            blockDeleteReason = current.blockDeleteReason,
        )
    }

    private fun contentState(
        worldName: String,
        campaignName: String?,
        locations: List<Location>,
        selected: Location?,
        overlays: List<LocationOverlay>,
        editor: LocationsViewState.LocationEditorState?,
        pendingDelete: LocationsViewState.PendingDelete?,
        blockDeleteReason: String?,
    ): LocationsViewState.Content {
        val visible = visibleLocations(locations, searchQuery, typeFilter)
        val storedOverlay = selected?.let { location ->
            overlays.firstOrNull { it.locationId == location.id }
        }
        val overlay = if (campaignName != null && selected != null) {
            LocationsViewState.OverlayState(
                campaignName = campaignName,
                hasPartyPresence = storedOverlay?.hasPartyPresence ?: false,
                notes = overlayNotesDraft ?: storedOverlay?.notes.orEmpty(),
            )
        } else {
            null
        }
        return LocationsViewState.Content(
            worldName = worldName,
            campaignName = campaignName,
            locations = locations,
            visibleTree = buildTree(visible),
            selectedLocation = selected,
            breadcrumbs = breadcrumbs(selected, locations),
            searchQuery = searchQuery,
            typeFilter = typeFilter,
            overlay = overlay,
            attachedLore = selected?.let { location ->
                latestLore
                    .filter { it.locationId == location.id }
                    .map { entry ->
                        LocationsViewState.AttachedLore(
                            loreId = entry.id,
                            title = entry.title,
                        )
                    }
            }.orEmpty(),
            attachedQuests = selected?.let { location ->
                latestQuests
                    .filter { it.locationId == location.id }
                    .map { quest ->
                        LocationsViewState.AttachedQuest(
                            questId = quest.id,
                            title = quest.title,
                        )
                    }
            }.orEmpty(),
            voiceClipPath = selected?.let { location ->
                voiceClipFileStore.pathIfPresent(VoiceClipRef.Location(location.id))
            },
            isRecordingVoice = isRecordingVoice,
            isPlayingVoice = isPlayingVoice,
            editor = editor,
            pendingDelete = pendingDelete,
            blockDeleteReason = blockDeleteReason,
        )
    }

    private fun locationIdFrom(interaction: LocationsInteraction): String {
        return when (interaction) {
            is LocationsInteraction.LocationSelected -> interaction.locationId
            is LocationsInteraction.LocationOpened -> interaction.locationId
            else -> ""
        }
    }

    private fun selectLocation(locationId: String) {
        if (selectedLocationId == locationId) {
            return
        }
        stopVoiceSession()
        selectedLocationId = locationId
        overlayNotesDraft = null
        refreshContent()
    }

    private fun saveVoiceClip(path: String) {
        val locationId = selectedLocationId ?: return
        val file = java.io.File(path)
        if (!file.isFile) {
            return
        }
        stopVoicePlayback()
        if (isRecordingVoice) {
            voiceClipRecorder.stop()
            isRecordingVoice = false
        }
        appScope.scope.launch {
            setVoiceClip(VoiceClipRef.Location(locationId), file.readBytes())
            refreshContent()
        }
    }

    private fun toggleVoiceRecord() {
        val locationId = selectedLocationId ?: return
        if (isRecordingVoice) {
            val wav = voiceClipRecorder.stop()
            isRecordingVoice = false
            if (wav != null) {
                appScope.scope.launch {
                    setVoiceClip(VoiceClipRef.Location(locationId), wav)
                    refreshContent()
                }
            } else {
                refreshContent()
            }
            return
        }
        stopVoicePlayback()
        isRecordingVoice = voiceClipRecorder.start()
        refreshContent()
    }

    private fun toggleVoicePlay() {
        val locationId = selectedLocationId ?: return
        if (isPlayingVoice) {
            stopVoicePlayback()
            refreshContent()
            return
        }
        val path = voiceClipFileStore.pathIfPresent(VoiceClipRef.Location(locationId)) ?: return
        isPlayingVoice = voiceClipPlayer.play(path) {
            appScope.scope.launch {
                isPlayingVoice = false
                refreshContent()
            }
        }
        refreshContent()
    }

    private fun removeVoiceClip() {
        val locationId = selectedLocationId ?: return
        stopVoicePlayback()
        appScope.scope.launch {
            clearVoiceClip(VoiceClipRef.Location(locationId))
            refreshContent()
        }
    }

    private fun stopVoiceSession() {
        if (isRecordingVoice) {
            voiceClipRecorder.stop()
            isRecordingVoice = false
        }
        stopVoicePlayback()
    }

    private fun stopVoicePlayback() {
        if (isPlayingVoice) {
            voiceClipPlayer.stop()
            isPlayingVoice = false
        }
    }

    private fun openCreateEditor() {
        when (val current = _state.value) {
            is LocationsViewState.Empty -> {
                _state.value = current.copy(editor = createEditor(emptyList(), null))
            }
            is LocationsViewState.Content -> {
                _state.value = current.copy(
                    editor = createEditor(current.locations, current.selectedLocation),
                )
            }
            LocationsViewState.Loading, is LocationsViewState.Error -> {
                openCreateOnNextLoad = true
            }
            LocationsViewState.NoActiveWorld -> Unit
        }
    }

    private fun openEditEditor(locationId: String) {
        val location = latestLocations.firstOrNull { it.id == locationId } ?: return
        val editor = LocationsViewState.LocationEditorState(
            locationId = location.id,
            type = location.type,
            parentLocationId = location.parentLocationId,
            parentOptions = parentOptions(location.type, latestLocations, location.id),
            name = location.name,
            description = location.description,
            climate = location.climate,
            terrain = location.terrain,
            government = location.government,
            landmarksText = location.landmarks.joinToString("\n"),
            history = location.history,
            notes = location.notes,
            nameError = null,
            parentError = null,
        )
        when (val current = _state.value) {
            is LocationsViewState.Content -> _state.value = current.copy(editor = editor)
            else -> Unit
        }
    }

    private fun requestDelete(locationId: String) {
        val location = latestLocations.firstOrNull { it.id == locationId } ?: return
        updateContentOverlays(
            pendingDelete = LocationsViewState.PendingDelete(
                locationId = location.id,
                locationName = location.name,
            ),
            blockDeleteReason = null,
        )
    }

    private fun confirmDelete() {
        val pending = pendingDeleteFrom(_state.value) ?: return
        appScope.scope.launch {
            when (val result = deleteLocation(pending.locationId)) {
                DeleteLocationUseCase.Result.Deleted,
                DeleteLocationUseCase.Result.NotFound,
                -> {
                    if (selectedLocationId == pending.locationId) {
                        selectedLocationId = null
                        overlayNotesDraft = null
                    }
                    updateContentOverlays(pendingDelete = null, blockDeleteReason = null)
                }
                is DeleteLocationUseCase.Result.Blocked -> {
                    val label = if (result.childCount == 1) {
                        "1 child location"
                    } else {
                        "${result.childCount} child locations"
                    }
                    updateContentOverlays(
                        pendingDelete = null,
                        blockDeleteReason = "Move or delete child locations first. This location still has $label.",
                    )
                }
            }
        }
    }

    private fun persistOverlay(hasPartyPresence: Boolean?) {
        val current = _state.value as? LocationsViewState.Content ?: return
        val selected = current.selectedLocation ?: return
        val overlay = current.overlay ?: return
        val presence = hasPartyPresence ?: overlay.hasPartyPresence
        if (hasPartyPresence != null) {
            _state.value = current.copy(
                overlay = overlay.copy(hasPartyPresence = presence),
            )
        }
        appScope.scope.launch {
            updateLocationOverlay(selected.id, presence, overlay.notes)
        }
    }

    private fun saveEditor() {
        val editor = editorFrom(_state.value) ?: return
        if (editor.name.trim().isEmpty()) {
            updateEditor { current -> current?.copy(nameError = "Name is required") }
            return
        }
        val draft = draftFrom(editor)
        appScope.scope.launch {
            val result = if (editor.locationId == null) {
                createLocation(draft)
            } else {
                updateLocation(editor.locationId, draft)
            }
            when (result) {
                is CreateLocationUseCase.Result.Created -> {
                    selectedLocationId = result.location.id
                    updateEditor { null }
                }
                CreateLocationUseCase.Result.InvalidName,
                UpdateLocationUseCase.Result.InvalidName,
                -> updateEditor { current -> current?.copy(nameError = "Name is required") }
                CreateLocationUseCase.Result.InvalidParent,
                UpdateLocationUseCase.Result.InvalidParent,
                -> updateEditor { current ->
                    current?.copy(parentError = parentErrorMessage(current.type))
                }
                CreateLocationUseCase.Result.NoActiveWorld -> updateEditor { null }
                UpdateLocationUseCase.Result.Updated,
                UpdateLocationUseCase.Result.NotFound,
                -> updateEditor { null }
            }
        }
    }

    private fun changeEditorType(
        editor: LocationsViewState.LocationEditorState,
        type: LocationType,
    ): LocationsViewState.LocationEditorState {
        val options = parentOptions(type, latestLocations, editor.locationId)
        val parentStillValid = options.any { it.id == editor.parentLocationId }
        return editor.copy(
            type = type,
            parentLocationId = editor.parentLocationId.takeIf { parentStillValid },
            parentOptions = options,
            parentError = null,
        )
    }

    private fun createEditor(
        locations: List<Location>,
        selected: Location?,
    ): LocationsViewState.LocationEditorState {
        val type = selected?.type?.let { childType(it) } ?: LocationType.Continent
        val parentId = selected?.id.takeIf { type.acceptsParent(selected) }
        return LocationsViewState.LocationEditorState(
            locationId = null,
            type = type,
            parentLocationId = parentId,
            parentOptions = parentOptions(type, locations, locationId = null),
            name = "",
            description = "",
            climate = "",
            terrain = "",
            government = "",
            landmarksText = "",
            history = "",
            notes = "",
            nameError = null,
            parentError = null,
        )
    }

    private fun refreshEditorParents(
        editor: LocationsViewState.LocationEditorState,
        locations: List<Location>,
    ): LocationsViewState.LocationEditorState {
        return editor.copy(
            parentOptions = parentOptions(editor.type, locations, editor.locationId),
        )
    }

    private fun parentOptions(
        type: LocationType,
        locations: List<Location>,
        locationId: String?,
    ): List<Location> {
        val required = type.requiredParentType() ?: return emptyList()
        return locations.filter { location ->
            location.type == required && location.id != locationId
        }
    }

    private fun childType(type: LocationType): LocationType {
        return when (type) {
            LocationType.Continent -> LocationType.Area
            LocationType.Area -> LocationType.City
            LocationType.City -> LocationType.Place
            LocationType.Place -> LocationType.Place
        }
    }

    private fun draftFrom(editor: LocationsViewState.LocationEditorState): LocationDraft {
        return LocationDraft(
            type = editor.type,
            parentLocationId = editor.parentLocationId,
            name = editor.name,
            description = editor.description,
            climate = editor.climate,
            terrain = editor.terrain,
            government = editor.government,
            landmarks = editor.landmarksText.lines(),
            history = editor.history,
            notes = editor.notes,
        )
    }

    private fun parentErrorMessage(type: LocationType): String {
        val required = type.requiredParentType()
        return if (required == null) {
            "A continent cannot have a parent."
        } else {
            "${type.displayName} must be under a ${required.displayName.lowercase()}."
        }
    }

    private fun visibleLocations(
        locations: List<Location>,
        query: String,
        type: LocationType?,
    ): List<Location> {
        val trimmed = query.trim()
        if (trimmed.isEmpty() && type == null) {
            return locations
        }
        val matches = locations.filter { location ->
            val typeMatches = type == null || location.type == type
            val textMatches = trimmed.isEmpty() || locationMatches(location, trimmed)
            typeMatches && textMatches
        }
        val byId = locations.associateBy { it.id }
        val included = LinkedHashMap<String, Location>()
        matches.forEach { match ->
            var current: Location? = match
            while (current != null && included.put(current.id, current) == null) {
                current = current.parentLocationId?.let { byId[it] }
            }
        }
        return included.values.toList()
    }

    private fun locationMatches(location: Location, query: String): Boolean {
        val haystack = buildList {
            add(location.name)
            add(location.description)
            add(location.climate)
            add(location.terrain)
            add(location.government)
            add(location.history)
            add(location.notes)
            addAll(location.landmarks)
        }
        return haystack.any { it.contains(query, ignoreCase = true) }
    }

    private fun buildTree(locations: List<Location>): List<LocationsViewState.LocationTreeNode> {
        val ids = locations.map { it.id }.toSet()
        val children = locations.groupBy { it.parentLocationId }
        fun nodesFor(parentId: String?): List<LocationsViewState.LocationTreeNode> {
            return children[parentId]
                .orEmpty()
                .sortedBy { it.name }
                .map { location ->
                    LocationsViewState.LocationTreeNode(
                        location = location,
                        children = nodesFor(location.id),
                    )
                }
        }
        val roots = locations.filter { location ->
            location.parentLocationId == null || location.parentLocationId !in ids
        }
        return roots.sortedBy { it.name }.map { location ->
            LocationsViewState.LocationTreeNode(
                location = location,
                children = nodesFor(location.id),
            )
        }
    }

    private fun breadcrumbs(
        selected: Location?,
        locations: List<Location>,
    ): List<Location> {
        if (selected == null) {
            return emptyList()
        }
        val byId = locations.associateBy { it.id }
        val path = ArrayDeque<Location>()
        var current: Location? = selected
        val seen = mutableSetOf<String>()
        while (current != null && seen.add(current.id)) {
            path.addFirst(current)
            current = current.parentLocationId?.let { byId[it] }
        }
        return path.toList()
    }

    private fun updateEditor(
        transform: (
            LocationsViewState.LocationEditorState?,
        ) -> LocationsViewState.LocationEditorState?,
    ) {
        when (val current = _state.value) {
            is LocationsViewState.Empty -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            is LocationsViewState.Content -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            else -> Unit
        }
    }

    private fun updateContentOverlays(
        pendingDelete: LocationsViewState.PendingDelete? = pendingDeleteFrom(_state.value),
        blockDeleteReason: String? = blockReasonFrom(_state.value),
    ) {
        val current = _state.value
        if (current is LocationsViewState.Content) {
            _state.value = current.copy(
                pendingDelete = pendingDelete,
                blockDeleteReason = blockDeleteReason,
            )
        }
    }

    private fun selectedFrom(locations: List<Location>): Location? {
        return selectedLocationId?.let { id -> locations.firstOrNull { it.id == id } }
    }

    private fun editorFrom(state: LocationsViewState): LocationsViewState.LocationEditorState? {
        return when (state) {
            is LocationsViewState.Empty -> state.editor
            is LocationsViewState.Content -> state.editor
            else -> null
        }
    }

    private fun pendingDeleteFrom(state: LocationsViewState): LocationsViewState.PendingDelete? {
        return (state as? LocationsViewState.Content)?.pendingDelete
    }

    private fun blockReasonFrom(state: LocationsViewState): String? {
        return (state as? LocationsViewState.Content)?.blockDeleteReason
    }

    private data class LoadedSnapshot(
        val details: ActiveContextDetails,
        val locations: List<Location>,
        val overlays: List<LocationOverlay>,
        val lore: List<Lore>,
        val quests: List<Quest>,
    )
}
