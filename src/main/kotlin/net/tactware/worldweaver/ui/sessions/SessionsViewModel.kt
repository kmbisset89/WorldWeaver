package net.tactware.worldweaver.ui.sessions

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
import net.tactware.worldweaver.domain.AbilityScoreMethod
import net.tactware.worldweaver.domain.ActiveContextDetails
import net.tactware.worldweaver.domain.CreatePlotThreadUseCase
import net.tactware.worldweaver.domain.CreateReferenceDocUseCase
import net.tactware.worldweaver.domain.CreateSessionUseCase
import net.tactware.worldweaver.domain.DeletePlotThreadUseCase
import net.tactware.worldweaver.domain.DeleteReferenceDocUseCase
import net.tactware.worldweaver.domain.DeleteSessionUseCase
import net.tactware.worldweaver.domain.GenerateRandomNpcUseCase
import net.tactware.worldweaver.domain.Location
import net.tactware.worldweaver.domain.LocationOverlay
import net.tactware.worldweaver.domain.MarchOrderEntry
import net.tactware.worldweaver.domain.ObserveActiveContextDetailsUseCase
import net.tactware.worldweaver.domain.ObserveLocationOverlaysForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveLocationsForActiveWorldUseCase
import net.tactware.worldweaver.domain.ObservePeopleForActiveContextUseCase
import net.tactware.worldweaver.domain.ObservePlotThreadsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveQuestsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveReferenceDocsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveSessionsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveWorldCalendarForActiveWorldUseCase
import net.tactware.worldweaver.domain.PeopleSnapshot
import net.tactware.worldweaver.domain.PersonKind
import net.tactware.worldweaver.domain.PersonRef
import net.tactware.worldweaver.domain.PlotThread
import net.tactware.worldweaver.domain.PlotThreadDraft
import net.tactware.worldweaver.domain.PlotThreadPriority
import net.tactware.worldweaver.domain.PlotThreadStatus
import net.tactware.worldweaver.domain.Quest
import net.tactware.worldweaver.domain.QuestLinkKind
import net.tactware.worldweaver.domain.QuestStatus
import net.tactware.worldweaver.domain.ReferenceDoc
import net.tactware.worldweaver.domain.ReferenceDocDraft
import net.tactware.worldweaver.domain.SaveSessionNpcDraftUseCase
import net.tactware.worldweaver.domain.Session
import net.tactware.worldweaver.domain.SessionDraft
import net.tactware.worldweaver.domain.SessionNpcDraftDestination
import net.tactware.worldweaver.domain.SessionScene
import net.tactware.worldweaver.domain.UpdatePlotThreadUseCase
import net.tactware.worldweaver.domain.UpdateReferenceDocUseCase
import net.tactware.worldweaver.domain.UpdateSessionUseCase
import net.tactware.worldweaver.domain.WorldCalendar
import net.tactware.worldweaver.domain.WorldDate
import net.tactware.worldweaver.domain.WorldDateFormatter

