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
import net.tactware.worldweaver.domain.FifthEditionSheet
import net.tactware.worldweaver.domain.GameSystem
import net.tactware.worldweaver.domain.Pathfinder2ESheet
import net.tactware.worldweaver.domain.PersonSheet
import net.tactware.worldweaver.domain.CampaignPerson
import net.tactware.worldweaver.domain.Location
import net.tactware.worldweaver.domain.LocationOverlay
import net.tactware.worldweaver.domain.ObserveActiveContextDetailsUseCase
import net.tactware.worldweaver.domain.ObserveCampaignsForActiveWorldUseCase
import net.tactware.worldweaver.domain.ObserveLocationOverlaysForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveLocationsForActiveWorldUseCase
import net.tactware.worldweaver.domain.ObservePeopleForActiveContextUseCase
import net.tactware.worldweaver.domain.ObserveQuestsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveSessionsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveWorldCalendarForActiveWorldUseCase
import net.tactware.worldweaver.domain.PeopleSnapshot
import net.tactware.worldweaver.domain.PersonKind
import net.tactware.worldweaver.domain.Quest
import net.tactware.worldweaver.domain.QuestStatus
import net.tactware.worldweaver.domain.Session
import net.tactware.worldweaver.domain.SetActiveCampaignUseCase
import net.tactware.worldweaver.domain.WorldCalendar
import net.tactware.worldweaver.domain.WorldDateFormatter
import net.tactware.worldweaver.domain.SetCampaignStatusUseCase
import net.tactware.worldweaver.domain.UpdateCampaignUseCase

