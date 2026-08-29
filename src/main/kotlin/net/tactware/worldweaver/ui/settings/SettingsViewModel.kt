package net.tactware.worldweaver.ui.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.tactware.worldweaver.ui.session.LocalUser
import net.tactware.worldweaver.ui.theme.ThemeMode

internal class SettingsViewModel(
    private val localUser: LocalUser,
    themeMode: ThemeMode,
) {
    private val _state = MutableStateFlow<SettingsViewState>(
        SettingsViewState.Content(
            themeLabel = themeMode.label(),
            displayName = localUser.displayName,
            email = localUser.email,
        )
    )
    val state: StateFlow<SettingsViewState> = _state.asStateFlow()

    fun onInteraction(interaction: SettingsInteraction) {
        when (interaction) {
            SettingsInteraction.ScreenStarted -> Unit
            is SettingsInteraction.ThemeModeChanged -> syncTheme(interaction.themeMode)
        }
    }

    private fun syncTheme(themeMode: ThemeMode) {
        _state.update { current ->
            when (current) {
                is SettingsViewState.Content -> current.copy(themeLabel = themeMode.label())
            }
        }
    }
}