internal class SessionsViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeSessions: ObserveSessionsForActiveCampaignUseCase,
    private val observeQuests: ObserveQuestsForActiveCampaignUseCase,
    private val observeLocations: ObserveLocationsForActiveWorldUseCase,
    private val observeOverlays: ObserveLocationOverlaysForActiveCampaignUseCase,
    private val observePeople: ObservePeopleForActiveContextUseCase,
    private val observeThreads: ObservePlotThreadsForActiveCampaignUseCase,
    private val observeDocs: ObserveReferenceDocsForActiveCampaignUseCase,
    private val observeCalendar: ObserveWorldCalendarForActiveWorldUseCase,
    private val createSession: CreateSessionUseCase,
    private val updateSession: UpdateSessionUseCase,
    private val deleteSession: DeleteSessionUseCase,
    private val createPlotThread: CreatePlotThreadUseCase,
    private val updatePlotThread: UpdatePlotThreadUseCase,
    private val deletePlotThread: DeletePlotThreadUseCase,
    private val createReferenceDoc: CreateReferenceDocUseCase,
    private val updateReferenceDoc: UpdateReferenceDocUseCase,
    private val deleteReferenceDoc: DeleteReferenceDocUseCase,
    private val generateRandomNpc: GenerateRandomNpcUseCase,
    private val saveSessionNpcDraft: SaveSessionNpcDraftUseCase,
    private val dateFormatter: WorldDateFormatter = WorldDateFormatter(),
) {
    private val _state = MutableStateFlow<SessionsViewState>(SessionsViewState.Loading)
    val state: StateFlow<SessionsViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SessionsViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<SessionsViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var openCreateOnNextLoad = false
    private var selectedSessionId: String? = null
    private var latestSessions: List<Session> = emptyList()
    private var latestQuests: List<Quest> = emptyList()
    private var latestLocations: List<Location> = emptyList()
    private var latestOverlays: List<LocationOverlay> = emptyList()
    private var latestPeople: PeopleSnapshot = PeopleSnapshot(emptyList(), emptyList())
    private var latestThreads: List<PlotThread> = emptyList()
    private var latestDocs: List<ReferenceDoc> = emptyList()
    private var latestWorldName: String = ""
    private var latestCampaignName: String = ""
    private var latestCalendar: WorldCalendar? = null

    init {
        observe()
    }

    fun onInteraction(interaction: SessionsInteraction) {
        when (interaction) {
            SessionsInteraction.ScreenStarted -> Unit
            SessionsInteraction.RetrySelected -> observe()
            SessionsInteraction.CreateWorldSelected -> _effects.tryEmit(SessionsViewEffect.OpenWorlds)
            SessionsInteraction.CreateCampaignSelected -> {
                _effects.tryEmit(SessionsViewEffect.OpenCampaigns)
            }
            SessionsInteraction.NewSessionSelected -> openCreateEditor()
            is SessionsInteraction.SessionSelected,
            is SessionsInteraction.SessionOpened,
            -> selectSession(sessionIdFrom(interaction))
            is SessionsInteraction.EditSessionSelected -> openEditEditor(interaction.sessionId)
            is SessionsInteraction.DeleteSessionSelected -> requestDelete(interaction.sessionId)
            SessionsInteraction.DeleteConfirmed -> confirmDelete()
            SessionsInteraction.DeleteCancelled -> updatePendingDelete(null)
            is SessionsInteraction.LinkedQuestSelected -> {
                _effects.tryEmit(SessionsViewEffect.OpenQuest(interaction.questId))
            }
            is SessionsInteraction.EditorNameChanged -> updateEditor { editor ->
                editor?.copy(name = interaction.name, nameError = null)
            }
            is SessionsInteraction.EditorNotesChanged -> updateEditor { editor ->
                editor?.copy(notes = interaction.notes)
            }
            is SessionsInteraction.EditorYearChanged -> updateEditor { editor ->
                editor?.copy(yearText = interaction.year, dateError = null)
                    ?.let(::withDatePreview)
            }
            is SessionsInteraction.EditorMonthSelected -> updateEditor { editor ->
                editor?.copy(monthId = interaction.monthId, dateError = null)
                    ?.let(::withDatePreview)
            }
            is SessionsInteraction.EditorDayChanged -> updateEditor { editor ->
                editor?.copy(dayText = interaction.day, dateError = null)
                    ?.let(::withDatePreview)
            }
            SessionsInteraction.EditorDateCleared -> updateEditor { editor ->
                editor?.copy(yearText = "", monthId = null, dayText = "", dateError = null)
                    ?.let(::withDatePreview)
            }
            SessionsInteraction.EditorSaved -> saveEditor()
            SessionsInteraction.EditorDismissed -> updateEditor { null }
            SessionsInteraction.SceneAdded -> mutateSelectedSession { session ->
                session.copy(
                    scenes = session.scenes + SessionScene(id = "", title = "New scene", notes = ""),
                )
            }
            is SessionsInteraction.SceneRemoved -> mutateSelectedSession { session ->
                session.copy(
                    scenes = session.scenes.filterIndexed { index, _ -> index != interaction.index },
                )
            }
            is SessionsInteraction.SceneTitleChanged -> mutateSelectedSession { session ->
                session.copy(
                    scenes = session.scenes.mapIndexed { index, scene ->
                        if (index == interaction.index) scene.copy(title = interaction.title) else scene
                    },
                )
            }
            is SessionsInteraction.SceneNotesChanged -> mutateSelectedSession { session ->
                session.copy(
                    scenes = session.scenes.mapIndexed { index, scene ->
                        if (index == interaction.index) scene.copy(notes = interaction.notes) else scene
                    },
                )
            }
            is SessionsInteraction.SceneMoved -> mutateSelectedSession { session ->
                session.copy(scenes = move(session.scenes, interaction.index, interaction.delta))
            }
            is SessionsInteraction.MarchPersonAdded -> addMarchPerson(interaction.person)
            is SessionsInteraction.MarchEntryRemoved -> mutateSelectedSession { session ->
                session.copy(
                    marchOrder = session.marchOrder.filterIndexed { index, _ ->
                        index != interaction.index
                    },
                )
            }
            is SessionsInteraction.MarchEntryMoved -> mutateSelectedSession { session ->
                session.copy(
                    marchOrder = move(session.marchOrder, interaction.index, interaction.delta),
                )
            }
            SessionsInteraction.ThreadEditorOpened -> openThreadEditor(null)
            is SessionsInteraction.ThreadEditSelected -> openThreadEditor(interaction.threadId)
            is SessionsInteraction.ThreadDeleteSelected -> requestThreadDelete(interaction.threadId)
            SessionsInteraction.ThreadDeleteConfirmed -> confirmThreadDelete()
            SessionsInteraction.ThreadDeleteCancelled -> updateThreadDelete(null)
            is SessionsInteraction.ThreadTitleChanged -> updateThreadEditor { editor ->
                editor?.copy(title = interaction.title, titleError = null)
            }
            is SessionsInteraction.ThreadDetailsChanged -> updateThreadEditor { editor ->
                editor?.copy(details = interaction.details)
            }
            is SessionsInteraction.ThreadStatusSelected -> updateThreadEditor { editor ->
                editor?.copy(status = interaction.status)
            }
            is SessionsInteraction.ThreadPrioritySelected -> updateThreadEditor { editor ->
                editor?.copy(priority = interaction.priority)
            }
            is SessionsInteraction.ThreadAttachToggled -> updateThreadEditor { editor ->
                editor?.copy(attachToSession = interaction.attach)
            }
            SessionsInteraction.ThreadSaved -> saveThreadEditor()
            SessionsInteraction.ThreadEditorDismissed -> updateThreadEditor { null }
            SessionsInteraction.DocEditorOpened -> openDocEditor(null)
            is SessionsInteraction.DocEditSelected -> openDocEditor(interaction.docId)
            is SessionsInteraction.DocDeleteSelected -> requestDocDelete(interaction.docId)
            SessionsInteraction.DocDeleteConfirmed -> confirmDocDelete()
            SessionsInteraction.DocDeleteCancelled -> updateDocDelete(null)
            is SessionsInteraction.DocTitleChanged -> updateDocEditor { editor ->
                editor?.copy(title = interaction.title, titleError = null)
            }
            is SessionsInteraction.DocPathChanged -> updateDocEditor { editor ->
                editor?.copy(pathOrUrl = interaction.pathOrUrl, pathError = null)
            }
            is SessionsInteraction.DocAttachToggled -> updateDocEditor { editor ->
                editor?.copy(attachToSession = interaction.attach)
            }
            SessionsInteraction.DocSaved -> saveDocEditor()
            SessionsInteraction.DocEditorDismissed -> updateDocEditor { null }
            SessionsInteraction.GeneratorOpened -> openGenerator()
            SessionsInteraction.GeneratorDismissed -> updateGenerator(null)
            is SessionsInteraction.GeneratorMethodSelected -> updateGenerator { current ->
                current?.copy(method = interaction.method)
            }
            SessionsInteraction.GeneratorRolled -> rollGenerator()
            is SessionsInteraction.GeneratorDestinationSelected -> updateGenerator { current ->
                current?.copy(destination = interaction.destination)
            }
            SessionsInteraction.GeneratorSaved -> saveGenerator()
        }
    }

    private fun sessionIdFrom(interaction: SessionsInteraction): String {
        return when (interaction) {
            is SessionsInteraction.SessionSelected -> interaction.sessionId
            is SessionsInteraction.SessionOpened -> interaction.sessionId
            else -> ""
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = SessionsViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                combine(
                    observeActiveContextDetails(),
                    observeSessions(),
                    observeQuests(),
                    observeLocations(),
                    observeCalendar(),
                ) { details, sessions, quests, locations, calendar ->
                    ContextBundle(details, sessions, quests, locations, calendar)
                },
                combine(
                    observeOverlays(),
                    observePeople(),
                    observeThreads(),
                    observeDocs(),
                ) { overlays, people, threads, docs ->
                    SupportBundle(overlays, people, threads, docs)
                },
            ) { context, support ->
                LoadedSnapshot(context, support)
            }
                .catch { error ->
                    _state.value = SessionsViewState.Error(
                        message = error.message ?: "Could not load sessions",
                        canRetry = true,
                    )
                }
                .collect { snapshot ->
                    applyLoaded(snapshot)
                }
        }
    }

    private fun applyLoaded(snapshot: LoadedSnapshot) {
        val world = snapshot.context.details.world
        if (world == null) {
            clearLatest()
            _state.value = SessionsViewState.NoActiveWorld
            return
        }
        val campaign = snapshot.context.details.campaign
        if (campaign == null) {
            clearLatest()
            latestWorldName = world.name
            _state.value = SessionsViewState.NoActiveCampaign
            return
        }
        latestSessions = snapshot.context.sessions
        latestQuests = snapshot.context.quests
        latestLocations = snapshot.context.locations
        latestOverlays = snapshot.support.overlays
        latestPeople = snapshot.support.people
        latestThreads = snapshot.support.threads
        latestDocs = snapshot.support.docs
        latestWorldName = world.name
        latestCampaignName = campaign.name
        latestCalendar = snapshot.context.calendar
        val current = _state.value
        val editor = if (openCreateOnNextLoad) {
            openCreateOnNextLoad = false
            createEditor()
        } else {
            editorFrom(current)
        }
        if (snapshot.context.sessions.isEmpty()) {
            selectedSessionId = null
            _state.value = SessionsViewState.Empty(
                worldName = world.name,
                campaignName = campaign.name,
                editor = editor,
            )
            return
        }
        val selected = selectedFrom(snapshot.context.sessions) ?: snapshot.context.sessions.first()
        selectedSessionId = selected.id
        _state.value = contentState(
            selected = selected,
            editor = editor,
            overlays = currentOverlays(current),
        )
    }

    private fun refreshContent() {
        val current = _state.value
        if (current !is SessionsViewState.Content) {
            return
        }
        val selected = selectedFrom(latestSessions) ?: latestSessions.firstOrNull()
        _state.value = contentState(
            selected = selected,
            editor = current.editor,
            overlays = currentOverlays(current),
        )
    }

    private fun contentState(
        selected: Session?,
        editor: SessionsViewState.SessionEditorState?,
        overlays: OverlayState,
    ): SessionsViewState.Content {
        return SessionsViewState.Content(
            worldName = latestWorldName,
            campaignName = latestCampaignName,
            sessions = latestSessions,
            sessionDateLabels = dateLabels(latestSessions),
            selectedSession = selected,
            selectedDateLabel = selected?.let { dateLabel(it) },
            checklist = checklist(selected),
            linkedQuests = linkedQuests(selected),
            threads = latestThreads,
            docs = latestDocs,
            personOptions = personOptions(),
            editor = editor,
            threadEditor = overlays.threadEditor,
            docEditor = overlays.docEditor,
            generator = overlays.generator,
            pendingDelete = overlays.pendingDelete,
            pendingThreadDelete = overlays.pendingThreadDelete,
            pendingDocDelete = overlays.pendingDocDelete,
        )
    }

    private fun checklist(selected: Session?): SessionsViewState.ChecklistState {
        val previousNotes = previousSession(selected)?.notes?.takeIf { it.isNotBlank() }
        val partyLocations = latestOverlays
            .filter { it.hasPartyPresence }
            .mapNotNull { overlay ->
                latestLocations.firstOrNull { it.id == overlay.locationId }?.name
            }
        return SessionsViewState.ChecklistState(
            activeQuestTitles = latestQuests
                .filter { it.status == QuestStatus.Active }
                .map { it.title },
            lastSessionRecap = previousNotes,
            partyLocationNames = partyLocations,
        )
    }

    private fun previousSession(selected: Session?): Session? {
        if (selected == null) {
            return null
        }
        return latestSessions
            .filter { it.id != selected.id }
            .filter { !it.createdAt.isAfter(selected.createdAt) }
            .maxByOrNull { it.createdAt }
    }

    private fun linkedQuests(selected: Session?): List<SessionsViewState.LinkedQuest> {
        if (selected == null) {
            return emptyList()
        }
        return latestQuests
            .filter { quest ->
                quest.links.any { link ->
                    link.kind == QuestLinkKind.SESSION && link.targetId == selected.id
                }
            }
            .map { quest ->
                SessionsViewState.LinkedQuest(questId = quest.id, title = quest.title)
            }
    }

    private fun personOptions(): List<SessionsViewState.PersonOption> {
        val campaign = latestPeople.campaignPeople.sortedWith(
            compareBy<net.tactware.worldweaver.domain.CampaignPerson> {
                it.kind != PersonKind.PlayerCharacter
            }.thenBy { it.name.lowercase() }
        ).map { person ->
            SessionsViewState.PersonOption(
                person = PersonRef.Campaign(person.id),
                name = person.name,
            )
        }
        val world = latestPeople.worldPeople.sortedBy { it.name.lowercase() }.map { person ->
            SessionsViewState.PersonOption(
                person = PersonRef.World(person.id),
                name = "${person.name} (world)",
            )
        }
        return campaign + world
    }

    private fun selectSession(sessionId: String) {
        if (sessionId.isEmpty() || selectedSessionId == sessionId) {
            return
        }
        selectedSessionId = sessionId
        refreshContent()
    }

    private fun openCreateEditor() {
        when (val current = _state.value) {
            is SessionsViewState.Empty -> {
                _state.value = current.copy(editor = createEditor())
            }
            is SessionsViewState.Content -> {
                _state.value = current.copy(editor = createEditor())
            }
            SessionsViewState.Loading, is SessionsViewState.Error -> {
                openCreateOnNextLoad = true
            }
            SessionsViewState.NoActiveWorld, SessionsViewState.NoActiveCampaign -> Unit
        }
    }

    private fun openEditEditor(sessionId: String) {
        val session = latestSessions.firstOrNull { it.id == sessionId } ?: return
        val editor = withDatePreview(
            SessionsViewState.SessionEditorState(
                sessionId = session.id,
                name = session.name,
                notes = session.notes,
                yearText = session.inWorldDate?.year?.toString().orEmpty(),
                monthId = session.inWorldDate?.monthId,
                dayText = session.inWorldDate?.day?.toString().orEmpty(),
                months = latestCalendar?.months.orEmpty(),
                datePreview = null,
                dateError = null,
                nameError = null,
            )
        )
        when (val current = _state.value) {
            is SessionsViewState.Content -> _state.value = current.copy(editor = editor)
            else -> Unit
        }
    }

    private fun requestDelete(sessionId: String) {
        val session = latestSessions.firstOrNull { it.id == sessionId } ?: return
        updatePendingDelete(
            SessionsViewState.PendingDelete(
                sessionId = session.id,
                sessionName = session.name,
            )
        )
    }

    private fun confirmDelete() {
        val pending = pendingDeleteFrom(_state.value) ?: return
        appScope.scope.launch {
            deleteSession(pending.sessionId)
            if (selectedSessionId == pending.sessionId) {
                selectedSessionId = null
            }
            updatePendingDelete(null)
        }
    }

    private fun saveEditor() {
        val editor = editorFrom(_state.value) ?: return
        if (editor.name.trim().isEmpty()) {
            updateEditor { current -> current?.copy(nameError = "Name is required") }
            return
        }
        appScope.scope.launch {
            val selected = selectedFrom(latestSessions)
            val inWorldDate = when (val parsed = parsedEditorDate(editor)) {
                DateParse.None -> null
                DateParse.Invalid -> {
                    updateEditor { current ->
                        current?.copy(dateError = "Enter a year, month, and day, or clear the date.")
                    }
                    return@launch
                }
                is DateParse.Found -> parsed.date
            }
            val result = if (editor.sessionId == null) {
                createSession(
                    SessionDraft(
                        name = editor.name,
                        notes = editor.notes,
                        inWorldDate = inWorldDate,
                        scenes = emptyList(),
                        marchOrder = emptyList(),
                    )
                )
            } else {
                updateSession(
                    editor.sessionId,
                    SessionDraft(
                        name = editor.name,
                        notes = editor.notes,
                        inWorldDate = inWorldDate,
                        scenes = selected?.scenes.orEmpty(),
                        marchOrder = selected?.marchOrder.orEmpty(),
                    ),
                )
            }
            when (result) {
                is CreateSessionUseCase.Result.Created -> {
                    selectedSessionId = result.session.id
                    updateEditor { null }
                }
                CreateSessionUseCase.Result.InvalidName,
                UpdateSessionUseCase.Result.InvalidName,
                -> updateEditor { current -> current?.copy(nameError = "Name is required") }
                CreateSessionUseCase.Result.InvalidDate,
                UpdateSessionUseCase.Result.InvalidDate,
                -> updateEditor { current ->
                    current?.copy(dateError = "That date is not valid on this world's calendar.")
                }
                CreateSessionUseCase.Result.NoActiveCampaign,
                UpdateSessionUseCase.Result.Updated,
                UpdateSessionUseCase.Result.NotFound,
                -> updateEditor { null }
            }
        }
    }

    private fun mutateSelectedSession(transform: (Session) -> Session) {
        val session = selectedFrom(latestSessions) ?: return
        appScope.scope.launch {
            updateSession(session.id, draftFrom(transform(session)))
        }
    }

    private fun addMarchPerson(person: PersonRef) {
        val session = selectedFrom(latestSessions) ?: return
        if (session.marchOrder.any { samePerson(it.person, person) }) {
            return
        }
        val name = personOptions().firstOrNull { samePerson(it.person, person) }?.name
            ?: return
        mutateSelectedSession { current ->
            current.copy(
                marchOrder = current.marchOrder + MarchOrderEntry(
                    id = "",
                    person = person,
                    displayName = name.substringBefore(" (world)"),
                ),
            )
        }
    }

    private fun samePerson(left: PersonRef, right: PersonRef): Boolean {
        return left == right
    }

    private fun openThreadEditor(threadId: String?) {
        val thread = threadId?.let { id -> latestThreads.firstOrNull { it.id == id } }
        val selectedId = selectedSessionId
        val editor = SessionsViewState.ThreadEditorState(
            threadId = thread?.id,
            title = thread?.title.orEmpty(),
            details = thread?.details.orEmpty(),
            status = thread?.status ?: PlotThreadStatus.Open,
            priority = thread?.priority ?: PlotThreadPriority.Medium,
            attachToSession = thread?.sessionId?.let { it == selectedId } ?: true,
            titleError = null,
        )
        updateThreadEditor { editor }
    }

    private fun saveThreadEditor() {
        val editor = threadEditorFrom(_state.value) ?: return
        if (editor.title.trim().isEmpty()) {
            updateThreadEditor { current -> current?.copy(titleError = "Title is required") }
            return
        }
        val sessionId = if (editor.attachToSession) selectedSessionId else null
        val draft = PlotThreadDraft(
            sessionId = sessionId,
            title = editor.title,
            details = editor.details,
            status = editor.status,
            priority = editor.priority,
        )
        appScope.scope.launch {
            if (editor.threadId == null) {
                createPlotThread(draft)
            } else {
                updatePlotThread(editor.threadId, draft)
            }
            updateThreadEditor { null }
        }
    }

    private fun requestThreadDelete(threadId: String) {
        val thread = latestThreads.firstOrNull { it.id == threadId } ?: return
        updateThreadDelete(
            SessionsViewState.PendingThreadDelete(
                threadId = thread.id,
                title = thread.title,
            )
        )
    }

    private fun confirmThreadDelete() {
        val pending = threadDeleteFrom(_state.value) ?: return
        appScope.scope.launch {
            deletePlotThread(pending.threadId)
            updateThreadDelete(null)
        }
    }

    private fun openDocEditor(docId: String?) {
        val doc = docId?.let { id -> latestDocs.firstOrNull { it.id == id } }
        val selectedId = selectedSessionId
        val editor = SessionsViewState.DocEditorState(
            docId = doc?.id,
            title = doc?.title.orEmpty(),
            pathOrUrl = doc?.pathOrUrl.orEmpty(),
            attachToSession = doc?.sessionId?.let { it == selectedId } ?: true,
            titleError = null,
            pathError = null,
        )
        updateDocEditor { editor }
    }

    private fun saveDocEditor() {
        val editor = docEditorFrom(_state.value) ?: return
        val titleBlank = editor.title.trim().isEmpty()
        val pathBlank = editor.pathOrUrl.trim().isEmpty()
        if (titleBlank || pathBlank) {
            updateDocEditor { current ->
                current?.copy(
                    titleError = if (titleBlank) "Title is required" else null,
                    pathError = if (pathBlank) "Path or URL is required" else null,
                )
            }
            return
        }
        val sessionId = if (editor.attachToSession) selectedSessionId else null
        val draft = ReferenceDocDraft(
            sessionId = sessionId,
            title = editor.title,
            pathOrUrl = editor.pathOrUrl,
        )
        appScope.scope.launch {
            if (editor.docId == null) {
                createReferenceDoc(draft)
            } else {
                updateReferenceDoc(editor.docId, draft)
            }
            updateDocEditor { null }
        }
    }

    private fun requestDocDelete(docId: String) {
        val doc = latestDocs.firstOrNull { it.id == docId } ?: return
        updateDocDelete(
            SessionsViewState.PendingDocDelete(
                docId = doc.id,
                title = doc.title,
            )
        )
    }

    private fun confirmDocDelete() {
        val pending = docDeleteFrom(_state.value) ?: return
        appScope.scope.launch {
            deleteReferenceDoc(pending.docId)
            updateDocDelete(null)
        }
    }

    private fun openGenerator() {
        updateGenerator(
            SessionsViewState.GeneratorState(
                method = AbilityScoreMethod.FourD6DropLowest,
                draft = null,
                destination = SessionNpcDraftDestination.WorldLibrary,
            )
        )
    }

    private fun rollGenerator() {
        val generator = generatorFrom(_state.value) ?: return
        val draft = generateRandomNpc(generator.method)
        updateGenerator { current -> current?.copy(draft = draft) }
    }

    private fun saveGenerator() {
        val generator = generatorFrom(_state.value) ?: return
        val draft = generator.draft ?: return
        appScope.scope.launch {
            saveSessionNpcDraft(draft, generator.destination)
            updateGenerator(null)
        }
    }

    private fun createEditor(): SessionsViewState.SessionEditorState {
        val defaultDate = latestCalendar?.currentDate
            ?: latestSessions.maxByOrNull { it.updatedAt }?.inWorldDate
        return withDatePreview(
            SessionsViewState.SessionEditorState(
                sessionId = null,
                name = "",
                notes = "",
                yearText = defaultDate?.year?.toString().orEmpty(),
                monthId = defaultDate?.monthId,
                dayText = defaultDate?.day?.toString().orEmpty(),
                months = latestCalendar?.months.orEmpty(),
                datePreview = null,
                dateError = null,
                nameError = null,
            )
        )
    }

    private fun draftFrom(session: Session): SessionDraft {
        return SessionDraft(
            name = session.name,
            notes = session.notes,
            inWorldDate = session.inWorldDate,
            scenes = session.scenes,
            marchOrder = session.marchOrder,
        )
    }

    private fun dateLabels(sessions: List<Session>): Map<String, String> {
        return sessions.mapNotNull { session ->
            dateLabel(session)?.let { session.id to it }
        }.toMap()
    }

    private fun dateLabel(session: Session): String? {
        val calendar = latestCalendar ?: return null
        val date = session.inWorldDate ?: return null
        return dateFormatter.format(calendar, date)
    }

    private fun withDatePreview(
        editor: SessionsViewState.SessionEditorState,
    ): SessionsViewState.SessionEditorState {
        val calendar = latestCalendar
        val date = (parsedEditorDate(editor) as? DateParse.Found)?.date
        val preview = if (calendar != null && date != null) {
            dateFormatter.format(calendar, date)
        } else {
            null
        }
        return editor.copy(datePreview = preview, months = calendar?.months.orEmpty())
    }

    private fun parsedEditorDate(editor: SessionsViewState.SessionEditorState): DateParse {
        val yearText = editor.yearText.trim()
        val dayText = editor.dayText.trim()
        if (yearText.isEmpty() && dayText.isEmpty() && editor.monthId == null) {
            return DateParse.None
        }
        val year = yearText.toIntOrNull()
        val day = dayText.toIntOrNull()
        val monthId = editor.monthId
        if (year == null || day == null || monthId.isNullOrBlank()) {
            return DateParse.Invalid
        }
        return DateParse.Found(WorldDate(year = year, monthId = monthId, day = day))
    }

    private fun <T> move(items: List<T>, index: Int, delta: Int): List<T> {
        val target = index + delta
        if (index !in items.indices || target !in items.indices) {
            return items
        }
        val mutable = items.toMutableList()
        val item = mutable.removeAt(index)
        mutable.add(target, item)
        return mutable
    }

    private fun updateEditor(
        transform: (SessionsViewState.SessionEditorState?) -> SessionsViewState.SessionEditorState?,
    ) {
        when (val current = _state.value) {
            is SessionsViewState.Empty -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            is SessionsViewState.Content -> {
                _state.value = current.copy(editor = transform(current.editor))
            }
            else -> Unit
        }
    }

    private fun updatePendingDelete(pendingDelete: SessionsViewState.PendingDelete?) {
        val current = _state.value
        if (current is SessionsViewState.Content) {
            _state.value = current.copy(pendingDelete = pendingDelete)
        }
    }

    private fun updateThreadEditor(
        transform: (SessionsViewState.ThreadEditorState?) -> SessionsViewState.ThreadEditorState?,
    ) {
        val current = _state.value
        if (current is SessionsViewState.Content) {
            _state.value = current.copy(threadEditor = transform(current.threadEditor))
        }
    }

    private fun updateThreadDelete(pending: SessionsViewState.PendingThreadDelete?) {
        val current = _state.value
        if (current is SessionsViewState.Content) {
            _state.value = current.copy(pendingThreadDelete = pending)
        }
    }

    private fun updateDocEditor(
        transform: (SessionsViewState.DocEditorState?) -> SessionsViewState.DocEditorState?,
    ) {
        val current = _state.value
        if (current is SessionsViewState.Content) {
            _state.value = current.copy(docEditor = transform(current.docEditor))
        }
    }

    private fun updateDocDelete(pending: SessionsViewState.PendingDocDelete?) {
        val current = _state.value
        if (current is SessionsViewState.Content) {
            _state.value = current.copy(pendingDocDelete = pending)
        }
    }

    private fun updateGenerator(generator: SessionsViewState.GeneratorState?) {
        val current = _state.value
        if (current is SessionsViewState.Content) {
            _state.value = current.copy(generator = generator)
        }
    }

    private fun updateGenerator(
        transform: (SessionsViewState.GeneratorState?) -> SessionsViewState.GeneratorState?,
    ) {
        updateGenerator(transform(generatorFrom(_state.value)))
    }

    private fun selectedFrom(sessions: List<Session>): Session? {
        return selectedSessionId?.let { id -> sessions.firstOrNull { it.id == id } }
    }

    private fun editorFrom(state: SessionsViewState): SessionsViewState.SessionEditorState? {
        return when (state) {
            is SessionsViewState.Empty -> state.editor
            is SessionsViewState.Content -> state.editor
            else -> null
        }
    }

    private fun pendingDeleteFrom(state: SessionsViewState): SessionsViewState.PendingDelete? {
        return (state as? SessionsViewState.Content)?.pendingDelete
    }

    private fun threadEditorFrom(state: SessionsViewState): SessionsViewState.ThreadEditorState? {
        return (state as? SessionsViewState.Content)?.threadEditor
    }

    private fun threadDeleteFrom(state: SessionsViewState): SessionsViewState.PendingThreadDelete? {
        return (state as? SessionsViewState.Content)?.pendingThreadDelete
    }

    private fun docEditorFrom(state: SessionsViewState): SessionsViewState.DocEditorState? {
        return (state as? SessionsViewState.Content)?.docEditor
    }

    private fun docDeleteFrom(state: SessionsViewState): SessionsViewState.PendingDocDelete? {
        return (state as? SessionsViewState.Content)?.pendingDocDelete
    }

    private fun generatorFrom(state: SessionsViewState): SessionsViewState.GeneratorState? {
        return (state as? SessionsViewState.Content)?.generator
    }

    private fun currentOverlays(state: SessionsViewState): OverlayState {
        val content = state as? SessionsViewState.Content
        return OverlayState(
            threadEditor = content?.threadEditor,
            docEditor = content?.docEditor,
            generator = content?.generator,
            pendingDelete = content?.pendingDelete,
            pendingThreadDelete = content?.pendingThreadDelete,
            pendingDocDelete = content?.pendingDocDelete,
        )
    }

    private fun clearLatest() {
        latestSessions = emptyList()
        latestQuests = emptyList()
        latestLocations = emptyList()
        latestOverlays = emptyList()
        latestPeople = PeopleSnapshot(emptyList(), emptyList())
        latestThreads = emptyList()
        latestDocs = emptyList()
        latestCalendar = null
        selectedSessionId = null
    }

    private sealed interface DateParse {
        data object None : DateParse
        data object Invalid : DateParse
        data class Found(val date: WorldDate) : DateParse
    }

    private data class ContextBundle(
        val details: ActiveContextDetails,
        val sessions: List<Session>,
        val quests: List<Quest>,
        val locations: List<Location>,
        val calendar: WorldCalendar?,
    )

    private data class SupportBundle(
        val overlays: List<LocationOverlay>,
        val people: PeopleSnapshot,
        val threads: List<PlotThread>,
        val docs: List<ReferenceDoc>,
    )

    private data class LoadedSnapshot(
        val context: ContextBundle,
        val support: SupportBundle,
    )

    private data class OverlayState(
        val threadEditor: SessionsViewState.ThreadEditorState?,
        val docEditor: SessionsViewState.DocEditorState?,
        val generator: SessionsViewState.GeneratorState?,
        val pendingDelete: SessionsViewState.PendingDelete?,
        val pendingThreadDelete: SessionsViewState.PendingThreadDelete?,
        val pendingDocDelete: SessionsViewState.PendingDocDelete?,
    )
}
