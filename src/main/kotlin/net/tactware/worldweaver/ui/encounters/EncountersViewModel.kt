package net.tactware.worldweaver.ui.encounters

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
import net.tactware.worldweaver.domain.AdvanceEncounterTurnUseCase
import net.tactware.worldweaver.domain.BattleMapFogEdit
import net.tactware.worldweaver.domain.BattleMap
import net.tactware.worldweaver.domain.BattleMapSituation
import net.tactware.worldweaver.domain.CampaignPerson
import net.tactware.worldweaver.domain.CombatState
import net.tactware.worldweaver.domain.CreateEncounterUseCase
import net.tactware.worldweaver.domain.DeathSaves
import net.tactware.worldweaver.domain.DeleteEncounterUseCase
import net.tactware.worldweaver.domain.Encounter
import net.tactware.worldweaver.domain.EncounterDraft
import net.tactware.worldweaver.domain.EncounterDifficulty
import net.tactware.worldweaver.domain.EncounterParticipant
import net.tactware.worldweaver.domain.EncounterParticipantCombatAction
import net.tactware.worldweaver.domain.EncounterParticipantSource
import net.tactware.worldweaver.domain.EncounterStatus
import net.tactware.worldweaver.domain.EndEncounterUseCase
import net.tactware.worldweaver.domain.FifthEditionCondition
import net.tactware.worldweaver.domain.FifthEditionSheet
import net.tactware.worldweaver.domain.Location
import net.tactware.worldweaver.domain.ObserveActiveContextDetailsUseCase
import net.tactware.worldweaver.domain.ObserveBattleMapSituationsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveBattleMapsForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveEncountersForActiveCampaignUseCase
import net.tactware.worldweaver.domain.ObserveLocationsForActiveWorldUseCase
import net.tactware.worldweaver.domain.ObservePeopleForActiveContextUseCase
import net.tactware.worldweaver.domain.ObservePersonCompanionsUseCase
import net.tactware.worldweaver.domain.PeopleSnapshot
import net.tactware.worldweaver.domain.PersonCompanion
import net.tactware.worldweaver.domain.PersonKind
import net.tactware.worldweaver.domain.PersonRef
import net.tactware.worldweaver.domain.RollAllEncounterInitiativeUseCase
import net.tactware.worldweaver.domain.RollEncounterInitiativeUseCase
import net.tactware.worldweaver.domain.StartEncounterUseCase
import net.tactware.worldweaver.domain.UpdateCampaignPersonDeathSavesUseCase
import net.tactware.worldweaver.domain.UpdateEncounterParticipantCombatUseCase
import net.tactware.worldweaver.domain.UpdateEncounterUseCase
import net.tactware.worldweaver.domain.WorldPerson
import net.tactware.worldweaver.ui.maps.BattleMapBoardSession
import ovh.plrapps.mapcompose.ui.state.MapState

