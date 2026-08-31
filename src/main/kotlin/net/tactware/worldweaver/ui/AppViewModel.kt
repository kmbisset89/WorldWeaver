package net.tactware.worldweaver.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import net.tactware.worldweaver.core.AppCoroutineScope
import net.tactware.worldweaver.domain.ObserveActiveContextDetailsUseCase
import net.tactware.worldweaver.domain.SearchKind
import net.tactware.worldweaver.domain.SetActiveCampaignUseCase
import net.tactware.worldweaver.domain.SetActiveWorldUseCase
import net.tactware.worldweaver.ui.calendar.CalendarViewEffect
import net.tactware.worldweaver.ui.calendar.CalendarViewModel
import net.tactware.worldweaver.ui.campaigns.CampaignsInteraction
import net.tactware.worldweaver.ui.campaigns.CampaignsViewEffect
import net.tactware.worldweaver.ui.campaigns.CampaignsViewModel
import net.tactware.worldweaver.ui.characters.CharactersInteraction
import net.tactware.worldweaver.ui.characters.CharactersViewEffect
import net.tactware.worldweaver.ui.characters.CharactersViewModel
import net.tactware.worldweaver.ui.characters.CharactersViewState
import net.tactware.worldweaver.ui.characters.PersonMembership
import net.tactware.worldweaver.ui.dice.DiceViewModel
import net.tactware.worldweaver.ui.encounters.EncountersViewEffect
import net.tactware.worldweaver.ui.encounters.EncountersViewModel
import net.tactware.worldweaver.ui.home.HomeViewEffect
import net.tactware.worldweaver.ui.home.HomeViewModel
import net.tactware.worldweaver.ui.locations.LocationsInteraction
import net.tactware.worldweaver.ui.locations.LocationsViewEffect
import net.tactware.worldweaver.ui.locations.LocationsViewModel
import net.tactware.worldweaver.ui.lore.LoreInteraction
import net.tactware.worldweaver.ui.lore.LoreViewEffect
import net.tactware.worldweaver.ui.lore.LoreViewModel
import net.tactware.worldweaver.ui.maps.MapsInteraction
import net.tactware.worldweaver.ui.maps.MapsViewEffect
import net.tactware.worldweaver.ui.maps.MapsViewModel
import net.tactware.worldweaver.ui.navigation.NavigationState
import net.tactware.worldweaver.ui.navigation.Screen
import net.tactware.worldweaver.ui.search.SearchViewEffect
import net.tactware.worldweaver.ui.search.SearchViewModel
import net.tactware.worldweaver.ui.quests.QuestsInteraction
import net.tactware.worldweaver.ui.quests.QuestsViewEffect
import net.tactware.worldweaver.ui.quests.QuestsViewModel
import net.tactware.worldweaver.ui.sessions.SessionsInteraction
import net.tactware.worldweaver.ui.sessions.SessionsViewEffect
import net.tactware.worldweaver.ui.sessions.SessionsViewModel
import net.tactware.worldweaver.ui.settings.SettingsViewEffect
import net.tactware.worldweaver.ui.settings.SettingsViewModel
import net.tactware.worldweaver.ui.settings.ShellSettings
import net.tactware.worldweaver.ui.settings.ShellSettingsStore
import net.tactware.worldweaver.ui.worlds.WorldsInteraction
import net.tactware.worldweaver.ui.worlds.WorldsViewEffect
import net.tactware.worldweaver.ui.worlds.WorldsViewModel

internal class AppViewModel(
    val homeViewModel: HomeViewModel,
    val worldsViewModel: WorldsViewModel,
    val campaignsViewModel: CampaignsViewModel,
    val locationsViewModel: LocationsViewModel,
    val loreViewModel: LoreViewModel,
    val calendarViewModel: CalendarViewModel,
    val charactersViewModel: CharactersViewModel,
    val questsViewModel: QuestsViewModel,
    val sessionsViewModel: SessionsViewModel,
    val encountersViewModel: EncountersViewModel,
    val mapsViewModel: MapsViewModel,
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
                    HomeViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                    HomeViewEffect.OpenCampaigns -> navigation.navigateToRoot(Screen.CAMPAIGNS)
                    HomeViewEffect.OpenCharacters -> navigation.navigateToRoot(Screen.CHARACTERS)
                }
            }
        }
        appScope.scope.launch {
            worldsViewModel.effects.collect { effect ->
                when (effect) {
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
            campaignsViewModel.effects.collect { effect ->
                when (effect) {
                    CampaignsViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                    CampaignsViewEffect.OpenCharacters -> navigation.navigateToRoot(Screen.CHARACTERS)
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
