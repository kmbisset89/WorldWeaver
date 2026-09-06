package io.github.kmbisset89.worldweaver.ui

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.SearchKind
import io.github.kmbisset89.worldweaver.domain.SetActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.SetActiveWorldUseCase
import io.github.kmbisset89.worldweaver.domain.ShellSettings
import io.github.kmbisset89.worldweaver.domain.ShellSettingsStore
import io.github.kmbisset89.worldweaver.ui.calendar.CalendarInteraction
import io.github.kmbisset89.worldweaver.ui.calendar.CalendarViewEffect
import io.github.kmbisset89.worldweaver.ui.calendar.CalendarViewModel
import io.github.kmbisset89.worldweaver.ui.campaigns.CampaignsInteraction
import io.github.kmbisset89.worldweaver.ui.campaigns.CampaignsViewEffect
import io.github.kmbisset89.worldweaver.ui.campaigns.CampaignsViewModel
import io.github.kmbisset89.worldweaver.ui.characters.CharactersInteraction
import io.github.kmbisset89.worldweaver.ui.characters.CharactersViewEffect
import io.github.kmbisset89.worldweaver.ui.characters.CharactersViewModel
import io.github.kmbisset89.worldweaver.ui.characters.CharactersViewState
import io.github.kmbisset89.worldweaver.ui.characters.PersonMembership
import io.github.kmbisset89.worldweaver.ui.sheet.CharacterSheetInteraction
import io.github.kmbisset89.worldweaver.ui.sheet.CharacterSheetViewEffect
import io.github.kmbisset89.worldweaver.ui.sheet.CharacterSheetViewModel
import io.github.kmbisset89.worldweaver.ui.sheet.CharacterSheetViewState
import io.github.kmbisset89.worldweaver.ui.dice.DiceViewModel
import io.github.kmbisset89.worldweaver.ui.atmosphere.AtmosphereInteraction
import io.github.kmbisset89.worldweaver.ui.atmosphere.AtmosphereViewModel
import io.github.kmbisset89.worldweaver.ui.factions.FactionsInteraction
import io.github.kmbisset89.worldweaver.ui.factions.FactionsViewEffect
import io.github.kmbisset89.worldweaver.ui.factions.FactionsViewModel
import io.github.kmbisset89.worldweaver.ui.links.LinksViewEffect
import io.github.kmbisset89.worldweaver.ui.links.LinksViewModel
import io.github.kmbisset89.worldweaver.domain.EncounterParticipantSource
import io.github.kmbisset89.worldweaver.ui.encounters.EncountersInteraction
import io.github.kmbisset89.worldweaver.ui.encounters.EncountersViewEffect
import io.github.kmbisset89.worldweaver.ui.encounters.EncountersViewModel
import io.github.kmbisset89.worldweaver.ui.home.HomeViewEffect
import io.github.kmbisset89.worldweaver.ui.home.HomeViewModel
import io.github.kmbisset89.worldweaver.ui.oneshot.OneShotWizardInteraction
import io.github.kmbisset89.worldweaver.ui.oneshot.OneShotWizardViewEffect
import io.github.kmbisset89.worldweaver.ui.oneshot.OneShotWizardViewModel
import io.github.kmbisset89.worldweaver.ui.locations.LocationsInteraction
import io.github.kmbisset89.worldweaver.ui.locations.LocationsViewEffect
import io.github.kmbisset89.worldweaver.ui.locations.LocationsViewModel
import io.github.kmbisset89.worldweaver.ui.lore.LoreInteraction
import io.github.kmbisset89.worldweaver.ui.lore.LoreViewEffect
import io.github.kmbisset89.worldweaver.ui.lore.LoreViewModel
import io.github.kmbisset89.worldweaver.ui.maps.MapsInteraction
import io.github.kmbisset89.worldweaver.ui.maps.MapsViewEffect
import io.github.kmbisset89.worldweaver.ui.maps.MapsViewModel
import io.github.kmbisset89.worldweaver.ui.worldmap.WorldMapInteraction
import io.github.kmbisset89.worldweaver.ui.worldmap.WorldMapViewEffect
import io.github.kmbisset89.worldweaver.ui.worldmap.WorldMapViewModel
import io.github.kmbisset89.worldweaver.ui.navigation.NavigationState
import io.github.kmbisset89.worldweaver.ui.navigation.Screen
import io.github.kmbisset89.worldweaver.ui.run.RunViewEffect
import io.github.kmbisset89.worldweaver.ui.run.RunViewModel
import io.github.kmbisset89.worldweaver.ui.dice.DiceInteraction
import io.github.kmbisset89.worldweaver.ui.search.SearchViewEffect
import io.github.kmbisset89.worldweaver.ui.search.SearchViewModel
import io.github.kmbisset89.worldweaver.ui.quests.QuestsInteraction
import io.github.kmbisset89.worldweaver.ui.quests.QuestsViewEffect
import io.github.kmbisset89.worldweaver.ui.quests.QuestsViewModel
import io.github.kmbisset89.worldweaver.ui.sessions.SessionsInteraction
import io.github.kmbisset89.worldweaver.ui.sessions.SessionsViewEffect
import io.github.kmbisset89.worldweaver.ui.sessions.SessionsViewModel
import io.github.kmbisset89.worldweaver.ui.settings.SettingsViewEffect
import io.github.kmbisset89.worldweaver.ui.settings.SettingsViewModel
import io.github.kmbisset89.worldweaver.ui.session.LocalUser
import io.github.kmbisset89.worldweaver.ui.worlds.WorldsInteraction
import io.github.kmbisset89.worldweaver.ui.worlds.WorldsViewEffect
import io.github.kmbisset89.worldweaver.ui.worlds.WorldsViewModel

