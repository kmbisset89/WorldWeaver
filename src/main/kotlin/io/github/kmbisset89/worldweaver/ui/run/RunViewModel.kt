package io.github.kmbisset89.worldweaver.ui.run

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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.ActiveContext
import io.github.kmbisset89.worldweaver.domain.ActiveContextDetails
import io.github.kmbisset89.worldweaver.domain.AwardPartyExperienceUseCase
import io.github.kmbisset89.worldweaver.domain.AwardPartyLevelUseCase
import io.github.kmbisset89.worldweaver.domain.CampaignPerson
import io.github.kmbisset89.worldweaver.domain.CloseSessionUseCase
import io.github.kmbisset89.worldweaver.domain.CreateSessionClockUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteSessionClockUseCase
import io.github.kmbisset89.worldweaver.domain.DeleteSessionRecordingUseCase
import io.github.kmbisset89.worldweaver.domain.Encounter
import io.github.kmbisset89.worldweaver.domain.EncounterStatus
import io.github.kmbisset89.worldweaver.domain.FifthEditionSheet
import io.github.kmbisset89.worldweaver.domain.LevelingMode
import io.github.kmbisset89.worldweaver.domain.LoadSessionReferencePeekUseCase
import io.github.kmbisset89.worldweaver.domain.LoadWikilinkCatalogUseCase
import io.github.kmbisset89.worldweaver.domain.Location
import io.github.kmbisset89.worldweaver.domain.LocationOverlay
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveEncountersForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLocationOverlaysForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLocationsForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObservePeopleForActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveQuestsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveRandomTablesForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveSessionClocksForActiveSessionUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveSessionsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldCalendarForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldCalendarObservancesForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.PeopleSnapshot
import io.github.kmbisset89.worldweaver.domain.PersonKind
import io.github.kmbisset89.worldweaver.domain.Quest
import io.github.kmbisset89.worldweaver.domain.QuestStatus
import io.github.kmbisset89.worldweaver.domain.RandomTable
import io.github.kmbisset89.worldweaver.domain.RollRandomTableUseCase
import io.github.kmbisset89.worldweaver.domain.SearchHit
import io.github.kmbisset89.worldweaver.domain.SearchSessionReferencesUseCase
import io.github.kmbisset89.worldweaver.domain.Session
import io.github.kmbisset89.worldweaver.domain.SessionCaptureDeviceProbe
import io.github.kmbisset89.worldweaver.domain.SessionCameraPermissionSettingsOpener
import io.github.kmbisset89.worldweaver.domain.SessionClock
import io.github.kmbisset89.worldweaver.domain.SessionMicrophoneDevice
import io.github.kmbisset89.worldweaver.domain.SessionRecording
import io.github.kmbisset89.worldweaver.domain.SessionRecordingCapture
import io.github.kmbisset89.worldweaver.domain.SessionRecordingFileStore
import io.github.kmbisset89.worldweaver.domain.SessionRecordingKind
import io.github.kmbisset89.worldweaver.domain.SessionReferencePeek
import io.github.kmbisset89.worldweaver.domain.UpdateSessionClockUseCase
import io.github.kmbisset89.worldweaver.domain.UpdateSessionRunnerNotesUseCase
import io.github.kmbisset89.worldweaver.domain.WikilinkCatalog
import io.github.kmbisset89.worldweaver.domain.WikilinkDraftCompleter
import io.github.kmbisset89.worldweaver.domain.WikilinkTarget
import io.github.kmbisset89.worldweaver.domain.WikilinkTextParser
import io.github.kmbisset89.worldweaver.domain.WikilinkTextResolver
import io.github.kmbisset89.worldweaver.domain.WorldCalendar
import io.github.kmbisset89.worldweaver.domain.WorldCalendarObservance
import io.github.kmbisset89.worldweaver.domain.WorldDateFormatter
import io.github.kmbisset89.worldweaver.ui.advancement.AdvancementPrompt
import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

