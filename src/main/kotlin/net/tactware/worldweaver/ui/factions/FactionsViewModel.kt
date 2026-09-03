package net.tactware.worldweaver.ui.factions

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
import net.tactware.worldweaver.domain.CreateFactionUseCase
import net.tactware.worldweaver.domain.DeleteFactionMembershipUseCase
import net.tactware.worldweaver.domain.DeleteFactionUseCase
import net.tactware.worldweaver.domain.Faction
import net.tactware.worldweaver.domain.FactionDraft
import net.tactware.worldweaver.domain.FactionMembership
import net.tactware.worldweaver.domain.ObserveActiveContextDetailsUseCase
import net.tactware.worldweaver.domain.ObserveFactionMembershipsUseCase
import net.tactware.worldweaver.domain.ObserveFactionsForActiveWorldUseCase
import net.tactware.worldweaver.domain.ObservePeopleForActiveContextUseCase
import net.tactware.worldweaver.domain.PeopleSnapshot
import net.tactware.worldweaver.domain.PersonRef
import net.tactware.worldweaver.domain.UpdateFactionUseCase

internal class FactionsViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeFactions: ObserveFactionsForActiveWorldUseCase,
    private val observeMemberships: ObserveFactionMembershipsUseCase,
    private val observePeople: ObservePeopleForActiveContextUseCase,
    private val createFaction: CreateFactionUseCase,
    private val updateFaction: UpdateFactionUseCase,
    private val deleteFaction: DeleteFactionUseCase,
    private val deleteMembership: DeleteFactionMembershipUseCase,
) {
    private val _state = MutableStateFlow<FactionsViewState>(FactionsViewState.Loading)
    val state: StateFlow<FactionsViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<FactionsViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<FactionsViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var openCreateOnNextLoad = false
    private var selectedFactionId: String? = null
    private var latestFactions: List<Faction> = emptyList()
    private var latestMemberships: List<FactionMembership> = emptyList()
    private var latestPeople: PeopleSnapshot = PeopleSnapshot(emptyList(), emptyList())
    private var latestWorldName: String = ""

    init {
        observe()
    }

    fun onInteraction(interaction: FactionsInteraction) {
        when (interaction) {
            FactionsInteraction.ScreenStarted -> Unit
            FactionsInteraction.RetrySelected -> observe()
            FactionsInteraction.CreateWorldSelected -> {
                _effects.tryEmit(FactionsViewEffect.OpenWorlds)
            }
            FactionsInteraction.NewFactionSelected -> openCreateEditor()
            is FactionsInteraction.FactionSelected,
            is FactionsInteraction.FactionOpened,
            -> selectFaction(selectedId(interaction))
            is FactionsInteraction.EditFactionSelected -> openEditEditor(interaction.factionId)
            is FactionsInteraction.DeleteFactionSelected -> requestDelete(interaction.factionId)
            FactionsInteraction.DeleteConfirmed -> confirmDelete()
            FactionsInteraction.DeleteCancelled -> updatePendingDelete(null)
            FactionsInteraction.BlockReasonDismissed -> updateBlockReason(null)
            is FactionsInteraction.MemberRemoved -> removeMember(interaction.membershipId)
            is FactionsInteraction.EditorNameChanged -> updateEditor { editor ->
                editor?.copy(name = interaction.name, nameError = null)
            }
            is FactionsInteraction.EditorDescriptionChanged -> updateEditor { editor ->
                editor?.copy(description = interaction.description)
            }
            is FactionsInteraction.EditorGoalsChanged -> updateEditor { editor ->
                editor?.copy(goals = interaction.goals)
            }
            is FactionsInteraction.EditorNotesChanged -> updateEditor { editor ->
                editor?.copy(notes = interaction.notes)
            }
            FactionsInteraction.EditorSaved -> saveEditor()
            FactionsInteraction.EditorDismissed -> updateEditor { null }
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = FactionsViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                observeActiveContextDetails(),
                observeFactions(),
                observeMemberships(),
                observePeople(),
            ) { details, factions, memberships, people ->
                LoadedSnapshot(details, factions, memberships, people)
            }
                .catch { error ->
                    _state.value = FactionsViewState.Error(
                        message = error.message ?: "Could not load factions",
                        canRetry = true,
                    )
                }
                .collect { snapshot ->
                    applyLoaded(
                        snapshot.details,
                        snapshot.factions,
                        snapshot.memberships,
                        snapshot.people,
                    )
                }
        }
    }

    private fun applyLoaded(
        details: ActiveContextDetails,
        factions: List<Faction>,
        memberships: List<FactionMembership>,
        people: PeopleSnapshot,
    ) {
        val world = details.world
        if (world == null) {
            latestFactions = emptyList()
            latestMemberships = emptyList()
            latestPeople = PeopleSnapshot(emptyList(), emptyList())
            selectedFactionId = null
            _state.value = FactionsViewState.NoActiveWorld
            return
        }
        latestFactions = factions
        latestMemberships = memberships
        latestPeople = people
        latestWorldName = world.name
        val current = _state.value
        val editor = if (openCreateOnNextLoad) {
            openCreateOnNextLoad = false
            createEditor()
        } else {
            editorFrom(current)
        }
        if (factions.isEmpty()) {
            selectedFactionId = null
            _state.value = FactionsViewState.Empty(
                worldName = world.name,
                editor = editor,
            )
            return
        }
        val selected = selectedFrom(factions) ?: factions.first()
        selectedFactionId = selected.id
        _state.value = contentState(
            selected = selected,
            editor = editor,
            pendingDelete = pendingDeleteFrom(current),
            blockDeleteReason = blockReasonFrom(current),
        )
    }

    private fun refreshContent() {
        val current = _state.value
        if (current !is FactionsViewState.Content) {
            return
        }
        val selected = selectedFrom(latestFactions) ?: latestFactions.firstOrNull()
        _state.value = contentState(
            selected = selected,
            editor = current.editor,
            pendingDelete = current.pendingDelete,
            blockDeleteReason = current.blockDeleteReason,
        )
    }

    private fun contentState(
        selected: Faction?,
        editor: FactionsViewState.FactionEditorState?,
        pendingDelete: FactionsViewState.PendingDelete?,
        blockDeleteReason: String?,
    ): FactionsViewState.Content {
        return FactionsViewState.Content(
            worldName = latestWorldName,
            factions = latestFactions,
            selectedFaction = selected,
            members = membersFor(selected),
            editor = editor,
            pendingDelete = pendingDelete,
            blockDeleteReason = blockDeleteReason,
        )
    }

    private fun membersFor(faction: Faction?): List<FactionsViewState.MemberRow> {
        if (faction == null) {
            return emptyList()
        }
        return latestMemberships
            .filter { it.factionId == faction.id }
            .map { membership ->
                FactionsViewState.MemberRow(
                    membershipId = membership.id,
                    personName = personName(membership.person),
                    role = membership.role,
                    notes = membership.notes,
                )
            }
            .sortedBy { it.personName.lowercase() }
    }

    private fun personName(ref: PersonRef): String {
        return when (ref) {
            is PersonRef.World -> latestPeople.worldPeople.firstOrNull { it.id == ref.id }?.name
            is PersonRef.Campaign -> latestPeople.campaignPeople.firstOrNull { it.id == ref.id }?.name
        } ?: "Unknown person"
    }

    private fun selectFaction(factionId: String) {
        if (factionId.isEmpty() || selectedFactionId == factionId) {
            return
        }
        selectedFactionId = factionId
        refreshContent()
    }

    private fun openCreateEditor() {
        when (val current = _state.value) {
            is FactionsViewState.Empty -> {
                _state.value = current.copy(editor = createEditor())
            }
            is FactionsViewState.Content -> {
                _state.value = current.copy(editor = createEditor())
            }
            FactionsViewState.Loading, is FactionsViewState.Error -> {
                openCreateOnNextLoad = true
            }
            FactionsViewState.NoActiveWorld -> Unit
        }
    }

    private fun openEditEditor(factionId: String) {
        val faction = latestFactions.firstOrNull { it.id == factionId } ?: return
        updateEditor {
            FactionsViewState.FactionEditorState(
                factionId = faction.id,
                name = faction.name,
                description = faction.description,
                goals = faction.goals,
                notes = faction.notes,
                nameError = null,
            )
        }
    }

    private fun createEditor(): FactionsViewState.FactionEditorState {
        return FactionsViewState.FactionEditorState(
            factionId = null,
            name = "",
            description = "",
            goals = "",
            notes = "",
            nameError = null,
        )
    }

    private fun saveEditor() {
        val editor = editorFrom(_state.value) ?: return
        val draft = FactionDraft(
            name = editor.name,
            description = editor.description,
            goals = editor.goals,
            notes = editor.notes,
        )
        appScope.scope.launch {
            val result = if (editor.factionId == null) {
                when (val created = createFaction(draft)) {
                    is CreateFactionUseCase.Result.Created -> {
                        selectedFactionId = created.faction.id
                        SaveResult.Saved
                    }
                    CreateFactionUseCase.Result.InvalidName -> SaveResult.InvalidName
                    CreateFactionUseCase.Result.DuplicateName -> SaveResult.DuplicateName
                    CreateFactionUseCase.Result.NoActiveWorld -> SaveResult.Failed
                }
            } else {
                when (updateFaction(editor.factionId, draft)) {
                    UpdateFactionUseCase.Result.Updated -> SaveResult.Saved
                    UpdateFactionUseCase.Result.InvalidName -> SaveResult.InvalidName
                    UpdateFactionUseCase.Result.DuplicateName -> SaveResult.DuplicateName
                    UpdateFactionUseCase.Result.NotFound -> SaveResult.Failed
                }
            }
            when (result) {
                SaveResult.Saved -> updateEditor { null }
                SaveResult.InvalidName -> updateEditor { current ->
                    current?.copy(nameError = "Name is required")
                }
                SaveResult.DuplicateName -> updateEditor { current ->
                    current?.copy(nameError = "A faction with that name already exists")
                }
                SaveResult.Failed -> updateEditor { current ->
                    current?.copy(nameError = "Could not save that faction")
                }
            }
        }
    }

    private fun requestDelete(factionId: String) {
        val faction = latestFactions.firstOrNull { it.id == factionId } ?: return
        updatePendingDelete(
            FactionsViewState.PendingDelete(
                factionId = faction.id,
                factionName = faction.name,
            )
        )
    }

    private fun confirmDelete() {
        val pending = pendingDeleteFrom(_state.value) ?: return
        appScope.scope.launch {
            when (val result = deleteFaction(pending.factionId)) {
                DeleteFactionUseCase.Result.Deleted -> {
                    if (selectedFactionId == pending.factionId) {
                        selectedFactionId = null
                    }
                    updatePendingDelete(null)
                    updateBlockReason(null)
                }
                is DeleteFactionUseCase.Result.Blocked -> {
                    updatePendingDelete(null)
                    updateBlockReason(blockedMessage(result))
                }
                DeleteFactionUseCase.Result.NotFound -> updatePendingDelete(null)
            }
        }
    }

    private fun blockedMessage(result: DeleteFactionUseCase.Result.Blocked): String {
        val parts = buildList {
            if (result.membershipCount > 0) {
                add("${result.membershipCount} member${if (result.membershipCount == 1) "" else "s"}")
            }
            if (result.relationshipCount > 0) {
                add(
                    "${result.relationshipCount} relationship" +
                        if (result.relationshipCount == 1) "" else "s"
                )
            }
        }
        return "Remove this faction from ${parts.joinToString(" and ")} before deleting it."
    }

    private fun removeMember(membershipId: String) {
        appScope.scope.launch {
            deleteMembership(membershipId)
        }
    }

    private fun selectedFrom(factions: List<Faction>): Faction? {
        val selectedId = selectedFactionId ?: return null
        return factions.firstOrNull { it.id == selectedId }
    }

    private fun selectedId(interaction: FactionsInteraction): String {
        return when (interaction) {
            is FactionsInteraction.FactionSelected -> interaction.factionId
            is FactionsInteraction.FactionOpened -> interaction.factionId
            else -> ""
        }
    }

    private fun updateEditor(
        transform: (FactionsViewState.FactionEditorState?) -> FactionsViewState.FactionEditorState?,
    ) {
        when (val current = _state.value) {
            is FactionsViewState.Empty -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            is FactionsViewState.Content -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            else -> Unit
        }
    }

    private fun updatePendingDelete(pending: FactionsViewState.PendingDelete?) {
        val current = _state.value
        if (current is FactionsViewState.Content) {
            _state.value = current.copy(pendingDelete = pending)
        }
    }

    private fun updateBlockReason(reason: String?) {
        val current = _state.value
        if (current is FactionsViewState.Content) {
            _state.value = current.copy(blockDeleteReason = reason)
        }
    }

    private fun editorFrom(state: FactionsViewState): FactionsViewState.FactionEditorState? {
        return when (state) {
            is FactionsViewState.Empty -> state.editor
            is FactionsViewState.Content -> state.editor
            else -> null
        }
    }

    private fun pendingDeleteFrom(state: FactionsViewState): FactionsViewState.PendingDelete? {
        return (state as? FactionsViewState.Content)?.pendingDelete
    }

    private fun blockReasonFrom(state: FactionsViewState): String? {
        return (state as? FactionsViewState.Content)?.blockDeleteReason
    }

    private enum class SaveResult {
        Saved,
        InvalidName,
        DuplicateName,
        Failed,
    }

    private data class LoadedSnapshot(
        val details: ActiveContextDetails,
        val factions: List<Faction>,
        val memberships: List<FactionMembership>,
        val people: PeopleSnapshot,
    )
}
