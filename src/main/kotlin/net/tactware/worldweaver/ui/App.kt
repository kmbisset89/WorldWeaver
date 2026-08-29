package net.tactware.worldweaver.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
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
import net.tactware.worldweaver.ui.campaigns.CampaignsScreen
import net.tactware.worldweaver.ui.components.Sidebar
import net.tactware.worldweaver.ui.home.HomeScreen
import net.tactware.worldweaver.ui.navigation.Screen
import net.tactware.worldweaver.ui.settings.SettingsScreen
import net.tactware.worldweaver.ui.theme.ErrorRed
import net.tactware.worldweaver.ui.theme.SuccessGreen
import net.tactware.worldweaver.ui.theme.WorldWeaverTheme
import net.tactware.worldweaver.ui.worlds.WorldsScreen

@Composable
internal fun App(
    viewModel: AppViewModel,
) {
    WorldWeaverTheme(themeMode = viewModel.themeMode) {
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
                    notificationCount = viewModel.notifications.size,
                    showNotifications = viewModel.showNotifications,
                    notifications = viewModel.notifications,
                    themeMode = viewModel.themeMode,
                    onCycleThemeMode = {
                        viewModel.onInteraction(AppInteraction.ThemeModeCycled)
                    },
                    onNavigate = { screen ->
                        viewModel.onInteraction(AppInteraction.ScreenSelected(screen))
                    },
                    onLogout = {
                        viewModel.onInteraction(AppInteraction.SignOutSelected)
                    },
                    onToggleNotifications = {
                        viewModel.onInteraction(AppInteraction.NotificationsToggled)
                    },
                    onDismissNotifications = {
                        viewModel.onInteraction(AppInteraction.NotificationsDismissed)
                    },
                    onNotificationClick = { notification ->
                        viewModel.onInteraction(AppInteraction.NotificationSelected(notification.id))
                    }
                )

                when (viewModel.navigation.currentScreen) {
                    Screen.HOME -> {
                        val homeState by viewModel.homeViewModel.state.collectAsState()
                        HomeScreen(
                            viewState = homeState,
                            onInteraction = viewModel.homeViewModel::onInteraction
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