internal class CampaignsViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeCampaigns: ObserveCampaignsForActiveWorldUseCase,
    private val observePeople: ObservePeopleForActiveContextUseCase,
    private val observeQuests: ObserveQuestsForActiveCampaignUseCase,
    private val observeSessions: ObserveSessionsForActiveCampaignUseCase,
    private val observeLocations: ObserveLocationsForActiveWorldUseCase,
    private val observeOverlays: ObserveLocationOverlaysForActiveCampaignUseCase,
    private val observeCalendar: ObserveWorldCalendarForActiveWorldUseCase,
    private val createCampaign: CreateCampaignUseCase,
    private val updateCampaign: UpdateCampaignUseCase,
    private val setCampaignStatus: SetCampaignStatusUseCase,
    private val deleteCampaign: DeleteCampaignUseCase,
    private val setActiveCampaign: SetActiveCampaignUseCase,
    private val dateFormatter: WorldDateFormatter = WorldDateFormatter(),
) {
    private val _state = MutableStateFlow<CampaignsViewState>(CampaignsViewState.Loading)
    val state: StateFlow<CampaignsViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<CampaignsViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<CampaignsViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var showRetired = false
    private var openCreateOnNextLoad = false
    private var latestWorldDefault = GameSystem.FifthEdition

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
            is CampaignsInteraction.CampaignSelected,
            is CampaignsInteraction.CampaignOpened,
            -> selectCampaign(campaignIdFrom(interaction))
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
            is CampaignsInteraction.EditorGameSystemSelected -> updateEditor { editor ->
                editor?.copy(gameSystem = interaction.gameSystem)
            }
            CampaignsInteraction.EditorSaved -> saveEditor()
            CampaignsInteraction.EditorDismissed -> updateEditor { null }
            CampaignsInteraction.OpenCharactersSelected -> {
                _effects.tryEmit(CampaignsViewEffect.OpenCharacters)
            }
            CampaignsInteraction.CreatePlayerCharacterSelected -> {
                createPlayerCharacter()
            }
            CampaignsInteraction.OpenQuestsSelected -> {
                _effects.tryEmit(CampaignsViewEffect.OpenQuests)
            }
            CampaignsInteraction.OpenSessionsSelected -> {
                _effects.tryEmit(CampaignsViewEffect.OpenSessions)
            }
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = CampaignsViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                combine(
                    observeActiveContextDetails(),
                    observeCampaigns(),
                    observePeople(),
                ) { details, campaigns, people ->
                    Triple(details, campaigns, people)
                },
                combine(
                    observeQuests(),
                    observeSessions(),
                    observeLocations(),
                    observeOverlays(),
                    observeCalendar(),
                ) { quests, sessions, locations, overlays, calendar ->
                    OverviewBundle(quests, sessions, locations, overlays, calendar)
                },
            ) { core, overview ->
                LoadedSnapshot(core.first, core.second, core.third, overview)
            }
                .catch { error ->
                    _state.value = CampaignsViewState.Error(
                        message = error.message ?: "Could not load campaigns",
                        canRetry = true,
                    )
                }
                .collect { snapshot ->
                    applyLoaded(
                        snapshot.details,
                        snapshot.campaigns,
                        snapshot.people,
                        snapshot.overview,
                    )
                }
        }
    }

    private fun applyLoaded(
        details: ActiveContextDetails,
        campaigns: List<Campaign>,
        people: PeopleSnapshot,
        overview: OverviewBundle,
    ) {
        val world = details.world
        if (world == null) {
            _state.value = CampaignsViewState.NoActiveWorld
            return
        }
        latestWorldDefault = world.defaultGameSystem
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
                worldDefaultGameSystem = world.defaultGameSystem,
                editor = editor,
            )
            return
        }
        val selected = details.campaign ?: campaigns.firstOrNull { it.status == CampaignStatus.Active }
        _state.value = CampaignsViewState.Content(
            worldName = world.name,
            worldDefaultGameSystem = world.defaultGameSystem,
            campaigns = campaigns,
            selectedCampaign = selected,
            showRetired = showRetired,
            partyMembers = partyMembers(people.campaignPeople),
            activeQuests = overview.quests
                .filter { it.status == QuestStatus.Active }
                .map { quest ->
                    CampaignsViewState.OverviewQuest(id = quest.id, title = quest.title)
                },
            lastSession = lastSession(overview.sessions, overview.calendar),
            nextSessionHint = nextSessionHint(overview),
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
            gameSystem = campaign.resolvedGameSystem(latestWorldDefault),
            nameError = null,
        )
        when (val current = _state.value) {
            is CampaignsViewState.Content -> _state.value = current.copy(editor = editor)
            else -> Unit
        }
    }

    private fun campaignIdFrom(interaction: CampaignsInteraction): String {
        return when (interaction) {
            is CampaignsInteraction.CampaignSelected -> interaction.campaignId
            is CampaignsInteraction.CampaignOpened -> interaction.campaignId
            else -> ""
        }
    }

    private fun selectCampaign(campaignId: String) {
        appScope.scope.launch {
            setActiveCampaign(campaignId)
        }
    }

    private fun createPlayerCharacter() {
        val campaignId = selectedCampaignId()
        appScope.scope.launch {
            if (campaignId != null) {
                setActiveCampaign(campaignId)
            }
            _effects.tryEmit(CampaignsViewEffect.CreatePlayerCharacter)
        }
    }

    private fun selectedCampaignId(): String? {
        return (_state.value as? CampaignsViewState.Content)?.selectedCampaign?.id
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
                createCampaign(
                    editor.name,
                    editor.description,
                    editor.notes,
                    editor.gameSystem,
                ) is
                    CreateCampaignUseCase.Result.InvalidName
            } else {
                updateCampaign(
                    editor.campaignId,
                    editor.name,
                    editor.description,
                    editor.notes,
                    editor.gameSystem,
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
            gameSystem = latestWorldDefault,
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

    private fun partyClassLabel(sheet: PersonSheet): String {
        return when (sheet) {
            is FifthEditionSheet -> {
                if (sheet.classLevels.isEmpty()) {
                    "Level ${sheet.totalLevel()}"
                } else {
                    sheet.classLevels.joinToString(", ") { level ->
                        "${level.className} ${level.level}"
                    }
                }
            }
            is Pathfinder2ESheet -> {
                val className = sheet.className.takeIf { it.isNotBlank() } ?: "Level ${sheet.level}"
                if (sheet.className.isBlank()) {
                    className
                } else {
                    "$className ${sheet.level}"
                }
            }
        }
    }

    private fun partyMembers(
        campaignPeople: List<CampaignPerson>,
    ): List<CampaignsViewState.PartyMember> {
        return campaignPeople
            .filter { it.kind == PersonKind.PlayerCharacter }
            .sortedBy { it.name.lowercase() }
            .map { person ->
                CampaignsViewState.PartyMember(
                    id = person.id,
                    name = person.name,
                    summary = listOfNotNull(
                        person.sheet.lineageLabel().takeIf { it.isNotBlank() },
                        partyClassLabel(person.sheet),
                    ).joinToString(" · "),
                )
            }
    }

    private fun campaignFrom(campaignId: String): Campaign? {
        return (_state.value as? CampaignsViewState.Content)
            ?.campaigns
            ?.firstOrNull { it.id == campaignId }
    }

    private fun lastSession(
        sessions: List<Session>,
        calendar: WorldCalendar?,
    ): CampaignsViewState.OverviewSession? {
        val session = sessions.maxByOrNull { it.updatedAt } ?: return null
        val dateLabel = if (calendar != null && session.inWorldDate != null) {
            dateFormatter.format(calendar, session.inWorldDate)
        } else {
            null
        }
        return CampaignsViewState.OverviewSession(
            id = session.id,
            name = session.name,
            recap = session.notes.lineSequence().firstOrNull().orEmpty(),
            dateLabel = dateLabel,
        )
    }

    private fun nextSessionHint(overview: OverviewBundle): String {
        val activeCount = overview.quests.count { it.status == QuestStatus.Active }
        val partyLocations = overview.overlays
            .filter { it.hasPartyPresence }
            .mapNotNull { overlay ->
                overview.locations.firstOrNull { it.id == overlay.locationId }?.name
            }
        val parts = buildList {
            if (overview.sessions.isEmpty()) {
                add("Create a session to run next")
            }
            if (activeCount > 0) {
                add("$activeCount active quest${if (activeCount == 1) "" else "s"}")
            }
            if (partyLocations.isNotEmpty()) {
                add("Party at ${partyLocations.joinToString(", ")}")
            }
        }
        return parts.joinToString(" · ").ifBlank {
            "No next-session hint yet. Add a session when you are ready to run."
        }
    }

    private data class OverviewBundle(
        val quests: List<Quest>,
        val sessions: List<Session>,
        val locations: List<Location>,
        val overlays: List<LocationOverlay>,
        val calendar: WorldCalendar?,
    )

    private data class LoadedSnapshot(
        val details: ActiveContextDetails,
        val campaigns: List<Campaign>,
        val people: PeopleSnapshot,
        val overview: OverviewBundle,
    )
}