internal class AppViewModel(
    val homeViewModel: HomeViewModel,
    val worldsViewModel: WorldsViewModel,
    val oneShotWizardViewModel: OneShotWizardViewModel,
    val campaignsViewModel: CampaignsViewModel,
    val locationsViewModel: LocationsViewModel,
    val loreViewModel: LoreViewModel,
    val calendarViewModel: CalendarViewModel,
    val factionsViewModel: FactionsViewModel,
    val linksViewModel: LinksViewModel,
    val charactersViewModel: CharactersViewModel,
    val characterSheetViewModel: CharacterSheetViewModel,
    val questsViewModel: QuestsViewModel,
    val sessionsViewModel: SessionsViewModel,
    val encountersViewModel: EncountersViewModel,
    val mapsViewModel: MapsViewModel,
    val worldMapViewModel: WorldMapViewModel,
    val runViewModel: RunViewModel,
    val diceViewModel: DiceViewModel,
    val atmosphereViewModel: AtmosphereViewModel,
    val searchViewModel: SearchViewModel,
    val settingsViewModel: SettingsViewModel,
    private val shellSettingsStore: ShellSettingsStore,
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val setActiveWorld: SetActiveWorldUseCase,
    private val setActiveCampaign: SetActiveCampaignUseCase,
) {
    private val navigation = NavigationState()
    private val _state = MutableStateFlow<AppViewState>(initialContent())
    val state: StateFlow<AppViewState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AppViewEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<AppViewEffect> = _effects.asSharedFlow()

    init {
        appScope.scope.launch {
            shellSettingsStore.settings.collect { settings ->
                applyShellSettings(settings)
            }
        }
        appScope.scope.launch {
            homeViewModel.effects.collect { effect ->
                when (effect) {
                    HomeViewEffect.OpenWorldCreator -> openWorldCreator()
                    HomeViewEffect.OpenOneShotWizard -> openOneShotWizard()
                    HomeViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                    HomeViewEffect.OpenCampaigns -> navigateToRoot(Screen.CAMPAIGNS)
                    HomeViewEffect.OpenCharacters -> navigateToRoot(Screen.CHARACTERS)
                    HomeViewEffect.OpenRun -> navigateTo(Screen.RUN)
                }
            }
        }
        appScope.scope.launch {
            worldsViewModel.effects.collect { effect ->
                when (effect) {
                    WorldsViewEffect.OpenOneShotWizard -> openOneShotWizard()
                    is WorldsViewEffect.Exported -> emitUiEvent(
                        UiEvent.Success("Exported “${effect.worldName}”")
                    )
                    is WorldsViewEffect.Imported -> emitUiEvent(
                        UiEvent.Success("Imported “${effect.worldName}”")
                    )
                    is WorldsViewEffect.Failed -> emitUiEvent(UiEvent.Error(effect.message))
                }
            }
        }
        appScope.scope.launch {
            oneShotWizardViewModel.effects.collect { effect ->
                when (effect) {
                    OneShotWizardViewEffect.Completed -> navigateToRoot(Screen.HOME)
                    OneShotWizardViewEffect.Dismissed -> goBack()
                }
            }
        }
        appScope.scope.launch {
            campaignsViewModel.effects.collect { effect ->
                when (effect) {
                    CampaignsViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                    CampaignsViewEffect.OpenCharacters -> navigateToRoot(Screen.CHARACTERS)
                    CampaignsViewEffect.CreatePlayerCharacter -> {
                        navigateToRoot(Screen.CHARACTERS)
                        charactersViewModel.onInteraction(
                            CharactersInteraction.NewPlayerCharacterSelected,
                        )
                    }
                    CampaignsViewEffect.OpenQuests -> navigateToRoot(Screen.QUESTS)
                    CampaignsViewEffect.OpenSessions -> navigateToRoot(Screen.SESSIONS)
                }
            }
        }
        appScope.scope.launch {
            locationsViewModel.effects.collect { effect ->
                when (effect) {
                    LocationsViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                    is LocationsViewEffect.OpenLore -> {
                        navigateToRoot(Screen.LORE)
                        loreViewModel.onInteraction(LoreInteraction.LoreOpened(effect.loreId))
                    }
                    is LocationsViewEffect.OpenQuest -> {
                        navigateToRoot(Screen.QUESTS)
                        questsViewModel.onInteraction(QuestsInteraction.QuestOpened(effect.questId))
                    }
                    is LocationsViewEffect.OpenWorldMap -> {
                        navigateToRoot(Screen.WORLD_MAP)
                        worldMapViewModel.onInteraction(WorldMapInteraction.MapOpened(effect.locationId))
                    }
                }
            }
        }
        appScope.scope.launch {
            loreViewModel.effects.collect { effect ->
                when (effect) {
                    LoreViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                    is LoreViewEffect.OpenCalendar -> {
                        navigateToRoot(Screen.CALENDAR)
                        calendarViewModel.onInteraction(
                            CalendarInteraction.ObservanceOpened(effect.observanceId)
                        )
                    }
                }
            }
        }
        appScope.scope.launch {
            calendarViewModel.effects.collect { effect ->
                when (effect) {
                    CalendarViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                    is CalendarViewEffect.OpenLore -> {
                        navigateToRoot(Screen.LORE)
                        loreViewModel.onInteraction(LoreInteraction.LoreOpened(effect.loreId))
                    }
                }
            }
        }
        appScope.scope.launch {
            factionsViewModel.effects.collect { effect ->
                when (effect) {
                    FactionsViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                }
            }
        }
        appScope.scope.launch {
            linksViewModel.effects.collect { effect ->
                when (effect) {
                    LinksViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                    is LinksViewEffect.OpenPerson -> {
                        navigateToRoot(Screen.CHARACTERS)
                        charactersViewModel.onInteraction(
                            CharactersInteraction.PersonOpened(effect.key),
                        )
                    }
                    is LinksViewEffect.OpenFaction -> {
                        navigateToRoot(Screen.FACTIONS)
                        factionsViewModel.onInteraction(
                            FactionsInteraction.FactionOpened(effect.factionId),
                        )
                    }
                }
            }
        }
        appScope.scope.launch {
            charactersViewModel.effects.collect { effect ->
                when (effect) {
                    CharactersViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                    is CharactersViewEffect.OpenLore -> {
                        navigateToRoot(Screen.LORE)
                        loreViewModel.onInteraction(LoreInteraction.LoreOpened(effect.loreId))
                    }
                    is CharactersViewEffect.OpenQuest -> {
                        navigateToRoot(Screen.QUESTS)
                        questsViewModel.onInteraction(QuestsInteraction.QuestOpened(effect.questId))
                    }
                    is CharactersViewEffect.OpenSheet -> {
                        characterSheetViewModel.onInteraction(
                            CharacterSheetInteraction.SheetOpened(sheetKeyFrom(effect.key)),
                        )
                    }
                }
            }
        }
        appScope.scope.launch {
            characterSheetViewModel.effects.collect { effect ->
                when (effect) {
                    is CharacterSheetViewEffect.OpenEditor -> {
                        navigateToRoot(Screen.CHARACTERS)
                        charactersViewModel.onInteraction(
                            CharactersInteraction.EditPersonSelected(characterKeyFrom(effect.key)),
                        )
                    }
                }
            }
        }
        appScope.scope.launch {
            questsViewModel.effects.collect { effect ->
                when (effect) {
                    QuestsViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                    QuestsViewEffect.OpenCampaigns -> navigateToRoot(Screen.CAMPAIGNS)
                    QuestsViewEffect.OpenLocations -> navigateToRoot(Screen.LOCATIONS)
                    is QuestsViewEffect.OpenLore -> {
                        navigateToRoot(Screen.LORE)
                        loreViewModel.onInteraction(LoreInteraction.LoreOpened(effect.loreId))
                    }
                    QuestsViewEffect.OpenCharacters -> navigateToRoot(Screen.CHARACTERS)
                    is QuestsViewEffect.OpenSession -> {
                        navigateToRoot(Screen.SESSIONS)
                        sessionsViewModel.onInteraction(SessionsInteraction.SessionOpened(effect.sessionId))
                    }
                }
            }
        }
        appScope.scope.launch {
            sessionsViewModel.effects.collect { effect ->
                when (effect) {
                    SessionsViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                    SessionsViewEffect.OpenCampaigns -> navigateToRoot(Screen.CAMPAIGNS)
                    is SessionsViewEffect.OpenQuest -> {
                        navigateToRoot(Screen.QUESTS)
                        questsViewModel.onInteraction(QuestsInteraction.QuestOpened(effect.questId))
                    }
                }
            }
        }
        appScope.scope.launch {
            encountersViewModel.effects.collect { effect ->
                when (effect) {
                    EncountersViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                    EncountersViewEffect.OpenCampaigns -> navigateToRoot(Screen.CAMPAIGNS)
                    EncountersViewEffect.OpenLocations -> navigateToRoot(Screen.LOCATIONS)
                    is EncountersViewEffect.OpenMap -> {
                        navigateToRoot(Screen.MAPS)
                        mapsViewModel.onInteraction(MapsInteraction.MapOpened(effect.battleMapId))
                    }
                    is EncountersViewEffect.OpenSheet -> openEncounterSheet(effect)
                }
            }
        }
        appScope.scope.launch {
                    mapsViewModel.effects.collect { effect ->
                when (effect) {
                    MapsViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                    MapsViewEffect.OpenCampaigns -> navigateToRoot(Screen.CAMPAIGNS)
                    is MapsViewEffect.UniversalVttExported -> emitUiEvent(
                        UiEvent.Success("Exported “${effect.mapName}” as Universal VTT")
                    )
                    is MapsViewEffect.Failed -> emitUiEvent(UiEvent.Error(effect.message))
                }
            }
        }
        appScope.scope.launch {
            worldMapViewModel.effects.collect { effect ->
                when (effect) {
                    WorldMapViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                    is WorldMapViewEffect.OpenLocations -> {
                        navigateToRoot(Screen.LOCATIONS)
                        effect.locationId?.let { locationId ->
                            locationsViewModel.onInteraction(LocationsInteraction.LocationOpened(locationId))
                        }
                    }
                }
            }
        }
        appScope.scope.launch {
            runViewModel.effects.collect { effect ->
                when (effect) {
                    RunViewEffect.OpenWorlds -> navigateToRoot(Screen.WORLDS)
                    RunViewEffect.OpenCampaigns -> navigateToRoot(Screen.CAMPAIGNS)
                    RunViewEffect.OpenSessions -> navigateToRoot(Screen.SESSIONS)
                    RunViewEffect.OpenEncounters -> navigateToRoot(Screen.ENCOUNTERS)
                    RunViewEffect.OpenMaps -> navigateToRoot(Screen.MAPS)
                    RunViewEffect.OpenPlayerView -> {
                        navigateToRoot(Screen.ENCOUNTERS)
                        encountersViewModel.onInteraction(EncountersInteraction.PlayerViewSelected)
                    }
                    RunViewEffect.OpenDiceTray -> {
                        diceViewModel.onInteraction(DiceInteraction.FloatingOpened)
                    }
                    RunViewEffect.OpenAtmosphereTray -> {
                        atmosphereViewModel.onInteraction(AtmosphereInteraction.FloatingOpened)
                    }
                    is RunViewEffect.OpenPersonSheet -> {
                        characterSheetViewModel.onInteraction(
                            CharacterSheetInteraction.SheetOpened(
                                CharacterSheetViewState.PersonKey(
                                    membership = effect.membership,
                                    id = effect.personId,
                                )
                            )
                        )
                    }
                    is RunViewEffect.OpenSearchHit -> openSearchHit(
                        SearchViewEffect.RecordOpened(effect.hit)
                    )
                    is RunViewEffect.OpenRecording -> openRecording(effect.path)
                }
            }
        }
        appScope.scope.launch {
            searchViewModel.effects.collect { effect ->
                when (effect) {
                    is SearchViewEffect.RecordOpened -> openSearchHit(effect)
                }
            }
        }
        appScope.scope.launch {
            settingsViewModel.effects.collect { effect ->
                when (effect) {
                    SettingsViewEffect.Exported -> emitUiEvent(
                        UiEvent.Success("Exported WorldWeaver backup")
                    )
                    SettingsViewEffect.RestoreReadyToQuit -> {
                        updateContent { it.copy(exitRequested = true) }
                        _effects.tryEmit(AppViewEffect.ExitRequested)
                    }
                    SettingsViewEffect.SrdImported -> emitUiEvent(
                        UiEvent.Success("Imported 5E SRD catalog")
                    )
                    SettingsViewEffect.SrdCleared -> emitUiEvent(
                        UiEvent.Success("Cleared imported SRD")
                    )
                    is SettingsViewEffect.Failed -> emitUiEvent(UiEvent.Error(effect.message))
                }
            }
        }
        appScope.scope.launch {
            observeActiveContextDetails().collect { details ->
                updateContent { content ->
                    content.copy(
                        activeWorldName = details.world?.name,
                        activeCampaignName = details.campaign?.name,
                    )
                }
            }
        }
    }

    fun onInteraction(interaction: AppInteraction) {
        when (interaction) {
            is AppInteraction.ScreenSelected -> navigateToRoot(interaction.screen)
            AppInteraction.ThemeModeCycled -> cycleThemeMode()
            AppInteraction.NavDensityToggled -> toggleNavDensity()
            AppInteraction.SignOutSelected -> emitUiEvent(
                UiEvent.Info("Sign out is not configured")
            )
            AppInteraction.SnackbarConsumed -> updateContent { it.copy(snackbar = null) }
        }
    }

    private fun openWorldCreator() {
        navigateToRoot(Screen.WORLDS)
        worldsViewModel.onInteraction(WorldsInteraction.NewWorldSelected)
    }

    private fun openOneShotWizard() {
        oneShotWizardViewModel.onInteraction(OneShotWizardInteraction.ScreenStarted)
        navigateTo(Screen.ONE_SHOT_WIZARD)
    }

    private fun openSearchHit(effect: SearchViewEffect.RecordOpened) {
        val hit = effect.hit
        appScope.scope.launch {
            when (hit.kind) {
                SearchKind.World -> {
                    setActiveWorld(hit.id)
                    navigateToRoot(Screen.WORLDS)
                    worldsViewModel.onInteraction(WorldsInteraction.WorldSelected(hit.id))
                }
                SearchKind.Campaign -> {
                    setActiveCampaign(hit.id)
                    navigateToRoot(Screen.CAMPAIGNS)
                    campaignsViewModel.onInteraction(CampaignsInteraction.CampaignOpened(hit.id))
                }
                SearchKind.Location -> {
                    hit.worldId?.let { setActiveWorld(it) }
                    navigateToRoot(Screen.LOCATIONS)
                    locationsViewModel.onInteraction(LocationsInteraction.LocationOpened(hit.id))
                }
                SearchKind.Lore -> {
                    hit.worldId?.let { setActiveWorld(it) }
                    navigateToRoot(Screen.LORE)
                    loreViewModel.onInteraction(LoreInteraction.LoreOpened(hit.id))
                }
                SearchKind.Observance -> {
                    hit.worldId?.let { setActiveWorld(it) }
                    navigateToRoot(Screen.CALENDAR)
                    calendarViewModel.onInteraction(CalendarInteraction.ObservanceOpened(hit.id))
                }
                SearchKind.CelestialBody -> {
                    hit.worldId?.let { setActiveWorld(it) }
                    navigateToRoot(Screen.CALENDAR)
                    calendarViewModel.onInteraction(CalendarInteraction.CelestialBodyOpened(hit.id))
                }
                SearchKind.Faction -> {
                    hit.worldId?.let { setActiveWorld(it) }
                    navigateToRoot(Screen.FACTIONS)
                    factionsViewModel.onInteraction(FactionsInteraction.FactionOpened(hit.id))
                }
                SearchKind.WorldPerson -> {
                    hit.worldId?.let { setActiveWorld(it) }
                    navigateToRoot(Screen.CHARACTERS)
                    charactersViewModel.onInteraction(
                        CharactersInteraction.PersonOpened(
                            CharactersViewState.PersonKey(
                                membership = PersonMembership.WorldLibrary,
                                id = hit.id,
                            )
                        )
                    )
                }
                SearchKind.CampaignPerson -> {
                    hit.campaignId?.let { setActiveCampaign(it) }
                    navigateToRoot(Screen.CHARACTERS)
                    charactersViewModel.onInteraction(
                        CharactersInteraction.PersonOpened(
                            CharactersViewState.PersonKey(
                                membership = PersonMembership.ThisCampaign,
                                id = hit.id,
                            )
                        )
                    )
                }
                SearchKind.Quest -> {
                    hit.campaignId?.let { setActiveCampaign(it) }
                    navigateToRoot(Screen.QUESTS)
                    questsViewModel.onInteraction(QuestsInteraction.QuestOpened(hit.id))
                }
                SearchKind.Session -> {
                    hit.campaignId?.let { setActiveCampaign(it) }
                    navigateToRoot(Screen.SESSIONS)
                    sessionsViewModel.onInteraction(SessionsInteraction.SessionOpened(hit.id))
                }
            }
        }
    }

    private fun openEncounterSheet(effect: EncountersViewEffect.OpenSheet) {
        val sourceId = effect.sourceId
        if (sourceId == null || effect.source == EncounterParticipantSource.Nameless) {
            characterSheetViewModel.onInteraction(CharacterSheetInteraction.UnavailableOpened)
            return
        }
        val membership = when (effect.source) {
            EncounterParticipantSource.WorldPerson -> PersonMembership.WorldLibrary
            EncounterParticipantSource.CampaignPerson -> PersonMembership.ThisCampaign
            EncounterParticipantSource.Nameless -> return
        }
        characterSheetViewModel.onInteraction(
            CharacterSheetInteraction.SheetOpened(
                CharacterSheetViewState.PersonKey(
                    membership = membership,
                    id = sourceId,
                )
            )
        )
    }

    private fun sheetKeyFrom(
        key: CharactersViewState.PersonKey,
    ): CharacterSheetViewState.PersonKey {
        return CharacterSheetViewState.PersonKey(
            membership = key.membership,
            id = key.id,
        )
    }

    private fun characterKeyFrom(
        key: CharacterSheetViewState.PersonKey,
    ): CharactersViewState.PersonKey {
        return CharactersViewState.PersonKey(
            membership = key.membership,
            id = key.id,
        )
    }

    private fun applyShellSettings(settings: ShellSettings) {
        updateContent { content ->
            content.copy(
                themeMode = settings.themeMode,
                themeSkin = settings.themeSkin,
                navExpanded = settings.navExpanded,
                localUser = localUserFrom(settings),
            )
        }
    }

    private fun localUserFrom(settings: ShellSettings): LocalUser {
        return LocalUser(
            displayName = settings.displayName,
            email = settings.email,
        )
    }

    private fun cycleThemeMode() {
        val current = content()
        shellSettingsStore.setThemeMode(current.themeMode.next())
    }

    private fun toggleNavDensity() {
        val current = content()
        shellSettingsStore.setNavExpanded(!current.navExpanded)
    }

    private fun emitUiEvent(event: UiEvent) {
        updateContent { it.copy(snackbar = event) }
    }

    private fun openRecording(path: String) {
        val file = java.io.File(path)
        if (!file.isFile) {
            emitUiEvent(UiEvent.Error("That recording is no longer available."))
            return
        }
        val opened = runCatching {
            java.awt.Desktop.getDesktop().open(file)
            true
        }.getOrDefault(false)
        if (!opened) {
            emitUiEvent(UiEvent.Error("Could not open the recording."))
        }
    }

    private fun navigateToRoot(screen: Screen) {
        navigation.navigateToRoot(screen)
        publishNavigation()
    }

    private fun navigateTo(screen: Screen) {
        navigation.navigateTo(screen)
        publishNavigation()
    }

    private fun goBack() {
        navigation.goBack()
        publishNavigation()
    }

    private fun publishNavigation() {
        updateContent { it.copy(currentScreen = navigation.currentScreen) }
    }

    private fun content(): AppViewState.Content {
        return _state.value as AppViewState.Content
    }

    private fun updateContent(
        transform: (AppViewState.Content) -> AppViewState.Content,
    ) {
        _state.update { current ->
            when (current) {
                is AppViewState.Content -> transform(current)
            }
        }
    }

    private fun initialContent(): AppViewState.Content {
        val settings = shellSettingsStore.settings.value
        return AppViewState.Content(
            currentScreen = Screen.HOME,
            themeMode = settings.themeMode,
            themeSkin = settings.themeSkin,
            navExpanded = settings.navExpanded,
            localUser = localUserFrom(settings),
            activeWorldName = null,
            activeCampaignName = null,
            snackbar = null,
            exitRequested = false,
        )
    }
}
