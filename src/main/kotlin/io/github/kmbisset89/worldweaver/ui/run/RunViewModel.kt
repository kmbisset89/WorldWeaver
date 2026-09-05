package io.github.kmbisset89.worldweaver.ui.run

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
import io.github.kmbisset89.worldweaver.domain.ActiveContext
import io.github.kmbisset89.worldweaver.domain.ActiveContextDetails
import io.github.kmbisset89.worldweaver.domain.CampaignPerson
import io.github.kmbisset89.worldweaver.domain.AwardPartyExperienceUseCase
import io.github.kmbisset89.worldweaver.domain.AwardPartyLevelUseCase
import io.github.kmbisset89.worldweaver.domain.CloseSessionUseCase
import io.github.kmbisset89.worldweaver.domain.Encounter
import io.github.kmbisset89.worldweaver.domain.EncounterStatus
import io.github.kmbisset89.worldweaver.domain.FifthEditionSheet
import io.github.kmbisset89.worldweaver.domain.LevelingMode
import io.github.kmbisset89.worldweaver.domain.Location
import io.github.kmbisset89.worldweaver.domain.LocationOverlay
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveEncountersForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLocationOverlaysForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveLocationsForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObservePeopleForActiveContextUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveQuestsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveSessionsForActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldCalendarForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ObserveWorldCalendarObservancesForActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.WorldCalendarObservance
import io.github.kmbisset89.worldweaver.domain.PeopleSnapshot
import io.github.kmbisset89.worldweaver.domain.PersonKind
import io.github.kmbisset89.worldweaver.domain.Quest
import io.github.kmbisset89.worldweaver.domain.QuestObjectiveStatus
import io.github.kmbisset89.worldweaver.domain.QuestStatus
import io.github.kmbisset89.worldweaver.domain.Session
import io.github.kmbisset89.worldweaver.domain.WorldCalendar
import io.github.kmbisset89.worldweaver.domain.WorldDateFormatter
import io.github.kmbisset89.worldweaver.ui.advancement.AdvancementPrompt
import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership

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
    private val closeSession: CloseSessionUseCase,
    private val awardPartyLevel: AwardPartyLevelUseCase,
    private val awardPartyExperience: AwardPartyExperienceUseCase,
    private val dateFormatter: WorldDateFormatter = WorldDateFormatter(),
) {
    private val _state = MutableStateFlow<RunViewState>(RunViewState.Loading)
    val state: StateFlow<RunViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<RunViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<RunViewEffect> = _effects.asSharedFlow()

    private var observeJob: Job? = null
    private var whyItMatters: String = ""
    private var isClosing: Boolean = false
    private var closeError: String? = null
    private var latestSessionId: String? = null
    private var latestCampaignId: String? = null
    private var latestLevelingMode = LevelingMode.Milestone
    private var advancementPrompt: AdvancementPrompt? = null

    init {
        observe()
    }

    fun onInteraction(interaction: RunInteraction) {
        when (interaction) {
            RunInteraction.ScreenStarted -> Unit
            RunInteraction.RetrySelected -> observe()
            RunInteraction.CreateWorldSelected -> emitEffect(RunViewEffect.OpenWorlds)
            RunInteraction.CreateCampaignSelected -> emitEffect(RunViewEffect.OpenCampaigns)
            RunInteraction.OpenSessionsSelected -> emitEffect(RunViewEffect.OpenSessions)
            RunInteraction.OpenEncountersSelected -> emitEffect(RunViewEffect.OpenEncounters)
            RunInteraction.OpenMapsSelected -> emitEffect(RunViewEffect.OpenMaps)
            RunInteraction.PlayerViewSelected -> emitEffect(RunViewEffect.OpenPlayerView)
            RunInteraction.DiceTraySelected -> emitEffect(RunViewEffect.OpenDiceTray)
            is RunInteraction.PersonPeeked -> emitEffect(
                RunViewEffect.OpenPersonSheet(
                    membership = interaction.membership,
                    personId = interaction.personId,
                )
            )
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
            ) { primary, support ->
                LoadedSnapshot(primary, support)
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
            advancementPrompt = null
            _state.value = RunViewState.NoActiveWorld
            return
        }
        val campaign = snapshot.primary.details.campaign
        if (campaign == null) {
            latestSessionId = null
            latestCampaignId = null
            advancementPrompt = null
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
            _state.value = RunViewState.NoActiveSession(
                worldName = world.name,
                campaignName = campaign.name,
            )
            return
        }
        if (latestSessionId != session.id) {
            whyItMatters = ""
            closeError = null
            advancementPrompt = null
        }
        latestSessionId = session.id
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
            sessionNotes = session.notes,
            recap = session.recap,
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
                whyItMatters = whyItMatters,
                isClosing = isClosing,
                closeError = closeError,
                advancementPrompt = advancementPrompt,
            )
        }
    }

    private fun closeActiveSession() {
        val sessionId = latestSessionId ?: return
        if (isClosing) {
            return
        }
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
    )
}
