package net.tactware.worldweaver.ui.worlds

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import net.tactware.worldweaver.core.AppCoroutineScope
import net.tactware.worldweaver.domain.ActiveContext
import net.tactware.worldweaver.domain.CreateWorldUseCase
import net.tactware.worldweaver.domain.DeleteWorldUseCase
import net.tactware.worldweaver.domain.ObserveActiveContextUseCase
import net.tactware.worldweaver.domain.ObserveWorldsUseCase
import net.tactware.worldweaver.domain.SetActiveWorldUseCase
import net.tactware.worldweaver.domain.UpdateWorldUseCase
import net.tactware.worldweaver.domain.World

internal class WorldsViewModel(
    private val appScope: AppCoroutineScope,
    private val observeWorlds: ObserveWorldsUseCase,
    private val observeActiveContext: ObserveActiveContextUseCase,
    private val createWorld: CreateWorldUseCase,
    private val updateWorld: UpdateWorldUseCase,
    private val deleteWorld: DeleteWorldUseCase,
    private val setActiveWorld: SetActiveWorldUseCase,
) {
    private val _state = MutableStateFlow<WorldsViewState>(WorldsViewState.Loading)
    val state: StateFlow<WorldsViewState> = _state.asStateFlow()

    private var observeJob: Job? = null
    private var openCreateOnNextLoad = false

    init {
        observe()
    }

    fun onInteraction(interaction: WorldsInteraction) {
        when (interaction) {
            WorldsInteraction.ScreenStarted -> Unit
            WorldsInteraction.RetrySelected -> observe()
            WorldsInteraction.NewWorldSelected -> openCreateEditor()
            is WorldsInteraction.WorldSelected -> selectWorld(interaction.worldId)
            is WorldsInteraction.EditWorldSelected -> openEditEditor(interaction.worldId)
            is WorldsInteraction.DeleteWorldSelected -> requestDelete(interaction.worldId)
            WorldsInteraction.DeleteConfirmed -> confirmDelete()
            WorldsInteraction.DeleteCancelled -> updateContentOverlays(pendingDelete = null)
            is WorldsInteraction.EditorNameChanged -> updateEditor { editor ->
                editor?.copy(name = interaction.name, nameError = null)
            }
            is WorldsInteraction.EditorDescriptionChanged -> updateEditor { editor ->
                editor?.copy(description = interaction.description)
            }
            WorldsInteraction.EditorSaved -> saveEditor()
            WorldsInteraction.EditorDismissed -> updateEditor { null }
            WorldsInteraction.BlockReasonDismissed -> updateContentOverlays(blockDeleteReason = null)
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = WorldsViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                observeWorlds(),
                observeActiveContext(),
            ) { worlds, context ->
                worlds to context
            }
                .catch { error ->
                    _state.value = WorldsViewState.Error(
                        message = error.message ?: "Could not load worlds",
                        canRetry = true,
                    )
                }
                .collect { (worlds, context) ->
                    applyLoaded(worlds, context)
                }
        }
    }

    private fun applyLoaded(
        worlds: List<World>,
        context: ActiveContext,
    ) {
        val current = _state.value
        val editor = if (openCreateOnNextLoad) {
            openCreateOnNextLoad = false
            createEditor()
        } else {
            editorFrom(current)
        }
        _state.value = if (worlds.isEmpty()) {
            WorldsViewState.Empty(editor = editor)
        } else {
            WorldsViewState.Content(
                worlds = worlds,
                activeWorldId = context.activeWorldId,
                editor = editor,
                pendingDelete = pendingDeleteFrom(current),
                blockDeleteReason = blockReasonFrom(current),
            )
        }
    }

    private fun openCreateEditor() {
        when (val current = _state.value) {
            is WorldsViewState.Empty -> {
                _state.value = current.copy(editor = createEditor())
            }
            is WorldsViewState.Content -> {
                _state.value = current.copy(editor = createEditor())
            }
            WorldsViewState.Loading, is WorldsViewState.Error -> {
                openCreateOnNextLoad = true
            }
        }
    }

    private fun openEditEditor(worldId: String) {
        val world = worldsFrom(_state.value).firstOrNull { it.id == worldId } ?: return
        val editor = WorldsViewState.WorldEditorState(
            worldId = world.id,
            name = world.name,
            description = world.description,
            nameError = null,
        )
        when (val current = _state.value) {
            is WorldsViewState.Content -> _state.value = current.copy(editor = editor)
            else -> Unit
        }
    }

    private fun selectWorld(worldId: String) {
        appScope.scope.launch {
            setActiveWorld(worldId)
        }
    }

    private fun requestDelete(worldId: String) {
        val world = worldsFrom(_state.value).firstOrNull { it.id == worldId } ?: return
        updateContentOverlays(
            pendingDelete = WorldsViewState.PendingDelete(
                worldId = world.id,
                worldName = world.name,
            ),
            blockDeleteReason = null,
        )
    }

    private fun confirmDelete() {
        val pending = pendingDeleteFrom(_state.value) ?: return
        appScope.scope.launch {
            when (val result = deleteWorld(pending.worldId)) {
                DeleteWorldUseCase.Result.Deleted -> {
                    updateContentOverlays(pendingDelete = null, blockDeleteReason = null)
                }
                is DeleteWorldUseCase.Result.Blocked -> {
                    val label = if (result.campaignCount == 1) {
                        "1 campaign"
                    } else {
                        "${result.campaignCount} campaigns"
                    }
                    updateContentOverlays(
                        pendingDelete = null,
                        blockDeleteReason = "Delete or archive campaigns first. This world still has $label.",
                    )
                }
            }
        }
    }

    private fun saveEditor() {
        val editor = editorFrom(_state.value) ?: return
        if (editor.name.trim().isEmpty()) {
            updateEditor { current ->
                current?.copy(nameError = "Name is required")
            }
            return
        }
        appScope.scope.launch {
            val invalidName = if (editor.worldId == null) {
                createWorld(editor.name, editor.description) is CreateWorldUseCase.Result.InvalidName
            } else {
                updateWorld(editor.worldId, editor.name, editor.description) is
                    UpdateWorldUseCase.Result.InvalidName
            }
            if (invalidName) {
                updateEditor { current ->
                    current?.copy(nameError = "Name is required")
                }
            } else {
                updateEditor { null }
            }
        }
    }

    private fun updateEditor(
        transform: (WorldsViewState.WorldEditorState?) -> WorldsViewState.WorldEditorState?,
    ) {
        when (val current = _state.value) {
            is WorldsViewState.Empty -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            is WorldsViewState.Content -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            else -> Unit
        }
    }

    private fun updateContentOverlays(
        pendingDelete: WorldsViewState.PendingDelete? = pendingDeleteFrom(_state.value),
        blockDeleteReason: String? = blockReasonFrom(_state.value),
    ) {
        val current = _state.value
        if (current is WorldsViewState.Content) {
            _state.value = current.copy(
                pendingDelete = pendingDelete,
                blockDeleteReason = blockDeleteReason,
            )
        }
    }

    private fun createEditor(): WorldsViewState.WorldEditorState {
        return WorldsViewState.WorldEditorState(
            worldId = null,
            name = "",
            description = "",
            nameError = null,
        )
    }

    private fun editorFrom(state: WorldsViewState): WorldsViewState.WorldEditorState? {
        return when (state) {
            is WorldsViewState.Empty -> state.editor
            is WorldsViewState.Content -> state.editor
            else -> null
        }
    }

    private fun pendingDeleteFrom(state: WorldsViewState): WorldsViewState.PendingDelete? {
        return (state as? WorldsViewState.Content)?.pendingDelete
    }

    private fun blockReasonFrom(state: WorldsViewState): String? {
        return (state as? WorldsViewState.Content)?.blockDeleteReason
    }

    private fun worldsFrom(state: WorldsViewState): List<World> {
        return (state as? WorldsViewState.Content)?.worlds.orEmpty()
    }
}
