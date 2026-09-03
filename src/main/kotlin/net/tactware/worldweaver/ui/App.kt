package net.tactware.worldweaver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import net.tactware.worldweaver.ui.calendar.CalendarScreen
import net.tactware.worldweaver.ui.campaigns.CampaignsScreen
import net.tactware.worldweaver.ui.characters.CharactersScreen
import net.tactware.worldweaver.ui.components.Sidebar
import net.tactware.worldweaver.ui.home.HomeScreen
import net.tactware.worldweaver.ui.oneshot.OneShotWizardScreen
import net.tactware.worldweaver.ui.locations.LocationsScreen
import net.tactware.worldweaver.ui.lore.LoreScreen
import net.tactware.worldweaver.ui.maps.BattleMapItemOverlay
import net.tactware.worldweaver.ui.maps.BattleMapTokenOverlay
import net.tactware.worldweaver.ui.maps.BattleMapViewerComposeWidget
import net.tactware.worldweaver.ui.maps.MapsInteraction
import net.tactware.worldweaver.ui.maps.MapsScreen
import net.tactware.worldweaver.ui.maps.MapsViewState
import net.tactware.worldweaver.ui.navigation.Screen
import net.tactware.worldweaver.ui.search.SearchBar
import net.tactware.worldweaver.ui.factions.FactionsScreen
import net.tactware.worldweaver.ui.links.LinksScreen
import net.tactware.worldweaver.ui.dice.DiceFloatingWindow
import net.tactware.worldweaver.ui.dice.DiceInteraction
import net.tactware.worldweaver.ui.dice.DiceScreen
import net.tactware.worldweaver.ui.dice.DiceViewState
import net.tactware.worldweaver.ui.encounters.EncountersInteraction
import net.tactware.worldweaver.ui.encounters.EncountersScreen
import net.tactware.worldweaver.ui.encounters.EncountersViewState
import net.tactware.worldweaver.ui.sheet.CharacterSheetInteraction
import net.tactware.worldweaver.ui.sheet.CharacterSheetScreen
import net.tactware.worldweaver.ui.sheet.CharacterSheetViewState
import net.tactware.worldweaver.ui.quests.QuestsScreen
import net.tactware.worldweaver.ui.run.RunScreen
import net.tactware.worldweaver.ui.sessions.SessionsScreen
import net.tactware.worldweaver.ui.settings.SettingsScreen
import net.tactware.worldweaver.ui.theme.ErrorRed
import net.tactware.worldweaver.ui.theme.SuccessGreen
import net.tactware.worldweaver.ui.theme.WorldWeaverTheme
import net.tactware.worldweaver.ui.worlds.WorldsScreen

