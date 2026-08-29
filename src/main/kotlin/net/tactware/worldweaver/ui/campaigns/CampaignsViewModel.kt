package net.tactware.worldweaver.ui.campaigns

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
import net.tactware.worldweaver.domain.Campaign
import net.tactware.worldweaver.domain.CampaignStatus
import net.tactware.worldweaver.domain.CreateCampaignUseCase
import net.tactware.worldweaver.domain.DeleteCampaignUseCase
import net.tactware.worldweaver.domain.ObserveActiveContextDetailsUseCase
import net.tactware.worldweaver.domain.ObserveCampaignsForActiveWorldUseCase
import net.tactware.worldweaver.domain.SetActiveCampaignUseCase
import net.tactware.worldweaver.domain.SetCampaignStatusUseCase
import net.tactware.worldweaver.domain.UpdateCampaignUseCase

internal class CampaignsViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeCampaigns: ObserveCampaignsForActiveWorldUseCase,
    private val createCampaign: CreateCampaignUseCase,
    private val updateCampaign: UpdateCampaignUseCase,
    private val setCampaignStatus: SetCampaignStatusUseCase,
    private val deleteCampaign: DeleteCampaignUseCase,
    private val setActiveCampaign: SetActiveCampaignUseCase,
) {
    private val _state = MutableStateFlow<CampaignsViewState>(CampaignsViewState.Loading)
    val state: StateFlow<CampaignsViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CampaignsViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<CampaignsViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var showRetired = false
    private var openCreateOnNextLoad = false

    init {
        observe()
    }

    fun onInteraction(interaction: CampaignsInteraction) {
        when (interaction) {
            CampaignsInteraction.ScreenStarted -> Unit
            CampaignsInteraction.RetrySelected -> observe()
            CampaignsInteraction.CreateWorldSelected -> {
                _effects.tryEmit(CampaignsViewEffect.OpenWorlds)
            }
            CampaignsInteraction.NewCampaignSelected -> openCreateEditor()
            is CampaignsInteraction.CampaignSelected -> selectCampaign(interaction.campaignId)
            is CampaignsInteraction.EditCampaignSelected -> openEditEditor(interaction.campaignId)
            is CampaignsInteraction.DeleteCampaignSelected -> requestDelete(interaction.campaignId)
            CampaignsInteraction.DeleteConfirmed -> confirmDelete()
            CampaignsInteraction.DeleteCancelled -> updateContentOverlays(pendingDelete = null)
            is CampaignsInteraction.StatusSelected -> changeStatus(
                interaction.campaignId,
                interaction.status,
            )
            CampaignsInteraction.RetiredVisibilityToggled -> {
                showRetired = !showRetired
                val current = _state.value
                if (current is CampaignsViewState.Content) {
                    _state.value = current.copy(showRetired = showRetired)
                }
            }
            is CampaignsInteraction.EditorNameChanged -> updateEditor { editor ->
                editor?.copy(name = interaction.name, nameError = null)
            }
            is CampaignsInteraction.EditorDescriptionChanged -> updateEditor { editor ->
                editor?.copy(description = interaction.description)
            }
            is CampaignsInteraction.EditorNotesChanged -> updateEditor { editor ->
                editor?.copy(notes = interaction.notes)
            }
            CampaignsInteraction.EditorSaved -> saveEditor()
            CampaignsInteraction.EditorDismissed -> updateEditor { null }
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = CampaignsViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                observeActiveContextDetails(),
                observeCampaigns(),
            ) { details, campaigns ->
                details to campaigns
            }
                .catch { error ->
                    _state.value = CampaignsViewState.Error(
                        message = error.message ?: "Could not load campaigns",
                        canRetry = true,
                    )
                }
                .collect { (details, campaigns) ->
                    applyLoaded(details, campaigns)
                }
        }
    }

    private fun applyLoaded(
        details: ActiveContextDetails,
        campaigns: List<Campaign>,
    ) {
        val world = details.world
        if (world == null) {
            _state.value = CampaignsViewState.NoActiveWorld
            return
        }
        val current = _state.value
        val editor = if (openCreateOnNextLoad) {
            openCreateOnNextLoad = false
            createEditor()
        } else {
            editorFrom(current)
        }
        if (campaigns.isEmpty()) {
            _state.value = CampaignsViewState.Empty(
                worldName = world.name,
                editor = editor,
            )
            return
        }
        val selected = details.campaign ?: campaigns.firstOrNull { it.status == CampaignStatus.Active }
        _state.value = CampaignsViewState.Content(
            worldName = world.name,
            campaigns = campaigns,
            selectedCampaign = selected,
            showRetired = showRetired,
            editor = editor,
            pendingDelete = pendingDeleteFrom(current),
        )
    }

    private fun openCreateEditor() {
        when (val current = _state.value) {
            is CampaignsViewState.Empty -> {
                _state.value = current.copy(editor = createEditor())
            }
            is CampaignsViewState.Content -> {
                _state.value = current.copy(editor = createEditor())
            }
            CampaignsViewState.Loading, is CampaignsViewState.Error -> {
                openCreateOnNextLoad = true
            }
            CampaignsViewState.NoActiveWorld -> Unit
        }
    }

    private fun openEditEditor(campaignId: String) {
        val campaign = campaignFrom(campaignId) ?: return
        val editor = CampaignsViewState.CampaignEditorState(
            campaignId = campaign.id,
            name = campaign.name,
            description = campaign.description,
            notes = campaign.notes,
            nameError = null,
        )
        when (val current = _state.value) {
            is CampaignsViewState.Content -> _state.value = current.copy(editor = editor)
            else -> Unit
        }
    }

    private fun selectCampaign(campaignId: String) {
        appScope.scope.launch {
            setActiveCampaign(campaignId)
        }
    }

    private fun requestDelete(campaignId: String) {
        val campaign = campaignFrom(campaignId) ?: return
        updateContentOverlays(
            pendingDelete = CampaignsViewState.PendingDelete(
                campaignId = campaign.id,
                campaignName = campaign.name,
            )
        )
    }

    private fun confirmDelete() {
        val pending = pendingDeleteFrom(_state.value) ?: return
        appScope.scope.launch {
            deleteCampaign(pending.campaignId)
            updateContentOverlays(pendingDelete = null)
        }
    }

    private fun changeStatus(
        campaignId: String,
        status: CampaignStatus,
    ) {
        appScope.scope.launch {
            setCampaignStatus(campaignId, status)
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
            val invalidName = if (editor.campaignId == null) {
                createCampaign(editor.name, editor.description, editor.notes) is
                    CreateCampaignUseCase.Result.InvalidName
            } else {
                updateCampaign(
                    editor.campaignId,
                    editor.name,
                    editor.description,
                    editor.notes,
                ) is UpdateCampaignUseCase.Result.InvalidName
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
        transform: (CampaignsViewState.CampaignEditorState?) -> CampaignsViewState.CampaignEditorState?,
    ) {
        when (val current = _state.value) {
            is CampaignsViewState.Empty -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            is CampaignsViewState.Content -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            else -> Unit
        }
    }

    private fun updateContentOverlays(
        pendingDelete: CampaignsViewState.PendingDelete?,
    ) {
        val current = _state.value
        if (current is CampaignsViewState.Content) {
            _state.value = current.copy(pendingDelete = pendingDelete)
        }
    }

    private fun createEditor(): CampaignsViewState.CampaignEditorState {
        return CampaignsViewState.CampaignEditorState(
            campaignId = null,
            name = "",
            description = "",
            notes = "",
            nameError = null,
        )
    }

    private fun editorFrom(state: CampaignsViewState): CampaignsViewState.CampaignEditorState? {
        return when (state) {
            is CampaignsViewState.Empty -> state.editor
            is CampaignsViewState.Content -> state.editor
            else -> null
        }
    }

    private fun pendingDeleteFrom(state: CampaignsViewState): CampaignsViewState.PendingDelete? {
        return (state as? CampaignsViewState.Content)?.pendingDelete
    }

    private fun campaignFrom(campaignId: String): Campaign? {
        return (_state.value as? CampaignsViewState.Content)
            ?.campaigns
            ?.firstOrNull { it.id == campaignId }
    }
}
