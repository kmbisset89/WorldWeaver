package io.github.kmbisset89.worldweaver.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import io.github.kmbisset89.worldweaver.core.AppCoroutineScope
import io.github.kmbisset89.worldweaver.domain.ObserveActiveContextDetailsUseCase
import io.github.kmbisset89.worldweaver.domain.SearchKind
import io.github.kmbisset89.worldweaver.domain.SetActiveCampaignUseCase
import io.github.kmbisset89.worldweaver.domain.SetActiveWorldUseCase
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
import io.github.kmbisset89.worldweaver.ui.settings.ShellSettings
import io.github.kmbisset89.worldweaver.ui.settings.ShellSettingsStore
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
    val runViewModel: RunViewModel,
    val diceViewModel: DiceViewModel,
    val searchViewModel: SearchViewModel,
    val settingsViewModel: SettingsViewModel,
    private val shellSettingsStore: ShellSettingsStore,
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
    private val setActiveWorld: SetActiveWorldUseCase,
    private val setActiveCampaign: SetActiveCampaignUseCase,
) {
    val navigation = NavigationState()

    var themeMode by mutableStateOf(shellSettingsStore.settings.value.themeMode)
        private set

    var themeSkin by mutableStateOf(shellSettingsStore.settings.value.themeSkin)
        private set

    var navExpanded by mutableStateOf(shellSettingsStore.settings.value.navExpanded)
        private set

    var localUser by mutableStateOf(shellSettingsStore.settings.value.toLocalUser())
        private set

    var uiEvent by mutableStateOf<UiEvent?>(null)
        private set

    var exitRequested by mutableStateOf(false)
        private set

    var activeWorldName by mutableStateOf<String?>(null)
        private set

    var activeCampaignName by mutableStateOf<String?>(null)
        private set

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
                    HomeViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                    HomeViewEffect.OpenCampaigns -> navigation.navigateToRoot(Screen.CAMPAIGNS)
                    HomeViewEffect.OpenCharacters -> navigation.navigateToRoot(Screen.CHARACTERS)
                    HomeViewEffect.OpenRun -> navigation.navigateTo(Screen.RUN)
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
                    OneShotWizardViewEffect.Completed -> navigation.navigateToRoot(Screen.HOME)
                    OneShotWizardViewEffect.Dismissed -> navigation.goBack()
                }
            }
        }
        appScope.scope.launch {
            campaignsViewModel.effects.collect { effect ->
                when (effect) {
                    CampaignsViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                    CampaignsViewEffect.OpenCharacters -> navigation.navigateToRoot(Screen.CHARACTERS)
                    CampaignsViewEffect.CreatePlayerCharacter -> {
                        navigation.navigateToRoot(Screen.CHARACTERS)
                        charactersViewModel.onInteraction(
                            CharactersInteraction.NewPlayerCharacterSelected,
                        )
                    }
                    CampaignsViewEffect.OpenQuests -> navigation.navigateToRoot(Screen.QUESTS)
                    CampaignsViewEffect.OpenSessions -> navigation.navigateToRoot(Screen.SESSIONS)
                }
            }
        }
        appScope.scope.launch {
            locationsViewModel.effects.collect { effect ->
                when (effect) {
                    LocationsViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                    is LocationsViewEffect.OpenLore -> {
                        navigation.navigateToRoot(Screen.LORE)
                        loreViewModel.onInteraction(LoreInteraction.LoreOpened(effect.loreId))
                    }
                    is LocationsViewEffect.OpenQuest -> {
                        navigation.navigateToRoot(Screen.QUESTS)
                        questsViewModel.onInteraction(QuestsInteraction.QuestOpened(effect.questId))
                    }
                }
            }
        }
        appScope.scope.launch {
            loreViewModel.effects.collect { effect ->
                when (effect) {
                    LoreViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                }
            }
        }
        appScope.scope.launch {
            calendarViewModel.effects.collect { effect ->
                when (effect) {
                    CalendarViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                }
            }
        }
        appScope.scope.launch {
            factionsViewModel.effects.collect { effect ->
                when (effect) {
                    FactionsViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                }
            }
        }
        appScope.scope.launch {
            linksViewModel.effects.collect { effect ->
                when (effect) {
                    LinksViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                    is LinksViewEffect.OpenPerson -> {
                        navigation.navigateToRoot(Screen.CHARACTERS)
                        charactersViewModel.onInteraction(
                            CharactersInteraction.PersonOpened(effect.key),
                        )
                    }
                    is LinksViewEffect.OpenFaction -> {
                        navigation.navigateToRoot(Screen.FACTIONS)
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
                    CharactersViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                    is CharactersViewEffect.OpenLore -> {
                        navigation.navigateToRoot(Screen.LORE)
                        loreViewModel.onInteraction(LoreInteraction.LoreOpened(effect.loreId))
                    }
                    is CharactersViewEffect.OpenQuest -> {
                        navigation.navigateToRoot(Screen.QUESTS)
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
                        navigation.navigateToRoot(Screen.CHARACTERS)
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
                    QuestsViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                    QuestsViewEffect.OpenCampaigns -> navigation.navigateToRoot(Screen.CAMPAIGNS)
                    QuestsViewEffect.OpenLocations -> navigation.navigateToRoot(Screen.LOCATIONS)
                    is QuestsViewEffect.OpenLore -> {
                        navigation.navigateToRoot(Screen.LORE)
                        loreViewModel.onInteraction(LoreInteraction.LoreOpened(effect.loreId))
                    }
                    QuestsViewEffect.OpenCharacters -> navigation.navigateToRoot(Screen.CHARACTERS)
                    is QuestsViewEffect.OpenSession -> {
                        navigation.navigateToRoot(Screen.SESSIONS)
                        sessionsViewModel.onInteraction(SessionsInteraction.SessionOpened(effect.sessionId))
                    }
                }
            }
        }
        appScope.scope.launch {
            sessionsViewModel.effects.collect { effect ->
                when (effect) {
                    SessionsViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                    SessionsViewEffect.OpenCampaigns -> navigation.navigateToRoot(Screen.CAMPAIGNS)
                    is SessionsViewEffect.OpenQuest -> {
                        navigation.navigateToRoot(Screen.QUESTS)
                        questsViewModel.onInteraction(QuestsInteraction.QuestOpened(effect.questId))
                    }
                }
            }
        }
        appScope.scope.launch {
            encountersViewModel.effects.collect { effect ->
                when (effect) {
                    EncountersViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                    EncountersViewEffect.OpenCampaigns -> navigation.navigateToRoot(Screen.CAMPAIGNS)
                    EncountersViewEffect.OpenLocations -> navigation.navigateToRoot(Screen.LOCATIONS)
                    is EncountersViewEffect.OpenMap -> {
                        navigation.navigateToRoot(Screen.MAPS)
                        mapsViewModel.onInteraction(MapsInteraction.MapOpened(effect.battleMapId))
                    }
                    is EncountersViewEffect.OpenSheet -> openEncounterSheet(effect)
                }
            }
        }
        appScope.scope.launch {
            mapsViewModel.effects.collect { effect ->
                when (effect) {
                    MapsViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                    MapsViewEffect.OpenCampaigns -> navigation.navigateToRoot(Screen.CAMPAIGNS)
                }
            }
        }
        appScope.scope.launch {
            runViewModel.effects.collect { effect ->
                when (effect) {
                    RunViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                    RunViewEffect.OpenCampaigns -> navigation.navigateToRoot(Screen.CAMPAIGNS)
                    RunViewEffect.OpenSessions -> navigation.navigateToRoot(Screen.SESSIONS)
                    RunViewEffect.OpenEncounters -> navigation.navigateToRoot(Screen.ENCOUNTERS)
                    RunViewEffect.OpenMaps -> navigation.navigateToRoot(Screen.MAPS)
                    RunViewEffect.OpenPlayerView -> {
                        navigation.navigateToRoot(Screen.ENCOUNTERS)
                        encountersViewModel.onInteraction(EncountersInteraction.PlayerViewSelected)
                    }
                    RunViewEffect.OpenDiceTray -> {
                        diceViewModel.onInteraction(DiceInteraction.FloatingOpened)
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
                        exitRequested = true
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
                activeWorldName = details.world?.name
                activeCampaignName = details.campaign?.name
            }
        }
    }

    fun onInteraction(interaction: AppInteraction) {
        when (interaction) {
            is AppInteraction.ScreenSelected -> navigation.navigateToRoot(interaction.screen)
            AppInteraction.ThemeModeCycled -> cycleThemeMode()
            AppInteraction.NavDensityToggled -> toggleNavDensity()
            AppInteraction.SignOutSelected -> emitUiEvent(
                UiEvent.Info("Sign out is not configured")
            )
        }
    }

    fun consumeUiEvent() {
        uiEvent = null
    }

    private fun openWorldCreator() {
        navigation.navigateToRoot(Screen.WORLDS)
        worldsViewModel.onInteraction(WorldsInteraction.NewWorldSelected)
    }

    private fun openOneShotWizard() {
        oneShotWizardViewModel.onInteraction(OneShotWizardInteraction.ScreenStarted)
        navigation.navigateTo(Screen.ONE_SHOT_WIZARD)
    }

    private fun openSearchHit(effect: SearchViewEffect.RecordOpened) {
        val hit = effect.hit
        appScope.scope.launch {
            when (hit.kind) {
                SearchKind.World -> {
                    setActiveWorld(hit.id)
                    navigation.navigateToRoot(Screen.WORLDS)
                    worldsViewModel.onInteraction(WorldsInteraction.WorldSelected(hit.id))
                }
                SearchKind.Campaign -> {
                    setActiveCampaign(hit.id)
                    navigation.navigateToRoot(Screen.CAMPAIGNS)
                    campaignsViewModel.onInteraction(CampaignsInteraction.CampaignOpened(hit.id))
                }
                SearchKind.Location -> {
                    hit.worldId?.let { setActiveWorld(it) }
                    navigation.navigateToRoot(Screen.LOCATIONS)
                    locationsViewModel.onInteraction(LocationsInteraction.LocationOpened(hit.id))
                }
                SearchKind.Lore -> {
                    hit.worldId?.let { setActiveWorld(it) }
                    navigation.navigateToRoot(Screen.LORE)
                    loreViewModel.onInteraction(LoreInteraction.LoreOpened(hit.id))
                }
                SearchKind.Faction -> {
                    hit.worldId?.let { setActiveWorld(it) }
                    navigation.navigateToRoot(Screen.FACTIONS)
                    factionsViewModel.onInteraction(FactionsInteraction.FactionOpened(hit.id))
                }
                SearchKind.WorldPerson -> {
                    hit.worldId?.let { setActiveWorld(it) }
                    navigation.navigateToRoot(Screen.CHARACTERS)
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
                    navigation.navigateToRoot(Screen.CHARACTERS)
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
                    navigation.navigateToRoot(Screen.QUESTS)
                    questsViewModel.onInteraction(QuestsInteraction.QuestOpened(hit.id))
                }
                SearchKind.Session -> {
                    hit.campaignId?.let { setActiveCampaign(it) }
                    navigation.navigateToRoot(Screen.SESSIONS)
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
        themeMode = settings.themeMode
        themeSkin = settings.themeSkin
        navExpanded = settings.navExpanded
        localUser = settings.toLocalUser()
    }

    private fun cycleThemeMode() {
        shellSettingsStore.setThemeMode(themeMode.next())
    }

    private fun toggleNavDensity() {
        shellSettingsStore.setNavExpanded(!navExpanded)
    }

    private fun emitUiEvent(event: UiEvent) {
        uiEvent = event
    }
}