@Composable
internal fun App(
    viewModel: AppViewModel,
) {
    WorldWeaverTheme(
        themeMode = viewModel.themeMode,
        themeSkin = viewModel.themeSkin,
    ) {
        val mapsState by viewModel.mapsViewModel.state.collectAsState()
        val encountersState by viewModel.encountersViewModel.state.collectAsState()
        val sheetState by viewModel.characterSheetViewModel.state.collectAsState()
        val diceState by viewModel.diceViewModel.state.collectAsState()
        val mapsPlayerMapState = viewModel.mapsViewModel.playerMapState
        val encounterPlayerMapState = viewModel.encountersViewModel.playerMapState
        val mapsPlayerContent = mapsState as? MapsViewState.Content
        val encounterPlayerContent = encountersState as? EncountersViewState.Running
        val playerMapState = when {
            encounterPlayerContent != null && encounterPlayerContent.playerViewOpen -> encounterPlayerMapState
            mapsPlayerContent != null && mapsPlayerContent.playerViewOpen -> mapsPlayerMapState
            else -> null
        }
        val playerTitle = encounterPlayerContent?.battleMapName
            ?: mapsPlayerContent?.selectedMap?.name
            ?: "Battle map"
        if (sheetState !is CharacterSheetViewState.Hidden) {
            val sheetWindowState = remember {
                WindowState(size = DpSize(1100.dp, 800.dp))
            }
            val sheetTitle = (sheetState as? CharacterSheetViewState.Content)?.name
                ?: "Character sheet"
            Window(
                title = sheetTitle,
                onCloseRequest = {
                    viewModel.characterSheetViewModel.onInteraction(
                        CharacterSheetInteraction.SheetDismissed
                    )
                },
                state = sheetWindowState,
            ) {
                WorldWeaverTheme(
                    themeMode = viewModel.themeMode,
                    themeSkin = viewModel.themeSkin,
                ) {
                    CharacterSheetScreen(
                        viewState = sheetState,
                        onInteraction = viewModel.characterSheetViewModel::onInteraction,
                    )
                }
            }
        }
        if (playerMapState != null) {
            val playerWindowState = remember {
                WindowState(size = DpSize(1024.dp, 768.dp))
            }
            Window(
                title = playerTitle,
                onCloseRequest = {
                    if (encounterPlayerContent != null && encounterPlayerContent.playerViewOpen) {
                        viewModel.encountersViewModel.onInteraction(
                            EncountersInteraction.PlayerViewClosed
                        )
                    } else {
                        viewModel.mapsViewModel.onInteraction(MapsInteraction.PlayerViewClosed)
                    }
                },
                state = playerWindowState,
            ) {
                WorldWeaverTheme(
                    themeMode = viewModel.themeMode,
                    themeSkin = viewModel.themeSkin,
                ) {
                    BattleMapViewerComposeWidget(
                        mapState = playerMapState,
                        modifier = Modifier.fillMaxSize(),
                        onMapTapped = { x, y ->
                            if (encounterPlayerContent != null && encounterPlayerContent.playerViewOpen) {
                                viewModel.encountersViewModel.onInteraction(
                                    EncountersInteraction.MapCellSelected(x, y)
                                )
                            } else {
                                viewModel.mapsViewModel.onInteraction(
                                    MapsInteraction.MapCellSelected(x, y)
                                )
                            }
                        },
                        onMarkerClicked = { markerId ->
                            BattleMapTokenOverlay.participantIdFrom(markerId)?.let { participantId ->
                                if (encounterPlayerContent != null &&
                                    encounterPlayerContent.playerViewOpen
                                ) {
                                    viewModel.encountersViewModel.onInteraction(
                                        EncountersInteraction.TokenSelected(participantId)
                                    )
                                } else {
                                    viewModel.mapsViewModel.onInteraction(
                                        MapsInteraction.TokenSelected(participantId)
                                    )
                                }
                            }
                            BattleMapItemOverlay.itemIdFrom(markerId)?.let { itemId ->
                                if (encounterPlayerContent != null &&
                                    encounterPlayerContent.playerViewOpen
                                ) {
                                    viewModel.encountersViewModel.onInteraction(
                                        EncountersInteraction.ItemSelected(itemId)
                                    )
                                } else {
                                    viewModel.mapsViewModel.onInteraction(
                                        MapsInteraction.ItemSelected(itemId)
                                    )
                                }
                            }
                        },
                    )
                }
            }
        }
        val diceContent = diceState as? DiceViewState.Content
        if (diceContent?.isFloatingOpen == true) {
            val diceWindowState = remember {
                WindowState(size = DpSize(440.dp, 720.dp))
            }
            Window(
                title = "Dice",
                alwaysOnTop = diceContent.isAlwaysOnTop,
                onCloseRequest = {
                    viewModel.diceViewModel.onInteraction(DiceInteraction.FloatingClosed)
                },
                state = diceWindowState,
            ) {
                WorldWeaverTheme(
                    themeMode = viewModel.themeMode,
                    themeSkin = viewModel.themeSkin,
                ) {
                    DiceFloatingWindow(
                        viewState = diceState,
                        onInteraction = viewModel.diceViewModel::onInteraction,
                    )
                }
            }
        }
        val snackbarHostState = remember { SnackbarHostState() }

        LaunchedEffect(viewModel.uiEvent) {
            val event = viewModel.uiEvent ?: return@LaunchedEffect
            snackbarHostState.showSnackbar(
                message = event.message,
                withDismissAction = true
            )
            viewModel.consumeUiEvent()
        }

        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    val event = viewModel.uiEvent
                    val containerColor = when (event) {
                        is UiEvent.Error -> ErrorRed
                        is UiEvent.Success -> SuccessGreen
                        is UiEvent.Info, null -> MaterialTheme.colorScheme.inverseSurface
                    }
                    Snackbar(
                        snackbarData = data,
                        containerColor = containerColor,
                        contentColor = Color.White
                    )
                }
            }
        ) { padding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Sidebar(
                    currentUser = viewModel.localUser,
                    currentScreen = viewModel.navigation.currentScreen,
                    activeWorldName = viewModel.activeWorldName,
                    activeCampaignName = viewModel.activeCampaignName,
                    themeMode = viewModel.themeMode,
                    expanded = viewModel.navExpanded,
                    onCycleThemeMode = {
                        viewModel.onInteraction(AppInteraction.ThemeModeCycled)
                    },
                    onToggleExpanded = {
                        viewModel.onInteraction(AppInteraction.NavDensityToggled)
                    },
                    onNavigate = { screen ->
                        viewModel.onInteraction(AppInteraction.ScreenSelected(screen))
                    },
                    onLogout = {
                        viewModel.onInteraction(AppInteraction.SignOutSelected)
                    },
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    val searchState by viewModel.searchViewModel.state.collectAsState()
                    SearchBar(
                        viewState = searchState,
                        onInteraction = viewModel.searchViewModel::onInteraction,
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                    when (viewModel.navigation.currentScreen) {
                    Screen.HOME -> {
                        val homeState by viewModel.homeViewModel.state.collectAsState()
                        HomeScreen(
                            viewState = homeState,
                            onInteraction = viewModel.homeViewModel::onInteraction
                        )
                    }

                    Screen.ONE_SHOT_WIZARD -> {
                        val oneShotState by viewModel.oneShotWizardViewModel.state.collectAsState()
                        OneShotWizardScreen(
                            viewState = oneShotState,
                            onInteraction = viewModel.oneShotWizardViewModel::onInteraction
                        )
                    }

                    Screen.WORLDS -> {
                        val worldsState by viewModel.worldsViewModel.state.collectAsState()
                        WorldsScreen(
                            viewState = worldsState,
                            onInteraction = viewModel.worldsViewModel::onInteraction
                        )
                    }

                    Screen.CAMPAIGNS -> {
                        val campaignsState by viewModel.campaignsViewModel.state.collectAsState()
                        CampaignsScreen(
                            viewState = campaignsState,
                            onInteraction = viewModel.campaignsViewModel::onInteraction
                        )
                    }

                    Screen.LOCATIONS -> {
                        val locationsState by viewModel.locationsViewModel.state.collectAsState()
                        LocationsScreen(
                            viewState = locationsState,
                            onInteraction = viewModel.locationsViewModel::onInteraction
                        )
                    }

                    Screen.LORE -> {
                        val loreState by viewModel.loreViewModel.state.collectAsState()
                        LoreScreen(
                            viewState = loreState,
                            onInteraction = viewModel.loreViewModel::onInteraction
                        )
                    }

                    Screen.CALENDAR -> {
                        val calendarState by viewModel.calendarViewModel.state.collectAsState()
                        CalendarScreen(
                            viewState = calendarState,
                            onInteraction = viewModel.calendarViewModel::onInteraction
                        )
                    }

                    Screen.FACTIONS -> {
                        val factionsState by viewModel.factionsViewModel.state.collectAsState()
                        FactionsScreen(
                            viewState = factionsState,
                            onInteraction = viewModel.factionsViewModel::onInteraction
                        )
                    }

                    Screen.LINKS -> {
                        val linksState by viewModel.linksViewModel.state.collectAsState()
                        LinksScreen(
                            viewState = linksState,
                            onInteraction = viewModel.linksViewModel::onInteraction
                        )
                    }

                    Screen.CHARACTERS -> {
                        val charactersState by viewModel.charactersViewModel.state.collectAsState()
                        CharactersScreen(
                            viewState = charactersState,
                            onInteraction = viewModel.charactersViewModel::onInteraction
                        )
                    }

                    Screen.QUESTS -> {
                        val questsState by viewModel.questsViewModel.state.collectAsState()
                        QuestsScreen(
                            viewState = questsState,
                            onInteraction = viewModel.questsViewModel::onInteraction
                        )
                    }

                    Screen.SESSIONS -> {
                        val sessionsState by viewModel.sessionsViewModel.state.collectAsState()
                        SessionsScreen(
                            viewState = sessionsState,
                            onInteraction = viewModel.sessionsViewModel::onInteraction
                        )
                    }

                    Screen.ENCOUNTERS -> {
                        EncountersScreen(
                            viewState = encountersState,
                            mapState = viewModel.encountersViewModel.mapState,
                            onInteraction = viewModel.encountersViewModel::onInteraction
                        )
                    }

                    Screen.MAPS -> {
                        MapsScreen(
                            viewState = mapsState,
                            mapState = viewModel.mapsViewModel.mapState,
                            onInteraction = viewModel.mapsViewModel::onInteraction
                        )
                    }

                    Screen.RUN -> {
                        val runState by viewModel.runViewModel.state.collectAsState()
                        RunScreen(
                            viewState = runState,
                            onInteraction = viewModel.runViewModel::onInteraction
                        )
                    }

                    Screen.DICE -> {
                        DiceScreen(
                            viewState = diceState,
                            onInteraction = viewModel.diceViewModel::onInteraction
                        )
                    }

                    Screen.SETTINGS -> {
                        val settingsState by viewModel.settingsViewModel.state.collectAsState()
                        SettingsScreen(
                            viewState = settingsState,
                            onInteraction = viewModel.settingsViewModel::onInteraction
                        )
                    }
                    }
                    }
                }
            }
        }
    }
}
