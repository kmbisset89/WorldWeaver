package net.tactware.worldweaver.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import net.tactware.worldweaver.core.AppCoroutineScope
import net.tactware.worldweaver.domain.ObserveActiveContextDetailsUseCase
import net.tactware.worldweaver.ui.campaigns.CampaignsViewEffect
import net.tactware.worldweaver.ui.campaigns.CampaignsViewModel
import net.tactware.worldweaver.ui.components.ShellNotification
import net.tactware.worldweaver.ui.home.HomeViewEffect
import net.tactware.worldweaver.ui.home.HomeViewModel
import net.tactware.worldweaver.ui.navigation.NavigationState
import net.tactware.worldweaver.ui.navigation.Screen
import net.tactware.worldweaver.ui.session.LocalUser
import net.tactware.worldweaver.ui.settings.SettingsInteraction
import net.tactware.worldweaver.ui.settings.SettingsViewModel
import net.tactware.worldweaver.ui.theme.ThemeMode
import net.tactware.worldweaver.ui.worlds.WorldsInteraction
import net.tactware.worldweaver.ui.worlds.WorldsViewModel

internal class AppViewModel(
    val homeViewModel: HomeViewModel,
    val worldsViewModel: WorldsViewModel,
    val campaignsViewModel: CampaignsViewModel,
    val settingsViewModel: SettingsViewModel,
    val localUser: LocalUser,
    private val appScope: AppCoroutineScope,
    private val observeActiveContextDetails: ObserveActiveContextDetailsUseCase,
) {
    val navigation = NavigationState()

    var themeMode by mutableStateOf(ThemeMode.load())
        private set

    var uiEvent by mutableStateOf<UiEvent?>(null)
        private set

    var showNotifications by mutableStateOf(false)
        private set

    var activeWorldName by mutableStateOf<String?>(null)
        private set

    var activeCampaignName by mutableStateOf<String?>(null)
        private set

    val notifications = mutableStateListOf(
        ShellNotification(
            id = "welcome",
            message = "Welcome to World Weaver",
        )
    )

    init {
        appScope.scope.launch {
            homeViewModel.effects.collect { effect ->
                when (effect) {
                    HomeViewEffect.OpenWorldCreator -> openWorldCreator()
                    HomeViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
                    HomeViewEffect.OpenCampaigns -> navigation.navigateToRoot(Screen.CAMPAIGNS)
                }
            }
        }
        appScope.scope.launch {
            campaignsViewModel.effects.collect { effect ->
                when (effect) {
                    CampaignsViewEffect.OpenWorlds -> navigation.navigateToRoot(Screen.WORLDS)
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
            AppInteraction.NotificationsToggled -> showNotifications = !showNotifications
            AppInteraction.NotificationsDismissed -> dismissNotifications()
            is AppInteraction.NotificationSelected -> selectNotification(interaction.id)
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

    private fun cycleThemeMode() {
        themeMode = themeMode.next().also { ThemeMode.save(it) }
        settingsViewModel.onInteraction(SettingsInteraction.ThemeModeChanged(themeMode))
    }

    private fun dismissNotifications() {
        notifications.clear()
        showNotifications = false
        emitUiEvent(UiEvent.Info("Notifications cleared"))
    }

    private fun selectNotification(id: String) {
        notifications.removeAll { it.id == id }
        showNotifications = false
        emitUiEvent(UiEvent.Info("Notification dismissed"))
    }

    private fun emitUiEvent(event: UiEvent) {
        uiEvent = event
    }
}
