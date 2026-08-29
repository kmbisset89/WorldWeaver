package net.tactware.worldweaver.ui.settings

import net.tactware.worldweaver.ui.theme.ThemeMode

internal sealed interface SettingsInteraction {
    data object ScreenStarted : SettingsInteraction
    data class ThemeModeChanged(val themeMode: ThemeMode) : SettingsInteraction
}
