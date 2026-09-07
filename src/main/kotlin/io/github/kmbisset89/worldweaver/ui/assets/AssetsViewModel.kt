package io.github.kmbisset89.worldweaver.ui.assets

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
import io.github.kmbisset89.worldweaver.domain.CreateAssetUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteAssetUseCase
import io.github.kmbisset89.worldweaver.domain.Asset
import io.github.kmbisset89.worldweaver.domain.AssetDraft
import io.github.kmbisset89.worldweaver.domain.AssetFileStore
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveAssetsForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateAssetUseCase
import java.io.File

internal class AssetsViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeAssets: ObserveAssetsForActiveWorldUseCase,
    private val createAsset: CreateAssetUseCase,
    private val updateAsset: UpdateAssetUseCase,
    private val deleteAsset: DeleteAssetUseCase,
    private val assetFileStore: AssetFileStore,
) {
    private val _state = MutableStateFlow<AssetsViewState>(AssetsViewState.Loading)
    val state: StateFlow<AssetsViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AssetsViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<AssetsViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var saveJob: Job? = null
    private var selectedAssetId: String? = null
    private var latestAssets: List<Asset> = emptyList()
    private var latestWorldName: String = ""
    private var draftName: String? = null
    private var draftNotes: String? = null
    private var nameError: String? = null
    private var pendingDelete: AssetsViewState.PendingDelete? = null

    init {
        observe()
    }

    fun onInteraction(interaction: AssetsInteraction) {
        when (interaction) {
            AssetsInteraction.ScreenStarted -> Unit
            AssetsInteraction.RetrySelected -> observe()
            AssetsInteraction.CreateWorldSelected -> {
                _effects.tryEmit(AssetsViewEffect.OpenWorlds)
            }
            is AssetsInteraction.FilesChosen -> importFiles(interaction.paths)
            is AssetsInteraction.AssetSelected,
            is AssetsInteraction.AssetOpened,
            -> selectAsset(selectedId(interaction))
            is AssetsInteraction.DisplayNameChanged -> {
                draftName = interaction.name
                nameError = null
                refreshContent()
                scheduleSave()
            }
            is AssetsInteraction.NotesChanged -> {
                draftNotes = interaction.notes
                refreshContent()
                scheduleSave()
            }
            is AssetsInteraction.OpenFileSelected -> openFile(interaction.assetId)
            is AssetsInteraction.DeleteAssetSelected -> requestDelete(interaction.assetId)
            AssetsInteraction.DeleteConfirmed -> confirmDelete()
            AssetsInteraction.DeleteCancelled -> {
                pendingDelete = null
                refreshContent()
            }
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = AssetsViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                observeActiveContextDetails(),
                observeAssets(),
            ) { details, assets ->
                details to assets
            }
                .catch { error ->
                    _state.value = AssetsViewState.Error(
                        message = error.message ?: "Could not load assets",
                        canRetry = true,
                    )
                }
                .collect { (details, assets) ->
                    applyLoaded(details, assets)
                }
        }
    }

    private fun applyLoaded(
        details: ActiveContextDetails,
        assets: List<Asset>,
    ) {
        val world = details.world
        if (world == null) {
            latestAssets = emptyList()
            selectedAssetId = null
            draftName = null
            draftNotes = null
            pendingDelete = null
            _state.value = AssetsViewState.NoActiveWorld
            return
        }
        latestAssets = assets
        latestWorldName = world.name
        if (assets.isEmpty()) {
            selectedAssetId = null
            draftName = null
            draftNotes = null
            pendingDelete = null
            _state.value = AssetsViewState.Empty(worldName = world.name)
            return
        }
        val selected = selectedFrom(assets) ?: assets.first()
        if (selectedAssetId != selected.id) {
            selectedAssetId = selected.id
            draftName = null
            draftNotes = null
            nameError = null
        }
        refreshContent()
    }

    private fun refreshContent() {
        val selected = selectedFrom(latestAssets)
        if (latestAssets.isEmpty()) {
            _state.value = AssetsViewState.Empty(worldName = latestWorldName)
            return
        }
        val items = latestAssets.map(::toItem)
        _state.value = AssetsViewState.Content(
            worldName = latestWorldName,
            assets = items,
            selectedAsset = selected?.let(::toItem),
            pendingDelete = pendingDelete,
            nameError = nameError,
        )
    }

    private fun toItem(asset: Asset): AssetsViewState.AssetItem {
        val isSelected = asset.id == selectedAssetId
        return AssetsViewState.AssetItem(
            id = asset.id,
            displayName = if (isSelected) draftName ?: asset.displayName else asset.displayName,
            originalFileName = asset.originalFileName,
            notes = if (isSelected) draftNotes ?: asset.notes else asset.notes,
            sizeLabel = sizeLabel(asset.byteSize),
            isImage = asset.isImage,
            filePath = assetFileStore.pathIfPresent(asset.id, asset.originalFileName),
        )
    }

    private fun selectAsset(assetId: String) {
        if (assetId.isEmpty() || selectedAssetId == assetId) {
            return
        }
        saveJob?.cancel()
        saveNow()
        selectedAssetId = assetId
        draftName = null
        draftNotes = null
        nameError = null
        refreshContent()
    }

    private fun importFiles(paths: List<String>) {
        if (paths.isEmpty()) {
            return
        }
        appScope.scope.launch {
            var lastId: String? = null
            paths.forEach { path ->
                when (val result = createAsset(File(path))) {
                    is CreateAssetUseCase.Result.Created -> lastId = result.asset.id
                    CreateAssetUseCase.Result.NoActiveWorld -> {
                        _effects.tryEmit(AssetsViewEffect.Failed("Select a world first."))
                        return@launch
                    }
                    CreateAssetUseCase.Result.MissingFile -> {
                        _effects.tryEmit(AssetsViewEffect.Failed("Could not read that file."))
                    }
                    CreateAssetUseCase.Result.EmptyFile -> {
                        _effects.tryEmit(AssetsViewEffect.Failed("That file is empty."))
                    }
                }
            }
            lastId?.let { selectedAssetId = it }
        }
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        saveJob = appScope.scope.launch {
            delay(SAVE_DELAY_MS)
            saveNow()
        }
    }

    private fun saveNow() {
        val assetId = selectedAssetId ?: return
        val name = draftName
        val notes = draftNotes
        if (name == null && notes == null) {
            return
        }
        val existing = latestAssets.firstOrNull { it.id == assetId } ?: return
        appScope.scope.launch {
            when (
                updateAsset(
                    assetId,
                    AssetDraft(
                        displayName = name ?: existing.displayName,
                        notes = notes ?: existing.notes,
                    ),
                )
            ) {
                UpdateAssetUseCase.Result.Updated -> {
                    nameError = null
                }
                UpdateAssetUseCase.Result.InvalidName -> {
                    nameError = "Name is required"
                    refreshContent()
                }
                UpdateAssetUseCase.Result.NotFound -> Unit
            }
        }
    }

    private fun openFile(assetId: String) {
        val asset = latestAssets.firstOrNull { it.id == assetId } ?: return
        val path = assetFileStore.pathIfPresent(asset.id, asset.originalFileName)
        if (path == null) {
            _effects.tryEmit(AssetsViewEffect.Failed("That file is no longer on disk."))
            return
        }
        _effects.tryEmit(AssetsViewEffect.OpenFile(path))
    }

    private fun requestDelete(assetId: String) {
        val asset = latestAssets.firstOrNull { it.id == assetId } ?: return
        pendingDelete = AssetsViewState.PendingDelete(
            assetId = asset.id,
            displayName = asset.displayName,
        )
        refreshContent()
    }

    private fun confirmDelete() {
        val pending = pendingDelete ?: return
        pendingDelete = null
        appScope.scope.launch {
            deleteAsset(pending.assetId)
            if (selectedAssetId == pending.assetId) {
                selectedAssetId = null
                draftName = null
                draftNotes = null
            }
        }
    }

    private fun selectedId(interaction: AssetsInteraction): String {
        return when (interaction) {
            is AssetsInteraction.AssetSelected -> interaction.assetId
            is AssetsInteraction.AssetOpened -> interaction.assetId
            else -> ""
        }
    }

    private fun selectedFrom(assets: List<Asset>): Asset? {
        return selectedAssetId?.let { id -> assets.firstOrNull { it.id == id } }
    }

    private fun sizeLabel(bytes: Long): String {
        if (bytes < 1024) {
            return "$bytes B"
        }
        if (bytes < 1024 * 1024) {
            return "${bytes / 1024} KB"
        }
        return "${bytes / (1024 * 1024)} MB"
    }

    private companion object {
        const val SAVE_DELAY_MS = 400L
    }
}
