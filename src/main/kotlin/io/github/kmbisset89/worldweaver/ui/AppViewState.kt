package io.github.kmbisset89.worldweaver.ui

import io.github.kmbisset89.worldweaver.domain.ThemeMode
import io.github.kmbisset89.worldweaver.domain.ThemeSkin
import io.github.kmbisset89.worldweaver.ui.navigation.Screen
import io.github.kmbisset89.worldweaver.ui.session.LocalUser

internal sealed class AppViewState {
    data class Content(
        val currentScreen: Screen,
        val themeMode: ThemeMode,
        val themeSkin: ThemeSkin,
        val navExpanded: Boolean,
        val localUser: LocalUser,
        val activeWorldName: String?,
        val activeCampaignName: String?,
        val snackbar: UiEvent?,
        val exitRequested: Boolean,
    ) : AppViewState()
}