internal class RunViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContext: ObserveActiveContextUseCase,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeSessions: ObserveSessionsForActiveCampaignUseCase,
    private val observeQuests: ObserveQuestsForActiveCampaignUseCase,
    private val observePeople: ObservePeopleForActiveContextUseCase,
    private val observeEncounters: ObserveEncountersForActiveCampaignUseCase,
    private val observeCalendar: ObserveWorldCalendarForActiveWorldUseCase,
    private val observeObservances: ObserveWorldCalendarObservancesForActiveWorldUseCase,
    private val observeOverlays: ObserveLocationOverlaysForActiveCampaignUseCase,
    private val observeLocations: ObserveLocationsForActiveWorldUseCase,
    private val observeClocks: ObserveSessionClocksForActiveSessionUseCase,
    private val observeTables: ObserveRandomTablesForActiveWorldUseCase,
    private val updateRunnerNotes: UpdateSessionRunnerNotesUseCase,
    private val createClock: CreateSessionClockUseCase,
    private val updateClock: UpdateSessionClockUseCase,
    private val deleteClock: DeleteSessionClockUseCase,
    private val searchReferences: SearchSessionReferencesUseCase,
    private val loadPeek: LoadSessionReferencePeekUseCase,
    private val closeSession: CloseSessionUseCase,
    private val awardPartyLevel: AwardPartyLevelUseCase,
    private val awardPartyExperience: AwardPartyExperienceUseCase,
    private val recordingCapture: SessionRecordingCapture,
    private val recordingFileStore: SessionRecordingFileStore,
    private val captureDeviceProbe: SessionCaptureDeviceProbe,
    private val cameraPermissionSettingsOpener: SessionCameraPermissionSettingsOpener,
    private val deleteRecording: DeleteSessionRecordingUseCase,
    private val loadWikilinkCatalog: LoadWikilinkCatalogUseCase,
    private val rollTable: RollRandomTableUseCase,
    private val dateFormatter: WorldDateFormatter = WorldDateFormatter(),
    private val wikilinkParser: WikilinkTextParser = WikilinkTextParser(),
    private val wikilinkResolver: WikilinkTextResolver = WikilinkTextResolver(),
    private val wikilinkCompleter: WikilinkDraftCompleter = WikilinkDraftCompleter(),
) {
    private val _state = MutableStateFlow<RunViewState>(RunViewState.Loading)
    val state: StateFlow<RunViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<RunViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<RunViewEffect> = _effects.asSharedFlow()

    private val _cameraPreview = MutableStateFlow<ImageBitmap?>(null)
    val cameraPreview: StateFlow<ImageBitmap?> = _cameraPreview.asStateFlow()

    private var observeJob: Job? = null
    private var notesSaveJob: Job? = null
    private var lookupJob: Job? = null
    private var timerJob: Job? = null
    private var whyItMatters: String = ""
    private var isClosing: Boolean = false
    private var closeError: String? = null
    private var latestSessionId: String? = null
    private var latestCampaignId: String? = null
    private var latestWorldId: String? = null
    private var latestLevelingMode = LevelingMode.Milestone
    private var advancementPrompt: AdvancementPrompt? = null
    private var draftNotes: String? = null
    private var draftScratchNotes: String? = null
    private var clockLabel: String = ""
    private var clockSegmentCount: Int = SessionClock.DEFAULT_SEGMENT_COUNT
    private var clockError: String? = null
    private var timerMinutesText: String = "5"
    private var timerRemainingSeconds: Int = 5 * 60
    private var timerRunning: Boolean = false
    private var timerFinished: Boolean = false
    private var lookupQuery: String = ""
    private var lookupResults: List<SearchHit> = emptyList()
    private var lookupPeek: SessionReferencePeek? = null
    private var latestClocks: List<SessionClock> = emptyList()
    private var latestTables: List<RandomTable> = emptyList()
    private var lastTableRoll: String? = null
    private var hasMicrophone: Boolean = false
    private var hasCamera: Boolean = false
    private var microphones: List<SessionMicrophoneDevice> = emptyList()
    private var selectedMicrophoneId: String? = null
    private var cameraNeedsPermission: Boolean = false
    private var recordingMode: SessionRecordingKind = SessionRecordingKind.Audio
    private var isRecording: Boolean = false
    private var recordingElapsedSeconds: Int = 0
    private var recordings: List<RunViewState.RecordingLine> = emptyList()
    private var recordingError: String? = null
    private var recordingElapsedJob: Job? = null
    private var latestCatalog: WikilinkCatalog = WikilinkCatalog(emptyList())
    private var catalogJob: Job? = null

    init {
        refreshCaptureDevices()
        observe()
        appScope.scope.launch {
            recordingCapture.previewImages.collect { image ->
                _cameraPreview.value = image?.toComposeImageBitmap()
            }
        }
    }

    fun onInteraction(interaction: RunInteraction) {
        when (interaction) {
            RunInteraction.ScreenStarted -> {
                probeDevices()
                loadRecordings()
            }
            RunInteraction.ScreenStopped -> recordingCapture.stopPreview()
            RunInteraction.RetrySelected -> observe()
            RunInteraction.CreateWorldSelected -> emitEffect(RunViewEffect.OpenWorlds)
            RunInteraction.CreateCampaignSelected -> emitEffect(RunViewEffect.OpenCampaigns)
            RunInteraction.OpenSessionsSelected -> emitEffect(RunViewEffect.OpenSessions)
            RunInteraction.OpenEncountersSelected -> emitEffect(RunViewEffect.OpenEncounters)
            RunInteraction.OpenMapsSelected -> emitEffect(RunViewEffect.OpenMaps)
            RunInteraction.PlayerViewSelected -> emitEffect(RunViewEffect.OpenPlayerView)
            RunInteraction.DiceTraySelected -> emitEffect(RunViewEffect.OpenDiceTray)
            RunInteraction.AtmosphereTraySelected -> emitEffect(RunViewEffect.OpenAtmosphereTray)
            is RunInteraction.PersonPeeked -> emitEffect(
                RunViewEffect.OpenPersonSheet(
                    membership = interaction.membership,
                    personId = interaction.personId,
                )
            )
            is RunInteraction.SessionNotesChanged -> {
                draftNotes = interaction.value
                refreshContentFields()
                scheduleNotesSave()
            }
            is RunInteraction.ScratchNotesChanged -> {
                draftScratchNotes = interaction.value
                refreshContentFields()
                scheduleNotesSave()
            }
            is RunInteraction.SessionNotesWikilinkSelected -> {
                draftNotes = wikilinkCompleter.complete(draftNotes.orEmpty().ifEmpty {
                    (_state.value as? RunViewState.Content)?.sessionNotes.orEmpty()
                }, interaction.target)
                refreshContentFields()
                scheduleNotesSave()
            }
            is RunInteraction.ScratchNotesWikilinkSelected -> {
                draftScratchNotes = wikilinkCompleter.complete(
                    draftScratchNotes.orEmpty().ifEmpty {
                        (_state.value as? RunViewState.Content)?.scratchNotes.orEmpty()
                    },
                    interaction.target,
                )
                refreshContentFields()
                scheduleNotesSave()
            }
            is RunInteraction.WikilinkSelected -> emitEffect(
                RunViewEffect.OpenSearchHit(interaction.target.toSearchHit())
            )
            is RunInteraction.LookupQueryChanged -> changeLookupQuery(interaction.query)
            is RunInteraction.LookupResultSelected -> selectLookupResult(interaction.hit)
            RunInteraction.LookupPeekDismissed -> {
                lookupPeek = null
                refreshContentFields()
            }
            RunInteraction.LookupOpened -> lookupPeek?.hit?.let { hit ->
                emitEffect(RunViewEffect.OpenSearchHit(hit))
            }
            is RunInteraction.ClockLabelChanged -> {
                clockLabel = interaction.value
                clockError = null
                refreshContentFields()
            }
            is RunInteraction.ClockSegmentCountSelected -> {
                clockSegmentCount = interaction.count.coerceIn(
                    SessionClock.MIN_SEGMENT_COUNT,
                    SessionClock.MAX_SEGMENT_COUNT,
                )
                refreshContentFields()
            }
            RunInteraction.ClockCreateSelected -> createProgressClock()
            is RunInteraction.ClockFilledSelected -> fillClock(interaction.clockId, interaction.filledCount)
            is RunInteraction.ClockDeleteSelected -> removeClock(interaction.clockId)
            is RunInteraction.TableRollSelected -> rollRandomTable(interaction.tableId)
            is RunInteraction.TimerMinutesChanged -> {
                timerMinutesText = interaction.value
                refreshContentFields()
            }
            is RunInteraction.TimerPresetSelected -> applyTimerPreset(interaction.minutes)
            RunInteraction.TimerStartSelected -> startTimer()
            RunInteraction.TimerPauseSelected -> pauseTimer()
            RunInteraction.TimerResetSelected -> resetTimer()
            is RunInteraction.WhyItMattersChanged -> {
                whyItMatters = interaction.value
                refreshContentFields()
            }
            RunInteraction.CloseSessionSelected -> closeActiveSession()
            RunInteraction.AdvancementDismissed -> dismissAdvancement()
            RunInteraction.AwardLevelConfirmed -> confirmAwardLevel()
            is RunInteraction.AwardExperienceAmountChanged -> {
                val current = advancementPrompt
                if (current is AdvancementPrompt.AwardExperience) {
                    advancementPrompt = current.copy(
                        amountText = interaction.value,
                        amountError = null,
                    )
                    refreshContentFields()
                }
            }
            RunInteraction.AwardExperienceConfirmed -> confirmAwardExperience()
            is RunInteraction.RecordingModeSelected -> selectRecordingMode(interaction.kind)
            is RunInteraction.MicrophoneSelected -> selectMicrophone(interaction.deviceId)
            RunInteraction.RecordToggled -> toggleRecording()
            RunInteraction.CameraPermissionRequested -> requestCameraPermission()
            is RunInteraction.RecordingOpened -> emitEffect(RunViewEffect.OpenRecording(interaction.path))
            is RunInteraction.RecordingDeleteSelected -> removeRecording(interaction.recordingId)
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = RunViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                combine(
                    observeActiveContext(),
                    observeActiveContextDetails(),
                    observeSessions(),
                    observeQuests(),
                    observePeople(),
                ) { context, details, sessions, quests, people ->
                    PrimaryBundle(context, details, sessions, quests, people)
                },
                combine(
                    observeEncounters(),
                    observeCalendar(),
                    observeObservances(),
                    observeOverlays(),
                    observeLocations(),
                ) { encounters, calendar, observances, overlays, locations ->
                    SupportBundle(encounters, calendar, observances, overlays, locations)
                },
                observeClocks(),
                observeTables(),
            ) { primary, support, clocks, tables ->
                LoadedSnapshot(primary, support, clocks, tables)
            }
                .catch { error ->
                    _state.value = RunViewState.Error(
                        message = error.message ?: "Could not load tonight",
                        canRetry = true,
                    )
                }
                .collect { snapshot ->
                    applyLoaded(snapshot)
                }
        }
    }

    private fun applyLoaded(snapshot: LoadedSnapshot) {
        val world = snapshot.primary.details.world
        if (world == null) {
            latestSessionId = null
            latestCampaignId = null
            latestWorldId = null
            advancementPrompt = null
            stopCaptureForInactiveSession()
            _state.value = RunViewState.NoActiveWorld
            return
        }
        latestWorldId = world.id
        refreshWikilinkCatalog(world.id)
        val campaign = snapshot.primary.details.campaign
        if (campaign == null) {
            latestSessionId = null
            latestCampaignId = null
            advancementPrompt = null
            stopCaptureForInactiveSession()
            _state.value = RunViewState.NoActiveCampaign
            return
        }
        latestCampaignId = campaign.id
        latestLevelingMode = campaign.levelingMode
        val sessionId = snapshot.primary.context.activeSessionId
        val session = snapshot.primary.sessions.firstOrNull { it.id == sessionId }
        if (session == null) {
            latestSessionId = null
            advancementPrompt = null
            stopCaptureForInactiveSession()
            _state.value = RunViewState.NoActiveSession(
                worldName = world.name,
                campaignName = campaign.name,
            )
            return
        }
        if (latestSessionId != session.id) {
            finalizeRecording()
            whyItMatters = ""
            closeError = null
            advancementPrompt = null
            draftNotes = null
            draftScratchNotes = null
            clockLabel = ""
            clockError = null
            lookupQuery = ""
            lookupResults = emptyList()
            lookupPeek = null
            recordings = recordingFileStore.list(session.id).map { recording -> recordingLine(recording) }
        }
        latestSessionId = session.id
        latestClocks = snapshot.clocks
        latestTables = snapshot.tables
        _state.value = contentState(
            worldName = world.name,
            campaignName = campaign.name,
            session = session,
            quests = snapshot.primary.quests,
            people = snapshot.primary.people,
            encounters = snapshot.support.encounters,
            calendar = snapshot.support.calendar,
            observances = snapshot.support.observances,
            overlays = snapshot.support.overlays,
            locations = snapshot.support.locations,
            clocks = snapshot.clocks,
        )
    }

    private fun contentState(
        worldName: String,
        campaignName: String,
        session: Session,
        quests: List<Quest>,
        people: PeopleSnapshot,
        encounters: List<Encounter>,
        calendar: WorldCalendar?,
        observances: List<WorldCalendarObservance>,
        overlays: List<LocationOverlay>,
        locations: List<Location>,
        clocks: List<SessionClock>,
    ): RunViewState.Content {
        val inWorldDateLabel = if (calendar != null && session.inWorldDate != null) {
            dateFormatter.format(calendar, session.inWorldDate)
        } else {
            null
        }
        val calendarTodayLabel = calendar?.currentDate?.let { date ->
            dateFormatter.format(calendar, date)
        }
        val matchDate = session.inWorldDate ?: calendar?.currentDate
        val observanceNames = if (matchDate == null) {
            emptyList()
        } else {
            observances.filter { it.matches(matchDate) }.map { it.name }
        }
        val activeEncounter = encounters.firstOrNull { it.status == EncounterStatus.Active }
        return RunViewState.Content(
            worldName = worldName,
            campaignName = campaignName,
            sessionId = session.id,
            sessionName = session.name,
            sessionNotes = draftNotes ?: session.notes,
            scratchNotes = draftScratchNotes ?: session.scratchNotes,
            recap = session.recap,
            sessionNotesSpans = wikilinkResolver.resolve(draftNotes ?: session.notes, latestCatalog),
            scratchNotesSpans = wikilinkResolver.resolve(
                draftScratchNotes ?: session.scratchNotes,
                latestCatalog,
            ),
            recapSpans = wikilinkResolver.resolve(session.recap, latestCatalog),
            notesSuggestions = suggestionsFor(draftNotes ?: session.notes),
            scratchSuggestions = suggestionsFor(draftScratchNotes ?: session.scratchNotes),
            inWorldDateLabel = inWorldDateLabel,
            calendarTodayLabel = calendarTodayLabel,
            observanceNames = observanceNames,
            party = people.campaignPeople
                .filter { it.kind == PersonKind.PlayerCharacter }
                .map { person -> partyMember(person) },
            questObjectives = quests
                .filter { it.status == QuestStatus.Active }
                .flatMap { quest ->
                    quest.objectives.map { objective ->
                        RunViewState.QuestObjectiveLine(
                            questTitle = quest.title,
                            objectiveTitle = objective.title,
                            status = objective.status.name,
                        )
                    }
                },
            scenes = session.scenes.map { scene ->
                RunViewState.SceneLine(title = scene.title, notes = scene.notes)
            },
            clocks = clocks.map { RunViewState.ClockLine(it) },
            clockLabel = clockLabel,
            clockSegmentCount = clockSegmentCount,
            clockError = clockError,
            timerMinutesText = timerMinutesText,
            timerRemainingSeconds = timerRemainingSeconds,
            timerRunning = timerRunning,
            timerFinished = timerFinished,
            lookupQuery = lookupQuery,
            lookupResults = lookupResults,
            lookupPeek = lookupPeek,
            activeEncounter = activeEncounter?.let { encounter ->
                RunViewState.EncounterLine(
                    name = encounter.name,
                    status = encounter.status.displayName,
                    hasMap = !encounter.battleMapId.isNullOrBlank(),
                    roundLabel = if (encounter.status == EncounterStatus.Active) {
                        "Round ${encounter.currentRound}"
                    } else {
                        null
                    },
                )
            },
            partyLocations = overlays.filter { it.hasPartyPresence }.map { overlay ->
                locations.firstOrNull { it.id == overlay.locationId }?.name ?: overlay.locationId
            },
            whyItMatters = whyItMatters,
            isClosing = isClosing,
            closeError = closeError,
            advancementPrompt = advancementPrompt,
            hasMicrophone = hasMicrophone,
            hasCamera = hasCamera,
            microphones = microphones,
            selectedMicrophoneId = selectedMicrophoneId,
            recordingMode = recordingMode,
            isRecording = isRecording,
            recordingElapsedLabel = elapsedLabel(recordingElapsedSeconds),
            recordings = recordings,
            recordingError = recordingError,
            cameraNeedsPermission = cameraNeedsPermission,
            tables = latestTables.map { table ->
                RunViewState.TableLine(tableId = table.id, name = table.name)
            },
            lastTableRoll = lastTableRoll,
        )
    }

    private fun partyMember(person: CampaignPerson): RunViewState.PartyMember {
        val sheet = person.sheet
        val fifth = sheet as? FifthEditionSheet
        val hitPoints = person.overlayHitPoints ?: sheet.hitPoints
        val slotsLabel = fifth?.spellSlots
            ?.filter { it.maximum > 0 }
            ?.joinToString("  ") { slot ->
                "L${slot.level} ${slot.remaining()}/${slot.maximum}"
            }
            .orEmpty()
        return RunViewState.PartyMember(
            personId = person.id,
            membership = PersonMembership.ThisCampaign,
            name = person.name,
            hitPoints = hitPoints,
            maxHitPoints = sheet.maxHitPoints,
            armorClass = sheet.armorClass,
            concentratingSpell = fifth?.concentratingSpell.orEmpty(),
            spellSlotsLabel = slotsLabel,
        )
    }

    private fun refreshContentFields() {
        val current = _state.value
        if (current is RunViewState.Content) {
            _state.value = current.copy(
                sessionNotes = draftNotes ?: current.sessionNotes,
                scratchNotes = draftScratchNotes ?: current.scratchNotes,
                sessionNotesSpans = wikilinkResolver.resolve(
                    draftNotes ?: current.sessionNotes,
                    latestCatalog,
                ),
                scratchNotesSpans = wikilinkResolver.resolve(
                    draftScratchNotes ?: current.scratchNotes,
                    latestCatalog,
                ),
                recapSpans = wikilinkResolver.resolve(current.recap, latestCatalog),
                notesSuggestions = suggestionsFor(draftNotes ?: current.sessionNotes),
                scratchSuggestions = suggestionsFor(draftScratchNotes ?: current.scratchNotes),
                clocks = latestClocks.map { RunViewState.ClockLine(it) },
                clockLabel = clockLabel,
                clockSegmentCount = clockSegmentCount,
                clockError = clockError,
                timerMinutesText = timerMinutesText,
                timerRemainingSeconds = timerRemainingSeconds,
                timerRunning = timerRunning,
                timerFinished = timerFinished,
                lookupQuery = lookupQuery,
                lookupResults = lookupResults,
                lookupPeek = lookupPeek,
                whyItMatters = whyItMatters,
                isClosing = isClosing,
                closeError = closeError,
                advancementPrompt = advancementPrompt,
                hasMicrophone = hasMicrophone,
                hasCamera = hasCamera,
                microphones = microphones,
                selectedMicrophoneId = selectedMicrophoneId,
                recordingMode = recordingMode,
                isRecording = isRecording,
                recordingElapsedLabel = elapsedLabel(recordingElapsedSeconds),
                recordings = recordings,
                recordingError = recordingError,
                cameraNeedsPermission = cameraNeedsPermission,
                tables = latestTables.map { table ->
                    RunViewState.TableLine(tableId = table.id, name = table.name)
                },
                lastTableRoll = lastTableRoll,
            )
        }
    }

    private fun scheduleNotesSave() {
        val sessionId = latestSessionId ?: return
        notesSaveJob?.cancel()
        notesSaveJob = appScope.scope.launch {
            delay(NOTES_SAVE_DELAY_MS)
            val notes = draftNotes
            val scratch = draftScratchNotes
            if (notes == null && scratch == null) {
                return@launch
            }
            val current = (_state.value as? RunViewState.Content) ?: return@launch
            updateRunnerNotes(
                sessionId = sessionId,
                notes = notes ?: current.sessionNotes,
                scratchNotes = scratch ?: current.scratchNotes,
            )
        }
    }

    private fun changeLookupQuery(query: String) {
        lookupQuery = query
        lookupPeek = null
        if (query.trim().length < 2) {
            lookupJob?.cancel()
            lookupResults = emptyList()
            refreshContentFields()
            return
        }
        refreshContentFields()
        val worldId = latestWorldId ?: return
        val campaignId = latestCampaignId ?: return
        lookupJob?.cancel()
        lookupJob = appScope.scope.launch {
            lookupResults = searchReferences(query, worldId, campaignId)
            refreshContentFields()
        }
    }

    private fun selectLookupResult(hit: SearchHit) {
        val campaignId = latestCampaignId ?: return
        appScope.scope.launch {
            lookupPeek = loadPeek(hit, campaignId)
            refreshContentFields()
        }
    }

    private fun createProgressClock() {
        clockError = null
        appScope.scope.launch {
            when (val result = createClock(clockLabel, clockSegmentCount)) {
                is CreateSessionClockUseCase.Result.Created -> {
                    clockLabel = ""
                    clockError = null
                    refreshContentFields()
                }
                CreateSessionClockUseCase.Result.InvalidLabel -> {
                    clockError = "Enter a label"
                    refreshContentFields()
                }
                CreateSessionClockUseCase.Result.InvalidSegmentCount -> {
                    clockError = "Choose 2 to 12 segments"
                    refreshContentFields()
                }
                CreateSessionClockUseCase.Result.NoActiveSession -> {
                    clockError = "No active session"
                    refreshContentFields()
                }
            }
        }
    }

    private fun fillClock(clockId: String, filledCount: Int) {
        appScope.scope.launch {
            updateClock(clockId, filledCount)
        }
    }

    private fun removeClock(clockId: String) {
        appScope.scope.launch {
            deleteClock(clockId)
        }
    }

    private fun rollRandomTable(tableId: String) {
        appScope.scope.launch {
            when (val result = rollTable(tableId)) {
                is RollRandomTableUseCase.Result.Rolled -> {
                    lastTableRoll = result.roll.displayText()
                    refreshContentFields()
                }
                RollRandomTableUseCase.Result.Empty,
                RollRandomTableUseCase.Result.NotFound,
                -> Unit
            }
        }
    }

    private fun applyTimerPreset(minutes: Int) {
        pauseTimer()
        timerMinutesText = minutes.toString()
        timerRemainingSeconds = minutes * 60
        timerFinished = false
        refreshContentFields()
    }

    private fun startTimer() {
        if (timerRunning) {
            return
        }
        val minutes = timerMinutesText.toIntOrNull()?.coerceIn(1, MAX_TIMER_MINUTES)
        if (minutes == null) {
            return
        }
        if (timerRemainingSeconds <= 0 || timerFinished) {
            timerRemainingSeconds = minutes * 60
        }
        timerFinished = false
        timerRunning = true
        refreshContentFields()
        timerJob?.cancel()
        timerJob = appScope.scope.launch {
            while (timerRemainingSeconds > 0 && timerRunning) {
                delay(TIMER_TICK_MS)
                if (!timerRunning) {
                    break
                }
                timerRemainingSeconds -= 1
                if (timerRemainingSeconds <= 0) {
                    timerRemainingSeconds = 0
                    timerRunning = false
                    timerFinished = true
                }
                refreshContentFields()
            }
        }
    }

    private fun pauseTimer() {
        timerRunning = false
        timerJob?.cancel()
        timerJob = null
        refreshContentFields()
    }

    private fun resetTimer() {
        pauseTimer()
        val minutes = timerMinutesText.toIntOrNull()?.coerceIn(1, MAX_TIMER_MINUTES) ?: 5
        timerMinutesText = minutes.toString()
        timerRemainingSeconds = minutes * 60
        timerFinished = false
        refreshContentFields()
    }

    private fun closeActiveSession() {
        val sessionId = latestSessionId ?: return
        if (isClosing) {
            return
        }
        finalizeRecording()
        isClosing = true
        closeError = null
        refreshContentFields()
        appScope.scope.launch {
            when (closeSession(sessionId, whyItMatters)) {
                is CloseSessionUseCase.Result.Closed -> {
                    isClosing = false
                    whyItMatters = ""
                    closeError = null
                    val partySize = (_state.value as? RunViewState.Content)?.party?.size ?: 0
                    advancementPrompt = promptFor(latestLevelingMode, partySize)
                    refreshContentFields()
                }
                CloseSessionUseCase.Result.NotFound -> {
                    isClosing = false
                    closeError = "That session is no longer available."
                    refreshContentFields()
                }
            }
        }
    }

    private fun dismissAdvancement() {
        advancementPrompt = null
        refreshContentFields()
    }

    private fun confirmAwardLevel() {
        val campaignId = latestCampaignId ?: return
        appScope.scope.launch {
            awardPartyLevel(campaignId, latestSessionId)
            advancementPrompt = null
            refreshContentFields()
        }
    }

    private fun confirmAwardExperience() {
        val current = advancementPrompt as? AdvancementPrompt.AwardExperience ?: return
        val amount = current.amountText.toIntOrNull()
        if (amount == null || amount <= 0) {
            advancementPrompt = current.copy(amountError = "Enter a positive number")
            refreshContentFields()
            return
        }
        val campaignId = latestCampaignId ?: return
        appScope.scope.launch {
            awardPartyExperience(campaignId, amount, latestSessionId)
            advancementPrompt = null
            refreshContentFields()
        }
    }

    private fun promptFor(mode: LevelingMode, partySize: Int): AdvancementPrompt? {
        if (partySize == 0) {
            return null
        }
        return when (mode) {
            LevelingMode.Milestone -> AdvancementPrompt.AwardLevel
            LevelingMode.Experience -> AdvancementPrompt.AwardExperience(
                amountText = "",
                amountError = null,
            )
        }
    }

    private fun emitEffect(effect: RunViewEffect) {
        _effects.tryEmit(effect)
    }

    private fun probeDevices() {
        refreshCaptureDevices()
        refreshContentFields()
    }

    private fun refreshCaptureDevices() {
        microphones = captureDeviceProbe.microphones()
        hasMicrophone = microphones.isNotEmpty()
        hasCamera = captureDeviceProbe.hasCamera()
        if (selectedMicrophoneId == null || microphones.none { device -> device.id == selectedMicrophoneId }) {
            selectedMicrophoneId = microphones.firstOrNull()?.id
        }
    }

    private fun loadRecordings() {
        val sessionId = latestSessionId ?: return
        recordings = recordingFileStore.list(sessionId).map { recording -> recordingLine(recording) }
        refreshContentFields()
    }

    private fun selectRecordingMode(kind: SessionRecordingKind) {
        if (isRecording) {
            return
        }
        recordingMode = kind
        recordingError = null
        if (kind == SessionRecordingKind.Video) {
            if (recordingCapture.startPreview()) {
                cameraNeedsPermission = false
            } else {
                cameraNeedsPermission = true
                recordingError = "Could not open the camera. Tap Allow camera to grant access."
            }
        } else {
            recordingCapture.stopPreview()
            cameraNeedsPermission = false
        }
        refreshContentFields()
    }

    private fun selectMicrophone(deviceId: String) {
        if (isRecording) {
            return
        }
        if (microphones.none { device -> device.id == deviceId }) {
            return
        }
        selectedMicrophoneId = deviceId
        recordingError = null
        refreshContentFields()
    }

    private fun requestCameraPermission() {
        if (isRecording) {
            return
        }
        recordingError = null
        recordingMode = SessionRecordingKind.Video
        if (recordingCapture.startPreview()) {
            cameraNeedsPermission = false
            hasCamera = true
            refreshContentFields()
            return
        }
        cameraNeedsPermission = true
        val openedSettings = cameraPermissionSettingsOpener.open()
        recordingError = if (openedSettings) {
            "Allow World Weaver under Camera in system privacy settings, then tap Allow camera again."
        } else {
            "Could not open the camera. Allow camera access in system settings, then tap Allow camera again."
        }
        refreshContentFields()
    }

    private fun toggleRecording() {
        if (isRecording) {
            finalizeRecording()
            refreshContentFields()
            return
        }
        val sessionId = latestSessionId ?: return
        val canRecord = when (recordingMode) {
            SessionRecordingKind.Audio -> hasMicrophone
            SessionRecordingKind.Video -> hasCamera
        }
        if (!canRecord) {
            recordingError = when (recordingMode) {
                SessionRecordingKind.Audio -> "No microphone available. Allow microphone access or plug one in."
                SessionRecordingKind.Video -> "No camera available. Allow camera access or plug one in."
            }
            refreshContentFields()
            return
        }
        recordingError = null
        if (!recordingCapture.start(sessionId, recordingMode, selectedMicrophoneId)) {
            recordingError = when (recordingMode) {
                SessionRecordingKind.Audio -> "Could not start the microphone."
                SessionRecordingKind.Video -> "Could not start the camera or microphone."
            }
            if (recordingMode == SessionRecordingKind.Video) {
                cameraNeedsPermission = true
            }
            refreshContentFields()
            return
        }
        if (recordingMode == SessionRecordingKind.Video) {
            cameraNeedsPermission = false
        }
        isRecording = true
        recordingElapsedSeconds = 0
        startRecordingElapsed()
        refreshContentFields()
    }

    private fun startRecordingElapsed() {
        recordingElapsedJob?.cancel()
        recordingElapsedJob = appScope.scope.launch {
            while (isRecording) {
                delay(TIMER_TICK_MS)
                if (!isRecording) {
                    break
                }
                recordingElapsedSeconds += 1
                refreshContentFields()
            }
        }
    }

    private fun finalizeRecording() {
        recordingElapsedJob?.cancel()
        recordingElapsedJob = null
        isRecording = false
        recordingElapsedSeconds = 0
        recordingCapture.stop()
        val sessionId = latestSessionId
        if (sessionId != null) {
            recordings = recordingFileStore.list(sessionId).map { recording -> recordingLine(recording) }
        }
        if (recordingMode == SessionRecordingKind.Video) {
            if (recordingCapture.startPreview()) {
                cameraNeedsPermission = false
            } else {
                cameraNeedsPermission = true
            }
        }
    }

    private fun stopCaptureForInactiveSession() {
        recordingElapsedJob?.cancel()
        recordingElapsedJob = null
        isRecording = false
        recordingElapsedSeconds = 0
        recordings = emptyList()
        recordingCapture.shutdown()
        refreshContentFields()
    }

    private fun removeRecording(recordingId: String) {
        val sessionId = latestSessionId ?: return
        appScope.scope.launch {
            deleteRecording(sessionId, recordingId)
            recordings = recordingFileStore.list(sessionId).map { recording -> recordingLine(recording) }
            refreshContentFields()
        }
    }

    private fun recordingLine(recording: SessionRecording): RunViewState.RecordingLine {
        return RunViewState.RecordingLine(
            id = recording.id,
            kindLabel = when (recording.kind) {
                SessionRecordingKind.Audio -> "Mic"
                SessionRecordingKind.Video -> "Camera"
            },
            startedLabel = STARTED_LABEL.format(recording.startedAt),
            sizeLabel = sizeLabel(recording.byteSize),
            path = recording.path,
        )
    }

    private fun suggestionsFor(text: String): List<WikilinkTarget> {
        val query = wikilinkParser.incompleteQuery(text) ?: return emptyList()
        return latestCatalog.suggest(query)
    }

    private fun refreshWikilinkCatalog(worldId: String) {
        catalogJob?.cancel()
        catalogJob = appScope.scope.launch {
            latestCatalog = loadWikilinkCatalog(worldId)
            refreshContentFields()
        }
    }

    private fun elapsedLabel(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainder = seconds % 60
        return if (hours > 0) {
            "$hours:${minutes.toString().padStart(2, '0')}:${remainder.toString().padStart(2, '0')}"
        } else {
            "${minutes.toString().padStart(2, '0')}:${remainder.toString().padStart(2, '0')}"
        }
    }

    private fun sizeLabel(bytes: Long): String {
        if (bytes < 1024) {
            return "$bytes B"
        }
        if (bytes < 1024 * 1024) {
            return "${bytes / 1024} KB"
        }
        val tenths = (bytes * 10) / (1024 * 1024)
        val whole = tenths / 10
        val fraction = tenths % 10
        return "$whole.$fraction MB"
    }

    private data class PrimaryBundle(
        val context: ActiveContext,
        val details: ActiveContextDetails,
        val sessions: List<Session>,
        val quests: List<Quest>,
        val people: PeopleSnapshot,
    )

    private data class SupportBundle(
        val encounters: List<Encounter>,
        val calendar: WorldCalendar?,
        val observances: List<WorldCalendarObservance>,
        val overlays: List<LocationOverlay>,
        val locations: List<Location>,
    )

    private data class LoadedSnapshot(
        val primary: PrimaryBundle,
        val support: SupportBundle,
        val clocks: List<SessionClock>,
        val tables: List<RandomTable>,
    )

    private companion object {
        const val NOTES_SAVE_DELAY_MS = 400L
        const val TIMER_TICK_MS = 1_000L
        const val MAX_TIMER_MINUTES = 180
        val STARTED_LABEL: DateTimeFormatter =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault())
    }
}