internal class EncountersViewModel(
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val observeEncounters: ObserveEncountersForActiveCampaignUseCase,
    private val observeLocations: ObserveLocationsForActiveWorldUseCase,
    private val observePeople: ObservePeopleForActiveContextUseCase,
    private val observeCompanions: ObservePersonCompanionsUseCase,
    private val observeBattleMaps: ObserveBattleMapsForActiveCampaignUseCase,
    private val observeSituations: ObserveBattleMapSituationsForActiveCampaignUseCase,
    private val createEncounter: CreateEncounterUseCase,
    private val updateEncounter: UpdateEncounterUseCase,
    private val deleteEncounter: DeleteEncounterUseCase,
    private val startEncounter: StartEncounterUseCase,
    private val endEncounter: EndEncounterUseCase,
    private val advanceTurn: AdvanceEncounterTurnUseCase,
    private val updateCombat: UpdateEncounterParticipantCombatUseCase,
    private val rollEncounterInitiative: RollEncounterInitiativeUseCase,
    private val rollAllInitiative: RollAllEncounterInitiativeUseCase,
    private val updateDeathSaves: UpdateCampaignPersonDeathSavesUseCase,
    private val boardSession: BattleMapBoardSession,
) {
    private val _state = MutableStateFlow<EncountersViewState>(EncountersViewState.Loading)
    val state: StateFlow<EncountersViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<EncountersViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<EncountersViewEffect> = _effects.asSharedFlow()

    val mapState: MapState?
        get() = boardSession.mapState
    val playerMapState: MapState?
        get() = boardSession.playerMapState

    private var observeJob: Job? = null
    private var openCreateOnNextLoad = false
    private var statusFilter: EncounterStatus? = null
    private var selectedEncounterId: String? = null
    private var selectedParticipantId: String? = null
    private var combatAmount: String = "1"
    private var runMode = false
    private var resumeRunOnActive = true
    private var latestEncounters: List<Encounter> = emptyList()
    private var latestLocations: List<Location> = emptyList()
    private var latestPeople: PeopleSnapshot = PeopleSnapshot(emptyList(), emptyList())
    private var latestCompanions: List<PersonCompanion> = emptyList()
    private var latestBattleMaps: List<BattleMap> = emptyList()
    private var latestSituations: List<BattleMapSituation> = emptyList()
    private var latestWorldName: String = ""
    private var latestCampaignName: String = ""

    init {
        observe()
    }

    fun onInteraction(interaction: EncountersInteraction) {
        when (interaction) {
            EncountersInteraction.ScreenStarted -> Unit
            EncountersInteraction.RetrySelected -> observe()
            EncountersInteraction.CreateWorldSelected -> _effects.tryEmit(EncountersViewEffect.OpenWorlds)
            EncountersInteraction.CreateCampaignSelected -> {
                _effects.tryEmit(EncountersViewEffect.OpenCampaigns)
            }
            EncountersInteraction.NewEncounterSelected -> openCreateSetup()
            is EncountersInteraction.EncounterSelected,
            is EncountersInteraction.EncounterOpened,
            -> selectEncounter(encounterIdFrom(interaction))
            is EncountersInteraction.DeleteEncounterSelected -> requestDelete(interaction.encounterId)
            EncountersInteraction.DeleteConfirmed -> confirmDelete()
            EncountersInteraction.DeleteCancelled -> updatePendingDelete(null)
            is EncountersInteraction.StatusFilterSelected -> {
                statusFilter = interaction.status
                refreshLibrary()
            }
            is EncountersInteraction.LinkedLocationSelected -> {
                _effects.tryEmit(EncountersViewEffect.OpenLocations)
            }
            is EncountersInteraction.OpenMapSelected -> {
                _effects.tryEmit(EncountersViewEffect.OpenMap(interaction.battleMapId))
            }
            is EncountersInteraction.EditorBattleMapSelected -> updateSetup { setup ->
                setup?.copy(battleMapId = interaction.battleMapId)
            }
            is EncountersInteraction.StartEncounterSelected -> start(interaction.encounterId)
            is EncountersInteraction.EndEncounterSelected -> requestEnd(interaction.encounterId)
            is EncountersInteraction.EndOutcomeChanged -> updatePendingEnd { pending ->
                pending?.copy(outcomeNote = interaction.outcomeNote)
            }
            EncountersInteraction.EndConfirmed -> confirmEnd()
            EncountersInteraction.EndCancelled -> updatePendingEnd { null }
            EncountersInteraction.LibrarySelected -> returnToLibrary()
            is EncountersInteraction.TurnAdvanced -> {
                appScope.scope.launch {
                    advanceTurn(interaction.encounterId, interaction.direction)
                }
            }
            is EncountersInteraction.ParticipantSelected -> {
                selectedParticipantId = interaction.participantId
                boardSession.selectParticipant(interaction.participantId)
                refreshRunning()
            }
            is EncountersInteraction.SheetSelected -> {
                _effects.tryEmit(
                    EncountersViewEffect.OpenSheet(
                        source = interaction.source,
                        sourceId = interaction.sourceId,
                    )
                )
            }
            is EncountersInteraction.CombatAmountChanged -> {
                combatAmount = interaction.amount
                refreshRunning()
            }
            is EncountersInteraction.DamageApplied -> applyCombat(
                interaction.encounterId,
                interaction.participantId,
                EncounterParticipantCombatAction.Damage(parsedAmount()),
            )
            is EncountersInteraction.HealApplied -> applyCombat(
                interaction.encounterId,
                interaction.participantId,
                EncounterParticipantCombatAction.Heal(parsedAmount()),
            )
            is EncountersInteraction.TemporaryHitPointsSet -> applyCombat(
                interaction.encounterId,
                interaction.participantId,
                EncounterParticipantCombatAction.SetTemporaryHitPoints(parsedAmount()),
            )
            is EncountersInteraction.ConditionToggled -> toggleCondition(
                interaction.encounterId,
                interaction.participantId,
                interaction.condition,
            )
            is EncountersInteraction.ConditionRemoved -> removeCondition(
                interaction.encounterId,
                interaction.participantId,
                interaction.condition,
            )
            is EncountersInteraction.CombatStateSelected -> applyCombat(
                interaction.encounterId,
                interaction.participantId,
                EncounterParticipantCombatAction.SetCombatState(interaction.state),
            )
            is EncountersInteraction.PlayerVisibilityToggled -> applyCombat(
                interaction.encounterId,
                interaction.participantId,
                EncounterParticipantCombatAction.SetVisibleToPlayers(interaction.visibleToPlayers),
            )
            is EncountersInteraction.AttacksUsedSelected -> applyCombat(
                interaction.encounterId,
                interaction.participantId,
                EncounterParticipantCombatAction.SetAttacksUsed(interaction.count),
            )
            is EncountersInteraction.AttacksAllowedSelected -> applyCombat(
                interaction.encounterId,
                interaction.participantId,
                EncounterParticipantCombatAction.SetAttacksAllowed(interaction.count),
            )
            is EncountersInteraction.BonusActionSet -> applyCombat(
                interaction.encounterId,
                interaction.participantId,
                EncounterParticipantCombatAction.SetBonusActionUsed(interaction.used),
            )
            is EncountersInteraction.ReactionSet -> applyCombat(
                interaction.encounterId,
                interaction.participantId,
                EncounterParticipantCombatAction.SetReactionUsed(interaction.used),
            )
            is EncountersInteraction.DeathSaveSuccessSelected -> applyDeathSaves(
                interaction.participantId,
            ) { current -> current.withSuccesses(interaction.successes) }
            is EncountersInteraction.DeathSaveFailureSelected -> applyDeathSaves(
                interaction.participantId,
            ) { current -> current.withFailures(interaction.failures) }
            is EncountersInteraction.InitiativeRollEntered -> applyCombat(
                interaction.encounterId,
                interaction.participantId,
                EncounterParticipantCombatAction.SetInitiative(
                    roll = interaction.roll.toIntOrNull(),
                    bonus = participantBonus(interaction.encounterId, interaction.participantId),
                ),
            )
            is EncountersInteraction.InitiativeBonusEntered -> applyCombat(
                interaction.encounterId,
                interaction.participantId,
                EncounterParticipantCombatAction.SetInitiative(
                    roll = participantRoll(interaction.encounterId, interaction.participantId),
                    bonus = interaction.bonus.toIntOrNull() ?: 0,
                ),
            )
            is EncountersInteraction.InitiativeRolled -> applyCombat(
                interaction.encounterId,
                interaction.participantId,
                EncounterParticipantCombatAction.RollInitiative,
            )
            is EncountersInteraction.RollAllInitiativeSelected -> rollAll(
                interaction.encounterId,
                interaction.overwriteExisting,
            )
            is EncountersInteraction.EditorNameChanged -> updateSetup { setup ->
                setup?.copy(name = interaction.name, nameError = null)
            }
            is EncountersInteraction.EditorNotesChanged -> updateSetup { setup ->
                setup?.copy(notes = interaction.notes)
            }
            is EncountersInteraction.EditorDifficultySelected -> updateSetup { setup ->
                setup?.copy(difficulty = interaction.difficulty)
            }
            is EncountersInteraction.EditorLocationSelected -> updateSetup { setup ->
                setup?.copy(locationId = interaction.locationId)
            }
            is EncountersInteraction.EditorNamelessNameChanged -> updateSetup { setup ->
                setup?.copy(namelessName = interaction.name)
            }
            is EncountersInteraction.EditorNamelessGroupCountChanged -> updateSetup { setup ->
                setup?.copy(namelessGroupCount = interaction.groupCount)
            }
            EncountersInteraction.EditorNamelessSwarmToggled -> updateSetup { setup ->
                setup?.copy(namelessAsSwarm = !setup.namelessAsSwarm)
            }
            is EncountersInteraction.EditorRosterSearchChanged -> updateSetup { setup ->
                setup?.copy(rosterSearch = interaction.query)
            }
            EncountersInteraction.EditorNamelessAdded -> addNameless()
            EncountersInteraction.EditorPartyAdded -> addParty()
            is EncountersInteraction.EditorWorldPersonAdded -> addLinkedPerson(
                interaction.personId,
                EncounterParticipantSource.WorldPerson,
            )
            is EncountersInteraction.EditorCampaignPersonAdded -> addLinkedPerson(
                interaction.personId,
                EncounterParticipantSource.CampaignPerson,
            )
            is EncountersInteraction.EditorOwnerCompanionsAdded -> addOwnerCompanions(
                interaction.ownerPersonId,
                interaction.ownerSource,
            )
            is EncountersInteraction.EditorParticipantRemoved -> updateSetup { setup ->
                setup?.copy(
                    participants = setup.participants.filterIndexed { index, _ ->
                        index != interaction.index
                    },
                )
            }
            is EncountersInteraction.EditorParticipantInitiativeChanged -> updateSetup { setup ->
                setup?.copy(
                    participants = setup.participants.mapIndexed { index, participant ->
                        if (index == interaction.index) {
                            participant.copy(initiativeRoll = interaction.roll.toIntOrNull())
                        } else {
                            participant
                        }
                    },
                )
            }
            EncountersInteraction.EditorSaved -> saveSetup()
            EncountersInteraction.EditorDismissed -> updateSetup { null }
            is EncountersInteraction.MapCellSelected -> {
                boardSession.selectCell(interaction.x, interaction.y)
                refreshRunning()
            }
            is EncountersInteraction.TokenSelected -> {
                selectedParticipantId = interaction.participantId
                boardSession.selectToken(interaction.participantId)
                refreshRunning()
            }
            is EncountersInteraction.MovementSpeedChanged -> {
                boardSession.changeMovementSpeed(interaction.speed)
                refreshRunning()
            }
            EncountersInteraction.MovementCleared -> {
                boardSession.clearMovement()
                refreshRunning()
            }
            EncountersInteraction.MeasureToggled -> {
                boardSession.toggleMeasure()
                refreshRunning()
            }
            EncountersInteraction.MeasureCleared -> {
                boardSession.clearMeasure()
                refreshRunning()
            }
            EncountersInteraction.FogToggled -> {
                boardSession.toggleFogPaint()
                refreshRunning()
            }
            EncountersInteraction.FogRevealBrushSelected -> {
                boardSession.setFogRevealBrush(true)
                refreshRunning()
            }
            EncountersInteraction.FogHideBrushSelected -> {
                boardSession.setFogRevealBrush(false)
                refreshRunning()
            }
            EncountersInteraction.FogRevealAllSelected -> {
                boardSession.applyFogEdit(BattleMapFogEdit.RevealAll)
                refreshRunning()
            }
            EncountersInteraction.FogHideAllSelected -> {
                boardSession.applyFogEdit(BattleMapFogEdit.HideAll)
                refreshRunning()
            }
            is EncountersInteraction.TerrainPaintSelected -> {
                boardSession.setTerrainPaint(interaction.kind)
                refreshRunning()
            }
            EncountersInteraction.ItemDropToggled -> {
                boardSession.toggleItemDrop()
                refreshRunning()
            }
            is EncountersInteraction.ItemNameChanged -> {
                boardSession.changeItemName(interaction.name)
                refreshRunning()
            }
            is EncountersInteraction.ItemSelected -> {
                boardSession.selectItem(interaction.itemId)
                refreshRunning()
            }
            EncountersInteraction.ItemRemoved -> {
                boardSession.removeSelectedItem()
                refreshRunning()
            }
            EncountersInteraction.PlayerViewSelected -> {
                boardSession.openPlayerView(currentTurnWalkSpeed())
                refreshRunning()
            }
            EncountersInteraction.PlayerViewClosed -> {
                boardSession.closePlayerView()
                refreshRunning()
            }
        }
    }

    private fun encounterIdFrom(interaction: EncountersInteraction): String {
        return when (interaction) {
            is EncountersInteraction.EncounterSelected -> interaction.encounterId
            is EncountersInteraction.EncounterOpened -> interaction.encounterId
            else -> ""
        }
    }

    private fun observe() {
        observeJob?.cancel()
        _state.value = EncountersViewState.Loading
        observeJob = appScope.scope.launch {
            combine(
                observeActiveContextDetails(),
                observeEncounters(),
                observeLocations(),
                observePeople(),
                combine(
                    observeCompanions(),
                    combine(observeBattleMaps(), observeSituations()) { maps, situations ->
                        maps to situations
                    },
                ) { companions, mapsAndSituations ->
                    Triple(companions, mapsAndSituations.first, mapsAndSituations.second)
                },
            ) { details, encounters, locations, people, companionMaps ->
                LoadedSnapshot(
                    details,
                    encounters,
                    locations,
                    people,
                    companionMaps.first,
                    companionMaps.second,
                    companionMaps.third,
                )
            }
                .catch { error ->
                    _state.value = EncountersViewState.Error(
                        message = error.message ?: "Could not load encounters",
                        canRetry = true,
                    )
                }
                .collect { snapshot ->
                    applyLoaded(snapshot)
                }
        }
    }

    private fun applyLoaded(snapshot: LoadedSnapshot) {
        val world = snapshot.details.world
        if (world == null) {
            clearLatest()
            boardSession.shutdown()
            _state.value = EncountersViewState.NoActiveWorld
            return
        }
        val campaign = snapshot.details.campaign
        if (campaign == null) {
            clearLatest()
            boardSession.shutdown()
            latestWorldName = world.name
            _state.value = EncountersViewState.NoActiveCampaign
            return
        }
        latestEncounters = snapshot.encounters
        latestLocations = snapshot.locations
        latestPeople = snapshot.people
        latestCompanions = snapshot.companions
        latestBattleMaps = snapshot.battleMaps
        latestSituations = snapshot.situations
        latestWorldName = world.name
        latestCampaignName = campaign.name
        val current = _state.value
        val setup = if (openCreateOnNextLoad) {
            openCreateOnNextLoad = false
            createSetup()
        } else {
            setupFrom(current)?.let { refreshSetupOptions(it) }
        }
        if (snapshot.encounters.isEmpty()) {
            selectedEncounterId = null
            runMode = false
            boardSession.shutdown()
            _state.value = EncountersViewState.Empty(
                worldName = world.name,
                campaignName = campaign.name,
                setup = setup,
            )
            return
        }
        val selected = selectedFrom(snapshot.encounters) ?: snapshot.encounters.first()
        selectedEncounterId = selected.id
        if (shouldShowRunning(selected)) {
            showRunning(selected, pendingEndFrom(current))
        } else {
            boardSession.shutdown()
            _state.value = contentState(
                selected = selected,
                setup = setup ?: setupFromEncounter(selected),
                pendingDelete = pendingDeleteFrom(current),
            )
        }
    }

    private fun shouldShowRunning(encounter: Encounter): Boolean {
        if (encounter.status != EncounterStatus.Active) {
            runMode = false
            return false
        }
        if (runMode || resumeRunOnActive || _state.value is EncountersViewState.Running) {
            runMode = true
            return true
        }
        return false
    }

    private fun refreshLibrary() {
        val current = _state.value
        if (current !is EncountersViewState.Content) {
            return
        }
        val selected = selectedFrom(latestEncounters) ?: latestEncounters.firstOrNull()
        _state.value = contentState(
            selected = selected,
            setup = current.setup,
            pendingDelete = current.pendingDelete,
        )
    }

    private fun refreshRunning() {
        val encounter = selectedFrom(latestEncounters) ?: return
        if (encounter.status != EncounterStatus.Active || !runMode) {
            return
        }
        showRunning(encounter, pendingEndFrom(_state.value))
    }

    private fun contentState(
        selected: Encounter?,
        setup: EncountersViewState.EncounterSetupState?,
        pendingDelete: EncountersViewState.PendingDelete?,
    ): EncountersViewState.Content {
        val visible = latestEncounters
            .filter { encounter -> statusFilter == null || encounter.status == statusFilter }
            .sortedWith(
                compareBy<Encounter> { it.status != EncounterStatus.Active }
                    .thenBy { it.name.lowercase() }
            )
        val missing = selected?.participants?.count { it.initiativeTotal() == null } ?: 0
        return EncountersViewState.Content(
            worldName = latestWorldName,
            campaignName = latestCampaignName,
            encounters = visible,
            selectedEncounter = selected,
            statusFilter = statusFilter,
            locationName = selected?.locationId?.let { locationId ->
                latestLocations.firstOrNull { it.id == locationId }?.name
            },
            battleMapName = selected?.battleMapId?.let { battleMapId ->
                latestBattleMaps.firstOrNull { it.id == battleMapId }?.name
            },
            setup = setup?.let(::refreshSetupOptions),
            pendingDelete = pendingDelete,
            startWarning = if (selected?.status != EncounterStatus.Active && missing > 0) {
                "$missing combatant${if (missing == 1) "" else "s"} have no initiative"
            } else {
                null
            },
        )
    }

    private fun showRunning(
        encounter: Encounter,
        pendingEnd: EncountersViewState.PendingEnd?,
    ) {
        val order = encounter.initiativeOrder()
        val turnId = order.getOrNull(encounter.currentTurnIndex)?.id
        if (selectedParticipantId == null ||
            order.none { it.id == selectedParticipantId }
        ) {
            selectedParticipantId = turnId ?: order.firstOrNull()?.id
        }
        val battleMap = encounter.battleMapId?.let { mapId ->
            latestBattleMaps.firstOrNull { it.id == mapId }
        }
        val situations = battleMap?.id?.let { mapId ->
            latestSituations.filter { it.battleMapId == mapId }.sortedBy { it.sortIndex }
        }.orEmpty()
        val board = boardSession.sync(
            battleMap = battleMap,
            situations = situations,
            encounter = encounter,
            people = latestPeople,
        )
        if (board.selectedTokenParticipantId != null) {
            selectedParticipantId = board.selectedTokenParticipantId
        }
        val selectedParticipant = order.firstOrNull { it.id == selectedParticipantId }
        _state.value = EncountersViewState.Running(
            worldName = latestWorldName,
            campaignName = latestCampaignName,
            encounter = encounter,
            locationName = encounter.locationId?.let { locationId ->
                latestLocations.firstOrNull { it.id == locationId }?.name
            },
            battleMapName = battleMap?.name,
            battleMap = battleMap,
            initiativeOrder = order,
            currentTurnParticipantId = turnId,
            selectedParticipantId = selectedParticipantId,
            combatAmount = combatAmount,
            availableConditions = FifthEditionCondition.entries,
            deathSaves = deathSavesFor(selectedParticipant),
            tokens = board.tokens,
            selectedTokenName = board.selectedTokenName,
            unplacedTokenCount = board.unplacedTokenCount,
            movementSpeedText = board.movementSpeedText,
            movementOrigin = board.movementOrigin,
            reachableCells = board.reachableCells,
            measureEnabled = board.measureEnabled,
            measureOrigin = board.measureOrigin,
            measureDestination = board.measureDestination,
            measureDistance = board.measureDistance,
            fogPaintEnabled = board.fogPaintEnabled,
            fogRevealBrush = board.fogRevealBrush,
            terrainPaint = board.terrainPaint,
            itemDropEnabled = board.itemDropEnabled,
            itemNameText = board.itemNameText,
            selectedItemId = board.selectedItemId,
            selectedItemName = board.selectedItemName,
            playerViewOpen = board.playerViewOpen,
            pendingEnd = pendingEnd,
        )
    }

    private fun selectEncounter(encounterId: String) {
        if (encounterId.isEmpty()) {
            return
        }
        selectedEncounterId = encounterId
        val encounter = selectedFrom(latestEncounters) ?: return
        if (encounter.status == EncounterStatus.Active) {
            runMode = true
            resumeRunOnActive = true
            showRunning(encounter, pendingEndFrom(_state.value))
            return
        }
        runMode = false
        boardSession.shutdown()
        when (val current = _state.value) {
            is EncountersViewState.Content -> {
                _state.value = contentState(
                    selected = encounter,
                    setup = setupFromEncounter(encounter),
                    pendingDelete = current.pendingDelete,
                )
            }
            is EncountersViewState.Running -> {
                _state.value = contentState(
                    selected = encounter,
                    setup = setupFromEncounter(encounter),
                    pendingDelete = null,
                )
            }
            else -> Unit
        }
    }

    private fun returnToLibrary() {
        runMode = false
        resumeRunOnActive = false
        boardSession.closePlayerView()
        val encounter = selectedFrom(latestEncounters)
        _state.value = contentState(
            selected = encounter,
            setup = encounter?.let(::setupFromEncounter),
            pendingDelete = null,
        )
    }

    private fun start(encounterId: String) {
        runMode = true
        resumeRunOnActive = true
        selectedEncounterId = encounterId
        appScope.scope.launch { startEncounter(encounterId) }
    }

    private fun openCreateSetup() {
        when (val current = _state.value) {
            is EncountersViewState.Empty -> {
                _state.value = current.copy(setup = createSetup())
            }
            is EncountersViewState.Content -> {
                _state.value = current.copy(setup = createSetup())
            }
            EncountersViewState.Loading, is EncountersViewState.Error -> {
                openCreateOnNextLoad = true
            }
            EncountersViewState.NoActiveWorld,
            EncountersViewState.NoActiveCampaign,
            is EncountersViewState.Running,
            -> Unit
        }
    }

    private fun requestDelete(encounterId: String) {
        val encounter = latestEncounters.firstOrNull { it.id == encounterId } ?: return
        updatePendingDelete(
            EncountersViewState.PendingDelete(
                encounterId = encounter.id,
                encounterName = encounter.name,
            )
        )
    }

    private fun confirmDelete() {
        val pending = pendingDeleteFrom(_state.value) ?: return
        appScope.scope.launch {
            deleteEncounter(pending.encounterId)
            if (selectedEncounterId == pending.encounterId) {
                selectedEncounterId = null
            }
            updatePendingDelete(null)
        }
    }

    private fun requestEnd(encounterId: String) {
        val encounter = latestEncounters.firstOrNull { it.id == encounterId } ?: return
        updatePendingEnd {
            EncountersViewState.PendingEnd(
                encounterId = encounter.id,
                encounterName = encounter.name,
                outcomeNote = encounter.outcomeNote,
            )
        }
    }

    private fun confirmEnd() {
        val pending = pendingEndFrom(_state.value) ?: return
        runMode = false
        resumeRunOnActive = false
        appScope.scope.launch {
            endEncounter(pending.encounterId, pending.outcomeNote)
            updatePendingEnd { null }
        }
    }

    private fun applyCombat(
        encounterId: String,
        participantId: String,
        action: EncounterParticipantCombatAction,
    ) {
        appScope.scope.launch {
            updateCombat(encounterId, participantId, action)
        }
    }

    private fun toggleCondition(
        encounterId: String,
        participantId: String,
        condition: FifthEditionCondition,
    ) {
        val encounter = latestEncounters.firstOrNull { it.id == encounterId } ?: return
        val participant = encounter.participants.firstOrNull { it.id == participantId } ?: return
        val label = condition.displayName
        val next = if (participant.conditions.any { it.equals(label, ignoreCase = true) }) {
            participant.conditions.filterNot { it.equals(label, ignoreCase = true) }
        } else {
            participant.conditions + label
        }
        applyCombat(
            encounterId,
            participantId,
            EncounterParticipantCombatAction.SetConditions(next),
        )
    }

    private fun removeCondition(
        encounterId: String,
        participantId: String,
        condition: String,
    ) {
        val encounter = latestEncounters.firstOrNull { it.id == encounterId } ?: return
        val participant = encounter.participants.firstOrNull { it.id == participantId } ?: return
        applyCombat(
            encounterId,
            participantId,
            EncounterParticipantCombatAction.SetConditions(
                participant.conditions.filterNot { it == condition },
            ),
        )
    }

    private fun applyDeathSaves(
        participantId: String,
        transform: (DeathSaves) -> DeathSaves,
    ) {
        val encounter = selectedFrom(latestEncounters) ?: return
        val participant = encounter.participants.firstOrNull { it.id == participantId } ?: return
        val personId = participant.sourceId ?: return
        if (participant.source != EncounterParticipantSource.CampaignPerson) {
            return
        }
        val current = deathSavesFor(participant) ?: DeathSaves.none()
        val next = transform(current)
        appScope.scope.launch {
            updateDeathSaves(personId, next)
            if (next.isDead() && participant.combatState != CombatState.Dead) {
                updateCombat(
                    encounter.id,
                    participantId,
                    EncounterParticipantCombatAction.SetCombatState(CombatState.Dead),
                )
            }
        }
    }

    private fun rollAll(encounterId: String?, overwriteExisting: Boolean) {
        if (encounterId != null && setupFrom(_state.value)?.encounterId == null) {
            appScope.scope.launch {
                rollAllInitiative(encounterId, overwriteExisting)
            }
            return
        }
        val setup = setupFrom(_state.value)
        if (setup != null && setup.encounterId == null) {
            updateSetup { current ->
                current?.copy(
                    participants = rollEncounterInitiative(
                        current.participants,
                        overwriteExisting,
                    ),
                )
            }
            return
        }
        if (encounterId != null) {
            appScope.scope.launch {
                rollAllInitiative(encounterId, overwriteExisting)
            }
        }
    }

    private fun participantBonus(encounterId: String, participantId: String): Int {
        return latestEncounters
            .firstOrNull { it.id == encounterId }
            ?.participants
            ?.firstOrNull { it.id == participantId }
            ?.initiativeBonus
            ?: 0
    }

    private fun participantRoll(encounterId: String, participantId: String): Int? {
        return latestEncounters
            .firstOrNull { it.id == encounterId }
            ?.participants
            ?.firstOrNull { it.id == participantId }
            ?.initiativeRoll
    }

    private fun parsedAmount(): Int {
        return combatAmount.toIntOrNull()?.coerceAtLeast(0) ?: 1
    }

    private fun saveSetup() {
        val setup = setupFrom(_state.value) ?: return
        if (setup.name.trim().isEmpty()) {
            updateSetup { current -> current?.copy(nameError = "Name is required") }
            return
        }
        val draft = EncounterDraft(
            name = setup.name,
            locationId = setup.locationId,
            battleMapId = setup.battleMapId,
            difficulty = setup.difficulty,
            notes = setup.notes,
            outcomeNote = latestEncounters
                .firstOrNull { it.id == setup.encounterId }
                ?.outcomeNote
                .orEmpty(),
            participants = setup.participants,
        )
        appScope.scope.launch {
            val result = if (setup.encounterId == null) {
                createEncounter(draft)
            } else {
                updateEncounter(setup.encounterId, draft)
            }
            when (result) {
                is CreateEncounterUseCase.Result.Created -> {
                    selectedEncounterId = result.encounter.id
                    updateSetup { null }
                }
                CreateEncounterUseCase.Result.InvalidName,
                UpdateEncounterUseCase.Result.InvalidName,
                -> updateSetup { current -> current?.copy(nameError = "Name is required") }
                CreateEncounterUseCase.Result.NoActiveCampaign,
                CreateEncounterUseCase.Result.InvalidLocation,
                CreateEncounterUseCase.Result.InvalidBattleMap,
                UpdateEncounterUseCase.Result.InvalidLocation,
                UpdateEncounterUseCase.Result.InvalidBattleMap,
                UpdateEncounterUseCase.Result.Updated,
                UpdateEncounterUseCase.Result.NotFound,
                -> {
                    val savedId = setup.encounterId ?: selectedEncounterId
                    val saved = savedId?.let { id -> latestEncounters.firstOrNull { it.id == id } }
                    updateSetup { saved?.let(::setupFromEncounter) }
                }
            }
        }
    }

    private fun addNameless() {
        updateSetup { setup ->
            if (setup == null) {
                return@updateSetup null
            }
            val name = setup.namelessName.trim()
            if (name.isEmpty()) {
                return@updateSetup setup
            }
            val groupCount = setup.namelessGroupCount.toIntOrNull()?.coerceAtLeast(1) ?: 1
            val added = if (setup.namelessAsSwarm || groupCount == 1) {
                listOf(namelessParticipant(name, if (setup.namelessAsSwarm) groupCount else 1))
            } else {
                (1..groupCount).map { index ->
                    namelessParticipant("$name $index", groupCount = 1)
                }
            }
            setup.copy(
                participants = setup.participants + added,
                namelessName = "",
                namelessGroupCount = "1",
            )
        }
    }

    private fun namelessParticipant(name: String, groupCount: Int): EncounterParticipant {
        return EncounterParticipant(
            id = "",
            name = name,
            source = EncounterParticipantSource.Nameless,
            sourceId = null,
            initiativeRoll = null,
            initiativeBonus = 0,
            armorClass = 13,
            hitPoints = 7,
            maxHitPoints = 7,
            temporaryHitPoints = 0,
            conditions = emptyList(),
            groupCount = groupCount,
            combatState = CombatState.Conscious,
        )
    }

    private fun addParty() {
        latestPeople.campaignPeople
            .filter { person -> person.kind == PersonKind.PlayerCharacter }
            .forEach { person ->
                addLinkedPerson(person.id, EncounterParticipantSource.CampaignPerson)
            }
    }

    private fun addLinkedPerson(
        personId: String,
        source: EncounterParticipantSource,
    ) {
        updateSetup { setup ->
            if (setup == null) {
                return@updateSetup null
            }
            val alreadyAdded = setup.participants.any { participant ->
                participant.source == source && participant.sourceId == personId
            }
            if (alreadyAdded) {
                return@updateSetup setup
            }
            val option = when (source) {
                EncounterParticipantSource.WorldPerson -> {
                    setup.worldPersonOptions.firstOrNull { it.id == personId }
                }
                EncounterParticipantSource.CampaignPerson -> {
                    setup.campaignPersonOptions.firstOrNull { it.id == personId }
                }
                EncounterParticipantSource.Nameless -> null
            } ?: return@updateSetup setup
            setup.copy(
                participants = setup.participants + EncounterParticipant(
                    id = "",
                    name = option.name,
                    source = source,
                    sourceId = option.id,
                    initiativeRoll = null,
                    initiativeBonus = option.initiativeBonus,
                    armorClass = option.armorClass,
                    hitPoints = option.hitPoints,
                    maxHitPoints = option.maxHitPoints,
                    temporaryHitPoints = option.temporaryHitPoints,
                    conditions = emptyList(),
                    groupCount = 1,
                    combatState = CombatState.Conscious,
                ),
            )
        }
    }

    private fun createSetup(): EncountersViewState.EncounterSetupState {
        return EncountersViewState.EncounterSetupState(
            encounterId = null,
            name = "",
            locationId = null,
            locationOptions = latestLocations,
            battleMapId = null,
            battleMapOptions = latestBattleMaps,
            difficulty = EncounterDifficulty.Medium,
            notes = "",
            participants = emptyList(),
            namelessName = "",
            namelessGroupCount = "1",
            namelessAsSwarm = false,
            rosterSearch = "",
            worldPersonOptions = worldPersonOptions(emptyList()),
            campaignPersonOptions = campaignPersonOptions(emptyList()),
            companionSuggestions = companionSuggestions(emptyList()),
            nameError = null,
            missingInitiativeCount = 0,
        )
    }

    private fun setupFromEncounter(
        encounter: Encounter,
    ): EncountersViewState.EncounterSetupState {
        return EncountersViewState.EncounterSetupState(
            encounterId = encounter.id,
            name = encounter.name,
            locationId = encounter.locationId,
            locationOptions = latestLocations,
            battleMapId = encounter.battleMapId,
            battleMapOptions = latestBattleMaps,
            difficulty = encounter.difficulty,
            notes = encounter.notes,
            participants = encounter.participants,
            namelessName = "",
            namelessGroupCount = "1",
            namelessAsSwarm = false,
            rosterSearch = "",
            worldPersonOptions = worldPersonOptions(encounter.participants),
            campaignPersonOptions = campaignPersonOptions(encounter.participants),
            companionSuggestions = companionSuggestions(encounter.participants),
            nameError = null,
            missingInitiativeCount = encounter.participants.count { it.initiativeTotal() == null },
        )
    }

    private fun refreshSetupOptions(
        setup: EncountersViewState.EncounterSetupState,
    ): EncountersViewState.EncounterSetupState {
        return setup.copy(
            locationOptions = latestLocations,
            battleMapOptions = latestBattleMaps,
            worldPersonOptions = worldPersonOptions(setup.participants),
            campaignPersonOptions = campaignPersonOptions(setup.participants),
            companionSuggestions = companionSuggestions(setup.participants),
            missingInitiativeCount = setup.participants.count { it.initiativeTotal() == null },
        )
    }

    private fun worldPersonOptions(
        participants: List<EncounterParticipant>,
    ): List<EncountersViewState.PersonOption> {
        return latestPeople.worldPeople
            .map { person -> personOption(person, participants) }
            .sortedBy { it.name.lowercase() }
    }

    private fun campaignPersonOptions(
        participants: List<EncounterParticipant>,
    ): List<EncountersViewState.PersonOption> {
        return latestPeople.campaignPeople
            .map { person -> personOption(person, participants) }
            .sortedBy { it.name.lowercase() }
    }

    private fun personOption(
        person: WorldPerson,
        participants: List<EncounterParticipant>,
    ): EncountersViewState.PersonOption {
        val sheet = person.sheet
        return EncountersViewState.PersonOption(
            id = person.id,
            name = person.name,
            source = EncounterParticipantSource.WorldPerson,
            alreadyAdded = participants.any { participant ->
                participant.source == EncounterParticipantSource.WorldPerson &&
                    participant.sourceId == person.id
            },
            initiativeBonus = sheet.abilityScores.modifierFor(sheet.abilityScores.dexterity),
            armorClass = sheet.armorClass,
            hitPoints = sheet.hitPoints,
            maxHitPoints = sheet.maxHitPoints,
            temporaryHitPoints = sheet.temporaryHitPoints,
        )
    }

    private fun addOwnerCompanions(
        ownerPersonId: String,
        ownerSource: EncounterParticipantSource,
    ) {
        val ownerRef = when (ownerSource) {
            EncounterParticipantSource.CampaignPerson -> PersonRef.Campaign(ownerPersonId)
            EncounterParticipantSource.WorldPerson -> PersonRef.World(ownerPersonId)
            EncounterParticipantSource.Nameless -> return
        }
        val links = latestCompanions.filter { link -> sameRef(link.owner, ownerRef) }
        links.forEach { link ->
            when (val companion = link.companion) {
                is PersonRef.Campaign -> addLinkedPerson(
                    companion.id,
                    EncounterParticipantSource.CampaignPerson,
                )
                is PersonRef.World -> addLinkedPerson(
                    companion.id,
                    EncounterParticipantSource.WorldPerson,
                )
            }
        }
    }

    private fun companionSuggestions(
        participants: List<EncounterParticipant>,
    ): List<EncountersViewState.CompanionSuggestion> {
        return participants.mapNotNull { participant ->
            val ownerRef = when (participant.source) {
                EncounterParticipantSource.CampaignPerson -> {
                    participant.sourceId?.let { PersonRef.Campaign(it) }
                }
                EncounterParticipantSource.WorldPerson -> {
                    participant.sourceId?.let { PersonRef.World(it) }
                }
                EncounterParticipantSource.Nameless -> null
            } ?: return@mapNotNull null
            val companions = latestCompanions
                .filter { link -> sameRef(link.owner, ownerRef) }
                .mapNotNull { link -> companionOption(link.companion, participants) }
                .filter { option -> !option.alreadyAdded }
            if (companions.isEmpty()) {
                null
            } else {
                EncountersViewState.CompanionSuggestion(
                    ownerPersonId = ownerRef.id,
                    ownerSource = participant.source,
                    ownerName = participant.name,
                    companions = companions,
                )
            }
        }
    }

    private fun companionOption(
        ref: PersonRef,
        participants: List<EncounterParticipant>,
    ): EncountersViewState.PersonOption? {
        return when (ref) {
            is PersonRef.World -> {
                latestPeople.worldPeople.firstOrNull { it.id == ref.id }?.let { person ->
                    personOption(person, participants)
                }
            }
            is PersonRef.Campaign -> {
                latestPeople.campaignPeople.firstOrNull { it.id == ref.id }?.let { person ->
                    personOption(person, participants)
                }
            }
        }
    }

    private fun sameRef(left: PersonRef, right: PersonRef): Boolean {
        return left.id == right.id && left::class == right::class
    }

    private fun personOption(
        person: CampaignPerson,
        participants: List<EncounterParticipant>,
    ): EncountersViewState.PersonOption {
        val sheet = person.sheet
        return EncountersViewState.PersonOption(
            id = person.id,
            name = person.name,
            source = EncounterParticipantSource.CampaignPerson,
            alreadyAdded = participants.any { participant ->
                participant.source == EncounterParticipantSource.CampaignPerson &&
                    participant.sourceId == person.id
            },
            initiativeBonus = sheet.abilityScores.modifierFor(sheet.abilityScores.dexterity),
            armorClass = sheet.armorClass,
            hitPoints = person.overlayHitPoints ?: sheet.hitPoints,
            maxHitPoints = sheet.maxHitPoints,
            temporaryHitPoints = sheet.temporaryHitPoints,
        )
    }

    private fun updateSetup(
        transform: (EncountersViewState.EncounterSetupState?) -> EncountersViewState.EncounterSetupState?,
    ) {
        when (val current = _state.value) {
            is EncountersViewState.Empty -> {
                _state.value = current.copy(setup = transform(current.setup)?.let(::refreshSetupOptions))
            }
            is EncountersViewState.Content -> {
                _state.value = current.copy(setup = transform(current.setup)?.let(::refreshSetupOptions))
            }
            else -> Unit
        }
    }

    private fun updatePendingDelete(pendingDelete: EncountersViewState.PendingDelete?) {
        val current = _state.value
        if (current is EncountersViewState.Content) {
            _state.value = current.copy(pendingDelete = pendingDelete)
        }
    }

    private fun updatePendingEnd(
        transform: (EncountersViewState.PendingEnd?) -> EncountersViewState.PendingEnd?,
    ) {
        when (val current = _state.value) {
            is EncountersViewState.Running -> {
                _state.value = current.copy(pendingEnd = transform(current.pendingEnd))
            }
            is EncountersViewState.Content -> Unit
            else -> Unit
        }
    }

    private fun selectedFrom(encounters: List<Encounter>): Encounter? {
        return selectedEncounterId?.let { id -> encounters.firstOrNull { it.id == id } }
    }

    private fun currentTurnWalkSpeed(): Int? {
        val encounter = selectedFrom(latestEncounters) ?: return null
        if (encounter.status != EncounterStatus.Active) {
            return null
        }
        val participant = encounter.initiativeOrder().getOrNull(encounter.currentTurnIndex)
            ?: return null
        val sourceId = participant.sourceId ?: return null
        return when (participant.source) {
            EncounterParticipantSource.WorldPerson -> {
                latestPeople.worldPeople.firstOrNull { it.id == sourceId }?.sheet?.movementSpeed()
            }
            EncounterParticipantSource.CampaignPerson -> {
                latestPeople.campaignPeople.firstOrNull { it.id == sourceId }?.sheet?.movementSpeed()
            }
            EncounterParticipantSource.Nameless -> null
        }
    }

    private fun deathSavesFor(participant: EncounterParticipant?): DeathSaves? {
        if (participant == null || participant.source != EncounterParticipantSource.CampaignPerson) {
            return null
        }
        val sourceId = participant.sourceId ?: return null
        val sheet = latestPeople.campaignPeople.firstOrNull { it.id == sourceId }?.sheet
        return (sheet as? FifthEditionSheet)?.deathSaves
    }

    private fun setupFrom(state: EncountersViewState): EncountersViewState.EncounterSetupState? {
        return when (state) {
            is EncountersViewState.Empty -> state.setup
            is EncountersViewState.Content -> state.setup
            else -> null
        }
    }

    private fun pendingDeleteFrom(state: EncountersViewState): EncountersViewState.PendingDelete? {
        return (state as? EncountersViewState.Content)?.pendingDelete
    }

    private fun pendingEndFrom(state: EncountersViewState): EncountersViewState.PendingEnd? {
        return (state as? EncountersViewState.Running)?.pendingEnd
    }

    private fun clearLatest() {
        latestEncounters = emptyList()
        latestLocations = emptyList()
        latestPeople = PeopleSnapshot(emptyList(), emptyList())
        latestCompanions = emptyList()
        latestBattleMaps = emptyList()
        latestSituations = emptyList()
        selectedEncounterId = null
        selectedParticipantId = null
        runMode = false
    }

    private data class LoadedSnapshot(
        val details: ActiveContextDetails,
        val encounters: List<Encounter>,
        val locations: List<Location>,
        val people: PeopleSnapshot,
        val companions: List<PersonCompanion>,
        val battleMaps: List<BattleMap>,
        val situations: List<BattleMapSituation>,
    )
}
